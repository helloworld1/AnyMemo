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

import org.json.JSONException;
import org.json.JSONObject;

public class DropboxUtils{
    public static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderUtils";
    private static final String API_KEY = "q2rclqr44ux8pe7";
    private static final String TOKEN_URL = "https://api.dropbox.com/0/token"
        + "?oauth_consumer_key=" + API_KEY;

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
}
