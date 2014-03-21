package com.chat.imapp.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.R;
import com.chat.imapp.ResponseRequestService;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Constant;

public class FriendAllAdapter extends BaseAdapter {
	
	private static final String TAG = "FriendAdapter";
	private LayoutInflater inflater;
	private ViewHolder holder;
	private List<FriendItem> list;
	private Context context;
	private ImageLoader imageLoader;
	
	public static final String _TYPE_NOT_FRIEND = "0";
	public static final String _TYPE_FRIEND = "2"; 
	public static final String _TYPE_NEW_FRIEND = "1";
	
	public static final String TYPE_NEW_REQUEST = "0";
	public static final String TYPE_ACCEPT_REQUEST = "1";
	public static final String TYPE_CANCEL_REQUEST = "2";
	public static final String TYPE_REJECT_REQUEST = "3";	
	public static final String TYPE_UNFRIEND = "4";
	
	private String response = "";
	
	boolean isNew = false;
	
	private Animation animation;
	
	public FriendAllAdapter(Context context, List<FriendItem> list) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.list = list;
		imageLoader = new ImageLoader(context);
		animation = AnimationUtils.loadAnimation(context, R.anim.fade_list_item);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}
	
	@Override
	public Object getItem(int position) {
		return null;
	}
	
	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	public void refresh(List<FriendItem> list) {
		this.list = list;
		notifyDataSetChanged();
	}
	
	public List<FriendItem> getList() {
		return list;
	}
	
	View hView;
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
        
    	hView = convertView;
    	
    	final FriendItem item = list.get(position);
    	
     	if (convertView == null) {
     		
            holder = new ViewHolder();
            	
            	hView = inflater.inflate(R.layout.friendlistitem, null);
            	holder.nameTextView			= (TextView) hView.findViewById(R.id.friend_name_text);
	            holder.statusTextView			= (TextView) hView.findViewById(R.id.friend_count_text);
	            holder.userImageView 			= (ImageView) hView.findViewById(R.id.friend_image);
	            holder.addFriendButton 			= (Button) hView.findViewById(R.id.friend_add_button);
	            holder.rejectFriendButton		= (Button) hView.findViewById(R.id.friend_reject_button);
               
	        //hView.startAnimation(animation);
	            
     		hView.setTag(holder);
        }
     	else {
     		holder = (ViewHolder) hView.getTag();
     	}
     	
	    try {	    
	    		// make add friend button visible
	    		holder.addFriendButton.setVisibility(View.VISIBLE);
	    		holder.rejectFriendButton.setVisibility(View.GONE);
	    		
	    		hView.setBackgroundResource(R.drawable.selector_list);
	    		
	    		if(item.getType().equals(_TYPE_NEW_FRIEND)) {
	    			hView.setBackgroundResource(R.drawable.selector_list_gray);
	    			if(item.getAdminId().equals(Sessions.getUserId(context))) {
	    				holder.addFriendButton.setText("Cancel Pending Request"); // if user
	    			}
	    			else {
	    				holder.addFriendButton.setText("Accept"); // if friend
	    				holder.rejectFriendButton.setVisibility(View.VISIBLE);
	    			}
	    		}
	    		else if(item.getType().equals(_TYPE_FRIEND)) {
	    			hView.setBackgroundResource(R.drawable.selector_list_green);
	    			holder.addFriendButton.setText("Unfriend");
	    		}
	    		else {
	    			hView.setBackgroundResource(R.drawable.selector_list);
	    			holder.addFriendButton.setText("Add"); // if user
	    		}
	    		
	    		holder.nameTextView.setText(""+item.getName()+" "+item.getLname());
	    		holder.statusTextView.setText(""+item.getStatus());
		   		String path = "http://www.gbggoa.org/testproject/four/images/pic.jpg";
		   		path = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getImage();
		   		imageLoader.displayImage(path, holder.userImageView, true, 70);
		   		
		   		holder.rejectFriendButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String filename = context.getResources().getString(R.string.friendrequest_php);
						AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
						Map<String, String> map = new HashMap<String, String>();
						map.put(Constant.USER_ID, Sessions.getUserId(context));
						map.put(Constant.FRIEND_ID, item.getId());
						
						final String friendId = item.getId();
						
						map.put(Constant.TYPE, "2");
						asyncLoadVolley.setBasicNameValuePair(map);
						asyncLoadVolley.setOnAsyncTaskListener(new OnAsyncTaskListener() {
							
							@Override
							public void onTaskComplete(boolean isComplete, String message) {
								Log.e(TAG, "mess : "+message);
								
								holder.addFriendButton.setClickable(true);
								holder.rejectFriendButton.setClickable(true);
								
								AsyncResponse asyncResponse = new AsyncResponse(message);
								if(asyncResponse.ifSuccess()) {
									
									List<FriendItem> myList = asyncResponse.getFriendAllListAfterRequest();
									String newType = myList.get(0).getType();
									String newAdminId = myList.get(0).getAdminId();
									String friendRequestId = myList.get(0).getFriendRequestId();
									
									Intent service = new Intent(context, ResponseRequestService.class);
									service.putExtra(Constant.TYPE, TYPE_REJECT_REQUEST);
									
									service.putExtra(Constant.FRIEND_REQUEST_ID, friendRequestId);
									service.putExtra(Constant.FRIEND_ID, friendId);
									service.putExtra(Constant.USER_ID, Sessions.getUserId(context));
									context.startService(service);
									
									item.setType(newType);
									item.setAdminId(newAdminId);
									notifyDataSetChanged();
									
									
								}
								else
								{
									showToast(""+asyncResponse.getMessage());
								}	
							}
							
							@Override
							public void onTaskBegin() {
								holder.addFriendButton.setClickable(false);
								holder.rejectFriendButton.setClickable(false);
								
							}
						});
						asyncLoadVolley.beginTask();
					}
		   		});
		   		
		   		holder.addFriendButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String filename = context.getResources().getString(R.string.friendrequest_php);
						AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
						Map<String, String> map = new HashMap<String, String>();
						map.put(Constant.USER_ID, Sessions.getUserId(context));
						map.put(Constant.FRIEND_ID, item.getId());
						
						final String friendId = item.getId();
						
						if(item.getType().equals(_TYPE_NOT_FRIEND)) {
							map.put(Constant.TYPE, "0");   					// send friend request
						}
						else // if Either request sent or recieved request
						if(item.getType().equals(_TYPE_NEW_FRIEND)) {
							if(item.getAdminId().equals(Sessions.getUserId(context))) { // if request sent
			    				map.put(Constant.TYPE, "2"); // Cancel Pending request
			    			}
			    			else // if request recieved	
			    			{
			    				map.put(Constant.TYPE, "1"); // accept request
			    			}
						}
						else // unfriend
						{
							map.put(Constant.TYPE, "2");
						}
						
						asyncLoadVolley.setBasicNameValuePair(map);
						asyncLoadVolley.setOnAsyncTaskListener(new OnAsyncTaskListener() {
							
							@Override
							public void onTaskComplete(boolean isComplete, String message) {
								Log.e(TAG, "mess : "+message);
								
								holder.addFriendButton.setClickable(true);
								
								AsyncResponse asyncResponse = new AsyncResponse(message);
								if(asyncResponse.ifSuccess()) {
									
									List<FriendItem> myList = asyncResponse.getFriendAllListAfterRequest();
									String newType = myList.get(0).getType();
									String newAdminId = myList.get(0).getAdminId();
									String friendRequestId = myList.get(0).getFriendRequestId();
									
									Intent service = new Intent(context, ResponseRequestService.class);
									
									if(item.getType().equals(_TYPE_NEW_FRIEND)) {
						    			if(item.getAdminId().equals(Sessions.getUserId(context))) {
											service.putExtra(Constant.TYPE, TYPE_CANCEL_REQUEST);
						    			}
						    			else {
											service.putExtra(Constant.TYPE, TYPE_ACCEPT_REQUEST);
						    			}
						    		}
						    		else if(item.getType().equals(_TYPE_FRIEND)) {
										service.putExtra(Constant.TYPE, TYPE_UNFRIEND);
						    		}
						    		else {
										service.putExtra(Constant.TYPE, TYPE_NEW_REQUEST);
						    		}
									
									item.setType(newType);
									item.setAdminId(newAdminId);
									notifyDataSetChanged();
									
									service.putExtra(Constant.FRIEND_REQUEST_ID, friendRequestId);
									service.putExtra(Constant.FRIEND_ID, friendId);
									service.putExtra(Constant.USER_ID, Sessions.getUserId(context));
									context.startService(service);
									
								}
								else
								{
									showToast(""+asyncResponse.getMessage());
								}	
							}
							
							@Override
							public void onTaskBegin() {
								holder.addFriendButton.setClickable(false);
							}
						});
						asyncLoadVolley.beginTask();
						
					}
				});
		   		
		   		/////////////
		   		
    	} catch (Exception e) {
          	e.printStackTrace();
    	}        
      	return hView;
	}	
	
	class ViewHolder
	{	
		TextView nameTextView, statusTextView;
        ImageView userImageView;
        Button addFriendButton;
        Button rejectFriendButton;
	}
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
}   