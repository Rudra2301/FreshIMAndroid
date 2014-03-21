package com.chat.imapp.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.R;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;

public class GroupEditMemberListAdapter extends BaseAdapter implements OnClickListener {
	
	private static final String TAG = "GroupEditMemberListAdapter";
	
	private LayoutInflater inflater;
	private ViewHolder holder;
	private List<FriendItem> list;
	private Context context;
	private ImageLoader imageLoader;
	
	private String groupId;
	private String adminId;
	
	public static final String DELETE_MEMBER = "2";
	public static final String ADD_MEMBER = "1";
	
	public GroupEditMemberListAdapter(Context context, List<FriendItem> list, String groupId, String adminId) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.list = list;
		imageLoader = new ImageLoader(context);
		this.groupId = groupId;
		this.adminId = adminId;
	}   
	
	@Override
	public int getCount() {
		return list.size();
	}
	
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return list.indexOf(getItem(position));
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
            holder.statusTextView		= (TextView) hView.findViewById(R.id.friend_count_text);
            holder.userImageView 		= (ImageView) hView.findViewById(R.id.friend_image);
	        holder.checkBox 			= (CheckBox) hView.findViewById(R.id.chekbox);
            
     		hView.setTag(holder);
        }
     	else {
     		holder = (ViewHolder) hView.getTag();
     	}
     	
	    try {
	    		holder.nameTextView.setText(""+item.getName());
	    		holder.statusTextView.setText(""+item.getStatus());
	    		
		   		String path = "http://www.gbggoa.org/testproject/four/images/pic.jpg";
		   		path = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getImage();
		   		imageLoader.displayImage(path, holder.userImageView, true, 70);
		   		
		   		if(item.getId().equals(adminId))
		   		{
			   		holder.checkBox.setVisibility(View.GONE);
		   		}
		   		else
		   		{
		   			holder.checkBox.setVisibility(View.VISIBLE);
		   		}
		   		
		   		if(item.isChecked())
		   		{
		   			holder.checkBox.setChecked(true);
		   			
		   			if(item.getId().equals(adminId))
			   		{
			   			hView.setBackgroundResource(R.drawable.selector_list_gray);
			   		}
			   		else
			   		{
			   			hView.setBackgroundResource(R.drawable.selector_list_green);
			   		}
		   		}
		   		else
		   		{
		   			holder.checkBox.setChecked(false);
		    		hView.setBackgroundResource(R.drawable.selector_list);
		   		}	
		   		
		   		holder.checkBox.setTag(position);
		   		holder.checkBox.setOnClickListener(this);
		   				   		
    	} catch (Exception e) {
          	e.printStackTrace();
    	}
        
      	return hView;
	}	
	
	class ViewHolder
	{	
		TextView nameTextView, statusTextView;
        ImageView userImageView;
        CheckBox checkBox;
	}
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}   
    
	@Override
	public void onClick(View view) {
		
		final int position = (Integer) view.getTag();
		
		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.chekbox);
		final boolean isChecked = checkBox.isChecked();
		Log.e(TAG, "b4 isChecked : "+isChecked);
		
		//list.get(position).setChecked(!isChecked);
		//notifyDataSetChanged();
		
		checkBox.setVisibility(View.GONE);
		
		String filename = context.getResources().getString(R.string.groupmemberedit_php);
        AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.GROUP_ID, groupId);
		String userId = list.get(position).getId();
		map.put(Constant.USER_ID, userId);
		
		if(isChecked)
		{
			map.put(Constant.TYPE, ADD_MEMBER); // INSERT
			Log.e(TAG, "ADD_MEMBER");
		}
		else
		{
			map.put(Constant.TYPE, DELETE_MEMBER); // DELETE
			Log.e(TAG, "DELETE_MEMBER");
		}
		
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(new OnAsyncTaskListener() {
			
			@Override
			public void onTaskComplete(boolean isComplete, String message) {
				Log.e(TAG, "mess : "+message);
				
				AsyncResponse asyncResponse = new AsyncResponse(message);
				if(asyncResponse.ifSuccess())
				{	
					if (isChecked) {
						showToast("Member is ADDED to the group.");
					}
					else
					{
						showToast("Member is REMOVED from the group.");
					}
					
					checkBox.setVisibility(View.VISIBLE);
					list.get(position).setChecked(isChecked);
					notifyDataSetChanged();
					
				}
				else
				{
					Log.e(TAG, "err : "+asyncResponse.getMessage());
					showToast(asyncResponse.getMessage());
				}
				
			}
			
			@Override
			public void onTaskBegin() {
				
			}
		});
		
		ConnectionDetector connectionDetector = new ConnectionDetector(context);
		if(connectionDetector.isConnectedToInternet())
			asyncLoadVolley.beginTask();
		else
			showToast("Not Connected to Internet");
		
	}
	
	
}   