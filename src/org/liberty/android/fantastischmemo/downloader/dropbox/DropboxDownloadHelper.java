/*
Copyright (C) 2012 Haowen Ning

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMEnv;

import org.liberty.android.fantastischmemo.downloader.DownloadItem;
import org.liberty.android.fantastischmemo.downloader.DownloadItem.ItemType;


import android.content.Context;
public class DropboxDownloadHelper {
    private Context mContext;

    private final String authToken;
    private final String authTokenSecret;
    
    private final String METADATA_ACCESS_URL = "https://api.dropbox.com/1/metadata/dropbox/?list=true";
    private final String DOWNLOAD_URL = "https://api-content.dropbox.com/1/files/dropbox/";

    public DropboxDownloadHelper(Context context, String authToken, String authTokenSecret) {
        this.authToken = authToken;
        this.authTokenSecret = authTokenSecret;
        mContext = context;
    }

    public List<DownloadItem> getListSpreadsheets() {
    	InputStream is = null;
    	try {
			String headerValue = "OAuth oauth_version=\"1.0\", "
					+ "oauth_signature_method=\"PLAINTEXT\", "
					+ "oauth_consumer_key=\"" + AMEnv.DROPBOX_CONSUMER_KEY + "\", "
					+ "oauth_token=\"" + authToken + "\", "
					+ "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET + "&"
					+ authTokenSecret + "\"";

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(METADATA_ACCESS_URL);
			httpGet.setHeader("Authorization", headerValue);
			HttpResponse response = null;
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			JSONObject jsonResponse = new JSONObject(DropboxUtils.convertStreamToString(is));
			JSONArray filesJSON = jsonResponse.getJSONArray("contents");
			JSONObject entryJSON;
			List<DownloadItem> spreadsheetList = new ArrayList<DownloadItem>(); 
			for(int i = 0 ; i < filesJSON.length(); i++){
				entryJSON = filesJSON.getJSONObject(i);
				if(entryJSON.getString("path").endsWith(".db")){
				    spreadsheetList.add(new DownloadItem(ItemType.Spreadsheet, entryJSON.getString("path"), entryJSON.getString("modified"),  ""));
				}
			}
			
			return spreadsheetList;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
			    if(is != null){
			        is.close();
			    }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			
		}
    	return null;
    }
    
   

    public String downloadSpreadsheetToDB(DownloadItem di) throws Exception {

        String url= DOWNLOAD_URL + di.getTitle();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = null;
        response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
        String saveDBPath= AMEnv.DEFAULT_ROOT_PATH  + di.getTitle();
        File f = new File(saveDBPath);
        DropboxUtils.convertStreamToFile(is, f);
        return saveDBPath;
    }
}
