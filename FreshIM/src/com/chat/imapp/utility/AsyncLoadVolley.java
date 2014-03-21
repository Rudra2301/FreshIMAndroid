package com.chat.imapp.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.chat.imapp.interfaces.OnAsyncTaskListener;

public class AsyncLoadVolley {
	
	private static final String TAG = "AsyncLoadVolley Response";
	
	private OnAsyncTaskListener asyncTaskListener;
	
	private List<BasicNameValuePair> params;
	
	private RequestQueue queue;
	
	private String url;
	
	private int currentPage = 0;
	
	private Context context;
	
	private ConnectionDetector connectionDetector;
		
	public AsyncLoadVolley(Context context, String filename) {
		queue = Volley.newRequestQueue(context);
		url = Constant.URL + Constant.FOLDER + filename + ".php";
		this.context = context;
		connectionDetector = new ConnectionDetector(context);
		init();
	}
	
	private void init() {
		params = new ArrayList<BasicNameValuePair>();	
	}
	
	public void setOnAsyncTaskListener(OnAsyncTaskListener listener) {
        this.asyncTaskListener=listener;            
    }
    
    public void setBasicNameValuePair(Map<String, String> map) {
    	
    	for (Map.Entry<String,String> entry : map.entrySet()) {
			  String key = entry.getKey();
			  String value = entry.getValue();
			  Log.i(TAG, "key : "+key + ", val : "+value);
			  
			  params.add(new BasicNameValuePair(key, value));
		}
	}
    
    public void setBasicNameValuePair(ArrayList<Map<String, String>> nameValuePair) {
		for (int i = 0; i < nameValuePair.size(); i++) {
			params.add(
					new BasicNameValuePair(
							nameValuePair.get(i).get(Constant.NAME), 
							nameValuePair.get(i).get(Constant.VALUE)));
		}	
	} 	
    
    public void setBasicNameValuePair1(ArrayList<BasicNameValuePair> nameValuePair) {
		this.params = nameValuePair;
	} 	
    
    public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
    
    public void beginTask() {
    	
    	asyncTaskListener.onTaskBegin();
    	
    	CustomRequestString request = new CustomRequestString(Request.Method.POST, url, params, listener, errorListener);
		queue.add(request);
	}
    
    Response.Listener<String> listener = new Response.Listener<String>() {
		
		@Override
		public void onResponse(String response) 
		{	
			Log.e(TAG, response);
			asyncTaskListener.onTaskComplete(true, response);
		}	
	};
	
	Response.ErrorListener errorListener =  new Response.ErrorListener() {
		
		@Override
		public void onErrorResponse(VolleyError error) {
			Log.e(TAG, "onErrorResponse : "+error.getMessage());
			asyncTaskListener.onTaskComplete(false, error.getMessage());
		}
	};
}