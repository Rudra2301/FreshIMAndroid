package com.chat.imapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.chat.imapp.adapters.FriendAllAdapter;
import com.chat.imapp.adapters.FriendDetailAdapter;
import com.chat.imapp.interfaces.OnAsyncTaskListener;
import com.chat.imapp.items.FriendDetailItem;
import com.chat.imapp.items.GcmDetailItem;
import com.chat.imapp.sessions.Sessions;
import com.chat.imapp.utility.AsyncLoadVolley;
import com.chat.imapp.utility.AsyncResponse;
import com.chat.imapp.utility.CommonUtilities;
import com.chat.imapp.utility.Constant;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
	private static final String TAG = "GcmIntentService";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    
    public static final String TYPE_SINGLE_MESSAGE = "1";
    public static final String TYPE_GROUP_MESSAGE = "2";
    public static final String TYPE_REQUEST_MESSAGE = "3";
    
    private static final int TYPE_NEW_REQUEST = 0;
    private static final int TYPE_ACCEPT_REQUEST = 1;
    private static final int TYPE_CANCEL_REQUEST = 2;
    private static final int TYPE_REJECT_REQUEST = 3;
    private static final int TYPE_UNFRIEND = 4;
    
    public static final String ACTION_ACCEPT = "ACCEPT";
    public static final String ACTION_REJECT = "REJECT";
	    
    public GcmIntentService() {
        super("GcmIntentService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        
        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
        	
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
        	
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString(), "1");
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
            	
            	String newMessage = intent.getExtras().getString("message"); //extras.toString();
            	Log.e(TAG, "newMessage : " + newMessage);
            	
            	List<GcmDetailItem> list = new ArrayList<GcmDetailItem>();
            	AsyncResponse asyncResponse = new AsyncResponse(newMessage);
    			if(asyncResponse.ifSuccess())
    			{
    				Log.e(TAG, "SUCC");
    				
    				list = asyncResponse.getGcmDetail();
    				Log.e(TAG, "WHO : " + list.get(0).getWho());
    				if(Sessions.isLoggedIn(this))
    				{
    					Log.e(TAG, "LOGGED IN");
    					
    					if(list.get(0).getWho().equals(TYPE_REQUEST_MESSAGE))	// FRIEND REQUEST STUFF
    					{
    						Log.e(TAG, "FRIEND REQUEST STUFF");
    						int type = Integer.parseInt(list.get(0).getType()); // To detect what exactly happens
    						
    						intent = new Intent(CommonUtilities.REQUEST_MESSAGE_ACTION);
    					    intent.putExtra(CommonUtilities.EXTRA_MESSAGE, newMessage);
    					    getApplicationContext().sendBroadcast(intent);
    						
    						String name = list.get(0).getName();
    						
    						switch (type) {
							case TYPE_NEW_REQUEST:
								sendRequestNotification("FreshIM", name+" wants to be your friend on FreshIM. "
										, list.get(0).getUserId(), list.get(0).getFriendId());
								break;
								
							case TYPE_ACCEPT_REQUEST:
								sendNotification(name+" accepted your friends request.", TYPE_REQUEST_MESSAGE);
								
								break;
								
							case TYPE_CANCEL_REQUEST:
								sendNotification(name+" cancelled his friend request. ", TYPE_REQUEST_MESSAGE);
								break;
								
							case TYPE_REJECT_REQUEST:
								sendNotification(name+" rejected your friend request. ", TYPE_REQUEST_MESSAGE);
								break;
								
							case TYPE_UNFRIEND:
								sendNotification(name+" unfriended you. ", TYPE_REQUEST_MESSAGE);
								break;
								
							default:
								break;
							}
    					}
    					else if(list.get(0).getWho().equals(TYPE_GROUP_MESSAGE))  	// MESSAGE TO A GROUP
    					{
    						Log.e(TAG, "GROUP_MESSAGE");
    						//startLoad(list.get(0).getId(), list.get(0).getWho());
    						sendGroupMessage(list.get(0).getId());
    					}
    					else if(list.get(0).getWho().equals(TYPE_SINGLE_MESSAGE))   // MESSAGE TO INDIVIDUAL FRIEND
    					{
    						Log.e(TAG, "SINGLE_MESSAGE");
    						sendSingleMessage(list.get(0).getId());
    					}   
    				}
    			}
    			else
    			{
    				Log.e(TAG, "err : "+asyncResponse.getMessage());
    			}
            	
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, String who) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, FriendsListActivity.class), 0);
        
        if(who.equals(TYPE_SINGLE_MESSAGE))
        {
        	contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, FriendsListActivity.class), 0);
        }
        else if(who.equals(TYPE_GROUP_MESSAGE))
        {
        	contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, GroupListActivity.class), 0);
        }
        else if(who.equals(TYPE_REQUEST_MESSAGE))
        {
        	contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, FriendsAllListActivity.class), 0);
        }
        
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.top_logo)
        .setContentTitle(getResources().getString(R.string.app_name))
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setTicker(msg)
        .setContentText(msg)
        .setAutoCancel(true);
        
        // Play default notification sound
        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL);
        
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
        
    private void sendSingleMessage(String id) {
		
    	String filename = getResources().getString(R.string.frienddetailgcm_php);
    	AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(this, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.ID, id);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(singleTaskListener);
		asyncLoadVolley.beginTask();
	}
    
    OnAsyncTaskListener singleTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "singleTaskListener mess : "+message);
			
			AsyncResponse asyncResponse = new AsyncResponse(message);
			List<FriendDetailItem> list = new ArrayList<FriendDetailItem>();
			if(asyncResponse.ifSuccess())
			{
				list = asyncResponse.getFriendDetail();
				String msg = list.get(0).getMessage();
				
				if(Integer.parseInt(list.get(0).getType())==FriendDetailAdapter.TYPE_IMAGE)
					msg = "Image";	
				
				Log.e(TAG, "msg : "+message);
				
			    Intent intent = new Intent(CommonUtilities.SINGLE_MESSAGE_ACTION);
			    intent.putExtra(CommonUtilities.EXTRA_MESSAGE, message);
			    getApplicationContext().sendBroadcast(intent);
				
				sendNotification(msg, TYPE_SINGLE_MESSAGE);				
			}
			else
			{
				Log.e(TAG, "err : "+asyncResponse.getMessage());
			}
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
	
	private void sendGroupMessage(String id) {
		
    	String filename = getResources().getString(R.string.frienddetailgcm_php);
    	AsyncLoadVolley asyncLoadVolley = new AsyncLoadVolley(this, filename);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constant.ID, id);
		asyncLoadVolley.setBasicNameValuePair(map);
		asyncLoadVolley.setOnAsyncTaskListener(groupTaskListener);
		asyncLoadVolley.beginTask();
	}
	
	OnAsyncTaskListener groupTaskListener = new OnAsyncTaskListener() {
		
		@Override
		public void onTaskComplete(boolean isComplete, String message) {
			Log.e(TAG, "groupTaskListener mess : "+message);
			
			AsyncResponse asyncResponse = new AsyncResponse(message);
			List<FriendDetailItem> list = new ArrayList<FriendDetailItem>();
			if(asyncResponse.ifSuccess())
			{	
				list = asyncResponse.getFriendDetail();
				String msg = list.get(0).getMessage();
				
				if(Integer.parseInt(list.get(0).getType())==FriendDetailAdapter.TYPE_IMAGE)
					msg = "Image";	
				
				Log.e(TAG, "msg : "+message);
				
			    Intent intent = new Intent(CommonUtilities.GROUP_MESSAGE_ACTION);
			    intent.putExtra(CommonUtilities.EXTRA_MESSAGE, message);
			    getApplicationContext().sendBroadcast(intent);
				
				sendNotification(msg, TYPE_GROUP_MESSAGE);
			}
			else
			{
				Log.e(TAG, "err : "+asyncResponse.getMessage());
			}
		}
		
		@Override
		public void onTaskBegin() {
			
		}
	};
    
	
	private void sendRequestNotification(String title, String message, String userId, String friendId) {
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(false);
        //builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentText(message);
        
        builder.setSmallIcon(R.drawable.top_logo);
        builder.setTicker(message);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        
        Intent intent = new Intent(this, FriendsAllListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        
        Intent acceptIntent = new Intent(this, FriendRequestService.class);
        acceptIntent.setAction(ACTION_ACCEPT);
        acceptIntent.putExtra(Constant.USER_ID, userId);
        acceptIntent.putExtra(Constant.FRIEND_ID, friendId);
        
        PendingIntent acceptPendingIntent = PendingIntent.getService(this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);        
        builder.addAction(android.R.drawable.ic_media_play, "Accept", acceptPendingIntent);
                
        Intent rejectIntent = new Intent(this, FriendRequestService.class);
        rejectIntent.setAction(ACTION_REJECT);
        rejectIntent.putExtra(Constant.USER_ID, userId);
        rejectIntent.putExtra(Constant.FRIEND_ID, friendId);
        
        PendingIntent rejectPendingIntent = PendingIntent.getService(this, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_action_cancel, "Reject", rejectPendingIntent);
        
        // if(artwork != null) {
//             builder.setLargeIcon(artwork);
        // }
        // builder.setContentText(artist);
        // builder.setSubText(album);
        
        // startForeground(R.id.notification_id, builder.build());
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }
	
	public static void cancelNotification(Context ctx, int notifyId) {
	    String ns = Context.NOTIFICATION_SERVICE;
	    NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
	    nMgr.cancel(notifyId);
	}
    
}
