package com.chat.imapp.adapters;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chat.imapp.R;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.items.FriendDetailItem;
import com.chat.imapp.items.GroupDetailItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.Constant;

public class GroupDetailAdapter extends BaseAdapter {
	
	private static final String TAG = "UserAdapter";
	private LayoutInflater inflater;
	private ViewHolder holder;
	private List<GroupDetailItem> list;
	private Context context;
	private ImageLoader imageLoader;
	
	public static final int TYPE_TEXT = 1;
	public static final int TYPE_IMAGE_ME = 2; 
	
	public static final String LIKE = "1";
	public static final String SAVE = "2"; 
	
	private String response = "";
	
	private int saveValue, likeValue;
	
	private String friendImage;
	
	public GroupDetailAdapter(Context context, List<GroupDetailItem> list) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.list = list;
		imageLoader = new ImageLoader(context);
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
	
	public void refresh(List<GroupDetailItem> list) {
		this.list = list;
		notifyDataSetChanged();
	}
	
	public List<GroupDetailItem> getList() {
		return list;
	}
	
	public void setFriendImage(String image) {
		this.friendImage = image;
	}
	
	View hView;
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
        
    	hView = convertView;
    	
    	final GroupDetailItem item = list.get(position);
    	
    	final int type = Integer.parseInt(item.getType());
    	
    	final String id = item.getUserId();
    	
    	Log.w(TAG, "pos : "+position);
    	Log.e(TAG, "id : "+id);
    	Log.i(TAG, " session ID : "+Sessions.getUserId(context));
    	
    	int Id = Integer.parseInt(id);
    	
     	if (convertView == null) {
     			
     			holder = new ViewHolder();
     			Log.v(TAG, "id : "+id);
     			
     			hView = inflater.inflate(R.layout.frienddetailtextitemnew, null);
 				holder.messageTextView		= (TextView) hView.findViewById(R.id.frienddetail1_message_text);
	            holder.timeTextView			= (TextView) hView.findViewById(R.id.frienddetail1_time_text);
	            holder.dataImageView		= (ImageView) hView.findViewById(R.id.frienddetail1_message_image);
	            holder.textLayout			= (RelativeLayout) hView.findViewById(R.id.frienddetail1_text_layout);
	            holder.imageLayout			= (RelativeLayout) hView.findViewById(R.id.frienddetail1_image_layout);  
	            holder.userBubble			= (RelativeLayout) hView.findViewById(R.id.userBubble);  
	            holder.progressBar			= (ProgressBar) hView.findViewById(R.id.progressLoader);
	                                                                                                                                                                                                                                                                                                                                                                                   
	            holder.nameLayout			= (RelativeLayout) hView.findViewById(R.id.frienddetail1_name_layout);
 				holder.userNameTextView		= (TextView) hView.findViewById(R.id.frienddetail1_name_text);
     			
	            holder.messageTextViewf		= (TextView) hView.findViewById(R.id.frienddetail_message_text_f);
	            holder.timeTextViewf			= (TextView) hView.findViewById(R.id.frienddetail_time_text_f);
	            holder.dataImageViewf		= (ImageView) hView.findViewById(R.id.frienddetail_message_image_f);
	            holder.textLayoutf			= (RelativeLayout) hView.findViewById(R.id.frienddetail_text_layout_f);
	            holder.imageLayoutf			= (RelativeLayout) hView.findViewById(R.id.frienddetail_image_layout_f);
	            holder.friendBubble			= (RelativeLayout) hView.findViewById(R.id.friendBubble);  
	            
	            holder.nameLayoutf			= (RelativeLayout) hView.findViewById(R.id.frienddetail_name_layout_f);
 				holder.userNameTextViewf	= (TextView) hView.findViewById(R.id.frienddetail_name_text_f);
	            
     			hView.setTag(holder);
        }
     	else {
     		holder = (ViewHolder) hView.getTag();
     	}
     	
