package com.chat.imapp.interfaces;

public interface OnUploadCompleteListener {
	void onUploadBegin();
	void onUploadComplete(boolean isComplete, String message);
}
