package com.chat.imapp.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class Validate {
	
	private static final String TAG = "Validate";
	
	public Validate() {
		// Empty Constructor
	}
	
	public boolean emailValidator(String email) 
	{
	    Pattern pattern;
	    Matcher matcher;
	    final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	    pattern = Pattern.compile(EMAIL_PATTERN);
	    matcher = pattern.matcher(email);
	    return matcher.matches();
	}
	
	public boolean isValidEmail(String target) {
	    if (target.equals("")) {
	        return false;
	    } else {
	    	
	    	if(android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches())
	    	{
	    		String topLevelDomain = target.substring(target.lastIndexOf(".") + 1);
	    		if(android.util.Patterns.TOP_LEVEL_DOMAIN.matcher(topLevelDomain).matches())
	    		{
	    			return true;
	    		}
	    	}
	    	return false;
	    }
	}
	
	public boolean isValidPhone(String target) {
	    if (target.equals("")) {
	        return false;
	    } else {
	    	if(PhoneNumberUtils.isGlobalPhoneNumber(target)) {
	    		if(target.length() > 8 && target.length() <= 10) {
	    			return true;
	    		}
	    		else {
	    			return false;
	    		}
	    	}
	    	else {
	    		return false;
	    	}
	    		
	    }
	}
	
	public boolean isAtleastValidLength(String target, int targetLength) {
	    if (target.equals("")) {
	        return false;
	    } else {
	    	if(target.length()>=targetLength)
	    		return true;
	    	else
	    		return false;
	    }
	}
	
	public boolean isNotEmpty(String target) {
	    return isAtleastValidLength(target, 1);
	}
	
	public boolean isValueBetween(String val, int start, int end) {
		String tVal = val;
		
		if(tVal.contains("."))
		{
			val = tVal.split("\\.")[0];
		}
		
		int value = Integer.parseInt(val);
		if(value>=start && value<=end)
			return true;
		return false;
	}
}
