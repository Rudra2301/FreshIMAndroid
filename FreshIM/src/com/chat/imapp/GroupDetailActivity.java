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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.adapters.GroupFriendListAdapter;
import com.chat.imapp.adapters.GroupFriendListDetailAdapter;
import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.ConnectionDetector;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.ImageCustomize;
import com.chat.imapp.utility.Validate;
import com.google.android.gms.internal.dd;

@SuppressLint("NewApi")
public class GroupDetailActivity extends Activity {

protected static final String TAG = "GroupDetailActivity";
	
	private Context context = GroupDetailActivity.this;
    
	EditText groupName;
    ImageView groupImage;
    
    private String name, image, groupId, adminId;
    
    private String path;
    
    private ImageLoader imageLoader;
    
    private ListView listView;
    private ProgressBar progressBar;
    private ImageView editImageView;
    
    private List<FriendItem> list;
	private GroupFriendListDetailAdapter adapter;
	
	private ConnectionDetector connectionDetector;
	
	private AsyncLoadVolley asyncLoadVolley;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.group_detail);
		
		getActionBar().setTitle("Group Detail");
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        Bundle bundle = getIntent().getExtras();
        name = bundle.getString(Constant.NAME);
        image = bundle.getString(Constant.IMAGE);
        groupId = bundle.getString(Constant.GROUP_ID);
        adminId = bundle.getString(Constant.ADMIN_ID);
        Log.e(TAG, "adminId : "+adminId);
        Log.e(TAG, "my id  : "+Sessions.getUserId(context));
        
        groupName = (EditText) findViewById(R.id.groupName);
        groupImage = (ImageView) findViewById(R.id.groupImage);
        
        listView 		= (ListView) findViewById(R.id.groupfriendlist);
        progressBar 	= (ProgressBar) findViewById(R.id.progressLoader);
        editImageView 	= (ImageView) findViewById(R.id.editgroupmemberimage);
        
        if (adminId.equals(Sessions.getUserId(context))) {
        	editImageView.setVisibility(View.VISIBLE);
        	editImageView.setClickable(true);	
        	
        	groupName.setEnabled(true);
        	groupName.setClickable(true);
        	
        	groupImage.setClickable(true);
		}
        else
        {
        	editImageView.setVisibility(View.GONE);
        	editImageView.setClickable(false);
        	
        	groupName.setEnabled(false);
        	groupName.setClickable(false);
        	
        	groupImage.setClickable(false);
        }
        
        imageLoader = new ImageLoader(context);
        
        groupName.setText(name);
        if(savedInstanceState==null) {
        	imageLoader.displayImage(image, groupImage);
        }
        
        list = new ArrayList<FriendItem>();
        adapter = new GroupFriendListDetailAdapter(context, list);
		listView.setAdapter(adapter);	
	}
	
	@Override
		protected void onResume() {
			super.onResume();
			
			String filename = getResources().getString(R.string.groupfriendlistmember_php);
	        AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
			Map<String, String> map = new HashMap<String, String>();
			map.put(Constant.GROUP_ID, groupId);
			map.put(Constant.TYPE, GroupFriendListDetailAdapter.FRIENDS_IN_GROUP);
			asyncLoadVolley.setBasicNameValuePair(map);
			asyncLoadVolley.setOnAsyncTaskListener(asyncTaskListener);
			
			connectionDetector = new ConnectionDetector(context);
			
			if(connectionDetector.isConnectedToInternet())
				asyncLoadVolley.beginTask();
		}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		if (adminId.equals(Sessions.getUserId(context))) {
			getMenuInflater().inflate(R.menu.group_detail, menu);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
			
			switch (item.getItemId()) {
				
			case android.R.id.home:
	        	finish();
	        return true;
	        
			case R.id.save:
	        	onNext();
	        return true;
	        
			case R.id.delete:
				showAlertDialog(context, "Delete Group "+name,
						"Are you sure, you want to permanently delete the group?");
	        
			default:
				return super.onOptionsItemSelected(item);
			}
	}
	
	public void showAlertDialog(final Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        
        // Setting Dialog Title
        alertDialog.setTitle(title);
        
        // Setting Dialog Message
        alertDialog.setMessage(message);
        
        alertDialog.setIcon(R.drawable.top_logo);
        
        // Setting OK Button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	deleteGroup(context, groupId);
            }
        });
        
        // Showing Alert Message
        alertDialog.show();
    }
	
	OnAsyncTaskListener asyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			setGroupList(message);
			progressBar.setVisibility(View.GONE);
			
			if (adminId.equals(Sessions.getUserId(context))) {
	        	editImageView.setVisibility(View.VISIBLE);
	        	editImageView.setClickable(true);
			}
	        else
	        {
	        	editImageView.setVisibility(View.GONE);
	        	editImageView.setClickable(false);
	        }
		}
		
		@Override
		public void onTaskBegin() {
			progressBar.setVisibility(View.VISIBLE);
			editImageView.setVisibility(View.GONE);
			editImageView.setClickable(false);
		}
	};
	
	private void setGroupList(String response) {
		
		AsyncResponse asyncResponse = new AsyncResponse(response);
		if(asyncResponse.ifSuccess())
		{	
			list = asyncResponse.getGroupFriendListMember();
			Log.i(TAG, "resp : "+response);
			adapter.refresh(list);
		}
		else
		{
			Log.e(TAG, "err : "+asyncResponse.getMessage());
			showToast(asyncResponse.getMessage());
		}
	}	
	
	private void onNext() {
		
		boolean check = true;
		Validate validate = new Validate();
		
		name = groupName.getText().toString();
		
		if(validate.isNotEmpty(name))
		{
			name = groupName.getText().toString();
		}
		else
		{
			name = "";
			check = false;
			groupName.setError("Enter a group name.");
		}
		
		if(check)
		{
			changeGroupNameAndPic(context, groupId, name);
		}
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		
		if(!groupName.getText().toString().equals(""))
			name = groupName.getText().toString();
		else
			name = "";
		outState.putString(Constant.NAME, name);
		super.onSaveInstanceState(outState);
	};
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);	
		name = savedInstanceState.getString(Constant.NAME);
		setDetails(name);
	};
	
	private void setDetails(String name) {
		groupName.setText(""+name);
	}
	
	public void onAddUserClick(View view) {
		Intent intent = new Intent();
		intent.setClass(context, GroupEditMemberListActivity.class);
		intent.putExtra(Constant.GROUP_ID, groupId);
		intent.putExtra(Constant.ADMIN_ID, adminId);
		intent.putExtra(Constant.NAME, name);
		startActivity(intent);
	}
	
	private void changeGroupNameAndPic(Context context, String groupId, String name) {
		
		String filename = getResources().getString(R.string.groupnameedit_php);
		AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.GROUP_ID, groupId);
		map.put(Constant.NAME, name);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(nameChangeGroupTaskListener);
		asyncLoadVolley.beginTask();
	}
	
	OnAsyncTaskListener nameChangeGroupTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			AsyncResponse asyncResponse = new AsyncResponse(message);
			if(asyncResponse.ifSuccess())
			{	
				showToast("Group name changed.");
				
				if(path!=null)
				{
					String name = "group"+groupId + new Date().getTime() +".jpg";
					onSelectPhoto(name, path, groupId);
				}
			}
			else
			{
				Log.e(TAG, "err : "+asyncResponse.getMessage());
				showToast(asyncResponse.getMessage());
			}
			setProgressBarIndeterminateVisibility(false);
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
	
	// delete group
	
	private void deleteGroup(Context context, String groupId) {
		
		showToast(" deleting group");
		String filename = getResources().getString(R.string.groupdelete_php);
		AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(context, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.GROUP_ID, groupId);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(deleteGroupTaskListener);
		asyncLoadVolley.beginTask();
	}
	
	OnAsyncTaskListener deleteGroupTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			AsyncResponse asyncResponse = new AsyncResponse(message);
			if(asyncResponse.ifSuccess())
			{	
				showToast("Group "+name+ " deleted successfully.");
				finish();
			}
			else
			{
				Log.e(TAG, "err : "+asyncResponse.getMessage());
				showToast(asyncResponse.getMessage());
			}
			setProgressBarIndeterminateVisibility(false);
		}
		
		@Override
		public void onTaskBegin() {
			setProgressBarIndeterminateVisibility(true);
		}
	};
	
	/////
	
	private static final int CAMERA_REQUEST = 1;
	private static final int RESULT_LOAD_IMAGE = 2;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK)
			return;
		
		switch (requestCode) {
		case CAMERA_REQUEST:
			
			if (resultCode == Activity.RESULT_OK)
			{
				File imagesFolder = new File(Environment.getExternalStorageDirectory(), "myfolder");
				File image = new File(imagesFolder, "image_002.jpg");
				
				String path=image.getAbsolutePath();
				File file = new File(path);
				Bitmap bitmap = ImageCustomize.decodeFile(file, 200);
				groupImage.setImageBitmap(bitmap);
				
				this.path = path;
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
			        	groupImage.setImageBitmap(bitmap);
			        	
						this.path = picturePath;
				 	}
			}
			break;
			
		case Constant.REQUEST_CODE_INT:
	            finish();
			break;
		}
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
	
	/////
	
	protected void showToast(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
	}		
    
    protected void showToastLong(String message) {
		Toast.makeText(context, ""+message, Toast.LENGTH_LONG).show();
	}
}
