package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.inject.assistedinject.Assisted;

public class DropboxUploadHelper {

    private final String authToken;
    private final String authTokenSecret;

    private static final String FILE_UPLOAD_URL="https://api-content.dropbox.com/1/files_put/dropbox/AnyMemo/";

    @Inject
    public DropboxUploadHelper(Context context, @Assisted("authToken") String authToken, @Assisted("authTokenSecret") String authTokenSecret) {
        this.authToken = authToken;
        this.authTokenSecret = authTokenSecret;
    }

    // Return true if upload succeeded. false if something goes wrong.
    public boolean upload(String fileName, String filePath) throws ClientProtocolException, IOException, JSONException{

        HttpClient httpclient = new DefaultHttpClient();
        String headerValue = DropboxUtils.getFileExchangeAuthHeader(authToken, authTokenSecret);

        // See related encoding change in DropboxDownloadHelper.
        String url = FILE_UPLOAD_URL + URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Authorization", headerValue);
        httppost.setEntity(new FileEntity(new File(filePath), "application/x-sqlite3"));
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        if(response.getStatusLine().getStatusCode() == 200 && resEntity != null){
            InputStream is = resEntity.getContent();
            JSONObject jsonResponse = new JSONObject(IOUtils.toString(is));
            if(jsonResponse.getString("modified") != null){
                is.close();
                return true;
            }
        }
        return false;
    };

}
