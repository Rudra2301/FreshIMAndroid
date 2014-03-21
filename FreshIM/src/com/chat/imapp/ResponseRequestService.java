package com.chat.imapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Base64;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.ImageCustomize;
import com.google.android.gms.internal.ay;

@SuppressLint("NewApi")
public class ResponseRequestService extends Service {
	
	protected static final String TAG = "ResponseRequestService";
	private Handler handler;
	SharedPreferences sharedPreferences;
	InputStream inputStream;
	private String friendRequestId;
	private String friendId, userId, adminId, type;
	
	AsyncLoadVolley asyncLoadVolley;
	
	public ResponseRequestService() {
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		handler = new Handler();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());		
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Bundle extras = intent.getExtras();
		friendRequestId = extras.getString(Constant.FRIEND_REQUEST_ID);
		friendId = extras.getString(Constant.FRIEND_ID);
		type = extras.getString(Constant.TYPE);
		userId = extras.getString(Constant.USER_ID);
		//adminId = extras.getString(Constant.ADMIN_ID);
		
		Log.e("IN CHECK", "START");
		//check();
		onUploadStart(friendRequestId, type, friendId, userId);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onUploadStart(String friendRequestId, String type, String friendId, String userId) {
		
			asyncLoadVolley = new AsyncLoadVolley(this, "friendrequestgcm");
			Map<String, String> map = new HashMap<String, String>();
			map.put(Constant.ID, friendRequestId);
			map.put(Constant.TYPE, type);
			map.put(Constant.FRIEND_ID, friendId);
			map.put(Constant.USER_ID, userId);
			asyncLoadVolley.setBasicNameValuePair(map);
			asyncLoadVolley.setOnAsyncTaskListener(uploadAsyncTaskListener);
			
			handler.post(new Runnable() {
			    public void run()
			    {
			    	asyncLoadVolley.beginTask();
			    }
			});	
	}
	
	OnAsyncTaskListener uploadAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			
			Log.e(TAG, " mess : "+message);
			/*
			Intent intent = new Intent(Constant.FRIEND);
			intent.putExtra(Constant.STATUS, true);
			intent.putExtra(Constant.POSITION, position);
			intent.putExtra(Constant.IMAGE, imageName);
			//sendBroadcast(intent);
			*/
			stopSelf();
			
				//onUploadComplete();
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
	
	
	
	
/////
		
	@Override
	public void onDestroy() {
		Log.w("mine", "CLICKED");
		super.onDestroy();
	}
	
}
