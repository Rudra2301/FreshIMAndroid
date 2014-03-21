package com.chat.imapp;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ImageView;

import com.chat.imapp.imagecache.ImageLoader;
import com.chat.imapp.imagecache.ImageLoaderNew;
import com.chat.imapp.imagecache.TouchImageView;
import com.chat.imapp.utility.Constant;

@SuppressLint("NewApi")
public class UserDetailActivity extends FragmentActivity {
	
	private static final String TAG = "UserDetailActivity";
	
	private Context context = UserDetailActivity.this;
		
	private ImageViewTouch userImageView;
	
	private ImageLoaderNew imageLoader;
	
	private String name = "User", image = "";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdetail);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		Bundle bundle = getIntent().getExtras();
		name = "Image"; 
		image = bundle.getString(Constant.IMAGE);
		
		getActionBar().setTitle(name);
		
		ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
		
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		userImageView = new ImageViewTouch(context);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		userImageView.setLayoutParams(layoutParams);
		viewGroup.addView(userImageView);
		userImageView.setDisplayType(DisplayType.FIT_TO_SCREEN);
		
		imageLoader = new ImageLoaderNew(context);
		String url = Constant.URL + Constant.FOLDER + Constant.FOLDER_IMG + image;
		imageLoader.displayImage(url, userImageView, false, 300);		
    }
    
    @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 				
 		case android.R.id.home:
 	        	finish();
 	        return true;
         
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}	
    }