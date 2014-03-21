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
import com.chat.imapp.utility.Validate;

@SuppressLint("NewApi")
public class EditProfileActivity extends Activity {

protected static final String TAG = "EditProfileActivity";
	
	private Context context = EditProfileActivity.this;
	
	private AsyncLoadVolley asyncLoadVolley;
    
	EditText txtName, txtLastName, txtEmail, txtPassword, txtStatus;
    ImageView userImageView;
    Button btnRegister;
    TextView txtEmailtxt, txtPasswordtxt;
    
    private String fname, lname, email, status;
    
    private Bitmap bitmap;
    private String path;
    
    private ImageLoader imageLoader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.register);
		
		getActionBar().setTitle("Edit Profile");
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        txtName = (EditText) findViewById(R.id.txtName);
        txtLastName = (EditText) findViewById(R.id.txtLastName);
        txtStatus = (EditText) findViewById(R.id.txtStatus);
        userImageView = (ImageView) findViewById(R.id.userImage);
        
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtEmailtxt = (TextView) findViewById(R.id.txtEmailtxt);
        txtPasswordtxt = (TextView) findViewById(R.id.txtPasswordtxt);
        
        txtPassword.setVisibility(View.GONE);
        txtPasswordtxt.setVisibility(View.GONE);
        btnRegister.setVisibility(View.GONE);
        
        email = Sessions.getEmail(context);
        txtEmail.setText(""+email);
        
        imageLoader = new ImageLoader(context);
        String url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + Sessions.getImage(context);
        
        if(bitmap==null || path == null)
        	imageLoader.displayImage(url, userImageView);
        else
        	userImageView.setImageBitmap(bitmap);
		
        if(savedInstanceState==null) 
        {
        	fname = Sessions.getName(context);
        	lname = Sessions.getLname(context);        	
        	status = Sessions.getStatus(context);
        	setDetails(fname, lname, status);
        	
    		
        }       
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
	        	onSave();
	        return true;
	        
			default:
				return super.onOptionsItemSelected(item);
			}
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		
		if(!txtName.getText().toString().equals(""))			
			fname = txtName.getText().toString();
		else
			fname = "";
		
		if(!txtLastName.getText().toString().equals(""))			
			lname = txtLastName.getText().toString();
		else
			lname = "";
		
		if(!txtStatus.getText().toString().equals(""))
			status = txtStatus.getText().toString();
		else
			status = "";
		
		outState.putString(Constant.NAME, fname);
		outState.putString(Constant.LNAME, lname);
		outState.putString(Constant.STATUS, status);
		super.onSaveInstanceState(outState);
	};
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);	
		fname = savedInstanceState.getString(Constant.NAME);
		lname = savedInstanceState.getString(Constant.LNAME);
		status = savedInstanceState.getString(Constant.STATUS);
		setDetails(fname, lname, status);
	};
	
	private void setDetails(String fname, String lname, String status) {
		txtName.setText(""+fname);
		txtLastName.setText(""+lname);
		txtStatus.setText(""+status);
	}
	
	private void onSave() {
		
		boolean check = true;
    	
    	Validate validate = new Validate();
    	
    	if(!txtStatus.getText().toString().equals(""))
			status = txtStatus.getText().toString();
		
		if(!txtName.getText().toString().equals(""))
			fname = txtName.getText().toString();
		
		if(!txtLastName.getText().toString().equals(""))			
			lname = txtLastName.getText().toString();
    	
    	if(validate.isNotEmpty(fname))
		{
			fname = txtName.getText().toString();
		}
		else
		{
			fname = "";
			check = false;
			txtName.setError("Enter First Name");
		}
		if(validate.isNotEmpty(lname))
			lname = txtLastName.getText().toString();
		else
		{
			lname = "";
			check = false;
			txtLastName.setError("Enter Last Name");
		}
		if(validate.isNotEmpty(status))
			status = txtStatus.getText().toString();
		else
		{
			status = "Hey !!";
		}
		
		if(check)
		{		
			String filename = getResources().getString(R.string.edit_profile_php);
			asyncLoadVolley = new AsyncLoadVolley(context, filename);
			Map<String, String> map = new HashMap<String, String>();
			map.put(Constant.ID, Sessions.getUserId(context));
			map.put(Constant.NAME, fname);
        	map.put(Constant.LNAME, lname);
    		map.put(Constant.STATUS, status);
			asyncLoadVolley.setBasicNameValuePair(map);
			asyncLoadVolley.setOnAsyncTaskListener(changeAsyncTaskListener);
			asyncLoadVolley.beginTask();
		}
	}
	
	private String response = "";
	OnAsyncTaskListener changeAsyncTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "mess : "+message);
			response = message;
			AsyncResponse asyncResponse = new AsyncResponse(response);
			if(asyncResponse.ifSuccess())
			{
				showToast("Successfully Changed");
				
				Sessions.setStatus(context, status);
				Sessions.setName(context, fname);
				Sessions.setLname(context, lname);
				
				if(bitmap==null || path==null) {}
				else
					onSelectPhoto(bitmap, path);
			}
			else
			{
				showToast("Error .");
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
	
	private void onSelectPhoto(Bitmap bitmap, String path) {
		
		String name = "user"+Sessions.getUserId(context) + new Date().getTime() +".jpg";
		
		Intent intent=new Intent(context, LoadImageService.class);
		intent.putExtra(Constant.NAME, name);
		intent.putExtra(Constant.PATH, path);		
		intent.putExtra(Constant.VALUE, LoadImageService.VALUE_PROFILE_PIC);
		startService(intent);
	}
	
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
				
				Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
				
				String path=image.getAbsolutePath();
				userImageView.setImageBitmap(bitmap);
				
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
			            
			        	Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
			        	userImageView.setImageBitmap(bitmap);
			        	
			        	this.bitmap = bitmap;
						this.path = picturePath;
				 	}
			}
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
