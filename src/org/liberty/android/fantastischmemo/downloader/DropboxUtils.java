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

import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.liberty.android.fantastischmemo.R;

import android.os.Environment;

import oauth.signpost.OAuthConsumer;

import oauth.signpost.basic.DefaultOAuthConsumer;

import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class DropboxUtils{
    public static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderUtils";
    private static final String API_KEY = "q2rclqr44ux8pe7";
    private static final String API_SECRET = "bmgikjefor073dh";
    private static final String TOKEN_URL = "https://api.dropbox.com/0/token"
        + "?oauth_consumer_key=" + API_KEY;
    private static final String METADATA_URL = "https://api.dropbox.com/0/metadata/dropbox";
    private static final String FILE_URL = "https://api-content.dropbox.com/0/files/dropbox";

    /*
     * Return value: The index 0 is the token, index 1 is the secret
     */
    public static String[] retrieveToken(String username, String password) throws Exception{
        String url = TOKEN_URL + "&email=" + URLEncoder.encode(username)
            + "&password=" + URLEncoder.encode(password);
        String jsonString = DownloaderUtils.downloadJSONString(url);
        System.out.println("String: " + jsonString);
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
        System.out.println("Get cards String: " + jsonString);
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
                di.setType(DownloadItem.TYPE_CATEGORY);
            }
            else {
                di.setType(DownloadItem.TYPE_DATABASE);
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
    public static void downloadFile(String oauthToken, String oauthSecret, DownloadItem di, String savePath) throws Exception {
        String url = signPathUrl(oauthToken, oauthSecret, FILE_URL, di.getAddress());

        DownloaderUtils.downloadFile(url, savePath + di.getTitle());
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
