package com.chat.imapp;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Constant;

@SuppressLint("NewApi")
public class StatusActivity extends Activity {

protected static final String TAG = "StatusActivity";
	
	private Context context = StatusActivity.this;
	
	private AsyncLoadVolley asyncLoadVolley;
    
    private EditText statusEditText;
    
    private String status;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.edit_status);
		
		getActionBar().setTitle("Change your status");
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);        
        
        statusEditText = (EditText) findViewById(R.id.statusedittext);
        
        if(savedInstanceState==null) 
        {
        	status = Sessions.getStatus(context);
        	setStatus(status);
        }       
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.status, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
			switch (item.getItemId()) {
				
			case android.R.id.home:
	        	finish();
	        return true;
	        
			case R.id.savestatus:
	        	onSave();
	        return true;
	        
			default:
				return super.onOptionsItemSelected(item);
			}
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		if(!statusEditText.getText().toString().equals(""))			
			status = statusEditText.getText().toString();
		else
			status = "";
		outState.putString(Constant.STATUS, status);
		super.onSaveInstanceState(outState);
	};
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);		
		status = savedInstanceState.getString(Constant.STATUS);
		setStatus(status);
	};
	
	private void setStatus(String status) {
		statusEditText.setText(""+status);
	}

	private void onSave() {
		
		if(!statusEditText.getText().toString().equals("")) {
			
			status = statusEditText.getText().toString();
			String filename = getResources().getString(R.string.status_change_php);
			asyncLoadVolley = new AsyncLoadVolley(context, filename);
			Map<String, String> map = new HashMap<String, String>();
			map.put(Constant.ID, Sessions.getUserId(context));
			map.put(Constant.STATUS, status);
			asyncLoadVolley.setBasicNameValuePair(map);
			asyncLoadVolley.setOnAsyncTaskListener(changeAsyncTaskListener);
			asyncLoadVolley.beginTask();
		}
	}
	
	private String response = "";
	OnAsyncTaskListener changeAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			response = message;
			AsyncResponse asyncResponse = new AsyncResponse(response);
			if(asyncResponse.ifSuccess())
			{	
				showToast("Status Changed");
				Sessions.setStatus(context, status);
			}
			else
			{
				showToast("Error .");
			}	
			setProgressBarIndeterminateVisibility(false);
		}
		
		@Override
		public void onTaskBegin() {
			setProgressBarIndeterminateVisibility(true);
		}
	};
		
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}		
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
}