     	//user
     	if(Sessions.getUserId(context).equals(id))
     	{   
     		holder.friendBubble.setVisibility(View.GONE);
     		holder.userBubble.setVisibility(View.VISIBLE);
     		
     		try {
    	    	
    	    	String url = "";
        		url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getUserImage();
        		
        		holder.nameLayout.setVisibility(View.VISIBLE);
        		holder.nameLayoutf.setVisibility(View.GONE);
        		holder.userNameTextView.setText("You");
    	    	
    	    	if(Integer.parseInt(item.getType())==GroupDetailAdapter.TYPE_TEXT) {
    	    		
    	    		holder.textLayout.setVisibility(View.VISIBLE);
    	    		holder.imageLayout.setVisibility(View.GONE);
    	    		
    	    		holder.messageTextView.setText(""+item.getMessage());
    	    		holder.timeTextView.setText("");
    	    		
    	    		hView.setClickable(false);
    	    	}
    	    	else if(Integer.parseInt(item.getType())==GroupDetailAdapter.TYPE_IMAGE_ME) {
    	    		
    	    		holder.textLayout.setVisibility(View.GONE);
    	    		holder.imageLayout.setVisibility(View.VISIBLE);
    	    		holder.timeTextView.setText("");
    	    		url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getMessage();
    	    		if(item.getImageType()==1) {
    	    			 holder.dataImageView.setImageBitmap(item.getBitmap());
    	    		}
    	    		else
    	    		{
    	    			if(item.getBitmap()!=null)
    	    				imageLoader.displayImage(url, holder.dataImageView, false, 200, item.getBitmap());
    	    			else
    	    				imageLoader.displayImage(url, holder.dataImageView, false, 200);
    	    		}
    	    	}
        	} catch (Exception e) {
              	e.printStackTrace();
        	}
     	}
     	
     	// Frn
     	else  
     	{
     		
     		holder.friendBubble.setVisibility(View.VISIBLE);
     		holder.userBubble.setVisibility(View.GONE);
     		
		    try {
		    	
		    	String url = "";
	    		url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getUserImage();
	    		
	    		holder.nameLayoutf.setVisibility(View.VISIBLE);
	    		holder.nameLayout.setVisibility(View.GONE);
	    		holder.userNameTextViewf.setText(""+item.getUserName());
		    	
		    	if(Integer.parseInt(item.getType())==GroupDetailAdapter.TYPE_TEXT) {
		    		
		    		holder.textLayoutf.setVisibility(View.VISIBLE);
		    		holder.imageLayoutf.setVisibility(View.GONE);
		    		
		    		holder.messageTextViewf.setText(""+item.getMessage());
		    		holder.timeTextViewf.setText("");
		    		
		    		hView.setClickable(false);
		    	}
		    	else if(Integer.parseInt(item.getType())==GroupDetailAdapter.TYPE_IMAGE_ME) {
		    		
		    		holder.textLayoutf.setVisibility(View.GONE);
		    		holder.imageLayoutf.setVisibility(View.VISIBLE);
		    		holder.timeTextViewf.setText("");
		    		url = "";
		    		url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getMessage();
		    		if(item.getImageType()==1) {
		    			 holder.dataImageViewf.setImageBitmap(item.getBitmap());
		    		}
		    		else
		    		{
		    			imageLoader.displayImage(url, holder.dataImageViewf, false, null);
		    		}
		    	}
	    	} catch (Exception e) {
	          	e.printStackTrace();
	    	}
     	}
      	return hView;
	}	
	
	class ViewHolder
		{	
			RelativeLayout textLayout, imageLayout, fileLayout, nameLayout;
		    TextView messageTextView, timeTextView;
		    ImageView dataImageView, userImageView;
		    RelativeLayout userBubble;
		    ProgressBar progressBar;
		    TextView userNameTextView;
		    
		    RelativeLayout textLayoutf, imageLayoutf, fileLayoutf, nameLayoutf;
		    TextView messageTextViewf, timeTextViewf;
		    ImageView dataImageViewf, userImageViewf;
		    RelativeLayout friendBubble;
		    TextView userNameTextViewf;
		}
	
}   