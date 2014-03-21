package com.chat.imapp.utility;

import android.content.Context;
import android.content.Intent;

public final class CommonUtilities {
    	
	// Google project id
	public static final String SENDER_ID = "808457187436"; 
	
	/**
    * Tag used on log messages.
    */
   static final String TAG = "AndroidHive GCM";
   
   public static final String SINGLE_MESSAGE_ACTION =
           "SINGLE_MESSAGE_ACTION";
   
   public static final String GROUP_MESSAGE_ACTION =
           "GROUP_MESSAGE_ACTION";
   
   public static final String REQUEST_MESSAGE_ACTION =
           "REQUEST_MESSAGE_ACTION";
   
   public static final String EXTRA_MESSAGE = "message";
   
   /**
    * Notifies UI to display a message.
    * <p>
    * This method is defined in the common helper because it's used both by
    * the UI and the background service.
    *
    * @param context application's context.
    * @param message message to be displayed.
    */
   public static void displayMessage(Context context, String message) {
       Intent intent = new Intent(SINGLE_MESSAGE_ACTION);
       intent.putExtra(EXTRA_MESSAGE, message);
       context.sendBroadcast(intent);
   }
}