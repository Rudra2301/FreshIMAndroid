package com.chat.imapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.interfaces.OnUploadCompleteListener;
import com.chat.imapp.items.FriendDetailItem;
import com.chat.imapp.items.UserDetailItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AlertDialogManager;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.CommonUtilities;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.ImageCustomize;
import com.chat.imapp.utility.Validate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
 
public class RegisterActivity extends Activity {
	
	protected static final String TAG = null;
	
	private Context context = RegisterActivity.this;
	
    // alert dialog manager
	private AlertDialogManager alert = new AlertDialogManager();
    
    // Internet detector
    private ConnectionDetector cd;
    
    // UI elements
    private EditText txtName, txtLastName, txtEmail, txtPassword, txtStatus;
    private ImageView userImageView;
    
    // Register button
    private Button btnRegister;
    
    private AsyncLoadVolley asyncLoadVolley;
    private String response = "";
	
	private ArrayList<UserDetailItem> list;
	
	////////////
	
    private GoogleCloudMessaging gcm;    
    private String regid;
    
    private String fname, lname, email, password, status;
    
    /// 
    
    private UploadService uploadService;
    private String defaultImage = "user.png";
    
    private Bitmap bitmap;
    private String path;
     
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        setContentView(R.layout.register);
        
        getActionBar().setTitle("Register");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);        
        
        String filename = getResources().getString(R.string.register_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);		
		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
		
        cd = new ConnectionDetector(getApplicationContext());
        
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = GcmRegistration.getRegistrationId(context);
            
            Log.e(TAG, " 1 -> Reg id : " + regid);
            
            if (regid.isEmpty()) {
            	registerInBackgroundMy();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        
        txtName = (EditText) findViewById(R.id.txtName);
        txtLastName = (EditText) findViewById(R.id.txtLastName);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtStatus = (EditText) findViewById(R.id.txtStatus);
        userImageView = (ImageView) findViewById(R.id.userImage);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        
        list = new ArrayList<UserDetailItem>();
        
        /*
         * Click event on Register button
         * */
        btnRegister.setOnClickListener(new View.OnClickListener() {
             
            @Override
            public void onClick(View arg0) {
            	
            	boolean check = true;
            	
            	Validate validate = new Validate();
            	
            	// Read EditText dat
        		fname = txtName.getText().toString();
                lname = txtLastName.getText().toString();
                email = txtEmail.getText().toString();
                password = txtPassword.getText().toString();
                status = txtStatus.getText().toString();
        		
        		if(validate.isNotEmpty(fname))
        		{
        			fname = txtName.getText().toString();
        		}
        		else
        		{
        			fname = "";
        			check = false;
        			txtName.setError("Enter First Name");
        		}
        		if(validate.isNotEmpty(lname))
        			lname = txtLastName.getText().toString();
        		else
        		{
        			lname = "";
        			check = false;
        			txtLastName.setError("Enter Last Name");
        		}
        		if(validate.isValidEmail(email))
        			email = txtEmail.getText().toString();
        		else
        		{
        			email = "";
        			check = false;
        			txtEmail.setError("Enter Valid email");
        		}
        		int passwordLength = 6;
        		if(validate.isAtleastValidLength(password, passwordLength))			
        			password = txtPassword.getText().toString();
        		else
        		{
        			password = "";
        			check = false;
        			txtPassword.setError("Password should be atleast "+passwordLength+" characters");
        		}
        		if(validate.isNotEmpty(status))
        			status = txtStatus.getText().toString();
        		else
        		{
        			status = "Hey !!";
        		}
        		if(check)
        		{	        			
        			if (regid.isEmpty()) {
                        registerInBackgroundMy();
                    }
        			else {
        				startRegistration();
        			}   
                } 
            }
        });
    }
    
    private void startRegistration() {
    	Map<String, String> map = new HashMap<String, String>();
    	map.put(Constant.NAME, fname);
    	map.put(Constant.LNAME, lname);
		map.put(Constant.EMAIL, email);
		map.put(Constant.PASSWORD, password);
		map.put(Constant.STATUS, status);
		map.put(Constant.REGID, GcmRegistration.getRegistrationId(context));
		asyncLoadVolley.setBasicNameValuePair(map);
    	asyncLoadVolley.beginTask();

	}
    
    private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			uploadService = ((UploadService.MyBinder)service).getService();
			uploadService.registerListener(listener);
			showToast("Connected");
		}
	};
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
	OnAsyncTaskListener asyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			response = message;
				
				AsyncResponse asyncResponse = new AsyncResponse(message);
				if(asyncResponse.ifSuccess())
				{	
					list = asyncResponse.getUserDetail();
										
					Log.e(TAG, "Status : "+status);
					
					String id = list.get(0).getId();
					String name = list.get(0).getName();
					String lname = list.get(0).getLname();
					String email = list.get(0).getEmail();
					String status = list.get(0).getStatus();
					String online = list.get(0).getOnline();
					
					Log.v(TAG, "Status l : "+list.get(0).getStatus());
					
					Log.v(TAG, "Status : "+status);
					
					Sessions.save(context, id, name, lname, email, status, online, defaultImage);
					
					if(bitmap==null || path==null) {}
					else
						onSelectPhoto(bitmap, path);
					
					// Launch Main Activity
			        Intent i = new Intent(getApplicationContext(), FriendsListActivity.class);
			        startActivity(i);
			        finish();
				}
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
	
	/////////////////////////***********8///////////
	
	//// GCM ////////////
		
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    GcmRegistration.PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
	private void registerInBackground() {
	    new AsyncTask() {
	        
	        protected void onPostExecute(Object result) {
	        	
	        };
	        
			@Override
			protected Object doInBackground(Object... arg0) {
				String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(GcmRegistration.SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;
	                
	                Log.e(TAG, "Reg id : " + regid);
	                GcmRegistration.storeRegistrationId(context, regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	            }
	            return msg;
			}
			
	    }.execute(null, null, null);
	}
	
	private void registerInBackgroundMy() {
	    new AsyncTask<Void, Void, Boolean>() {
	    	
	    	@Override
	    	protected void onPreExecute() {
	    		super.onPreExecute();
	    	}
	    	
	    	@Override
	    	protected Boolean doInBackground(Void... params) {
	    		String msg = "";
	    		try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(GcmRegistration.SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;
	                
	                Log.e(TAG, "Reg id : " + regid);
	                GcmRegistration.storeRegistrationId(context, regid);
	                return true;
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                return false;
	            }
	    		
	    	}
	    	
	    	@Override
	    	protected void onPostExecute(Boolean result) {	    		
	    		super.onPostExecute(result);
	    		
	    		if(result) {
	    			startRegistration();
	    		}
	    		else {
	    			showToast("Please try again");
	    		}
	    	}
		};
	}
	
	private static final int CAMERA_REQUEST = 1;
	private static final int RESULT_LOAD_IMAGE = 2;
	
	private void onSelectPhoto(Bitmap bitmap, String path) {
		
		String name = "user"+Sessions.getUserId(context) + new Date().getTime() +".jpg";
		
		Intent intent=new Intent(context, LoadImageService.class);
		intent.putExtra(Constant.NAME, name);
		intent.putExtra(Constant.PATH, path);
		intent.putExtra(Constant.VALUE, LoadImageService.VALUE_PROFILE_PIC);
		startService(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK)
			return;
		
		switch (requestCode) {
		case CAMERA_REQUEST:
			
			if (resultCode == Activity.RESULT_OK)
			{
				File imagesFolder = new File(Environment.getExternalStorageDirectory(), "myfolder");
				File image = new File(imagesFolder, "image_002.jpg");
								
				String path=image.getAbsolutePath();
				
				File file = new File(path);
				Bitmap bitmap = ImageCustomize.decodeFile(file, 200);
				
				userImageView.setImageBitmap(bitmap);
				
				this.bitmap = bitmap;
				this.path = path;
			}
			break;
			
		case RESULT_LOAD_IMAGE:
			{
				 if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			            Uri selectedImage = data.getData();
			            String[] filePathColumn = { MediaStore.Images.Media.DATA };
			            Cursor cursor = getContentResolver().query(selectedImage,
			                    filePathColumn, null, null, null);
			            cursor.moveToFirst();
			            
			            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			            String picturePath = cursor.getString(columnIndex);
			            cursor.close();
			            

						File file = new File(picturePath);
						Bitmap bitmap = ImageCustomize.decodeFile(file, 200);
			        	userImageView.setImageBitmap(bitmap);
									        	
			        	this.bitmap = bitmap;
						this.path = picturePath;	
				 	}
			}
		}
	}
	
	public void onUploadClick(View view) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Choose Image Source");
		builder.setItems(new CharSequence[] {"Gallery", "Camera"}, 
		        new DialogInterface.OnClickListener() {

		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which) {
		        case 0:
		        	callgalery();
		            break;

		        case 1:
		        	callCamera();
		            break;

		        default:
		            break;
		        }
		    }
		});
		
		builder.show();
	}
    
    public void callCamera() {
		
		File imagesFolder = new File(Environment.getExternalStorageDirectory(), "myfolder");
		if (!imagesFolder.exists()) {
			imagesFolder.mkdirs();
			}
		File image = new File(imagesFolder, "image_002.jpg");
				
		Uri uriSavedImage = Uri.fromFile(image);
		
		 Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
		 i.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage); 
	        startActivityForResult(i, CAMERA_REQUEST);
	        
	}
	
	protected void callgalery()
	{
		Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
	}
	
	OnUploadCompleteListener listener = new OnUploadCompleteListener() {
		
		@Override
		public void onUploadComplete(boolean isComplete, String message) {
			Log.e(TAG, "Uploading completed.");
			unbindService(mConnection);
			showToast("Uploading completed.");
		}
		
		@Override
		public void onUploadBegin() {
			showToast("Uploading started.");
			Log.e(TAG, "Uploading started.");
		}
	};
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}		
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
    
    
    
    
    
}
