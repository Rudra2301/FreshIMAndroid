package com.chat.imapp;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.adapters.FriendDetailAdapter;
import com.chat.imapp.adapters.GroupDetailAdapter;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendDetailItem;
import com.chat.imapp.items.GroupDetailItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.CommonUtilities;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.ImageCustomize;

@SuppressLint("NewApi")
public class GroupMessageDetailActivity extends Activity {

protected static final String TAG = "GroupDetailActivity";
	
	private Context context = GroupMessageDetailActivity.this;
	
	private ListView listView;
	private EditText edittext;
	
	private AsyncLoadVolley asyncLoadVolley;
	private String filename;
	
	private List<GroupDetailItem> list;
	private GroupDetailAdapter adapter;
	
	private TextView nameTextView, descTextView;
	private ImageView profileImageView;
	private ImageLoader imageLoader;
	
	private String groupId, name, image, adminId;
	private int pos = 0;
	
	////
	
	private static final int CAMERA_REQUEST = 1;
	private static final int RESULT_LOAD_IMAGE = 2;
	String Path;
	ImageView proimg;
	
	////
	
	ConnectionDetector connectionDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frienddetail);
		
		Bundle bundle = getIntent().getExtras();
		groupId = bundle.getString(Constant.GROUP_ID);
		name = bundle.getString(Constant.NAME);
		pos = bundle.getInt(Constant.POSITION);
		data = bundle.getString(Constant.DATA);
		image = bundle.getString(Constant.IMAGE);
		adminId = bundle.getString(Constant.ADMIN_ID);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
		getActionBar().setTitle(name);
		
		listView = (ListView) findViewById(R.id.frienddetail_listview);
		edittext = (EditText) findViewById(R.id.frienddetail_edittext);
		
		imageLoader = new ImageLoader(context);
		
		connectionDetector = new ConnectionDetector(context);
		
		list = new ArrayList<GroupDetailItem>();
		adapter = new GroupDetailAdapter(context, list);
		listView.setAdapter(adapter);
		
		filename = getResources().getString(R.string.groupdetail_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.USER_ID, Sessions.getUserId(context));
		map.put(Constant.GROUP_ID, groupId);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
		
		if(savedInstanceState==null)
			asyncLoadVolley.beginTask();
		
		listView.setOnItemClickListener(listItemClickListener);
		
		registerReceiver(mHandleMessageReceiver, new IntentFilter(CommonUtilities.GROUP_MESSAGE_ACTION));		
		registerReceiver(mUploadProgressReceiver, new IntentFilter(Constant.IMAGE));
	}
	
	//////////////////
	
	///////////////
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.group_message, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Intent intent = new Intent();
		
			switch (item.getItemId()) {
			
			case R.id.group_message:
				intent.setClass(context, GroupDetailActivity.class);
				intent.putExtra(Constant.NAME, name);
	    		intent.putExtra(Constant.IMAGE, Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + image);
	    		intent.putExtra(Constant.GROUP_ID, groupId);
	    		intent.putExtra(Constant.ADMIN_ID, adminId);
				startActivity(intent);
	        return true;
				
			case android.R.id.home:
				sendResponseToBackActivity();
	        	finish();
	        return true;
			default:
				return super.onOptionsItemSelected(item);
			}
	}
	
	private String response = "";
	OnAsyncTaskListener asyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			response = message;
			setFriendList(message);
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
	
	OnItemClickListener listItemClickListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			Log.e(TAG, "type : "+list.get(position).getType());
			GroupDetailItem item = list.get(position);
			if(item.getType().equals(String.valueOf(FriendDetailAdapter.TYPE_IMAGE))) {
				if(item.getImageType()==0) {
					Intent intent = new Intent();
					intent.setClass(context, UserDetailActivity.class);
					intent.putExtra(Constant.IMAGE, list.get(position).getMessage());
		        	startActivity(intent);	
				}
			}
		}
	};
	
	private void setFriendList(String response) {
		
		AsyncResponse asyncResponse = new AsyncResponse(response);
		if(asyncResponse.ifSuccess())
		{
			list = asyncResponse.getGroupDetail();
			Log.i(TAG, "resp : "+response);
			adapter.refresh(list);
			scrollMyListViewToBottom();
		}
		else
		{
			Log.e(TAG, "err : "+asyncResponse.getMessage());
		}
	}
	
	public void onSendClick(View view) {
		
		if(!edittext.getText().toString().equals("")) {
			
			String text = edittext.getText().toString();
						
			if (connectionDetector.isConnectedToInternet()) {
				send(text, "1");
				edittext.setText("");
			}
			else
			{
				showToast("Not connected to the internet.");
			}
		}	
	}
	
	private void scrollMyListViewToBottom() {
		listView.post(new Runnable() {
	        @Override
	        public void run() {
	            listView.setSelection(adapter.getCount() - 1);
	        }
	    }); 
	}
	
	private void send(String text, String type) {
		String filename = getResources().getString(R.string.message_group_php);
		AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.USER_ID, Sessions.getUserId(context));
		map.put(Constant.FRIEND_ID, groupId);
		map.put(Constant.MESSAGE, text);
		map.put(Constant.TYPE, type);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(sendAsyncTaskListener);
		asyncLoadVolley.beginTask();
		
		GroupDetailItem item = new GroupDetailItem();
		item.setId(Sessions.getUserId(context));
		item.setUserId(Sessions.getUserId(context));
		item.setUserName("You");
        item.setType(type);
        item.setMessage(text);
        item.setTime("Just Now");
        
		list.add(item);
		adapter.refresh(list);
		scrollMyListViewToBottom();
		
		data = text;
	}
	
	OnAsyncTaskListener sendAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			
			showToast("Message Sent");
		}
		
		@Override
		public void onTaskBegin() {
			
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
    			List<GroupDetailItem> listNewMessage = asyncResponse.getGroupDetail();
    			
    			Log.e(TAG, "id : "+listNewMessage.get(0).getId());
    			Log.e(TAG, "id 2 : "+listNewMessage.get(0).getUserId());
    			
    			if(listNewMessage.get(0).getGroupId().equals(groupId))
    			{
    				list.addAll(listNewMessage);
	    			Log.i(TAG, "resp : "+newMessage);
	    			adapter.refresh(list);
	    			scrollMyListViewToBottom();
    			}
    		}
    		else
    		{
    			Log.e(TAG, "err : "+asyncResponse.getMessage());
    		}
        }
    };	
    
    private final BroadcastReceiver mUploadProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	boolean status = intent.getExtras().getBoolean(Constant.STATUS);
        	String message = intent.getExtras().getString(Constant.IMAGE);
        	int position = intent.getExtras().getInt(Constant.POSITION);
        	if(status)
        	{   
        		showToast("Upload Complete");
        		
        		list.get(position).setMessage(message);
        		list.get(position).setImageType(0);
                
        		adapter.refresh(list);
        		scrollMyListViewToBottom();
         	}        	
        }
    };	
    
    //////// upload
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK)
			return;
		
		switch (requestCode) {
		case CAMERA_REQUEST:
			
			if (resultCode == Activity.RESULT_OK)
			{
				{
							File imagesFolder = new File(Environment.getExternalStorageDirectory(), "myfolder");
							File image = new File(imagesFolder, "image_002.jpg");
							
							Path = image.getAbsolutePath();
							
							File file = new File(Path);
							Bitmap bitmap = ImageCustomize.decodeFile(file, 200);
							
							SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
							editor.putString(Constant.USER_ID, "pic"+Sessions.getUserId(context) + groupId + ".jpg");
				        	editor.putString(Constant.PATH, Path);
				        	editor.putString(Constant.NAME, Sessions.getUserId(context));
					        editor.commit();
							
							onSelectPhoto(bitmap);
				}				
			}
			break;
			
		case RESULT_LOAD_IMAGE:
			{
				 if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			            Uri selectedImage = data.getData();
			            String[] filePathColumn = { MediaStore.Images.Media.DATA };
			            Cursor cursor = getContentResolver().query(selectedImage,
			                    filePathColumn, null, null, null);
			            cursor.moveToFirst();
			            
			            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			            String picturePath = cursor.getString(columnIndex);
			            cursor.close();
			            
			            File file = new File(picturePath);
						Bitmap bitmap = ImageCustomize.decodeFile(file, 200);
						
						Path=picturePath;
						
						onSelectPhoto(bitmap);
						
				 	}
			}
		}
	}
	
	private void onSelectPhoto(Bitmap bitmap) {
		
		GroupDetailItem item = new GroupDetailItem();
        item.setId("1");
        item.setUserId(Sessions.getUserId(context));
		item.setUserName("You");
        item.setMessage(Path);
        item.setType("2");
        item.setTime("Just Now");
        item.setImageType(1);
        item.setBitmap(bitmap);
        list.add(item);
		adapter.refresh(list);
		scrollMyListViewToBottom();
		
		String name = "imapp"+Sessions.getUserId(context) + groupId + new Date().getTime() +".jpg";
		
		Intent intent=new Intent(getApplicationContext(), LoadImageService.class);
		intent.putExtra(Constant.NAME, name);
		intent.putExtra(Constant.PATH, Path);
		intent.putExtra(Constant.MESSAGE, name);
		intent.putExtra(Constant.TYPE, "2");
		intent.putExtra(Constant.FRIEND_ID, groupId);
		intent.putExtra(Constant.VALUE, "1");
		intent.putExtra(Constant.DATA, "2");
		intent.putExtra(Constant.POSITION, list.size()-1);
		startService(intent);
	}
    
    public void onUploadClick(View view) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Choose Image Source");
		builder.setItems(new CharSequence[] {"Gallery", "Camera"}, 
		        new DialogInterface.OnClickListener() {

		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which) {
		        case 0:
		        	callgalery();
		            break;

		        case 1:
		        	callCamera();
		            break;

		        default:
		            break;
		        }
		    }
		});
		
		builder.show();
	}
    
    public void callCamera() {
		
		File imagesFolder = new File(Environment.getExternalStorageDirectory(), "myfolder");
		if (!imagesFolder.exists()) {
			imagesFolder.mkdirs();
			}
		File image = new File(imagesFolder, "image_002.jpg");
				
		Uri uriSavedImage = Uri.fromFile(image);
		
		 Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
		 i.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage); 
	        startActivityForResult(i, CAMERA_REQUEST);

	}
	
	protected void callgalery()
	{
		Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
	}
    
    String data = "";
    @Override
    public void onBackPressed() {
    	
    	sendResponseToBackActivity();
    	
    	super.onBackPressed();
    		
    }
    
    private void sendResponseToBackActivity() {
    	
    	boolean textEmpty = true, imageEmpty = true;
    	
    	for (int i = list.size()-1; i >= 0; --i) {
    		
    		Log.v(TAG, "type : "+list.get(i).getType());
    		
    		if(list.get(i).getType().equals(String.valueOf(FriendDetailAdapter.TYPE_TEXT))) {
    			data = list.get(i).getMessage();
    			textEmpty = false;
    			break;
    		}   
    		if(list.get(i).getType().equals(String.valueOf(FriendDetailAdapter.TYPE_IMAGE)) && imageEmpty) {
    			data = "Image";
    			imageEmpty = false;
    		}
		}
    	
    	Log.i(TAG, "data : "+data);
    	
    	if(textEmpty && imageEmpty) {
    		data = "";
    	}
    	Log.v(TAG, "data : "+data);
    	
    	Intent output = new Intent();
		output.putExtra(Constant.DATA, data);
		output.putExtra(Constant.POSITION, pos);
		setResult(RESULT_OK, output);
	}
    
    protected void onDestroy() {
    	super.onDestroy();
    	try {
            unregisterReceiver(mHandleMessageReceiver);
            unregisterReceiver(mUploadProgressReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
    };
    
    protected void onResume() {
    	super.onResume();
    };
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}		
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
}
