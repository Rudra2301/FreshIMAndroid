package com.chat.imapp.utility;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
 
/**
 * Custom Request Class for JSON
 */
public class CustomRequestString extends Request<String> {
	
    private List<BasicNameValuePair> params;        // the request params
    private Response.Listener<String> listener; // the response listener
    
    public CustomRequestString(int requestMethod, String url, List<BasicNameValuePair> params,
                          Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
    	
        super(requestMethod, url, errorListener); // Call parent constructor
        this.params = params;
        this.listener = responseListener;
        
        Log.e("IN REQUEST", params.toString());
    }
    
    // We HAVE TO implement this function
    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response); // Call response listener
    }
    
    // Proper parameter behavior
    @Override
    public Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> map = new HashMap<String, String>();
 
        // Iterate through the params and add them to our HashMap
        for (BasicNameValuePair pair : params) {
            map.put(pair.getName(), pair.getValue());
        }
        
        return map;
    }
	
    // Same as JsonObjectRequest#parseNetworkResponse
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(jsonString,null);
          //  HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception je) {
            return Response.error(new ParseError(je));
        }
    }

 
}