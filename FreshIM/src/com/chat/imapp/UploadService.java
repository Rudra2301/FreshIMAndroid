package com.chat.imapp;

import java.io.ByteArrayOutputStream;
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
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.interfaces.OnUploadCompleteListener;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.Base64;
import com.chat.imapp.utility.Constant;
import com.google.android.gms.internal.ay;

@SuppressLint("NewApi")
public class UploadService extends Service {
	
	protected static final String TAG = "UploadService";
	private Handler handler;
	//ConnectionDetector cd;
	SharedPreferences sharedPreferences;
	InputStream inputStream;
	private String imageName, imagePath;
	private String friendId, message, type, value;
	
	private String filename;
	private AsyncLoadVolley asyncLoadVolley;
	
	private OnUploadCompleteListener listener;
	
	private final IBinder mBinder = new MyBinder();
	
	public UploadService() {
		
	}
	
	public class MyBinder extends Binder {
		UploadService getService() {
	      return UploadService.this;
	    }
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void registerListener(OnUploadCompleteListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void onCreate() {
		handler = new Handler();
		//cd=new ConnectionDetector(getApplicationContext());      
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Bundle extras = intent.getExtras();
		imageName = extras.getString(Constant.NAME);
		imagePath = extras.getString(Constant.PATH);
		
		Log.e(TAG,"IN CHECK"+ "START");
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
			Log.e(TAG, "mess : "+message);
			Log.e(TAG, "Completed upload.");
			onUploadComplete(isComplete, message);
		}
		
		@Override
		public void onTaskBegin() {
			onUploadBegin();
		}
	};
	
	private String getImageFromPath(String imagePath) {
		if(imagePath.length()!=0)
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;			
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath); //BitmapFactory.decodeFile(imagePath, options);	//
	        ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream); //compress to which format you want.
	        byte [] byte_arr = stream.toByteArray();
	        String imageStr = Base64.encodeBytes(byte_arr);
	        return imageStr;
		}
        return null;
	}
	
	private void onUploadBegin() {
		
		handler.post(new Runnable() {
		    public void run()
		    {
				listener.onUploadBegin();
		    }
		});		
	}
	
	private void onUploadComplete(final boolean isComplete, final String message) {
		
		handler.post(new Runnable() {
		    public void run()
		    {		    	
		    	listener.onUploadComplete(isComplete, message);
		    }
		});		
	}
	
	@Override
	public void onDestroy() {
		Log.w("mine", "CLICKED");
		super.onDestroy();
	}
	
}
