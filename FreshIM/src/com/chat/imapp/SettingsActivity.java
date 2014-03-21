package com.chat.imapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.adapters.FriendAdapter;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Constant;

@SuppressLint("NewApi")
public class SettingsActivity extends Activity {

protected static final String TAG = "SettingsActivity";
	
	private Context context = SettingsActivity.this;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		getActionBar().setTitle("Account Settings");
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
	}
	
	public void onChangeStatusClick(View view) {
		Intent intent = new Intent();
		intent.setClass(context, StatusActivity.class);
    	startActivity(intent);
	}
	
	public void onChangePasswordClick(View view) {
		Intent intent = new Intent();
		intent.setClass(context, EditPasswordActivity.class);
    	startActivity(intent);
	}
	
	public void onEditProfileClick(View view) {
		Intent intent = new Intent();
		intent.setClass(context, EditProfileActivity.class);
    	startActivity(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
			switch (item.getItemId()) {
				
			case android.R.id.home:
	        	finish();
	        return true;
	        
			default:
				return super.onOptionsItemSelected(item);
			}
	}
}
