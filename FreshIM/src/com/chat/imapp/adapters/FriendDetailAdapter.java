package com.chat.imapp.adapters;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chat.imapp.DownLoadFileService;
import com.chat.imapp.R;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.items.FriendDetailItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.Constant;

public class FriendDetailAdapter extends BaseAdapter {
	
	private static final String TAG = "UserAdapter";
	private LayoutInflater inflater;
	private ViewHolder holder;
	private List<FriendDetailItem> list;
	private Context context;
	private ImageLoader imageLoader;
	
	public static final int TYPE_TEXT = 1;
	public static final int TYPE_IMAGE = 2; 
	public static final int TYPE_FILE = 3; 
	
	public static final String LIKE = "1";
	public static final String SAVE = "2"; 
	
	private String response = "";
	
	private int saveValue, likeValue;
	
	private String friendImage;
	
	public FriendDetailAdapter(Context context, List<FriendDetailItem> list) {
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
	
	public void refresh(List<FriendDetailItem> list) {
		this.list = list;
		notifyDataSetChanged();
	}
	
	public List<FriendDetailItem> getList() {
		return list;
	}
	
	public void setFriendImage(String image) {
		this.friendImage = image;
	}
	
	View hView;
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
        
    	hView = convertView;
    	
    	final FriendDetailItem item = list.get(position);
    	
    	final int type = Integer.parseInt(item.getType());
    	
    	final String userId = item.getUserId();
    	
    	Log.w(TAG, "pos : "+position);
    	Log.e(TAG, "userId : "+userId);
    	Log.i(TAG, " session ID : "+Sessions.getUserId(context));
    	
    	int Id = Integer.parseInt(userId);
    	
     	if (convertView == null) {
     			
     			holder = new ViewHolder();
     			
     			hView = inflater.inflate(R.layout.frienddetailtextitemnew, null);
 				holder.messageTextView		= (TextView) hView.findViewById(R.id.frienddetail1_message_text);
	            holder.timeTextView			= (TextView) hView.findViewById(R.id.frienddetail1_time_text);
	            holder.dataImageView		= (ImageView) hView.findViewById(R.id.frienddetail1_message_image);
	            //holder.userImageView		= (ImageView) hView.findViewById(R.id.frienddetail2_user_image);
	            holder.textLayout			= (RelativeLayout) hView.findViewById(R.id.frienddetail1_text_layout);
	            holder.imageLayout			= (RelativeLayout) hView.findViewById(R.id.frienddetail1_image_layout);  
	            holder.userBubble			= (RelativeLayout) hView.findViewById(R.id.userBubble);  
	            holder.progressBar			= (ProgressBar) hView.findViewById(R.id.progressLoader);
	            //file
	            holder.filenameTextView		= (TextView) hView.findViewById(R.id.message_file_textview);
	            holder.fileLayout			= (RelativeLayout) hView.findViewById(R.id.message_file_layout);
     			
	            holder.messageTextViewf		= (TextView) hView.findViewById(R.id.frienddetail_message_text_f);
	            holder.timeTextViewf			= (TextView) hView.findViewById(R.id.frienddetail_time_text_f);
	            holder.dataImageViewf		= (ImageView) hView.findViewById(R.id.frienddetail_message_image_f);
	            //holder.userImageViewf		= (ImageView) hView.findViewById(R.id.frienddetail2_user_image);0
	            holder.textLayoutf			= (RelativeLayout) hView.findViewById(R.id.frienddetail_text_layout_f);
	            holder.imageLayoutf			= (RelativeLayout) hView.findViewById(R.id.frienddetail_image_layout_f);
	            holder.friendBubble			= (RelativeLayout) hView.findViewById(R.id.friendBubble);
	            //file
	            holder.filenameTextViewf	= (TextView) hView.findViewById(R.id.message_file_textview_f);
	            holder.fileLayoutf			= (RelativeLayout) hView.findViewById(R.id.message_file_layout_f);
	            
	            holder.fileDownloadButton	= (Button) hView.findViewById(R.id.message_file_download_button_f);
	            
     			hView.setTag(holder);            	
        }
     	else {
     		holder = (ViewHolder) hView.getTag();
     	}
     	
     	// Show User Bubble
     	if(Sessions.getUserId(context).equals(userId)) 
     	{     		
     		holder.friendBubble.setVisibility(View.GONE);
     		holder.userBubble.setVisibility(View.VISIBLE);
     		
     		try {
    	    	
    	    	String url = "";
        		url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getUserImage();
    	    	
    	    	if(Integer.parseInt(item.getType())==FriendDetailAdapter.TYPE_TEXT) {
    	    		
    	    		holder.textLayout.setVisibility(View.VISIBLE);
    	    		holder.imageLayout.setVisibility(View.GONE);
    	    		holder.fileLayout.setVisibility(View.GONE);
    	    		
    	    		holder.messageTextView.setText(""+item.getMessage());
    	    		holder.timeTextView.setText("");
    	    		
    	    		hView.setClickable(false);
    	    	}
    	    	else if(Integer.parseInt(item.getType())==FriendDetailAdapter.TYPE_IMAGE) {
    	    		
    	    		holder.textLayout.setVisibility(View.GONE);
    	    		holder.imageLayout.setVisibility(View.VISIBLE);
    	    		holder.fileLayout.setVisibility(View.GONE);
    	    		
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
    	    	else if(Integer.parseInt(item.getType())==FriendDetailAdapter.TYPE_FILE) {
    	    		
    	    		holder.textLayout.setVisibility(View.GONE);
    	    		holder.imageLayout.setVisibility(View.GONE);
    	    		holder.fileLayout.setVisibility(View.VISIBLE);
    	    		
    	    		holder.filenameTextView.setText(""+item.getMessage());
    	    	}
        	} catch (Exception e) {
              	e.printStackTrace();
        	}
     	}
     	
     	else  // Show Friend Bubble
     	{     		
     		holder.friendBubble.setVisibility(View.VISIBLE);
     		holder.userBubble.setVisibility(View.GONE);
     		
		    try {		    	
		    	String url = "";
	    		url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getUserImage();
		    	
		    	if(Integer.parseInt(item.getType())==FriendDetailAdapter.TYPE_TEXT) {
		    		
		    		holder.textLayoutf.setVisibility(View.VISIBLE);
    	    		holder.imageLayoutf.setVisibility(View.GONE);
    	    		holder.fileLayoutf.setVisibility(View.GONE);
		    		
		    		holder.messageTextViewf.setText(""+item.getMessage());
		    		holder.timeTextViewf.setText("");
		    		
		    		hView.setClickable(false);
		    	}
		    	else if(Integer.parseInt(item.getType())==FriendDetailAdapter.TYPE_IMAGE) {
		    		
		    		holder.textLayoutf.setVisibility(View.GONE);
    	    		holder.imageLayoutf.setVisibility(View.VISIBLE);
    	    		holder.fileLayoutf.setVisibility(View.GONE);
    	    		
		    		holder.timeTextViewf.setText("");
		    		url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getMessage();
		    		if(item.getImageType()==1) {
		    			 holder.dataImageViewf.setImageBitmap(item.getBitmap());
		    		}
		    		else
		    		{
		    			imageLoader.displayImage(url, holder.dataImageViewf, false, null);
		    		}
		    	}
		    	else if(Integer.parseInt(item.getType())==FriendDetailAdapter.TYPE_FILE) {
    	    		
		    		holder.textLayoutf.setVisibility(View.GONE);
    	    		holder.imageLayoutf.setVisibility(View.GONE);
    	    		holder.fileLayoutf.setVisibility(View.VISIBLE);
    	    		
    	    		holder.filenameTextViewf.setText(""+item.getMessage());
    	    		
    	    		File f = new File(Environment.getExternalStorageDirectory(),  "Files");
    	    		if (!f.exists()) {
    					f.mkdir();
    				}
    	    		File file = new File(f, item.getMessage());
    	    		if(file.exists()) {
    	    			holder.fileDownloadButton.setVisibility(View.GONE);
    	    		}
    	    		else
    	    		{
    	    			holder.fileDownloadButton.setVisibility(View.VISIBLE);
    	    		}
    	    		
    	    		holder.fileDownloadButton.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							
							Log.e(TAG, "Clicked");
							
							Intent intent = new Intent(context.getApplicationContext(), DownLoadFileService.class);
							String url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + item.getMessage();
							intent.putExtra(Constant.NAME, item.getMessage());
							intent.putExtra(Constant.URL, url);
							intent.putExtra(Constant.PATH, "files");
							intent.putExtra(Constant.VALUE, "1");
							context.startService(intent);
						}
					});
    	    	}
	    	} catch (Exception e) {
	          	e.printStackTrace();
	    	}
     	}
      	return hView;
	}	
	
	class ViewHolder
		{	

			RelativeLayout userBubble, friendBubble;
		
			RelativeLayout textLayout, imageLayout, fileLayout;
			
		    TextView messageTextView, timeTextView;
		    ImageView dataImageView, userImageView;
		    
		    ProgressBar progressBar;
		    //file
		    TextView filenameTextView;
		    
		    RelativeLayout textLayoutf, imageLayoutf, fileLayoutf;
		    TextView messageTextViewf, timeTextViewf;
		    ImageView dataImageViewf, userImageViewf;
		    //file
		    TextView filenameTextViewf;
		    
		    Button fileDownloadButton;
		}
	
}   