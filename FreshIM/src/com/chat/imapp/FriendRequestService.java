package com.chat.imapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import com.chat.imapp.adapters.FriendAllAdapter;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Constant;

@SuppressLint("NewApi")
public class FriendRequestService extends Service {
	
	protected static final String TAG = "FriendRequestService";
	private Handler handler;
	SharedPreferences sharedPreferences;
	InputStream inputStream;
	private String friendRequestId;
	private String friendId, userId, adminId, type;
	
	AsyncLoadVolley asyncLoadVolley;
	
	public FriendRequestService() {
		
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
		userId = extras.getString(Constant.USER_ID);
		friendId = extras.getString(Constant.FRIEND_ID);
		
		String action = intent.getAction();
		Log.e(TAG, "action : "+action);
		
		if(action!=null)
		{
			if(action.equals(GcmIntentService.ACTION_ACCEPT))
			{
				type = "1";
			}
			else if(action.equals(GcmIntentService.ACTION_REJECT))
			{
				type = "2";
			}
		}

		GcmIntentService.cancelNotification(getApplicationContext(), GcmIntentService.NOTIFICATION_ID);
		
		Log.e("IN CHECK", "START");
		//check();
		
		Log.e(TAG, "type : "+type);
		
		if(type!=null) {
		
			if(type.equals("1")) {
				onAcceptRequest(userId, type, friendId);
			}
			else if(type.equals("2")) {
				onRejectRequest(userId, type, friendId);
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onAcceptRequest(String userId, String type, String friendId) {
		
		String filename = getResources().getString(R.string.friendrequest_php);
		asyncLoadVolley = new AsyncLoadVolley(this, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.USER_ID, friendId);
		map.put(Constant.FRIEND_ID, userId);
		map.put(Constant.TYPE, type);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(acceptAsyncTaskListener);
		
		handler.post(new Runnable() {
		    public void run()
		    {
		    	asyncLoadVolley.beginTask();
		    }
		});	
	}
	
	OnAsyncTaskListener acceptAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			
			Log.e(TAG, " mess : "+message);
			
			AsyncResponse asyncResponse = new AsyncResponse(message);
			if(asyncResponse.ifSuccess()) {
				
				List<FriendItem> myList = asyncResponse.getFriendAllListAfterRequest();
				String newType = myList.get(0).getType();
				String newAdminId = myList.get(0).getAdminId();
				String friendRequestId = myList.get(0).getFriendRequestId();
				
				Intent service = new Intent(getApplicationContext(), ResponseRequestService.class);
				service.putExtra(Constant.TYPE, FriendAllAdapter.TYPE_ACCEPT_REQUEST);	
				service.putExtra(Constant.FRIEND_REQUEST_ID, friendRequestId);
				service.putExtra(Constant.FRIEND_ID, newAdminId);
				service.putExtra(Constant.USER_ID, Sessions.getUserId(getApplicationContext()));
				startService(service);
				
				stopSelf();				
			}
			else
			{
				//showToast(""+asyncResponse.getMessage());
			}	
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
	
	
	public void onRejectRequest(String userId, String type, String friendId) {
			
			String filename = getResources().getString(R.string.friendrequest_php);
			asyncLoadVolley = new AsyncLoadVolley(this, filename);
			Map<String, String> map = new HashMap<String, String>();
			map.put(Constant.USER_ID, friendId);				 // opposite because no user becomes the friend,			
			map.put(Constant.FRIEND_ID, userId);				// as he is rejecting the request.
			map.put(Constant.TYPE, type);
			asyncLoadVolley.setBasicNameValuePair(map);
			asyncLoadVolley.setOnAsyncTaskListener(rejectAsyncTaskListener);
			
			handler.post(new Runnable() {
			    public void run()
			    {
			    	asyncLoadVolley.beginTask();
			    }
			});	
	}
	
	OnAsyncTaskListener rejectAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			
			Log.e(TAG, " mess : "+message);
			
			AsyncResponse asyncResponse = new AsyncResponse(message);
			if(asyncResponse.ifSuccess()) {
				
				List<FriendItem> myList = asyncResponse.getFriendAllListAfterRequest();
				String newType = myList.get(0).getType();
				String newAdminId = myList.get(0).getAdminId();
				String friendRequestId = myList.get(0).getFriendRequestId();
				
				Intent service = new Intent(getApplicationContext(), ResponseRequestService.class);
				service.putExtra(Constant.TYPE, FriendAllAdapter.TYPE_REJECT_REQUEST);
				service.putExtra(Constant.FRIEND_REQUEST_ID, friendRequestId);
				service.putExtra(Constant.FRIEND_ID, newAdminId);
				service.putExtra(Constant.USER_ID, Sessions.getUserId(getApplicationContext()));
				startService(service);
				
				stopSelf();
				
			}
			else
			{
				//showToast(""+asyncResponse.getMessage());
			}	
			
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
