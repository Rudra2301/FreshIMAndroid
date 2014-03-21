package com.chat.imapp;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
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
import android.widget.TextView;
import android.widget.Toast;

import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.Constant;
import com.chat.imapp.utility.ImageCustomize;
import com.chat.imapp.utility.Validate;

@SuppressLint("NewApi")
public class GroupCreateActivity extends Activity {

protected static final String TAG = "CreateGroupActivity";
	
	private Context context = GroupCreateActivity.this;
    
	EditText groupName;
    ImageView groupImage;
    
    private String name;
    
    private Bitmap bitmap;
    private String path;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.group_create);
		
		getActionBar().setTitle("Create Group");
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        groupName = (EditText) findViewById(R.id.groupName);
        groupImage = (ImageView) findViewById(R.id.groupImage);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.status, menu);
		return true;
	}
	
	@Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			menu.clear();
		    getMenuInflater().inflate(R.menu.status, menu);
		    MenuItem item = menu.findItem(R.id.savestatus);
		    item.setTitle("Next");		
			return super.onPrepareOptionsMenu(menu);
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
			Intent intent=new Intent();
			intent.setClass(context, GroupChooseFriendListActivity.class);
			if(path==null) 
				path="";
			intent.putExtra(Constant.IMAGEPATH, path);
			intent.putExtra(Constant.NAME, name);
			startActivityForResult(intent, Constant.REQUEST_CODE_INT);
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
				
				this.bitmap = bitmap;
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
			        	
			        	this.bitmap = bitmap;
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
