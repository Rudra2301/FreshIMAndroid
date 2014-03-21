package com.chat.imapp.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chat.imapp.R;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.utility.Constant;

public class FriendAdapter extends BaseAdapter {
	
	private static final String TAG = "FriendAdapter";
	private LayoutInflater inflater;
	private ViewHolder holder;
	private List<FriendItem> list;
	private Context context;
	private ImageLoader imageLoader;
	
	public static final int TYPE_TIP = 1;
	public static final int TYPE_CHECKIN = 2; 
	
	private String response = "";
	
	boolean isNew = false;
	
	private Animation animation;
	
	public FriendAdapter(Context context, List<FriendItem> list) {
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
	            holder.onlineImageView 			= (ImageView) hView.findViewById(R.id.friend_onlineimage);
               
	        hView.startAnimation(animation);    
	            
     		hView.setTag(holder);
        }
     	else {
     		holder = (ViewHolder) hView.getTag();
     	}
     	
	    try {
	    	
	    	
	    	
	    		hView.setBackgroundResource(R.drawable.selector_list);
	    		if(item.isNew())
	    			hView.setBackgroundResource(R.drawable.selector_list_gray);
	    		
	    		holder.nameTextView.setText(""+item.getName());
	    		holder.statusTextView.setText("");
	    		if(item.getStatus()!=null) {
	    			if(item.getStatus().equals("") || item.getStatus().equals("null"))
	    			{}
	    			else
	    			{
	    				holder.statusTextView.setText(""+item.getStatus());
	    			}
	    		}
		   		String path = "http://www.gbggoa.org/testproject/four/images/pic.jpg";
		   		path = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getImage();
		   		imageLoader.displayImage(path, holder.userImageView, true, 70);
		   			
		   		holder.onlineImageView.setVisibility(View.VISIBLE);
		   		
		   		if(Integer.parseInt(item.getIsOnline())==1) {
		   			holder.onlineImageView.setImageResource(R.drawable.online);
		   		}
		   		else {
		   			holder.onlineImageView.setImageResource(R.drawable.offline);
		   		}
		   				   		
		   		/////////////
		   		
    	} catch (Exception e) {
          	e.printStackTrace();
    	}
        
      	return hView;
	}	
	
	class ViewHolder
	{	
		TextView nameTextView, statusTextView;
        ImageView userImageView, onlineImageView;
	}
	
}   