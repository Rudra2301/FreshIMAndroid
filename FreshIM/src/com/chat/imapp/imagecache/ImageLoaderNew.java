package com.chat.imapp.imagecache;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

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
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

import com.chat.imapp.R;

public class ImageLoaderNew {
    
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageViewTouch, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageViewTouch, String>());
    ExecutorService executorService; 
    
    boolean makeCircle = false;
    
    Bitmap tempBitmap;
    
    int size = 70;
    
    public ImageLoaderNew(Context context){
        fileCache=new FileCache(context);
        executorService=Executors.newFixedThreadPool(5);
    }
    
    final int stub_id=R.drawable.ic_launcher;
    public void displayImage(String url, ImageViewTouch imageView)
    {
    	Log.e(null, " -> "+url);
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null) {
        	
        	imageView.setImageBitmap(bitmap);
        }
        else
        {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }
    
    public void displayImage(String url, ImageViewTouch imageView, boolean makeCircle, int size)
    {
    	this.makeCircle = makeCircle;
    	this.size = size;
    	Log.e(null, " -> "+url);
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null) {
        	if(makeCircle)
        		bitmap = change(bitmap, size);
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }
    
    public void displayImage(String url, ImageViewTouch imageView, boolean makeCircle, Bitmap tempImage)
    {
    	this.makeCircle = makeCircle;
    	Log.e(null, " -> "+url);
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null) {
        	if(makeCircle)
        		bitmap = change(bitmap, size);
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            queuePhoto(url, imageView);
            this.tempBitmap = tempImage;
            if(tempBitmap!=null) {
            	if(makeCircle)
            		tempBitmap = change(tempBitmap, size);
            	imageView.setImageBitmap(tempBitmap);
            }
            else
            	imageView.setImageResource(stub_id);
        }
    }
    
    private void queuePhoto(String url, ImageViewTouch imageView)
    {	
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    private Bitmap getBitmap(String url)
    {	
        File f=fileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f, size);
        if(b!=null)
            return b;
        
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f, size);
            return bitmap;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               memoryCache.clear();
           return null;
        }
    }
    
    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f ,int size) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=size;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true) {
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageViewTouch i){
            url=u; 
            imageView=i;
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
            if(bitmap!=null) 
            {	
            	if(makeCircle)
            		bitmap = change(bitmap, size);
                photoToLoad.imageView.setImageBitmap(bitmap);
            }
            else {
                if(tempBitmap!=null) {
                	if(makeCircle)
                		tempBitmap = change(tempBitmap, size);
                	photoToLoad.imageView.setImageBitmap(tempBitmap);
                }
                else
                	photoToLoad.imageView.setImageResource(stub_id);
            }
        }
    }
    
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
    
    public Bitmap change(Bitmap bitmap, int size)
    {
    	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
    	            bitmap.getHeight(), Config.ARGB_8888);
    	    Canvas canvas = new Canvas(output);
    	    
    	    final int color = 0xff424242;
    	    final Paint paint = new Paint();
    	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    	    int rad=size;
    	    paint.setAntiAlias(true);
    	    canvas.drawARGB(0, 0, 0, 0);
    	    paint.setColor(color);
    	    
    	    canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
    	            rad/ 2, paint);
    	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	    canvas.drawBitmap(bitmap, rect, rect, paint);
    	    return output;
    	
	}
}
