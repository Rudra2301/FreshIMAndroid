package com.chat.imapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SlidingPaneLayout.PanelSlideListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.adapters.FriendAdapter;
import com.chat.imapp.adapters.FriendAllAdapter;
import com.chat.imapp.adapters.FriendDetailAdapter;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendDetailItem;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.CommonUtilities;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;

@SuppressLint("NewApi")
public class FriendsAllListActivity extends Activity {

protected static final String TAG = "FriendsAllListActivity";
	
	private Context context = FriendsAllListActivity.this;
	
	private ListView listView;
	
	private List<FriendItem> list;
	private FriendAllAdapter adapter;
	
	private ConnectionDetector connectionDetector;
	
	private AsyncLoadVolley asyncLoadVolley;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simplelistview);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle("Friends");
		
		listView = (ListView) findViewById(R.id.list);
		
		list = new ArrayList<FriendItem>();
		adapter = new FriendAllAdapter(context, list);
		
		listView.setAdapter(adapter);
		
		String filename = getResources().getString(R.string.friendalllist_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.ID, Sessions.getUserId(context));
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
		
		connectionDetector = new ConnectionDetector(context);
		
		if(savedInstanceState==null) {
			
			
		}
		listView.setOnItemClickListener(listItemClickListener);
		
		registerReceiver(mHandleFriendsRequestReceiver, new IntentFilter(CommonUtilities.REQUEST_MESSAGE_ACTION));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(connectionDetector.isConnectedToInternet())
			asyncLoadVolley.beginTask();
		else
			showToast("Not Connected to Internet");
	}
	
	OnAsyncTaskListener asyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			setFriendList(message);
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
	
	OnItemClickListener listItemClickListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			showToast("clicked");
			
			/*
			if (list.get(position).isNew()) {
				list.get(position).setNew(false);
				adapter.refresh(list);
			}
			
			Intent intent = new Intent();			
			intent.setClass(context, FriendsDetailActivity.class);
			intent.putExtra(Constant.FRIEND_ID, list.get(position).getId());
			intent.putExtra(Constant.NAME, list.get(position).getName());
			intent.putExtra(Constant.IMAGE, list.get(position).getImage());
			intent.putExtra(Constant.ONLINE, list.get(position).getIsOnline());
			intent.putExtra(Constant.POSITION, position);
			intent.putExtra(Constant.DATA, list.get(position).getStatus());
        	startActivityForResult(intent, 0);
        	*/
		}
	};
	
	private void setFriendList(String response) {
		
		AsyncResponse asyncResponse = new AsyncResponse(response);
		if(asyncResponse.ifSuccess())
		{	
			list = asyncResponse.getFriendAllList();
			Log.i(TAG, "resp : "+response);
			adapter.refresh(list);
		}
		else
		{
			Log.e(TAG, "err : "+asyncResponse.getMessage());
			showToast(asyncResponse.getMessage());
		}	
	}	
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
    
	/**
     * Refreshing friends details
     * */
    private final BroadcastReceiver mHandleFriendsRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString("message");
            
            Log.i(TAG, "newMessage : "+newMessage);
            
            String filename = getResources().getString(R.string.friendalllist_php);
    		asyncLoadVolley = new AsyncLoadVolley(context, filename);
    		Map<String, String> map = new HashMap<String, String>();
    		map.put(Constant.ID, Sessions.getUserId(context));
    		asyncLoadVolley.setBasicNameValuePair(map);
    		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
    		
    		connectionDetector = new ConnectionDetector(context);
    		if(connectionDetector.isConnectedToInternet())
    			asyncLoadVolley.beginTask();
    		
        }
    };	
    
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
    
    protected void onDestroy() {
    	super.onDestroy();
    	try {
            unregisterReceiver(mHandleFriendsRequestReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
    };
}
