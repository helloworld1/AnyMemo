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
package org.liberty.android.fantastischmemo;

import java.util.ArrayList;
import java.util.Stack;
import java.net.URLEncoder;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.os.Bundle;
import android.content.Context;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.util.Log;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloaderAnyMemo extends DownloaderBase{
    private static final String TAG = "org.liberty.android.fantastischmemo.DownloaderAnyMemo";
    private DownloadListAdapter dlAdapter;
    /* 
     * dlStack caches the previous result so user can press 
     * back button to go back
     */
    private Stack<ArrayList<DownloadItem>> dlStack;

    @Override
    protected void initialRetrieve(){
        dlAdapter = new DownloadListAdapter(this, R.layout.filebrowser_item);
        ListView listView = (ListView)findViewById(R.id.file_list);
        listView.setAdapter(dlAdapter);
        DownloadItem dItem = new DownloadItem(1, "hello", "world", "");
        DownloadItem dItem2 = new DownloadItem(2, "hello2", "world", "");
        //dlAdapter.add(dItem);
        //dlAdapter.add(dItem2);
        try{
            dlAdapter.addList(obtainCategories());
        }
        catch(Exception e){
            Log.e(TAG, "Error obtaining categories", e);

        }



    }

    @Override
    protected void openCategory(DownloadItem di){
        try{
            ArrayList<DownloadItem> list = obtainDatabases(di);
            dlAdapter.clear();
            dlAdapter.addList(list);
        }
        catch(Exception e){
            Log.e("TAG", "Fail to obtain databases", e);
        }
    }

    @Override
    protected DownloadItem getDownloadItem(int position){
        return dlAdapter.getItem(position);
    }

    @Override
    protected void goBack(){
        finish();
    }

    @Override
    protected void fetchDatabase(DownloadItem di){
    }


    private ArrayList<DownloadItem> obtainCategories() throws Exception{
        ArrayList<DownloadItem> categoryList = new ArrayList<DownloadItem>();
        HttpClient httpclient = new DefaultHttpClient();
        String url = getString(R.string.website_json);
        url += "?action=getcategory";
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        response = httpclient.execute(httpget);
        //Log.i(TAG, "Response: " + response.getStatusLine().toString());
        HttpEntity entity = response.getEntity();

        if(entity != null){
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
           // Log.i(TAG, "RESULT" + result);

            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonItem = jsonArray.getJSONObject(i);
                String dbcategory = jsonItem.getString("DBCategory");
                DownloadItem di = new DownloadItem();
                di.setType(DownloadItem.TYPE_CATEGORY);
                di.setTitle(dbcategory);
                di.setAddress(getString(R.string.website_json) + "?action=getdb&category=" + URLEncoder.encode(dbcategory));
                categoryList.add(di);
            }



            instream.close();
        }
        else{
            throw new Exception("Http Entity is null");
        }
        return categoryList;
    }

    private ArrayList<DownloadItem> obtainDatabases(DownloadItem category) throws Exception{
        ArrayList<DownloadItem> databaseList = new ArrayList<DownloadItem>();
        HttpClient httpclient = new DefaultHttpClient();
        String url = category.getAddress();
        Log.v(TAG, "URL: " + url);
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        response = httpclient.execute(httpget);
        //Log.i(TAG, "Response: " + response.getStatusLine().toString());
        HttpEntity entity = response.getEntity();

        if(entity != null){
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
           // Log.i(TAG, "RESULT" + result);

            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonItem = jsonArray.getJSONObject(i);
                String dbname = jsonItem.getString("DBName");
                String dbnote = jsonItem.getString("DBNote");
                String dbcategory = jsonItem.getString("DBCategory");
                String filename = jsonItem.getString("FileName");
                DownloadItem di = new DownloadItem();
                di.setType(DownloadItem.TYPE_DATABASE);
                di.setTitle(dbname);
                di.setDescription(dbnote);
                di.setAddress(getString(R.string.website_download_head) + "?action=getdb&category=" + URLEncoder.encode(filename));
                di.setExtras("filename", filename);
                databaseList.add(di);
            }

            instream.close();
        }
        else{
            throw new Exception("Http Entity is null");
        }
        return databaseList;
    }



}
