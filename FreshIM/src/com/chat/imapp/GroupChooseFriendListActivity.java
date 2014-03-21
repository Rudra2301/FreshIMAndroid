package com.chat.imapp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.chat.imapp.adapters.GroupFriendListAdapter;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.items.GroupListItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.Validate;
import com.google.android.gms.internal.ay;

@SuppressLint("NewApi")
public class GroupChooseFriendListActivity extends Activity {

protected static final String TAG = "UserActivity";
	
	private Context context = GroupChooseFriendListActivity.this;
	
	private ListView listView;
	private RelativeLayout actionBarLayout;
	
	private AsyncLoadVolley asyncLoadVolley;
	
	private List<FriendItem> list;
	private GroupFriendListAdapter adapter;
    
	private ConnectionDetector connectionDetector;
	
	private String name, path;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.friends_list);
		
		getActionBar().setTitle("Choose friends to add");
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
        Bundle bundle = getIntent().getExtras();
        name = bundle.getString(Constant.NAME);
        path = bundle.getString(Constant.IMAGEPATH);
        
		listView = (ListView) findViewById(R.id.listview);
		actionBarLayout = (RelativeLayout) findViewById(R.id.actionbar);
		actionBarLayout.setVisibility(View.GONE);
		
		list = new ArrayList<FriendItem>();
		adapter = new GroupFriendListAdapter(context, list);
		
		listView.setAdapter(adapter);
		
		String filename = getResources().getString(R.string.groupfriendlist_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.ID, Sessions.getUserId(context));
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
	        	onNext();
	        return true;
	        
			default:
				return super.onOptionsItemSelected(item);
			}
	}
	
	private void onNext() {
		
		boolean check = false;
		Validate validate = new Validate();
		
		JSONArray myarray=new JSONArray();
	    for(int i=0;i<list.size();i++)
	    {
	    	if(list.get(i).isChecked())
			{		    	
			    JSONObject myo=new JSONObject();
			    try {
				    myo.put(Constant.USER_ID, list.get(i).getId());
				    myarray.put(myo);
				    check=true;
				    
			    } catch (JSONException e)
			    {
			    	e.printStackTrace();
			    }
			}
	    }
	    
		if(check)
		{	
			String users = myarray.toString();
			Log.e(TAG, "users : "+users);
			/*
			if()
			{
				
			}
			else
				*/
			{
				String filename = getResources().getString(R.string.creategroup_php);
				AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
				
				Map<String, String> map = new HashMap<String, String>();
				map.put(Constant.NAME, name);
				map.put(Constant.USER_ID, Sessions.getUserId(context));
				map.put("users", users);
				asyncLoadVolley.setBasicNameValuePair(map);
				asyncLoadVolley.setOnAsyncTaskListener(saveAsyncTaskListener);
				asyncLoadVolley.beginTask();
			}
		}
		else
		{
			showToast("Choose atleast one friend.");
		}	
	}
	
	OnAsyncTaskListener saveAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			setProgressBarIndeterminateVisibility(false);
			
			AsyncResponse asyncResponse = new AsyncResponse(message);
			if(asyncResponse.ifSuccess())
			{
				ArrayList<GroupListItem> list = asyncResponse.getGroupIdList();
				String groupId = list.get(0).getId();
				String name = "group"+groupId + new Date().getTime() +".jpg";
				if(!path.equals(""))
					onSelectPhoto(name, path, groupId);
				
				Intent intent = new Intent();
				intent.setClass(context, GroupListActivity.class);
				//startActivity(intent);
				
				setResult(Constant.RESULT_CODE_INT);
				finish();
			}
			else
			{
				showToast(""+asyncResponse.getMessage());
			}			
		}
		
		@Override
		public void onTaskBegin() {
			setProgressBarIndeterminateVisibility(true);
		}
	};
	
	private void onSelectPhoto(String name, String path, String groupId) {
		
		Intent intent=new Intent(context, LoadImageService.class);
		intent.putExtra(Constant.NAME, name);
		intent.putExtra(Constant.PATH, path);		
		intent.putExtra(Constant.VALUE, LoadImageService.VALUE_GROUP_PIC);
		intent.putExtra(Constant.GROUP_ID, groupId);
		startService(intent);
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
			list = asyncResponse.getGroupFriendList();
			Log.i(TAG, "resp : "+response);
			adapter.refresh(list);
		}
		else
		{
			Log.e(TAG, "err : "+asyncResponse.getMessage());
			showToast(asyncResponse.getMessage());
		}
	}	
	
    protected JSONArray getresponse()
    {
	    JSONArray myarray=new JSONArray();
	    for(int i=0;i<list.size();i++)
	    {
		    JSONObject myo=new JSONObject();
		    try {
			    myo.put(Constant.USER_ID, list.get(i).getId());
			    myarray.put(myo);
			    
		    } catch (JSONException e)
		    {
		    	e.printStackTrace();
		    }
	    }
	    return myarray;
    }
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
    
}
