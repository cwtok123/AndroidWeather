package com.weather;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

import android.R.string;
import android.util.Log;

public class Http {
	//使用VOLLEY进行网络操作
	public static weatherInfo Info = new weatherInfo();	
	public static weatherInfo getWeather(String string) {				
		String url = "http://apis.baidu.com/apistore/weatherservice/cityname?cityname="+string;	
		JsonObjectRequest weatherQuest = new JsonObjectRequest(Method.GET,url,
				null, new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// TODO 自动生成的方法存根
						Log.e("TAG", response.toString());
						json(response);					
						
					}

				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO 自动生成的方法存根
						Log.e("TAG", error.getMessage(), error);
												
					}
				}){
					public Map<String, String> getHeaders() throws AuthFailureError{
						HashMap<String, String> headers = new HashMap<String, String>();
						headers.put("apikey", "7a2c5b467b44f81a17466e35d04b3554");
						return headers;
					}
				};
		weatherQuest.setTag("weather");
		MainActivity.requestQueue.add(weatherQuest);
		MainActivity.requestQueue.start();
		return Info;
		}
	private static void json(JSONObject response) {		
		try {
			Info.errNum = response.getInt("errNum");
			JSONObject retData = response.getJSONObject("retData");
			Info.city = retData.getString("city");
			Info.weather = retData.getString("weather");
			Info.tmp = retData.getString("temp");
			Info.HighTmp = retData.getString("h_tmp");
			Info.LowTmp = retData.getString("l_tmp");
		} catch (JSONException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

}
