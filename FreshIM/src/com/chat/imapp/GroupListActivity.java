package com.chat.imapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chat.imapp.adapters.FriendDetailAdapter;
import com.chat.imapp.adapters.GroupListAdapter;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendDetailItem;
import com.chat.imapp.items.GroupListItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.CommonUtilities;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;

@SuppressLint("NewApi")
public class GroupListActivity extends Activity {

protected static final String TAG = "GroupListActivity";
	
	private Context context = GroupListActivity.this;
	
	private ListView listView;
	private RelativeLayout actionBarLayout;
	
	private AsyncLoadVolley asyncLoadVolley;
	
	private List<GroupListItem> list;
	private GroupListAdapter adapter;
    
	private ConnectionDetector connectionDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.friends_list);
		
		Log.e(TAG, "onCreate");
		
		getActionBar().setTitle("Groups");
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
		listView = (ListView) findViewById(R.id.listview);
		actionBarLayout = (RelativeLayout) findViewById(R.id.actionbar);
		actionBarLayout.setVisibility(View.GONE);
		
		list = new ArrayList<GroupListItem>();
		adapter = new GroupListAdapter(context, list);
		
		listView.setAdapter(adapter);
		
		String filename = getResources().getString(R.string.grouplist_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.USER_ID, Sessions.getUserId(context));
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
		
		connectionDetector = new ConnectionDetector(context);
		
		/*
		if(savedInstanceState==null) {
			if(connectionDetector.isConnectedToInternet())
				asyncLoadVolley.beginTask();
			else
				showToast("Not Connected to Internet");
		}*/
		
		listView.setOnItemClickListener(listItemClickListener);
		listView.setOnItemLongClickListener(longClickListener);
		
		registerReceiver(mHandleMessageReceiver, new IntentFilter(CommonUtilities.GROUP_MESSAGE_ACTION));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.group_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
				
		Intent intent = new Intent();
			
			switch (item.getItemId()) {
				
			case android.R.id.home:
					finish();
				return true;
				
			case R.id.creategroup:			
				intent.setClass(context, GroupCreateActivity.class);
				startActivity(intent);
				
			default:
				return super.onOptionsItemSelected(item);
			}
	}
	
	OnAsyncTaskListener asyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			setGroupList(message);
			setProgressBarIndeterminateVisibility(false);
		}
		
		@Override
		public void onTaskBegin() {
			setProgressBarIndeterminateVisibility(true);
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
	
	OnItemClickListener listItemClickListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			if (list.get(position).isNew()) {
				list.get(position).setNew(false);
				adapter.refresh(list);
			}
			
			Intent intent = new Intent();
			
			intent.setClass(context, GroupMessageDetailActivity.class);
			intent.putExtra(Constant.GROUP_ID, list.get(position).getId());
			intent.putExtra(Constant.NAME, list.get(position).getName());
			intent.putExtra(Constant.IMAGE, list.get(position).getImage());
			intent.putExtra(Constant.POSITION, position);
			intent.putExtra(Constant.DATA, list.get(position).getStatus());
    		intent.putExtra(Constant.ADMIN_ID, list.get(position).getAdminId());
			
    		//intent.putExtra(Constant.IMAGE, Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + list.get(position).getImage());
    		
			
        	startActivityForResult(intent, 0);
		}
	};
	
	private void setGroupList(String response) {
		
		AsyncResponse asyncResponse = new AsyncResponse(response);
		if(asyncResponse.ifSuccess())
		{
			list = asyncResponse.getGroupList();
			Log.i(TAG, "resp : "+response);
			adapter.refresh(list);
		}
		else
		{
			list = new ArrayList<GroupListItem>();
			adapter.refresh(list);
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
    			
    			List<GroupListItem> listFriend = new ArrayList<GroupListItem>();
    			listFriend = list;
    			
    			for (int i = 0; i < list.size(); i++) {
					if(list.get(i).getId().equals(userid)) {
						 
						 String msg = listDetail.get(0).getMessage();
						 
						 if(Integer.parseInt(listDetail.get(0).getType())==FriendDetailAdapter.TYPE_IMAGE)
							msg = "Image";	
						 
						list.get(i).setStatus(msg);
						list.get(i).setNew(true);
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
    
    protected void onDestroy() {
    	super.onDestroy();
    	try {
            unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
    };
    
    OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
    	@Override
    	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    		//if(list.get(position).getAdminId().equals(Sessions.getUserId(context)))
    		showMyDialog(context, position);
    		return false;
    	}
	};
    
	String[] values = new String[] 
			{ 
				"Group Details"
			};
	
	private void showMyDialog(final Context context, final int position) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.simplelistview);
		ListView listView = (ListView) dialog.findViewById(R.id.list);
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
	              android.R.layout.simple_list_item_1, android.R.id.text1, values);
		listView.setAdapter(arrayAdapter);
		dialog.show();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(android.widget.AdapterView<?> arg0, View arg1, int pos, long arg3) 
			{
				if(pos==0) {
					//deleteGroup(context, groupId);
					
					Intent intent = new Intent();
		    		intent.setClass(context, GroupDetailActivity.class);
		    		intent.putExtra(Constant.NAME, list.get(position).getName());
		    		intent.putExtra(Constant.IMAGE, Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + list.get(position).getImage());
		    		intent.putExtra(Constant.GROUP_ID, list.get(position).getId());
		    		intent.putExtra(Constant.ADMIN_ID, list.get(position).getAdminId());
		    		startActivity(intent);
		    		
		    		dialog.dismiss();
					
				}
			}
		});
	}
	
    @Override
    	protected void onResume() {
    		super.onResume();
    		Log.e(TAG, "onResume");
    		if(connectionDetector.isConnectedToInternet())
				asyncLoadVolley.beginTask();
			else
				showToast("Not Connected to Internet");
    	}
    
    
    
}
