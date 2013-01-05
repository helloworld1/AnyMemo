package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.content.Context;

public class DropboxUploadHelper {

    private final String authToken;
    private final String authTokenSecret;
    
    private static final String FILE_UPLOAD_URL="https://api-content.dropbox.com/1/files_put/dropbox/";


    public DropboxUploadHelper(Context context, String authToken, String authTokenSecret) {
        this.authToken = authToken;
        this.authTokenSecret = authTokenSecret;
    }

    public boolean upload(String fileName, String filePath){
        
        HttpClient httpclient = new DefaultHttpClient();
        String headerValue = DropboxUtils.getFileExchangeAuthHeader(authToken, authTokenSecret);
        
        try {
            String url = FILE_UPLOAD_URL + URLEncoder.encode(fileName, "utf-8"); 
            HttpPost httppost = new HttpPost(url);
            httppost.addHeader("Authorization", headerValue);
            httppost.setEntity(new FileEntity(new File(filePath), "application/x-sqlite3"));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                InputStream is = resEntity.getContent();
                String resultString = DropboxUtils.convertStreamToString(is);
                JSONObject jsonResponse = new JSONObject(resultString);
                if(jsonResponse.getString("modified") != null){
                    return true;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        
        return false;
    };
    
}
