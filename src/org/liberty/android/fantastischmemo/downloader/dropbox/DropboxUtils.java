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

    
    
    /*
     * Return value: The index 0 is the token, index 1 is the secret
     */
//    public static String[] retrieveToken(String username, String password) throws Exception{
//        String url = TOKEN_URL + "&email=" + URLEncoder.encode(username)
//            + "&password=" + URLEncoder.encode(password);
//        String jsonString = DownloaderUtils.downloadJSONString(url);
//        JSONObject jsonObject = new JSONObject(jsonString); 
//        String error = null;
//        try {
//            error = jsonObject.getString("error");
//        }
//        catch (JSONException e) {
//            /* If error does not exist, the error will be null */
//        }
//            
//        if (error != null) {
//            throw new Exception(error);
//        }
//        String token = jsonObject.getString("token");
//        String secret = jsonObject.getString("secret");
//        return new String[]{token, secret};
//    }

//    public static List<DownloadItem> listFiles(String oauthToken, String oauthSecret, String path) throws Exception{
//        String url = signPathUrl(oauthToken, oauthSecret, METADATA_URL, path);
//        String jsonString = DownloaderUtils.downloadJSONString(url);
//        String error = null;
//        JSONObject jsonObject = new JSONObject(jsonString); 
//        try {
//            error = jsonObject.getString("error");
//        }
//        catch (JSONException e) {
//            /* If error does not exist, the error will be null */
//        }
//        if (error != null) {
//            throw new Exception(error);
//        }
//        JSONArray contentArray = jsonObject.getJSONArray("contents");
//        List<DownloadItem> diList = new ArrayList<DownloadItem>();
//        for(int i = 0; i < contentArray.length(); i++) {
//            JSONObject item = contentArray.getJSONObject(i);
//            boolean isDir = item.getBoolean("is_dir");
//            DownloadItem di = new DownloadItem();
//            if(isDir) {
//                di.setType(DownloadItem.ItemType.Category);
//            }
//            else {
//                di.setType(DownloadItem.ItemType.Database);
//            }
//            /* The address is like /graphics/mydb/aaa.db */
//            String address = item.getString("path");
//            String[] splitted = address.split("/");
//            String title = splitted[splitted.length - 1];
//            di.setAddress(address);
//            di.setTitle(title);
//            diList.add(di);
//        }
//        return diList;
//    }
//
//    /*
//     * savePath should be like /mnt/sdcard/anymemo/
//     */
//    public static File downloadFile(String oauthToken, String oauthSecret, DownloadItem di, String savePath) throws Exception {
//        String url = signPathUrl(oauthToken, oauthSecret, FILE_URL, di.getAddress());
//
//        return DownloaderUtils.downloadFile(url, savePath + di.getTitle());
//    }
//
//    public static void uploadFile(String oauthToken, String oauthSecret, String fileName, String remotePath) throws Exception {
//        OAuthConsumer oauthConsumer = new CommonsHttpOAuthConsumer(API_KEY, API_SECRET);
//        oauthConsumer.setTokenWithSecret(oauthToken, oauthSecret);
//        File file = new File(fileName);
//        HttpClient client = new DefaultHttpClient();
//        String url = FILE_URL + remotePath;
//            HttpPost req = new HttpPost(url);
//            // this has to be done this way because of how oauth signs params
//            // first we add a "fake" param of file=path of *uploaded* file
//            // THEN we sign that.
//            List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
//            nvps.add(new BasicNameValuePair("file", file.getName()));
//            req.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
//            oauthConsumer.sign(req);
//
//            // now we can add the real file multipart and we're good
//            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//            FileBody bin = new FileBody(file);
//            entity.addPart("file", bin);
//            // this resets it to the new entity with the real file
//            req.setEntity(entity);
//
//            HttpResponse resp = client.execute(req);
//            
//            String responseString = DownloaderUtils.readResponse(resp);
//            if (!responseString.contains("winner")) {
//                throw new Exception(responseString);
//            }
//    }

//    private static String signPathUrl(String oauthToken, String oauthSecret, 
//            String apiUrl, String path) throws Exception {
//        OAuthConsumer oauthConsumer = new DefaultOAuthConsumer(API_KEY, API_SECRET);
//        oauthConsumer.setTokenWithSecret(oauthToken, oauthSecret);
//        /* Make the suitable URL for dropbox API */
//        path = URLEncoder.encode(path);
//        path = path.replace("%2F", "/");
//        path = path.replace("+", "%20");
//        String url = apiUrl + path;
//        url = oauthConsumer.sign(url);
//        return url;
//    }



}
