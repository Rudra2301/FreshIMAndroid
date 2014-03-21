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
public class FriendsListActivity extends Activity {

protected static final String TAG = "UserActivity";
	
	private Context context = FriendsListActivity.this;
	
	private ListView listView;
	
	private List<FriendItem> list;
	private FriendAdapter adapter;
	
	private TextView nameTextView, statusTextView;
	private ImageView profileImageView;
	private ImageLoader imageLoader;
		
	AsyncLoadVolley asyncLoadVolley;
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private RelativeLayout mDrawerListLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private RelativeLayout actionBarLayout;
    
    private String[] listItems;
    
    private static final int HOME = 0;
    private static final int STATUS = 1;
    private static final int DIRECTORY = 2;
    private static final int SETTINGS = 3;
    private static final int LOGOUT = 4;
    
    private SlidingPaneLayout pane;
	private ImageView toggleImageView;
	private ImageView nofriendsImageView;
	
	
	
	private ConnectionDetector connectionDetector;
	
	private Animation animation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.homeslidingpane);
		
		actionBarLayout = (RelativeLayout) findViewById(R.id.actionbar);
		animation = AnimationUtils.loadAnimation(context, R.anim.fade_actionbar);
		actionBarLayout.setAnimation(animation);
		actionBarLayout.setVisibility(View.VISIBLE);
				
		listItems = getResources().getStringArray(R.array.homelistitem);
        
		mDrawerList 		= (ListView) findViewById(R.id.left_drawer);
		
		pane 		= (SlidingPaneLayout) findViewById(R.id.drawer_layout);
		pane.setVerticalFadingEdgeEnabled(false);
		pane.setPanelSlideListener(slideListener);
		
		View homelist = (View) findViewById(R.id.homelist);
		profileImageView = (ImageView) homelist.findViewById(R.id.user_image);
		nameTextView = (TextView) homelist.findViewById(R.id.usernameTextview);
		statusTextView = (TextView) homelist.findViewById(R.id.statusTextview);
		
		View friendlist = (View) findViewById(R.id.friendslist);
		toggleImageView = (ImageView) friendlist.findViewById(R.id.toggle);
		nofriendsImageView = (ImageView) friendlist.findViewById(R.id.nofriendsimage);
		nofriendsImageView.setVisibility(View.GONE);
		
		nameTextView.setText(""+Sessions.getName(context));
		statusTextView.setText("\""+Sessions.getStatus(context)+"\"");
		
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.simplelistitem, listItems));
		mDrawerList.setOnItemClickListener(listItemClickListener);
		mDrawerList.setOnItemClickListener(listener);
		
		listView = (ListView) findViewById(R.id.listview);
		listView.setFocusable(true);
		
		imageLoader = new ImageLoader(context);
		String url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + Sessions.getImage(context);
		imageLoader.displayImage(url, profileImageView, true, 90);
		
		list = new ArrayList<FriendItem>();
		adapter = new FriendAdapter(context, list);
		
		listView.setAdapter(adapter);
		
		/*
		String filename = getResources().getString(R.string.friendlistonlyfriends_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.ID, Sessions.getUserId(context));
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
		
		connectionDetector = new ConnectionDetector(context);
		*/
		
		/*
		if(savedInstanceState==null) {
			
			if(connectionDetector.isConnectedToInternet())
				asyncLoadVolley.beginTask();
			else
				showToast("Not Connected to Internet");
		}*/
		listView.setOnItemClickListener(listItemClickListener);
		
		registerReceiver(mHandleMessageReceiver, new IntentFilter(CommonUtilities.SINGLE_MESSAGE_ACTION));		
		registerReceiver(mHandleFriendsRequestReceiver, new IntentFilter(CommonUtilities.REQUEST_MESSAGE_ACTION));
	}
	
	@Override
		protected void onResume() {
			super.onResume();
			nameTextView.setText(""+Sessions.getName(context));
			statusTextView.setText("\""+Sessions.getStatus(context)+"\"");			
			
			String filename = getResources().getString(R.string.friendlistonlyfriends_php);
			asyncLoadVolley = new AsyncLoadVolley(context, filename);
			Map<String, String> map = new HashMap<String, String>();
			map.put(Constant.ID, Sessions.getUserId(context));
			asyncLoadVolley.setBasicNameValuePair(map);
			asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
			
			connectionDetector = new ConnectionDetector(context);
			
			if(connectionDetector.isConnectedToInternet())
				asyncLoadVolley.beginTask();
			else
				showToast("Not Connected to Internet");		
		}
	
	private void logoutUser() {
		
		String filename = getResources().getString(R.string.logout_php);
		AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.ID, Sessions.getUserId(context));
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(logoutAsyncTaskListener);
		asyncLoadVolley.beginTask();
	}
	
	OnAsyncTaskListener asyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			setFriendList(message);
		}
		
		@Override
		public void onTaskBegin() {
			nofriendsImageView.setVisibility(View.GONE);
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK)
			return;
		else 
		{
			switch (requestCode) {
			case 0:
				String text = data.getStringExtra(Constant.DATA);
				int pos = data.getIntExtra(Constant.POSITION, 0);
				list.get(pos).setStatus(text);
				adapter.refresh(list);
				break;

			default:
				break;
			}
		}	
	}
	
	OnAsyncTaskListener logoutAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			
			AsyncResponse asyncResponse = new AsyncResponse(message);
			if(asyncResponse.ifSuccess())
			{
				Sessions.clear(context);
				
				Intent intent = new Intent();				
				intent.setClass(context, LoginActivity.class);
				startActivity(intent);
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
		}
	};
	
	OnItemClickListener listener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			Intent intent = new Intent();
			
			switch (position) {
			
			case HOME:	        		
				pane.closePane();
				break;
				
			case STATUS:			
				intent.setClass(context, StatusActivity.class);
				intent.putExtra(Constant.STATUS, Sessions.getStatus(context));
				startActivity(intent);
				break;
				
			case SETTINGS:
				intent.setClass(context, SettingsActivity.class);
				intent.putExtra(Constant.STATUS, Sessions.getStatus(context));
				startActivity(intent);
				break;
				
			case LOGOUT:
				logoutUser();
				break;
				
			case DIRECTORY:			
				intent.setClass(context, GroupListActivity.class);
				startActivity(intent);
				break;
			
			default:
				break;
			}
		}
	};
	
	private void setFriendList(String response) {
				
		AsyncResponse asyncResponse = new AsyncResponse(response);
		if(asyncResponse.ifSuccess())
		{	
			nofriendsImageView.setVisibility(View.GONE);
			list = asyncResponse.getFriendList();
			Log.i(TAG, "resp : "+response);
			adapter.refresh(list);
		}
		else
		{
			Log.e(TAG, "err : "+asyncResponse.getMessage());
			//showToast(asyncResponse.getMessage());
			nofriendsImageView.setVisibility(View.VISIBLE);
			list = new ArrayList<FriendItem>();
			adapter.refresh(list);
		}	
	}		
	
	public void onToggleClick(View view) {
		if(pane.isOpen()) 
			pane.closePane();
		else
			pane.openPane();
	}
	
	public void onAddClick(View view) {
		
		Intent intent = new Intent();
		intent.setClass(context, FriendsAllListActivity.class);
		startActivity(intent);
		
	}
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
    
    PanelSlideListener slideListener = new PanelSlideListener() {
		
		@Override
		public void onPanelSlide(View arg0, float arg1) {
			
		}
		
		@Override
		public void onPanelOpened(View arg0) {
			toggleImageView.setImageResource(R.drawable.ic_action_previous_item);
		}
		
		@Override
		public void onPanelClosed(View arg0) {
			toggleImageView.setImageResource(R.drawable.ic_action_next_item);
		}
	};
	
	/**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString("message");
            
            Log.i(TAG, "newMessage : "+newMessage);
            
            AsyncResponse asyncResponse = new AsyncResponse(newMessage);
    		if(asyncResponse.ifSuccess())
    		{	
    			List<FriendDetailItem> listDetail = asyncResponse.getFriendDetail();
    			
    			String userid = listDetail.get(0).getUserId();
    			
    			List<FriendItem> listFriend = new ArrayList<FriendItem>();
    			listFriend = list;
    			
    			for (int i = 0; i < list.size(); i++) {
    				
					if(list.get(i).getId().equals(userid)) {
						
						 String msg = listDetail.get(0).getMessage();
						 
						 if(Integer.parseInt(listDetail.get(0).getType())==FriendDetailAdapter.TYPE_IMAGE)
							msg = "Image";	
						 
						 list.get(i).setStatus(msg);
						 list.get(i).setNew(true);
						break;
					}
				}
    			adapter.refresh(list);
    		}
    		else
    		{
    			Log.e(TAG, "err : "+asyncResponse.getMessage());
    		}
        }
    };	
    
    /**
     * Refreshing friends details
     * */
    private final BroadcastReceiver mHandleFriendsRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString("message");
            
            Log.i(TAG, "newMessage : "+newMessage);
            
            String filename = getResources().getString(R.string.friendlistonlyfriends_php);
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
    
    protected void onDestroy() {
    	super.onDestroy();
    	try {
            unregisterReceiver(mHandleMessageReceiver);
            unregisterReceiver(mHandleFriendsRequestReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
    };
}
