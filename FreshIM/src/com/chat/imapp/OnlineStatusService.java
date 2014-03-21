package com.chat.imapp;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.Constant;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class OnlineStatusService extends Service {
	
	Timer timer = new Timer();
	TimerTask task;
	
	int time = (int)(1000 * 60 * 60 * 0.5); // 30 minutes
	//int time = (int)(1000 * 60 * 1);
	//int time = (int)(1000 * 20);
	//int time = 1000 * 10;
	
	public OnlineStatusService() {
		super();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(intent!=null) {
			
		}
		return super.onStartCommand(intent, flags, startId);				
	}
	
	@Override
	public void onDestroy() {
		timer.cancel();
		Log.w("mine", "CLICKED");
		super.onDestroy();
	}
	
	
}
