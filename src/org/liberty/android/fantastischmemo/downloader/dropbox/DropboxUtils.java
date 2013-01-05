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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

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
    
    
    private static String oauthRequestTokenSecret = null;
    private static String oauthRequestToken = null;
    
    
    public static void retrieveOAuthRequestToken(){
    	
    	HttpClient httpClient = new DefaultHttpClient();
    	HttpPost httpPost = new HttpPost(REQUEST_TOKEN_URL);
    	httpPost.setHeader("Authorization", buildOAuthRequestHeader());
    	
    	HttpResponse response = null;
    	BufferedReader reader = null;
    	
        try {
			response = httpClient.execute(httpPost);
        	HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            reader = new BufferedReader(new InputStreamReader(instream));
            String result = reader.readLine();
            String[] parsedResult = null;
            if(result.length() != 0){
            	parsedResult = result.split("&");
            	oauthRequestTokenSecret=parsedResult[0].split("=")[1];
            	oauthRequestToken=parsedResult[1].split("=")[1];
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
    
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    
    public static void convertStreamToFile(InputStream is, File f) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(f);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = is.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally { 
            try {
                is.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static String getAuthorizationPageUrl(){
        return AUTHORIZE_TOKEN_URL + "?oauth_token="+ DropboxUtils.oauthRequestToken+"&oauth_callback="+AMEnv.DROPBOX_REDIRECT_URI;
    }
    

    
    public static String buildOAuthAccessHeader(){
        String headerValue = 
                "OAuth oauth_version=\""+ AMEnv.DROPBOX_OAUTH_VERSION +"\", "
                + "oauth_signature_method=\"PLAINTEXT\", "
                + "oauth_token=\"" + oauthRequestToken + "\", " 
                + "oauth_consumer_key=\""+ AMEnv.DROPBOX_CONSUMER_KEY +"\", "
                + "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET+ "&" + oauthRequestTokenSecret + "\"";
        
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
    
    
    public static String getFileExchangeAuthHeader(String authToken, String authTokenSecret){
        return "OAuth oauth_version=\"1.0\", "
                + "oauth_signature_method=\"PLAINTEXT\", "
                + "oauth_consumer_key=\"" + AMEnv.DROPBOX_CONSUMER_KEY + "\", "
                + "oauth_token=\"" + authToken + "\", "
                + "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET + "&"
                + authTokenSecret + "\"";
    }
    

}
