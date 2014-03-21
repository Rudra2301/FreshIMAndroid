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
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.Validate;

@SuppressLint("NewApi")
public class EditPasswordActivity extends Activity {

protected static final String TAG = "EditPasswordActivity";
	
	private Context context = EditPasswordActivity.this;
	
	private AsyncLoadVolley asyncLoadVolley;
    
	EditText txtPasswordOld, txtPasswordNew, txtPasswordConfirm;
    
    private String oldPassword, newPassword, confirmPassword;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.edit_password);
		
		getActionBar().setTitle("Edit Profile");
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        txtPasswordOld = (EditText) findViewById(R.id.oldedittext);
        txtPasswordNew = (EditText) findViewById(R.id.newedittext);
        txtPasswordConfirm = (EditText) findViewById(R.id.confirmedittext);
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
		
		if(!txtPasswordOld.getText().toString().equals("")) {
			oldPassword = txtPasswordOld.getText().toString();			
		}
		else
			oldPassword = "";
		
		if(!txtPasswordNew.getText().toString().equals(""))
			newPassword = txtPasswordNew.getText().toString();
		else
			newPassword = "";
		
		if(!txtPasswordConfirm.getText().toString().equals(""))			
			confirmPassword = txtPasswordConfirm.getText().toString();
		else
			confirmPassword = "";
		
		
		outState.putString(Constant.PASSWORD_OLD, oldPassword);
		outState.putString(Constant.PASSWORD_NEW, newPassword);
		outState.putString(Constant.PASSWORD_CONFIRM, confirmPassword);
		super.onSaveInstanceState(outState);
	};
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);	
		oldPassword = savedInstanceState.getString(Constant.PASSWORD_OLD);
		newPassword = savedInstanceState.getString(Constant.PASSWORD_NEW);
		confirmPassword = savedInstanceState.getString(Constant.PASSWORD_CONFIRM);
		setDetails(oldPassword, newPassword, confirmPassword);
	};
	
	private void setDetails(String oldPassword, String lname, String status) {
		txtPasswordOld.setText(""+oldPassword);
		txtPasswordNew.setText(""+lname);
		txtPasswordConfirm.setText(""+status);
	}
	
	private void onSave() {
		
		boolean check = true;
		
		Validate validate = new Validate();
		
		oldPassword 	= txtPasswordOld.getText().toString();
		newPassword 	= txtPasswordNew.getText().toString();
		confirmPassword = txtPasswordConfirm.getText().toString();
		
		if(validate.isNotEmpty(oldPassword))
		{
			oldPassword = txtPasswordOld.getText().toString();
		}
		else
		{
			oldPassword = "";
			check = false;
			txtPasswordOld.setError("Enter Old Password");
		}
		int passwordLength = 6;
		if(validate.isAtleastValidLength(newPassword, passwordLength))	
			newPassword = txtPasswordNew.getText().toString();
		else
		{
			newPassword = "";
			check = false;
			txtPasswordNew.setError("New Password should be atleast "+passwordLength+" characters.");
		}
		if(validate.isAtleastValidLength(confirmPassword, passwordLength))
			confirmPassword = txtPasswordConfirm.getText().toString();
		else
		{
			confirmPassword = "";
			check = false;
			txtPasswordConfirm.setError("Confirmation Password not matching.");
		}
		if(check)
		{	
			if(newPassword.equals(confirmPassword))
			{
				String filename = getResources().getString(R.string.edit_password_php);
				asyncLoadVolley = new AsyncLoadVolley(context, filename);
				Map<String, String> map = new HashMap<String, String>();
				map.put(Constant.ID, Sessions.getUserId(context));
				map.put(Constant.PASSWORD_OLD, oldPassword);
	        	map.put(Constant.PASSWORD_NEW, newPassword);
	    		map.put(Constant.PASSWORD_CONFIRM, confirmPassword);
				asyncLoadVolley.setBasicNameValuePair(map);
				asyncLoadVolley.setOnAsyncTaskListener(changeAsyncTaskListener);
				asyncLoadVolley.beginTask();
			}
			else
			{
				showToast("Password doesn't match.");
				
			}
		}
		else
		{
			
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
				showToast("Successfully Changed");
			}
			else
			{
				showToast(""+ asyncResponse.getMessage());
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
