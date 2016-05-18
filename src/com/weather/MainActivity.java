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
        // ��λ��ʼ��
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // ��gps
        option.setCoorType("bd09ll"); // ������������
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
		// ��ʼ������ģ��
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		
		requestQueue = Volley.newRequestQueue(this);
		
		Button();					
	}
		
	//��ť�¼�
	private void Button() {
		search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO �Զ����ɵķ������
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
	
	//��λͼ��
	public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view ���ٺ��ڴ����½��յ�λ��
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
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
	

	//������룬�ɵ�����ȡ��γ��
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "��Ǹ��δ���ҵ����", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mBaiduMap.clear();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
				.getLocation()));

	}

	//��������룬�ɾ�γ�Ȼ�ȡ����
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "��Ǹ��δ���ҵ����", Toast.LENGTH_LONG)
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
	
	//������ʾ
	public void dialog(weatherInfo info)
	{
		if(info.errNum == 0){
			AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle(info.city+"��������")
			.setMessage("������"+info.weather+
					    "\n�¶ȣ�"+info.tmp+"��"+
					    "\n���£�"+info.HighTmp+"��"+
					    "\n���£�"+info.LowTmp+"��")
			.setPositiveButton("������", null);
			aDialog.show();
		}
		else {
			AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle("������")
			.setMessage("��������ȷ��ַ")
			.setPositiveButton("���Ǻܿ���", null);
			aDialog.show();
		}
	}
	//�����ͼ�ص�
	@Override
	public void onMapClick(LatLng arg0) {
		// TODO �Զ����ɵķ������
		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(arg0));
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		// TODO �Զ����ɵķ������
		return false;
	}
	
	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        //��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���  
        mMapView.onDestroy();  
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���  
        mMapView.onResume();  
        }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���  
        mMapView.onPause();  
        } 
}
