package com.chat.imapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.adapters.FriendDetailAdapter;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendDetailItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Base64;
import com.chat.imapp.utility.CommonUtilities;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.ImageCustomize;

@SuppressLint("NewApi")
public class FriendsDetailActivity extends Activity {

protected static final String TAG = "FriendsDetailActivity";
	
	private Context context = FriendsDetailActivity.this;
	
	private ListView listView;
	private EditText edittext;
	
	private AsyncLoadVolley asyncLoadVolley;
	private String filename;
	
	private List<FriendDetailItem> list;
	private FriendDetailAdapter adapter;
	
	private TextView nameTextView, descTextView;
	private ImageView profileImageView;
	private ImageLoader imageLoader;
	
	private String friendId, name, isOnline;
	private int pos = 0;
	
	public static final String TYPE_TEXT = "1";
	public static final String TYPE_IMAGE = "2";
	public static final String TYPE_FILE = "3";
	
	////
	
	private static final int CAMERA_REQUEST = 1;
	private static final int RESULT_LOAD_IMAGE = 2;
	private static final int PICKFILE_RESULT_CODE = 3;
	
	Bitmap theImage;
	String path;
	ImageView proimg;
	
	ConnectionDetector connectionDetector;
	
	////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frienddetail);
		
		Bundle bundle = getIntent().getExtras();
		friendId = bundle.getString(Constant.FRIEND_ID);
		name = bundle.getString(Constant.NAME);
		isOnline = bundle.getString(Constant.ONLINE);
		pos = bundle.getInt(Constant.POSITION);
		data = bundle.getString(Constant.DATA);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
		getActionBar().setTitle(name);
		if(Integer.parseInt(isOnline)==1)
			getActionBar().setSubtitle("Online");
		else
			getActionBar().setSubtitle("Offline");
		
		listView = (ListView) findViewById(R.id.frienddetail_listview);
		edittext = (EditText) findViewById(R.id.frienddetail_edittext);
		
		imageLoader = new ImageLoader(context);
		
		connectionDetector = new ConnectionDetector(context);
		
		list = new ArrayList<FriendDetailItem>();
		adapter = new FriendDetailAdapter(context, list);
		listView.setAdapter(adapter);
		
