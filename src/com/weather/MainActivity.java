package com.weather;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnGetGeoCoderResultListener, OnMapClickListener {

	MapView mMapView = null;
	BaiduMap mBaiduMap = null;
	GeoCoder mSearch = null;
	
	public Button search;
	
	boolean isFirstLoc = true;	
	LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    
    public static RequestQueue requestQueue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		
		search = (Button)findViewById(R.id.btn_Serach);
		
		mMapView = (MapView)findViewById(R.id.map_Main);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setOnMapClickListener(this);
		
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
		// 初始化搜索模块
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		
		requestQueue = Volley.newRequestQueue(this);
		
		Button();					
	}
		
	//按钮事件
	private void Button() {
		search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				EditText add = (EditText) findViewById(R.id.text_Address);
				if (add!=null){
					mSearch.geocode(new GeoCodeOption().city("").address(add.getText().toString()));
					weatherInfo info = new weatherInfo();
					info = Http.getWeather(add.getText().toString());
					dialog(info);
				}
			}
		});
		
	}
	
	//定位图层
	public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                                
            }
        }
        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
	

	//地理编码，由地名获取经纬度
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mBaiduMap.clear();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
				.getLocation()));

	}

	//反地理编码，由经纬度获取地名
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mBaiduMap.clear();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
				.getLocation()));
		weatherInfo info = new weatherInfo();
		String str = result.getAddressDetail().city;
		info = Http.getWeather(str.substring(0, str.length()-1));
		dialog(info);
	}
	
	//弹窗显示
	public void dialog(weatherInfo info)
	{
		if(info.errNum == 0){
			AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle(info.city+"今日天气")
			.setMessage("天气："+info.weather+
					    "\n温度："+info.tmp+"℃"+
					    "\n低温："+info.HighTmp+"℃"+
					    "\n高温："+info.LowTmp+"℃")
			.setPositiveButton("朕已阅", null);
			aDialog.show();
		}
		else {
			AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("出错了")
			.setMessage("请输入正确地址")
			.setPositiveButton("不是很开心", null);
			aDialog.show();
		}
	}
	//点击地图回调
	@Override
	public void onMapClick(LatLng arg0) {
		// TODO 自动生成的方法存根
		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(arg0));
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		// TODO 自动生成的方法存根
		return false;
	}
	
	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mMapView.onDestroy();  
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mMapView.onResume();  
        }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mMapView.onPause();  
        } 
}
