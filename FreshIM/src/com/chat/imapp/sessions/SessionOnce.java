package com.chat.imapp.sessions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.chat.imapp.utility.Constant;

public class SessionOnce {
	
	private static final String KEY = Constant.TAG_APP_NAME;
			
	public static boolean isLoggedIn(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		int online = Integer.parseInt(preferences.getString(Constant.ONLINE, "0"));
		if(online==1)
			return true;
		else
			return false;
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
	
	public static boolean clear(Context context) {
		Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
		editor.clear();
        return editor.commit();
	}
	
	
}	