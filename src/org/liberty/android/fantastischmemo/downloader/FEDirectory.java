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

import org.liberty.android.fantastischmemo.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import android.os.Bundle;
import android.content.Context;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
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

/* Show FlashcardExchange's directory */
public class FEDirectory extends DownloaderBase{
    private DownloadListAdapter dlAdapter;
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderFE";
    private static final String FE_API_KEY = "anymemo_android";

    private static final String FE_API_DIRECTORY = "http://api.flashcardexchange.com/v1/get_directory?api_key=" + FE_API_KEY;
    @Override
    protected void initialRetrieve(){
        dlAdapter = new DownloadListAdapter(this, R.layout.filebrowser_item);
        ListView listView = (ListView)findViewById(R.id.file_list);
        listView.setAdapter(dlAdapter);

        AMGUIUtility.doProgressTask(this, R.string.loading_please_wait, R.string.loading_connect_net, new AMGUIUtility.ProgressTask(){
            private List<DownloadItem> dil;
            public void doHeavyTask() throws Exception{
                dil = retrieveList();
            }
            public void doUITask(){
                dlAdapter.addList(dil);
            }
        });
    }

    @Override
    protected void openCategory(DownloadItem di){
        String tag = di.getTitle();
        Intent myIntent = new Intent(this, DownloaderFE.class);
        myIntent.setAction(DownloaderFE.INTENT_ACTION_SEARCH_TAG);
        myIntent.putExtra("search_criterion", tag);
        startActivity(myIntent);
    }

    @Override
    protected DownloadItem getDownloadItem(int position){
        return dlAdapter.getItem(position);
    }

    @Override
    protected void goBack(){
        /* Just go back, do nothing */
        finish();
    }

    @Override
    protected void fetchDatabase(DownloadItem di){
        throw new AssertionError("Should not call this method");
    }

    private List<DownloadItem> retrieveList() throws Exception{
        List<DownloadItem> diList = new ArrayList<DownloadItem>();
        String url = FE_API_DIRECTORY;
        String jsonString = DownloaderUtils.downloadJSONString(url);
        JSONObject jsonObject = new JSONObject(jsonString);
        String status =  jsonObject.getString("response_type");
        Log.v(TAG, "JSON String: " + jsonString);
        if(!status.equals("ok")){
            Log.v(TAG, "JSON String: " + jsonString);
            throw new IOException("Status is not OK. Status: " + status);
        }
        JSONArray directoryArray = jsonObject.getJSONArray("results");
        /* 
         * Each result has tags which is an array containing 
         * tags and a string of tag group title
         */
        for(int i = 0; i < directoryArray.length(); i++){
            JSONArray tagsArray = directoryArray.getJSONObject(i).getJSONArray("tags");
            for(int j = 0; j < tagsArray.length(); j++){
                JSONObject jsonItem = tagsArray.getJSONObject(j);
                String s = jsonItem.getString("tag");
                DownloadItem di = new DownloadItem(DownloadItem.TYPE_DATABASE, s, "", "");
                di.setType(DownloadItem.TYPE_CATEGORY);
                diList.add(di);
            }
        }
        return diList;
    }

}
