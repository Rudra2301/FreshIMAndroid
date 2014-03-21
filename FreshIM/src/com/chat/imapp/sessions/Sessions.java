package com.chat.imapp.sessions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.chat.imapp.utility.Constant;

public class Sessions {
	
	private static final String KEY = Constant.LOGIN;
	
	
	
	public static boolean save(Context context, String id) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putString(Constant.ID, id);
		editor.putBoolean(Constant.STATUS, true);
        return editor.commit();
	}
	
	public static boolean save(Context context, String id, String name, String lname, String email, String status, String online, String image) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putString(Constant.ID, id);
		editor.putString(Constant.NAME, name);
		editor.putString(Constant.LNAME, lname);
		editor.putString(Constant.EMAIL, email);
		editor.putString(Constant.VALUE, status);
		editor.putString(Constant.ONLINE, online);
		editor.putString(Constant.IMAGE, image);
        return editor.commit();
	}
	
	public static boolean clear(Context context) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.clear();
        return editor.commit();
	}
	
	public static boolean isLoggedIn(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		int online = Integer.parseInt(preferences.getString(Constant.ONLINE, "0"));
		if(online==1)
			return true;
		else
			return false;
	}
	
	public static boolean logout(Context context) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putBoolean(Constant.STATUS, false);
        return editor.commit();
		
	}
	
	public static String getUserId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return preferences.getString(Constant.ID, null);
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
	
	public static String getStatus(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return preferences.getString(Constant.VALUE, "");
	}
	
	public static boolean setStatus(Context context, String status) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putString(Constant.VALUE, status);
        return editor.commit();		
	}
	
	public static String getImage(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return preferences.getString(Constant.IMAGE, null);
	}
	
	public static String getEmail(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return preferences.getString(Constant.EMAIL, null);
	}
	
	public static String getLname(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return preferences.getString(Constant.LNAME, null);
	}

	public static boolean setLname(Context context, String lname) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putString(Constant.LNAME, lname);
        return editor.commit();		
	}
	
	
	
	public static boolean setImage(Context context, String image) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putString(Constant.IMAGE, image);
        return editor.commit();		
	}
	
	public static String getRegId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		return preferences.getString(Constant.REGID, "");
	}
	
	public static boolean setRegId(Context context, String regId) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.putString(Constant.REGID, regId);
        return editor.commit();		
	}
	
}	