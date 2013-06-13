/*
Copyright (C) 2010 Haowen Ning

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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import android.content.Context;
import android.util.Log;

public class DownloaderUtils {
    public static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderUtils";


    private AMFileUtil amFileUtil;

    @Inject
    public DownloaderUtils(Context context) {
    }


    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    public String downloadJSONString(String url) throws Exception{
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        response = httpclient.execute(httpget);
        Log.i(TAG, "Response: " + response.getStatusLine().toString());
        HttpEntity entity = response.getEntity();

        if(entity == null){
            throw new NullPointerException("Null entity error");
        }

        InputStream instream = entity.getContent();
        // Now convert stream to string
        BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
        StringBuilder sb = new StringBuilder();
        String line = null;
        String result = null;
        while((line = reader.readLine()) != null){
            sb.append(line + "\n");
        }
        result = sb.toString();

        return result;
    }

    public File downloadFile(String url, String savedPath) throws IOException{
        File outFile = new File(savedPath);
        OutputStream out;

        // Delete and backup if the file exists
        amFileUtil.deleteFileWithBackup(savedPath);

        // Create the dir if necessary
        File parentDir = outFile.getParentFile();
        parentDir.mkdirs();

        outFile.createNewFile();
        out  =new FileOutputStream(outFile);

        Log.i(TAG, "URL to download is: " + url);
        URL myURL = new URL(url);
        URLConnection ucon = myURL.openConnection();
        byte[] buf = new byte[8192];

        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is, 8192);
        int len = 0;
        while((len = bis.read(buf)) != -1){
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        return outFile;
    }

    public boolean validateEmail(String testString){
        Pattern p = Pattern.compile("^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Za-z]{2,4}$");
        Matcher m = p.matcher(testString);
        return m.matches();
    }

    /* Return a valid dbname from original name */
    public String validateDBName(String orngName){
        String s1 = orngName.replaceAll("[/:|]", "_");
        return s1;
    }

    /*
     * Read the HTTPResponse, return the string content
     */
    public String readResponse(HttpResponse response) throws IOException {
        HttpEntity ent = response.getEntity();
        if (ent != null) {
            BufferedReader in = new BufferedReader(new InputStreamReader(ent.getContent()), 8192);
            String inputLine = null;
            String result = "";

            while((inputLine = in.readLine()) != null) {
                result += inputLine;
            }

            response.getEntity().consumeContent();
            return result;
        } else {
            return "";
        }
    }

    /* https://spreadsheets.google.com/feeds/list/key/worksheetId/private/full/rowId -> rowId */
    public String getLastPartFromUrl(final String url){
        String id = url.replaceFirst(".*/([^/?]+).*", "$1");
        //if (id.contains("%3A")) {
        //	String[] parts = id.split("%3A");
        //	id = parts[parts.length - 1];
        //}
        return id;
    }
}
