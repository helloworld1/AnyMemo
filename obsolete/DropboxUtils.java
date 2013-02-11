/*
Copyright (C) 2011 Haowen Ning

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
package org.liberty.android.fantastischmemo.downloader;

import java.io.File;

import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;

import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.apache.http.client.methods.HttpPost;

import org.apache.myhttp.entity.mime.HttpMultipartMode;

import org.apache.myhttp.entity.mime.content.FileBody;

import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.message.BasicNameValuePair;

import org.apache.http.protocol.HTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import oauth.signpost.OAuthConsumer;

import oauth.signpost.basic.DefaultOAuthConsumer;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.myhttp.entity.mime.MultipartEntity;

public class DropboxUtils{
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.DropboxUtils";
    private static final String API_KEY = "q2rclqr44ux8pe7";
    private static final String API_SECRET = "bmgikjefor073dh";
    private static final String TOKEN_URL = "https://api.dropbox.com/0/token"
        + "?oauth_consumer_key=" + API_KEY;
    private static final String METADATA_URL = "https://api.dropbox.com/0/metadata/dropbox";
    private static final String FILE_URL = "https://api-content.dropbox.com/0/files/dropbox";
    private static final String CREATE_FOLDER_URL = "https://api.dropbox.com/0/fileops/create_folder";

    /*
     * Return value: The index 0 is the token, index 1 is the secret
     */
    public static String[] retrieveToken(String username, String password) throws Exception{
        String url = TOKEN_URL + "&email=" + URLEncoder.encode(username)
            + "&password=" + URLEncoder.encode(password);
        String jsonString = DownloaderUtils.downloadJSONString(url);
        JSONObject jsonObject = new JSONObject(jsonString); 
        String error = null;
        try {
            error = jsonObject.getString("error");
        }
        catch (JSONException e) {
            /* If error does not exist, the error will be null */
        }
            
        if (error != null) {
            throw new Exception(error);
        }
        String token = jsonObject.getString("token");
        String secret = jsonObject.getString("secret");
        return new String[]{token, secret};
    }

    public static List<DownloadItem> listFiles(String oauthToken, String oauthSecret, String path) throws Exception{
        String url = signPathUrl(oauthToken, oauthSecret, METADATA_URL, path);
        String jsonString = DownloaderUtils.downloadJSONString(url);
        String error = null;
        JSONObject jsonObject = new JSONObject(jsonString); 
        try {
            error = jsonObject.getString("error");
        }
        catch (JSONException e) {
            /* If error does not exist, the error will be null */
        }
        if (error != null) {
            throw new Exception(error);
        }
        JSONArray contentArray = jsonObject.getJSONArray("contents");
        List<DownloadItem> diList = new ArrayList<DownloadItem>();
        for(int i = 0; i < contentArray.length(); i++) {
            JSONObject item = contentArray.getJSONObject(i);
            boolean isDir = item.getBoolean("is_dir");
            DownloadItem di = new DownloadItem();
            if(isDir) {
                di.setType(DownloadItem.ItemType.Category);
            }
            else {
                di.setType(DownloadItem.ItemType.Database);
            }
            /* The address is like /graphics/mydb/aaa.db */
            String address = item.getString("path");
            String[] splitted = address.split("/");
            String title = splitted[splitted.length - 1];
            di.setAddress(address);
            di.setTitle(title);
            diList.add(di);
        }
        return diList;
    }

    /*
     * savePath should be like /mnt/sdcard/anymemo/
     */
    public static File downloadFile(String oauthToken, String oauthSecret, DownloadItem di, String savePath) throws Exception {
        String url = signPathUrl(oauthToken, oauthSecret, FILE_URL, di.getAddress());

        return DownloaderUtils.downloadFile(url, savePath + di.getTitle());
    }

    public static void uploadFile(String oauthToken, String oauthSecret, String fileName, String remotePath) throws Exception {
        OAuthConsumer oauthConsumer = new CommonsHttpOAuthConsumer(API_KEY, API_SECRET);
        oauthConsumer.setTokenWithSecret(oauthToken, oauthSecret);
        File file = new File(fileName);
        HttpClient client = new DefaultHttpClient();
        String url = FILE_URL + remotePath;
            HttpPost req = new HttpPost(url);
            // this has to be done this way because of how oauth signs params
            // first we add a "fake" param of file=path of *uploaded* file
            // THEN we sign that.
            List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
            nvps.add(new BasicNameValuePair("file", file.getName()));
            req.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            oauthConsumer.sign(req);

            // now we can add the real file multipart and we're good
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody bin = new FileBody(file);
            entity.addPart("file", bin);
            // this resets it to the new entity with the real file
            req.setEntity(entity);

            HttpResponse resp = client.execute(req);
            
            String responseString = DownloaderUtils.readResponse(resp);
            if (!responseString.contains("winner")) {
                throw new Exception(responseString);
            }
    }

    private static String signPathUrl(String oauthToken, String oauthSecret, 
            String apiUrl, String path) throws Exception {
        OAuthConsumer oauthConsumer = new DefaultOAuthConsumer(API_KEY, API_SECRET);
        oauthConsumer.setTokenWithSecret(oauthToken, oauthSecret);
        /* Make the suitable URL for dropbox API */
        path = URLEncoder.encode(path);
        path = path.replace("%2F", "/");
        path = path.replace("+", "%20");
        String url = apiUrl + path;
        url = oauthConsumer.sign(url);
        return url;
    }



}
