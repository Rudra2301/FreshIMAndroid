package com.chat.imapp.sessions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.chat.imapp.utility.Constant;

public class SessionGroup {
	
	private static final String KEY = Constant.GROUP;
	
	public static boolean clear(Context context) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.clear();
        return editor.commit();
	}
	
	public static String getName(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return preferences.getString(Constant.NAME, null);
	}
	
	public static boolean setName(Context context, String name) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putString(Constant.NAME, name);
        return editor.commit();		
	}
	
	public static String getImageName(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return preferences.getString(Constant.IMAGE, null);
	}
	
	public static boolean setImageName(Context context, String name) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putString(Constant.IMAGE, name);
        return editor.commit();		
	}
	
	public static String getImagePath(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return preferences.getString(Constant.IMAGEPATH, null);
	}
	
	public static boolean setImagePath(Context context, String path) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putString(Constant.IMAGEPATH, path);
        return editor.commit();		
	}
}	