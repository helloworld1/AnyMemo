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
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import android.content.Context;

import com.google.inject.assistedinject.Assisted;
public class DropboxDownloadHelper {

    private final String authToken;
    private final String authTokenSecret;

    private static final String METADATA_ACCESS_URL = "https://api.dropbox.com/1/metadata/dropbox/anymemo?list=true";
    private static final String DOWNLOAD_URL = "https://api-content.dropbox.com/1/files/dropbox/anymemo/";

    private final RecentListUtil recentListUtil;

    private AMFileUtil amFileUtil;

    @Inject
    public DropboxDownloadHelper(Context context, RecentListUtil recentListUtil,
            @Assisted("authToken") String authToken, @Assisted("authTokenSecret") String authTokenSecret) {
        this.authToken = authToken;
        this.authTokenSecret = authTokenSecret;
        this.recentListUtil = recentListUtil;
    }

    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    // Fetch the list of db files
    public List<DownloadItem> fetchDBFileList() throws ClientProtocolException, IOException, JSONException {
        List<DownloadItem> dbFileList = new ArrayList<DownloadItem>();

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(METADATA_ACCESS_URL);
        httpGet.setHeader("Authorization", DropboxUtils.getFileExchangeAuthHeader(authToken, authTokenSecret));
        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();

        if(statusCode == 200 ){
            InputStream is = response.getEntity().getContent();
            JSONObject jsonResponse = new JSONObject(IOUtils.toString(is));
            JSONArray fileList = jsonResponse.getJSONArray("contents");
            JSONObject file;
            File filePath;
            for(int i = 0 ; i < fileList.length(); i++){
                file = fileList.getJSONObject(i);
                if(file.getString("path").endsWith(".db")){
                    filePath = new File(file.getString("path"));
                    dbFileList.add(new DownloadItem(DownloadItem.ItemType.Database, filePath.getName(), file.getString("modified"),  ""));
                }
            }
            is.close();
        } else {
            throw new IOException("Error fetching file list. Get status code: " + statusCode);
        }

        return dbFileList;
    }


    public String downloadDBFromDropbox(DownloadItem di) throws ClientProtocolException, IOException  {
        String saveDBPath = AMEnv.DEFAULT_ROOT_PATH  + new File(di.getTitle()).getName();

        // Back up and delete db if it exists.
        if (new File(saveDBPath).exists()) {
            amFileUtil.deleteFileWithBackup(saveDBPath);
        }

        // Make sure the space is translated correctly.
        // application/x-www-form-urlencoded specified that
        // The space character " " is converted into a plus sign "+".
        // However dropbox need %20.
        String url = DOWNLOAD_URL + URLEncoder.encode(di.getTitle(), "UTF-8").replace("+", "%20");

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", DropboxUtils.getFileExchangeAuthHeader(authToken, authTokenSecret));
        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200) {
            InputStream is = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(is, new File(saveDBPath));
            recentListUtil.addToRecentList(saveDBPath);
            is.close();
            return saveDBPath;
        } if (statusCode == 404) {
            throw new IOException("Could not found requested file. Get status code: " + statusCode);
        } else {
            throw new IOException("Error Downloading file. Get status code: " + statusCode);
        }
    }
}
