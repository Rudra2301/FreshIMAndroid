package com.chat.imapp.imagecache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chat.imapp.R;

public class ImageLoaderWithProgress {
	
	Context context;
    
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    
    private boolean load = false;
    int newHeight = 150;
    
    public ImageLoaderWithProgress(Context context) {
    	this.context = context;
        fileCache=new FileCache(context);
        executorService=Executors.newFixedThreadPool(5);
        load = false;
    }
    
    final int stub_id=R.drawable.ic_launcher;
    
    public void displayImage(String url, ImageView imageView, ProgressBar progressBar)
    {	
    	Log.i(null, " -> "+url);
    	imageView.setVisibility(View.GONE);
    	progressBar.setVisibility(View.VISIBLE);
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null) {
        	if(load)	bitmap = getResizedBitmap(bitmap, newHeight);
        	imageView.setVisibility(View.VISIBLE);
        	progressBar.setVisibility(View.GONE);
        	imageView.setImageBitmap(bitmap);
        }   
        else
        {	
            queuePhoto(url, imageView, progressBar);
            imageView.setImageResource(stub_id);
        }	
    }	
    
    private void queuePhoto(String url, ImageView imageView, ProgressBar progressBar)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView, progressBar);
        executorService.submit(new PhotosLoader(p));
    }
    
    private File getFile(String url) 
    {
    	File f=fileCache.getFile(url);
		return f;
    }
    
    private Bitmap getBitmap(String url) 
    {	
        File f = getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b!=null)
            return b;
        
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               memoryCache.clear();
           return null;
        }
    }
    
    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=newHeight;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    
  //decodes image and scales it to reduce memory consumption
    private Bitmap resizeBitmap(File f) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=newHeight;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.outHeight = newHeight;
            
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public ProgressBar progressBar;
        public PhotoToLoad(String u, ImageView i, ProgressBar progressBar){
            url=u; 
            imageView=i;
            this.progressBar = progressBar;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bmp=getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            if(imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {	
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p)
        {	
        	bitmap=b;
        	photoToLoad=p;
        }	
        public void run()
        {	
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null) {
            	if(load)	bitmap = getResizedBitmap(bitmap, newHeight);
            	photoToLoad.imageView.setVisibility(View.VISIBLE);
            	photoToLoad.progressBar.setVisibility(View.GONE);
                photoToLoad.imageView.setImageBitmap(bitmap);
                Animation animationFade = new AlphaAnimation(0, 1);
                animationFade.setDuration(1000);
                animationFade.setFillAfter(true);
                animationFade.setInterpolator(new DecelerateInterpolator());
                photoToLoad.imageView.setAnimation(animationFade);
            }
            else {
            	photoToLoad.imageView.setVisibility(View.VISIBLE);
            	photoToLoad.progressBar.setVisibility(View.GONE);
                photoToLoad.imageView.setImageResource(stub_id);
            }
        }
    }
    
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
    
    public void setAdjustSize(int newHeight) {
		this.load = true;
		this.newHeight = newHeight;
	}   
    
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight) {
		 
		 Bitmap resizedBitmap = bm;
		 
		int width = bm.getWidth();
		int height = bm.getHeight();
	    float aspect = (float)height / width;
	    
	    float scaleWidth = width;
	    float scaleHeight = height;       // yeah!
	    
	    if(height>newHeight)
	    {	
		    //Adjust width according to height
		    scaleHeight = newHeight;
		    scaleWidth = scaleHeight / aspect;       // yeah!
		    		    
		    // create a matrix for the manipulation	    
		    Matrix matrix = new Matrix();
		    
		    // resize the bitmap
		    matrix.postScale(scaleWidth / width, scaleHeight / height);
		    
		    // recreate the new Bitmap
		    resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
		    
		    bm.recycle();
	    }
	    return resizedBitmap;
	}
}
