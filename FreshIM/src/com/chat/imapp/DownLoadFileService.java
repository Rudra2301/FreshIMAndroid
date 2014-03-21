package com.chat.imapp;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.widget.Toast;

import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Base64;
import com.chat.imapp.utility.Constant;

@SuppressLint("NewApi")
public class DownLoadFileService extends Service {
	
	protected static final String TAG = "DownLoadFileService";
	private Handler handler;
	SharedPreferences sharedPreferences;
	InputStream inputStream;
	private String imageName, imageUrl, imagePath;
	private String friendId, message, type, value;
	private int position = 0;
	private String data = "1";
	private String groupId;
	
	private String filename;
	private AsyncLoadVolley asyncLoadVolley;
	
	public static final String VALUE_MESSAGE = "1";
	
	public DownLoadFileService() {
		
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
		imageUrl = extras.getString(Constant.URL);
		imagePath = extras.getString(Constant.PATH);
		value = extras.getString(Constant.VALUE);
				
		Log.e("IN CHECK", "START");
		
		onDownloadStart(imageUrl, imagePath);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onDownloadStart(final String imageUrl, String imagePath) {
		
			handler.post(new Runnable() {
			    public void run()
			    {
			    	new DownloadFileFromURL().execute(imageUrl);
			    }
			});		
	}
	
	/**
	 * Background Async Task to download file
	 * */
	class DownloadFileFromURL extends AsyncTask<String, String, String> {
	 
		private static final String FOLDER_FILE = "/files";
		
	    /**
	     * Before starting background thread
	     * Show Progress Bar Dialog
	     * */
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	    }
	    
	    /**
	     * Downloading file in background thread
	     * */
	    @Override
	    protected String doInBackground(String... f_url) {
	        int count;
	        try {
	        	
	        	Log.e("IN CHECK", "url : "+f_url[0]);
	            URL url = new URL(f_url[0]);
	            URLConnection conection = url.openConnection();
	            conection.connect();
	            // getting file length
	            int lenghtOfFile = conection.getContentLength();
	            
	            // input stream to read file - with 8k buffer
	            InputStream input = new BufferedInputStream(url.openStream(), 8192);
	            
	            File f = new File(Environment.getExternalStorageDirectory(),  "Files");
	            if (!f.exists()) {
					f.mkdir();
				}
	            File file = new File(f, imageName);
	            // Output stream to write file
	            OutputStream output = new FileOutputStream(file);
	            
	            byte data[] = new byte[1024];
	            
	            long total = 0;
	            
	            while ((count = input.read(data)) != -1) {
	                total += count;
	                // publishing the progress....
	                // After this onProgressUpdate will be called
	                publishProgress(""+(int)((total*100)/lenghtOfFile));
	 
	                // writing data to file
	                output.write(data, 0, count);
	            }
	 
	            // flushing output
	            output.flush();
	 
	            // closing streams
	            output.close();
	            input.close();
	 
	        } catch (Exception e) {
	            Log.e("Error: ", e.getMessage());
	        }
	 
	        return null;
	    }
	 
	    /**
	     * Updating progress bar
	     * */
	    protected void onProgressUpdate(String... progress) {
	        // setting progress percentage
	        //pDialog.setProgress(Integer.parseInt(progress[0]));
	    	//Toast.makeText(getApplicationContext(), "Progress : " + progress[0], Toast.LENGTH_SHORT).show();
	   }
	 
	    /**
	     * After completing background task
	     * Dismiss the progress dialog
	     * **/
	    @Override
	    protected void onPostExecute(String file_url) {
	        // dismiss the dialog after the file was downloaded
	    	
	    	stopSelf();
	    	
	    	
	    	/*
	        // Displaying downloaded image into image view
	        // Reading image path from sdcard
	        String imagePath = Environment.getExternalStorageDirectory().toString() + "/downloadedfile.jpg";
	        // setting downloaded into image view
	        my_image.setImageDrawable(Drawable.createFromPath(imagePath));
	        */
	        
	    }	 
	}
	
/////
		
	@Override
	public void onDestroy() {
		Log.w("mine", "CLICKED");
		super.onDestroy();
	}
	
}
