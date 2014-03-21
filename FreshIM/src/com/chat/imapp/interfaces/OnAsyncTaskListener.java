package com.chat.imapp.interfaces;

public interface OnAsyncTaskListener {
	void onTaskBegin();
	void onTaskComplete(boolean isComplete, String message);
}