		filename = getResources().getString(R.string.frienddetail_php);
		asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.USER_ID, Sessions.getUserId(context));
		map.put(Constant.FRIEND_ID, friendId);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
		
		if(savedInstanceState==null)
			asyncLoadVolley.beginTask();
		
		listView.setOnItemClickListener(listItemClickListener);
		
		registerReceiver(mHandleMessageReceiver, new IntentFilter(CommonUtilities.SINGLE_MESSAGE_ACTION));
		
		registerReceiver(mUploadProgressReceiver, new IntentFilter(Constant.IMAGE));
	}
	
	//////////////////
	
	///////////////
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
			switch (item.getItemId()) {
				
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
			FriendDetailItem item = list.get(position);
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
			list = asyncResponse.getFriendDetail();
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
		String filename = getResources().getString(R.string.message_php);
		AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.USER_ID, Sessions.getUserId(context));
		map.put(Constant.FRIEND_ID, friendId);
		map.put(Constant.MESSAGE, text);
		map.put(Constant.TYPE, type);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(sendAsyncTaskListener);
		asyncLoadVolley.beginTask();
		
		FriendDetailItem item = new FriendDetailItem();
		item.setId(Sessions.getUserId(context));
		item.setUserId(Sessions.getUserId(context));
        item.setType(type);
        item.setTime("");
        item.setMessage(text);
        
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
    			List<FriendDetailItem> listNewMessage = asyncResponse.getFriendDetail();
    			
    			Log.i(TAG, "listNewMessage.get(0).getUserId() : "+listNewMessage.get(0).getUserId());
    			Log.i(TAG, "friendId : "+friendId);
    			
    			if(listNewMessage.get(0).getUserId().equals(friendId))
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
							
							path = image.getAbsolutePath();
							
							File file = new File(path);
							Bitmap bitmap = ImageCustomize.decodeFile(file, 200);
							theImage=bitmap;
							
							SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
							editor.putString(Constant.USER_ID, "pic"+Sessions.getUserId(context) + friendId + ".jpg");
				        	editor.putString(Constant.PATH, path);
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
						theImage=bitmap;
						
						path=picturePath;
						
						onSelectPhoto(bitmap);												
				 	}
			}
			
			case PICKFILE_RESULT_CODE:
			{
				if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK && null != data) {
					Uri selectedFile = data.getData();
					path = selectedFile.getPath();
					
					onSelectFile(path);
					
					/*
					File file = new File(Path);
					String string = getStringFromFile(file);
					String SPLIT_TEXT = "_x_z_x_";
					name = "testfile.txt";
					String filename = file.getName();
					if(filename.contains("\\."))
					{
						String array[] = filename.split("\\.");					
						String extension = array[1];
						String tname = array[0];
						
						name = tname + SPLIT_TEXT + new Date().getTime() + "." + extension;
					}
					else
					{
						name = filename;
					}
					
					Intent intent=new Intent(getApplicationContext(), LoadFileService.class);
					intent.putExtra(Constant.NAME, name);
					intent.putExtra(Constant.PATH, path);
					intent.putExtra(Constant.VALUE, "0");
					startService(intent);			
					*/	
				}
			}
		}
	}
	
	public String getStringFromFile(File file) {
			
		 String text = "";
		 byte[] byte_arr = new byte[(int) file.length()];
		 try {
			 FileInputStream fileInputStream = new FileInputStream(file);
             fileInputStream.read(byte_arr);
             
             for (int i = 0; i < byte_arr.length; i++) {
                 System.out.print((char)byte_arr[i]);
             }
             
             String imageStr = Base64.encodeBytes(byte_arr);
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		 return text;
	}
	
	/*
	public String getStringFromFile(File file){
		
		 String text = "";
		 try {
		        FileInputStream is = new FileInputStream(file);
		        int size = is.available();
		        byte[] buffer = new byte[size];
		        is.read(buffer);
		        is.close();
		        text = new String(buffer);
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		 return text;
	}
	*/
	private void onSelectPhoto(Bitmap bitmap) {
		
		FriendDetailItem item = new FriendDetailItem();
        item.setId("1");
        item.setUserId(Sessions.getUserId(context));
        item.setMessage(path);
        item.setType("2");
        item.setTime(""+new Date().getTime());
        item.setImageType(1);
        item.setBitmap(bitmap);
        list.add(item);
		adapter.refresh(list);
		scrollMyListViewToBottom();
		
		String name = "imapp"+Sessions.getUserId(context) + friendId + new Date().getTime() +".jpg";
		
		Intent intent=new Intent(getApplicationContext(), LoadImageService.class);
		intent.putExtra(Constant.NAME, name);
		intent.putExtra(Constant.PATH, path);
		intent.putExtra(Constant.MESSAGE, name);
		intent.putExtra(Constant.TYPE, "2");
		intent.putExtra(Constant.FRIEND_ID, friendId);
		intent.putExtra(Constant.VALUE, "1");
		intent.putExtra(Constant.DATA, "1");
		intent.putExtra(Constant.POSITION, list.size()-1);
		startService(intent);
	}
	
	
	/**
	 * 
	 * 
	 * @param view
	 */
	
	private void onSelectFile(String path) {
		
		File file = new File(path);
		String string = getStringFromFile(file);
		String SPLIT_TEXT = "_x_z_x_";
		name = "testfile.txt";
		String filename = file.getName();
		Log.e(TAG, "filename : "+filename);
		if(filename.contains("."))
		{
			String array[] = filename.split("\\.");
			String extension = array[1];
			String tname = array[0];
			
			name = tname + SPLIT_TEXT + new Date().getTime() + "." + extension;
		}
		else
		{
			name = filename;
		}
		
		FriendDetailItem item = new FriendDetailItem();
        item.setId("1");
        item.setUserId(Sessions.getUserId(context));
        item.setMessage("File has been sent : "+name);
        item.setType("3");
        item.setTime(""+new Date().getTime());
        list.add(item);
		adapter.refresh(list);
		scrollMyListViewToBottom();
		
		Intent intent=new Intent(getApplicationContext(), LoadFileService.class);
		intent.putExtra(Constant.NAME, name);
		intent.putExtra(Constant.PATH, path);
		intent.putExtra(Constant.VALUE, "1");
		
		intent.putExtra(Constant.MESSAGE, name);
		intent.putExtra(Constant.TYPE, "3");
		intent.putExtra(Constant.FRIEND_ID, friendId);
		intent.putExtra(Constant.DATA, "1");
		intent.putExtra(Constant.POSITION, list.size()-1);
		
		startService(intent);	
		
	}
    
    public void onUploadClick(View view) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Choose a Image/File");
		builder.setItems(new CharSequence[] {"Gallery", "Camera", "Choose File"}, 
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
		            
		        case 2:
		        	//callFile();
		        	openFile("file/*");
		        	//openFile("*/*");
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
	
	public void openFile(String minmeType) {
		
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
         // if you want any file type, you can skip next line 
        sIntent.putExtra("CONTENT_TYPE", minmeType); 
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);
        
        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        }
        else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, PICKFILE_RESULT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
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
