/*
Copyright (C) 2012 Haowen Ning, Xinxin Wang

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import org.liberty.android.fantastischmemo.AMEnv;


public class DropboxUtils{
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.dropbox.DropboxUtils";
    private static final String REQUEST_TOKEN_URL = "https://api.dropbox.com/1/oauth/request_token";
    private static final String AUTHORIZE_TOKEN_URL = "https://www.dropbox.com/1/oauth/authorize";
    
    
    private static String OAUTH_REQUEST_TOKEN_SECRET = null;
    private static String OAUTH_REQUEST_TOKEN = null;
    
    
    public static void retrieveOAuthRequestToken(){
    	
    	HttpClient httpClient = new DefaultHttpClient();
    	HttpPost httpPost = new HttpPost(REQUEST_TOKEN_URL);
    	httpPost.setHeader("Authorization", buildOAuthRequestHeader());
    	
    	HttpResponse response = null;
    	BufferedReader reader = null;
    	
        try {
            //response example: oauth_token_secret=f5pua2ozvgd1qnm&oauth_token=7npmduv9camokae
			response = httpClient.execute(httpPost);
        	HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            reader = new BufferedReader(new InputStreamReader(instream));
            String result = reader.readLine();
            String[] parsedResult = null;
            if(result.length() != 0){
            	parsedResult = result.split("&");
            	OAUTH_REQUEST_TOKEN_SECRET=parsedResult[0].split("=")[1];
            	OAUTH_REQUEST_TOKEN=parsedResult[1].split("=")[1];
            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(reader!= null){
			    try {
					reader.close();
				} catch (IOException e) {}
			}
		}
        
    }
    
    public static String getAuthorizationPageUrl(){
        return AUTHORIZE_TOKEN_URL + "?oauth_token="+ DropboxUtils.OAUTH_REQUEST_TOKEN+"&oauth_callback="+AMEnv.DROPBOX_REDIRECT_URI;
    }
    
    
    public static String buildOAuthAccessHeader(){
        String headerValue = 
                "OAuth oauth_version=\""+ AMEnv.DROPBOX_OAUTH_VERSION +"\", "
                + "oauth_signature_method=\"PLAINTEXT\", "
                + "oauth_token=\"" + OAUTH_REQUEST_TOKEN + "\", " 
                + "oauth_consumer_key=\""+ AMEnv.DROPBOX_CONSUMER_KEY +"\", "
                + "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET+ "&" + OAUTH_REQUEST_TOKEN_SECRET + "\"";
        
        return headerValue;
    }
    
    private static String buildOAuthRequestHeader(){
        String requestHeader = 
                "OAuth oauth_version=\""+AMEnv.DROPBOX_OAUTH_VERSION+"\", " +
                "oauth_signature_method=\"PLAINTEXT\", " +
                "oauth_consumer_key=\""+ AMEnv.DROPBOX_CONSUMER_KEY +"\", "+
                "oauth_signature=\""+ AMEnv.DROPBOX_CONSUMER_SECRET + "&\"";
        return requestHeader;
        
    }

}
