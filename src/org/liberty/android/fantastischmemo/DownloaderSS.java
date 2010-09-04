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
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.net.URLEncoder;
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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * This is the downloader class for StudyStack
 */
public class DownloaderSS extends DownloaderBase{
    private static final String TAG = "org.liberty.android.fantastischmemo.DownloaderSS";
    private static final String SS_API_GET_DECK = "http://www.studystack.com/servlet/json?studyStackId=";
    private static final String SS_API_GET_CATEGORIES = "http://www.studystack.com/servlet/categoryListJson";
    private static final String SS_API_GET_CATEGORY_CONTENT = "http://www.studystack.com/servlet/categoryStackListJson?sortOrder=stars&categoryId=";
    private DownloadListAdapter dlAdapter;
    private Stack<List<DownloadItem>> dlStack;
    private ListView listView;
    private ProgressDialog mProgressDialog;
    private int mDownloadProgress;
    private Handler mHandler;
    private List<DownloadItem> categoryList = null;

    @Override
    protected void initialRetrieve(){
        dlAdapter = new DownloadListAdapter(this, R.layout.filebrowser_item);
        dlStack = new Stack<List<DownloadItem>>();
        mHandler = new Handler();
        listView = (ListView)findViewById(R.id.file_list);
        listView.setAdapter(dlAdapter);
        try{
            categoryList = retrieveCategories();
        }
        catch(Exception e){
            Log.e(TAG, "Error retrieveing categories", e);
        }
        showRootCategories();

    }

    protected void openCategory(DownloadItem di){
        dlStack.push(dlAdapter.getList());
        dlAdapter.clear();
        for(DownloadItem i : categoryList){
            if(i.getExtras("pid").equals(di.getExtras("id"))){
                dlAdapter.add(i);
            }
        }
        try{
            List<DownloadItem> databaseList = retrieveDatabaseList(di);
            dlAdapter.addList(databaseList);
        }
        catch(Exception e){
            Log.e(TAG, "Error retrieving the database list", e);
        }
    }

    protected DownloadItem getDownloadItem(int position){
        return dlAdapter.getItem(position);
    }

    protected void goBack(){
        if(dlStack == null || dlStack.empty()){
            finish();
        }
        else{
            dlAdapter.clear();
            dlAdapter.addList(dlStack.pop());
            listView.setSelection(0);
        }
    }

    protected void fetchDatabase(DownloadItem di){
    }

    private List<DownloadItem> retrieveCategories() throws Exception{
        List<DownloadItem> diList = new LinkedList<DownloadItem>();
        JSONArray jsonArray = getJSONArray(SS_API_GET_CATEGORIES);
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            Log.v(TAG, jsonItem.getString("name"));
            Log.v(TAG, "" + jsonItem.getInt("id"));
            Log.v(TAG, "" + jsonItem.getInt("parentId"));
            DownloadItem di = new DownloadItem();
            di.setType(DownloadItem.TYPE_CATEGORY);
            di.setTitle(jsonItem.getString("name"));
            di.setExtras("id", jsonItem.getString("id"));
            di.setExtras("pid", jsonItem.getString("parentId"));
            if(di.getTitle() != null){
                diList.add(di);
            }
        }
        return diList;
    }

    private List<DownloadItem> retrieveDatabaseList(DownloadItem category) throws Exception{
        List<DownloadItem> diList = new LinkedList<DownloadItem>();
        String url = SS_API_GET_CATEGORY_CONTENT + category.getExtras("id");

        JSONArray jsonArray = getJSONArray(url);
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            DownloadItem di = new DownloadItem();
            di.setType(DownloadItem.TYPE_DATABASE);
            di.setTitle(jsonItem.getString("stackName"));
            di.setDescription(jsonItem.getString("description"));
            di.setExtras("id", jsonItem.getString("id"));
            if(di.getTitle() != null){
                diList.add(di);
            }
        }
        return diList;
    }
    
    private JSONArray getJSONArray(String url) throws Exception{
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
        Log.i(TAG, "RESULT" + result);

        return new JSONArray(result);
    }

    private void showRootCategories(){
        if(categoryList == null){
            return;
        }
        for(DownloadItem di : categoryList){
            if(di.getExtras("pid").equals("0")){
                dlAdapter.add(di);
            }
        }
    }
}

