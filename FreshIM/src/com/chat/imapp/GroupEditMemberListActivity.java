package com.chat.imapp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chat.imapp.adapters.GroupEditMemberListAdapter;
import com.chat.imapp.adapters.GroupFriendListDetailAdapter;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.items.GroupListItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.Validate;

@SuppressLint("NewApi")
public class GroupEditMemberListActivity extends Activity {

protected static final String TAG = "GroupEditMemberListActivity";
	
	private Context context = GroupEditMemberListActivity.this;
	
	private ListView listView;
	private RelativeLayout actionBarLayout;
	
	private AsyncLoadVolley asyncLoadVolley;
	
	private List<FriendItem> list;
	private GroupEditMemberListAdapter adapter;
    
	private ConnectionDetector connectionDetector;
	
	private String name, groupId, adminId;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.friends_list);		
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
        Bundle bundle = getIntent().getExtras();
        name = bundle.getString(Constant.NAME);
        groupId = bundle.getString(Constant.GROUP_ID);
        adminId = bundle.getString(Constant.ADMIN_ID);
        
        getActionBar().setTitle("Edit friends from '"+name+"'");
        
		listView = (ListView) findViewById(R.id.listview);
		actionBarLayout = (RelativeLayout) findViewById(R.id.actionbar);
		actionBarLayout.setVisibility(View.GONE);
		
		list = new ArrayList<FriendItem>();
		adapter = new GroupEditMemberListAdapter(context, list, groupId, adminId);
		
		listView.setAdapter(adapter);
		
		String filename = getResources().getString(R.string.groupfriendlistmember_php);
        AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.GROUP_ID, groupId);
		map.put(Constant.TYPE, GroupFriendListDetailAdapter.ALL_FRIENDS_GROUP);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
		
		connectionDetector = new ConnectionDetector(context);
		
		if(savedInstanceState==null) {
			if(connectionDetector.isConnectedToInternet())
				asyncLoadVolley.beginTask();
			else
				showToast("Not Connected to Internet");
		}
		listView.setOnItemClickListener(listItemClickListener);
		listView.setFocusable(true);
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
	
	OnItemClickListener listItemClickListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
		}
	};
	
	private void setGroupList(String response) {
		
		AsyncResponse asyncResponse = new AsyncResponse(response);
		if(asyncResponse.ifSuccess())
		{	
			list = asyncResponse.getGroupFriendAndMemberList();
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
    
}
