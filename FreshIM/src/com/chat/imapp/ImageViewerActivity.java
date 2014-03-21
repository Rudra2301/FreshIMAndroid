package com.chat.imapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.adapters.FriendAdapter;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendDetailItem;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Constant;

@SuppressLint("NewApi")
public class ImageViewerActivity extends Activity {

protected static final String TAG = "ImageViewerActivity";
	
	private Context context = ImageViewerActivity.this;
	
	private ListView listView;
	
	private AsyncLoadVolley asyncLoadVolley;
	private String filename;
	
	private List<FriendItem> list;
	private FriendAdapter adapter;
	
	private TextView nameTextView, descTextView;
	private ImageView profileImageView;
	private ImageLoader imageLoader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_list);
		
		getActionBar().setTitle(Sessions.getName(context) + "'s friends");
		
		listView = (ListView) findViewById(R.id.listview);
		
		imageLoader = new ImageLoader(context);
		
		list = new ArrayList<FriendItem>();
		adapter = new FriendAdapter(context, list);
		listView.setAdapter(adapter);
		
		filename = getResources().getString(R.string.friendlist_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.ID, Sessions.getUserId(context));
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
		
		if(savedInstanceState==null)
			asyncLoadVolley.beginTask();
		
		listView.setOnItemClickListener(listItemClickListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.friends_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
			switch (item.getItemId()) {
			case R.id.logout:
					logoutUser();
					
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
	}
	
	private void logoutUser() {
		
		String filename = getResources().getString(R.string.logout_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.ID, Sessions.getUserId(context));
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(logoutAsyncTaskListener);
		asyncLoadVolley.beginTask();
	}
	
	private String response = "";
	OnAsyncTaskListener asyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			response = message;
			setFriendList(response);
		}
		
		@Override
		public void onTaskBegin() {
		}
	};
	
	OnAsyncTaskListener logoutAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			
			AsyncResponse asyncResponse = new AsyncResponse(response);
			if(asyncResponse.ifSuccess())
			{
				Sessions.clear(context);
				finish();				
			}
			else
			{
				showToast("Error Logging Out.");
			}			
		}
		
		@Override
		public void onTaskBegin() {
		}
	};
	
	OnItemClickListener listItemClickListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			Intent intent = new Intent();
			
			intent.setClass(context, FriendsDetailActivity.class);
			intent.putExtra(Constant.FRIEND_ID, list.get(position).getId());
			intent.putExtra(Constant.NAME, list.get(position).getName());
			intent.putExtra(Constant.ONLINE, list.get(position).getIsOnline());
        	startActivity(intent);		
		}
	};
	
	private void setFriendList(String response) {
		
		AsyncResponse asyncResponse = new AsyncResponse(response);
		if(asyncResponse.ifSuccess())
		{	
			list = asyncResponse.getFriendList();
			Log.i(TAG, "resp : "+response);
			adapter.refresh(list);
		}
		else
		{
			Log.e(TAG, "err : "+asyncResponse.getMessage());
		}	
	}	
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}		
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
}
