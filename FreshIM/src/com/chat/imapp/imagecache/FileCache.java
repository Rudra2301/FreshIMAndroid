package com.chat.imapp.imagecache;

import java.io.File;
import java.util.UUID;

import android.content.Context;

import com.chat.imapp.R;

public class FileCache {
    
    private static final String TAG = "FileCache";
	private File cacheDir;
    UUID uuid;
    
    public FileCache(Context context) {
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(), context.getResources().getString(R.string.app_name));
        }
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }
    
    public File getFile(String url) {
    	
        String filename=String.valueOf(url.hashCode());        
        File f = new File(cacheDir, filename);
        return f;        
    }
    
    public void clear() {
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }
}