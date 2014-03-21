package com.chat.imapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

@SuppressLint("NewApi")
public class LoadImageService extends Service {
	
	protected static final String TAG = "LoadImageService";
	
	private Handler handler;
	SharedPreferences sharedPreferences;
	InputStream inputStream;
	private String imageName, imagePath;
	private String friendId, message, type, value;
	private int position = 0;
	private String data = "1";
	private String groupId;
	
	private String filename;
	private AsyncLoadVolley asyncLoadVolley;
	
	public static final String VALUE_MESSAGE = "1";
	public static final String VALUE_PROFILE_PIC = "2";
	public static final String VALUE_GROUP_PIC = "3";
	public static final String VALUE_FILE_MESSAGE = "4";
	
	public LoadImageService() {
		
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
		imageName = extras.getString(Constant.NAME);
		imagePath = extras.getString(Constant.PATH);
		value = extras.getString(Constant.VALUE);
		
		if(value.equals(VALUE_MESSAGE)) 
		{
			friendId = extras.getString(Constant.FRIEND_ID);
			message = extras.getString(Constant.MESSAGE);
			type = extras.getString(Constant.TYPE);
			position = extras.getInt(Constant.POSITION);
			data = extras.getString(Constant.DATA);
		}
		else if(value.equals(VALUE_PROFILE_PIC))
		{
			
		}
		else if(value.equals(VALUE_GROUP_PIC))
		{
			groupId = extras.getString(Constant.GROUP_ID);
		}
		else if(value.equals(VALUE_FILE_MESSAGE)) {
			friendId = extras.getString(Constant.FRIEND_ID);
			message = extras.getString(Constant.MESSAGE);
			type = extras.getString(Constant.TYPE);
			position = extras.getInt(Constant.POSITION);
			data = extras.getString(Constant.DATA);
		}
		
		Log.e("IN CHECK", "START");
		//check();
		
		onUploadStart(imageName, imagePath);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onUploadStart(String imageName, String imagePath) {
		
		String image = getImageFromPath(imagePath);
		
		if(image!=null) {
			asyncLoadVolley = new AsyncLoadVolley(this, "images/upload_image");
			Map<String, String> map = new HashMap<String, String>();
			map.put(Constant.IMAGE, image);
			map.put(Constant.NAME, imageName);
			asyncLoadVolley.setBasicNameValuePair(map);
			asyncLoadVolley.setOnAsyncTaskListener(uploadAsyncTaskListener);
			
			handler.post(new Runnable() {
			    public void run()
			    {
			    	asyncLoadVolley.beginTask();
			    }
			});
		}
		else {
			Log.e(TAG, "Could not decode image..");
		}
	}
	
	OnAsyncTaskListener uploadAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "value : "+value+", mess : "+message);
			if(value.equals("1")) 
			{
				Intent intent = new Intent(Constant.IMAGE);
				intent.putExtra(Constant.STATUS, true);
				intent.putExtra(Constant.POSITION, position);
				intent.putExtra(Constant.IMAGE, imageName);
				sendBroadcast(intent);
				
				onUploadComplete();
			}
			else if(value.equals("2"))
			{
				onUpdateProfileImageInfo();
			}
			else if(value.equals(VALUE_GROUP_PIC))
			{
				onUpdateGroupImageInfo();
				Log.e(TAG, "onUpdateGroupImageInfo ");
			}
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
	
	private String getImageFromPath(String imagePath) {
		
		if(imagePath!=null)
		{
			if(imagePath.length()!=0)
			{
				
				File file = new File(imagePath);
				Bitmap bitmap = ImageCustomize.decodeFile(file, 200); 
		        ByteArrayOutputStream stream = new ByteArrayOutputStream();
		        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream); //compress to which format you want.
		        byte [] byte_arr = stream.toByteArray();
		        String imageStr = Base64.encodeBytes(byte_arr);
		        return imageStr;
			}
		}
        return null;
	}	
		
	
	// // VALUE = 1
		public void onUploadComplete() {
			
			if(data.equals("2")) 
				filename = getResources().getString(R.string.message_group_php);
			else
				filename = getResources().getString(R.string.message_php);
			asyncLoadVolley = new AsyncLoadVolley(this, filename);
			Map<String, String> map = new HashMap<String, String>();
			map.put(Constant.USER_ID, Sessions.getUserId(this));
			map.put(Constant.FRIEND_ID, friendId);
			map.put(Constant.MESSAGE, message);
			map.put(Constant.TYPE, type);
			asyncLoadVolley.setBasicNameValuePair(map);
			asyncLoadVolley.setOnAsyncTaskListener(messageAsyncTaskListener);
			
			handler.post(new Runnable() {
			    public void run()
			    {
			    	asyncLoadVolley.beginTask();
			    }
			});
		}
	
		OnAsyncTaskListener messageAsyncTaskListener = new OnAsyncTaskListener() {
			
			@Override
			public void onTaskComplete(boolean isComplete, String message) {
				
				AsyncResponse asyncResponse = new AsyncResponse(message);
				if(asyncResponse.ifSuccess()) {
					Log.e(TAG, "Upload complete and notified : mess : "+message);
				}
				else {
					Log.e(TAG, "err : "+asyncResponse.getMessage());
				}
				
				stopSelf();			
			}
			
			@Override
			public void onTaskBegin() {
				
			}
		};
	
// VALUE = 2
	
	public void onUpdateProfileImageInfo() {
		
		String filename = getResources().getString(R.string.edit_image_php);
		final AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(this, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.ID, Sessions.getUserId(this));
		map.put(Constant.IMAGE, imageName);
		map.put(Constant.TYPE, "1");
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(profileAsyncTaskListener);
		
		handler.post(new Runnable() {
		    public void run()
		    {
		    	asyncLoadVolley.beginTask();
		    }
		});
	}	
	

	OnAsyncTaskListener profileAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			
			AsyncResponse asyncResponse = new AsyncResponse(message);
			if(asyncResponse.ifSuccess()) {
				Log.e(TAG, "Upload complete and notified : mess : "+message);
				Sessions.setImage(getApplicationContext(), imageName);
			}
			else {
				Log.e(TAG, "err : "+asyncResponse.getMessage());
			}
			
			Log.e(TAG, "Upload complete and notified : mess : "+message);
			stopSelf();			
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
	
	
	// VALUE = 3
	
		public void onUpdateGroupImageInfo() {
			
			String filename = getResources().getString(R.string.edit_image_php);
			final AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(this, filename);
			Map<String, String> map = new HashMap<String, String>();
			
			Log.e(TAG, "onUpdateGroupImageInfo : group ID : "+groupId);
			map.put(Constant.ID, groupId);
			map.put(Constant.IMAGE, imageName);
			map.put(Constant.TYPE, "2");
			asyncLoadVolley.setBasicNameValuePair(map);
			asyncLoadVolley.setOnAsyncTaskListener(groupAsyncTaskListener);
			
			handler.post(new Runnable() {
			    public void run()
			    {
			    	asyncLoadVolley.beginTask();
			    }
			});
		}	
		
		OnAsyncTaskListener groupAsyncTaskListener = new OnAsyncTaskListener() {
			
			@Override
			public void onTaskComplete(boolean isComplete, String message) {
				
				AsyncResponse asyncResponse = new AsyncResponse(message);
				if(asyncResponse.ifSuccess()) {
					Log.e(TAG, "Upload complete and notified : mess : "+message);
				}
				else {
					Log.e(TAG, "err : "+asyncResponse.getMessage());
				}
				
				Log.e(TAG, "Upload complete and notified : mess : "+message);
				stopSelf();			
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
