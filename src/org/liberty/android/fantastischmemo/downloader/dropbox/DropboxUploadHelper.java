package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.myhttp.entity.mime.MultipartEntity;
import org.apache.myhttp.entity.mime.content.FileBody;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMEnv;
import android.content.Context;
import android.util.Log;

public class DropboxUploadHelper {

    
    private Context mContext;

    private final String authToken;
    private final String authTokenSecret;
    
    private final String FILE_UPLOAD_URL="https://api-content.dropbox.com/1/files_put/dropbox/";

//    private static SimpleDateFormat ISO8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public DropboxUploadHelper(Context context, String authToken, String authTokenSecret) {
        this.authToken = authToken;
        this.authTokenSecret = authTokenSecret;
        mContext = context;
    }

    public boolean upload(String fileName, String filePath){
        
        HttpClient httpclient = new DefaultHttpClient();
        
        String headerValue = "OAuth oauth_version=\"1.0\", "
              + "oauth_signature_method=\"PLAINTEXT\", "
              + "oauth_consumer_key=\"" + AMEnv.DROPBOX_CONSUMER_KEY + "\", "
              + "oauth_token=\"" + authToken + "\", "
              + "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET + "&"
              + authTokenSecret + "\"";

        try {
            String url = FILE_UPLOAD_URL + URLEncoder.encode(fileName, "utf-8"); 
            HttpPost httppost = new HttpPost(url);
            httppost.addHeader("Authorization", headerValue);
            httppost.addHeader("Content-Type", "application/x-sqlite3");

            FileBody fileToUpload = new FileBody(new File(filePath));

            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("file to upload", fileToUpload);

            httppost.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();

            Log.v("xinxin**** ", response.getStatusLine().toString());
            if (resEntity != null) {
                InputStream is = resEntity.getContent();
                String resultString = DropboxDownloadHelper.convertStreamToString(is);
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
    
//    private String getRev(String path){
//        String rev = null;
//        String url = "https://api.dropbox.com/1/metadata/dropbox/" + path;
//        
//        InputStream is = null;
//        try {
//            String headerValue = "OAuth oauth_version=\"1.0\", "
//                    + "oauth_signature_method=\"PLAINTEXT\", "
//                    + "oauth_consumer_key=\"" + AMEnv.DROPBOX_CONSUMER_KEY + "\", "
//                    + "oauth_token=\"" + authToken + "\", "
//                    + "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET + "&"
//                    + authTokenSecret + "\"";
//
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet(url);
//            httpGet.setHeader("Authorization", headerValue);
//            HttpResponse response = null;
//            response = httpClient.execute(httpGet);
//            HttpEntity entity = response.getEntity();
//            is = entity.getContent();
//            JSONObject jsonResponse = new JSONObject(DropboxDownloadHelper.convertStreamToString(is));
//            JSONArray filesJSON = jsonResponse.getJSONArray("contents");
//            JSONObject entryJSON;
//            List<DownloadItem> spreadsheetList = new ArrayList<DownloadItem>(); 
//            for(int i = 0 ; i < filesJSON.length(); i++){
//                entryJSON = filesJSON.getJSONObject(i);
//                if(entryJSON.getString("path").endsWith(".db")){
//                    spreadsheetList.add(new DownloadItem(ItemType.Spreadsheet, entryJSON.getString("path"), entryJSON.getString("modified"),  ""));
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if(is != null){
//                    is.close();
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            
//        }
//        return rev;
//    }
    
    
}
