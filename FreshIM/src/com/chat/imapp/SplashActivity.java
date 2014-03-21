package com.chat.imapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.UserDetailItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AlertDialogManager;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.CommonUtilities;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.ScreenSize;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
 
public class SplashActivity extends Activity {
	
	protected static final String TAG = null;
	
	private Context context = SplashActivity.this;
	
    // alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();
    
    // Internet detector
    ConnectionDetector cd;
    
    // UI elements
    EditText txtPassword;
    EditText txtEmail;
    
    // Register button
    Button btnLogin;
    
    private ImageView logoImageView1;
    
    private AsyncLoadVolley asyncLoadVolley;
	private String filename;
	
	ArrayList<UserDetailItem> list;
	
	private ScreenSize screenSize;
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(Sessions.isLoggedIn(context)) {
        	Intent i = new Intent(getApplicationContext(), FriendsListActivity.class);
            startActivity(i);
            finish();
        }
        else 
        {
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        	setContentView(R.layout.splash);
                
        filename = getResources().getString(R.string.login_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);		
		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
		
        cd = new ConnectionDetector(getApplicationContext());
        
        txtEmail = (EditText) findViewById(R.id.loginEmail);
        txtPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        
        logoImageView1 = (ImageView) findViewById(R.id.logo_image1);
        
        screenSize = new ScreenSize(context);
		
		int duration = 1300;
        
        Animation animationTop = new TranslateAnimation(0, 0, (int)(screenSize.getScreenHeightPixel()), 0); 
        animationTop.setDuration(duration);
        animationTop.setFillAfter(true);
        animationTop.setInterpolator(new DecelerateInterpolator());
        logoImageView1.setAnimation(animationTop);
        
        
        
        list = new ArrayList<UserDetailItem>();
        
        /*
         * Click event on Register button
         * */
        btnLogin.setOnClickListener(new View.OnClickListener() {
             
            @Override
            public void onClick(View arg0) {
                // Read EditText dat
                String email = txtEmail.getText().toString();
                String password = txtPassword.getText().toString();
                 
                // Check if user filled the form
                if(password.trim().length() > 0 && email.trim().length() > 0) {
                	
                	Map<String, String> map = new HashMap<String, String>();
            		map.put(Constant.EMAIL, email);
            		map.put(Constant.PASSWORD, password);
            		asyncLoadVolley.setBasicNameValuePair(map);
                	asyncLoadVolley.beginTask();
                    
                } else {
                    // user doen't filled that data
                    // ask him to fill the form
                    alert.showAlertDialog(SplashActivity.this, "Login Error!", "Please enter email and/or password", false);
                }
            }
        });
        }
    }
    
    private String response = "";
	OnAsyncTaskListener asyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			response = message;
			
				AsyncResponse asyncResponse = new AsyncResponse(response);
				if(asyncResponse.ifSuccess())
				{	
					list = asyncResponse.getUserDetail();
					
					String id = list.get(0).getId();
					String name = list.get(0).getName();
					String lname = list.get(0).getLname();
					String email = list.get(0).getEmail();
					String status = list.get(0).getStatus();
					String online = list.get(0).getOnline();
					String image = list.get(0).getImage();
					Log.e(TAG, "image : "+image);
					storeId(context, id, name, lname, email, status, online, image);
					
					// Launch Main Activity
			        Intent i = new Intent(getApplicationContext(), FriendsListActivity.class);
			        startActivity(i);
			        finish();
				}
				else
				{
					showToast("Incorrect email or password.");
				}
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
	
	private void storeId(Context context, String id, String name, String lname, String email, String status, String online, String image) {
	    Sessions.save(context, id, name, lname, email, status, online, image);
	}
	
	public void onSkipClick(View view) {
		Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(i);
        finish();
	}	
	
	
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}		
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
}
