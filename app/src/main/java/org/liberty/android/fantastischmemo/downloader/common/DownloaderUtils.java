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
package org.liberty.android.fantastischmemo.downloader.common;

import android.util.Log;

import org.liberty.android.fantastischmemo.modules.PerApplication;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@PerApplication
public class DownloaderUtils {
    public static final String TAG = DownloaderUtils.class.getSimpleName();

    private final OkHttpClient httpClient;

    private final AMFileUtil amFileUtil;

    @Inject
    public DownloaderUtils(OkHttpClient httpClient, AMFileUtil amFileUtil) {
        this.httpClient = httpClient;
        this.amFileUtil = amFileUtil;
    }

    public String downloadJSONString(String url) throws Exception{
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();

        return response.body().string();
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
