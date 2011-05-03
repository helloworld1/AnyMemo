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

import oauth.signpost.*;

import oauth.signpost.basic.DefaultOAuthConsumer;

public class FEUpload extends AMActivity{
    private static final int FILE_BROWSER = 6;
    private static final String FE_API_KEY = "anymemo_android";
    private static final String FE_API_ADD_CARDSET= "http://api.flashcardexchange.com/v1/add_card_set" + "?api_key=" + FE_API_KEY;
    private static final String FE_API_ADD_CARD= "http://api.flashcardexchange.com/v1/add_card?api_key=" + FE_API_KEY;
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.FEUpload";
    private String oauthToken= null;
    private String oauthTokenSecret = null;
    private OAuthConsumer consumer;

	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            oauthToken = extras.getString("oauth_token");
            oauthTokenSecret = extras.getString("oauth_token_secret");
            consumer = new DefaultOAuthConsumer(FEOauth.CONSUMER_KEY, FEOauth.CONSUMER_SECRET);
            consumer.setTokenWithSecret(oauthToken, oauthTokenSecret);
            Intent myIntent = new Intent(this, FileBrowser.class);
            startActivityForResult(myIntent, FILE_BROWSER);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Result: " + requestCode + " " + resultCode + " " + data);
        if(resultCode == Activity.RESULT_OK){
            switch(requestCode){
                case FILE_BROWSER:
                {
                    Bundle resultExtras = data.getExtras();
                    if(resultExtras != null){
                        final String dbPath = resultExtras.getString("org.liberty.android.fantastischmemo.dbPath");
                        final String dbName = resultExtras.getString("org.liberty.android.fantastischmemo.dbName");
                        Intent myIntent = new Intent(this, FileBrowser.class);
                        AMGUIUtility.doProgressTask(this, R.string.loading_please_wait, R.string.upload_wait, new AMGUIUtility.ProgressTask(){
                            private String authUrl;
                            public void doHeavyTask() throws Exception{
                                uploadDB(dbPath, dbName);
                            }
                            public void doUITask(){
                                new AlertDialog.Builder(FEUpload.this)
                                    .setTitle(R.string.upload_finish)
                                    .setMessage(dbName + " " + getString(R.string.upload_finish_message))
                                    .setPositiveButton(R.string.ok_text, AMGUIUtility.getDialogFinishListener(FEUpload.this))
                                    .create()
                                    .show();
                            }
                        });
                    }
                    break;
                }

            }
        }
    }

    private void uploadDB(String dbpath, String dbname) throws Exception{
        int cardId = addCardSet(dbname, "Import from AnyMemo");
        DatabaseHelper dbHelper = new DatabaseHelper(this, dbpath, dbname);
        /* Get all items */
        List<Item> li = dbHelper.getListItems(1, -1, 0, "");
        dbHelper.close();
        while(!li.isEmpty()){
            Item item = li.remove(0);
            String question = item.getQuestion();
            String answer = item.getAnswer();
            if(question.equals("")){
                question = "(Empty)";
            }
            if(answer.equals("")){
                answer = "(Empty)";
            }
            addCard(cardId, question, answer);
        }
    }

    private int addCardSet(String title, String description) throws Exception{
        String urlTitle = URLEncoder.encode(title);
        String urlDescription = URLEncoder.encode(description);
        String url = FE_API_ADD_CARDSET + "&title="+ urlTitle + "&tags=" + urlTitle + "&description=" + urlDescription + "&private=false&oauth_token_secret=" + oauthTokenSecret+ "&oauth_token=" + oauthToken;
        url = consumer.sign(url);
        String jsonString = DownloaderUtils.downloadJSONString(url);
        Log.v(TAG, "Request url: " + url);
        Log.v(TAG, jsonString);
        JSONObject rootObject = new JSONObject(jsonString);
        String status = rootObject.getString("response_type");
        if(!status.equals("ok")){
            throw new IOException("Status is not OK. Status: " + status);
        }
        JSONObject resultObject=  rootObject.getJSONObject("results");
        int cardSetId = resultObject.getInt("card_set_id");
        return cardSetId;
    }

    private void addCard(int cardSetId, String question, String answer) throws Exception{
        String url = FE_API_ADD_CARD + "&card_set_id=" + cardSetId + "&question=" + URLEncoder.encode(question) + "&answer=" + URLEncoder.encode(answer) + "&oauth_token_secret=" + oauthTokenSecret + "&oauth_token=" + oauthToken;
        Log.v(TAG, "Request url_old: " + url);
        url = consumer.sign(url);
        Log.v(TAG, "Request url_signed: " + url);

        String jsonString = DownloaderUtils.downloadJSONString(url);
        JSONObject rootObject = new JSONObject(jsonString);
        String status = rootObject.getString("response_type");
        if(!status.equals("ok")){
            Log.v(TAG, jsonString);
            throw new IOException("Adding card is not OK. Status: " + status);
        }
    }
}
