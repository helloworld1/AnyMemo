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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMEnv;

import org.liberty.android.fantastischmemo.downloader.DownloadItem;
import org.liberty.android.fantastischmemo.downloader.DownloadItem.ItemType;
import org.liberty.android.fantastischmemo.downloader.google.Spreadsheet;
import org.liberty.android.fantastischmemo.downloader.google.WorksheetFactory;


import android.content.Context;
import android.util.Log;

public class DropboxDownloadHelper {
    private Context mContext;

    private final String authToken;
    private final String authTokenSecret;
    
    private final String DATA_ACCESS_URL = "https://api.dropbox.com/1/metadata/dropbox/?list=true";

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
			HttpGet httpGet = new HttpGet(DATA_ACCESS_URL);
			httpGet.setHeader("Authorization", headerValue);
			HttpResponse response = null;
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			JSONObject jsonResponse = new JSONObject(convertStreamToString(is));
			JSONArray filesJSON = jsonResponse.getJSONArray("contents");
			JSONObject entryJSON;
			List<DownloadItem> spreadsheetList = new ArrayList<DownloadItem>(); 
			for(int i = 0 ; i < filesJSON.length(); i++){
				entryJSON = filesJSON.getJSONObject(i);
				if(entryJSON.getString("path").endsWith(".db")){
				    spreadsheetList.add(new DownloadItem(ItemType.Spreadsheet, entryJSON.getString("path"), entryJSON.getString("modified"),  ""));
				}
			}
			
			Log.v("xinxin *************done parse json", "done");
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
    
    
    public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
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
    
    private static void convertStreamToFile(InputStream is, File f) {
        try {
            // write the inputStream to a FileOutputStream
            OutputStream out = new FileOutputStream(f);
         
            int read = 0;
            byte[] bytes = new byte[1024];
         
            while ((read = is.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
         
            is.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String downloadSpreadsheetToDB(DownloadItem di) throws Exception {
        
        String downloadURL = di.getAddress();
        

        String url="https://api-content.dropbox.com/1/files/dropbox/"+ di.getTitle();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = null;
        response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
        String saveDBPath= AMEnv.DEFAULT_ROOT_PATH + "/" + di.getTitle();
        File f = new File(saveDBPath);
        convertStreamToFile(is, f);
        return saveDBPath;
    }
}
