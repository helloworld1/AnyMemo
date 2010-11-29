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

/*
 * Download from FlashcardExchange using its web api
 */
public class DownloaderFE extends DownloaderBase{
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderFE";
    private static final String FE_API_EMAIL = "http://xml.flashcardexchange.com/android/email/";
    private static final String FE_API_TAG = "http://xml.flashcardexchange.com/android/tag/";
    private static final String FE_API_FLASHCARDS = "http://xml.flashcardexchange.com/android/flashcards/";
    private DownloadListAdapter dlAdapter;
    /* 
     * dlStack caches the previous result so user can press 
     * back button to go back
     */
    private ListView listView;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;

    @Override
    protected void initialRetrieve(){
        mHandler = new Handler();
        dlAdapter = new DownloadListAdapter(this, R.layout.filebrowser_item);
        listView = (ListView)findViewById(R.id.file_list);
        listView.setAdapter(dlAdapter);
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();
        String prevInput = settings.getString("prev_fe_input", "");

        final EditText input = new EditText(this);
        input.setText(prevInput);
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.fe_search_title))
            .setMessage(getString(R.string.fe_search_message))
            .setView(input)
            .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which ){
                final String value = input.getText().toString();
                editor.putString("prev_fe_input", value);
                editor.commit();
                mProgressDialog = ProgressDialog.show(DownloaderFE.this, getString(R.string.loading_please_wait), getString(R.string.loading_connect_net), true, true, new DialogInterface.OnCancelListener(){
                    @Override
                    public void onCancel(DialogInterface dialog){
                        finish();
                    }
                });
                new Thread(){
                    public void run(){
                        try{
                            final List<DownloadItem> dil = retrieveList(value);
                            mHandler.post(new Runnable(){
                                public void run(){
                                    dlAdapter.addList(dil);
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                        catch(final Exception e){
                            mHandler.post(new Runnable(){
                                public void run(){
                                    mProgressDialog.dismiss();
                                    new AlertDialog.Builder(DownloaderFE.this)
                                        .setTitle(R.string.downloader_connection_error)
                                        .setMessage(getString(R.string.downloader_connection_error_message) + e.toString())
                                        .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which ){
                                                finish();
                                            }
                                        })
                                        .create()
                                        .show();
                                }
                            });
                        }

                    }
                }.start();

                
            }
        })
        .setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which ){
                finish();
            }
        })
        .setOnCancelListener(new DialogInterface.OnCancelListener(){
            public void onCancel(DialogInterface dialog){
                finish();
            }
        })
        .create()
        .show();
    }
    @Override
    protected void openCategory(DownloadItem di){
        /* No category for FlashcardExchange */
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
    protected void fetchDatabase(final DownloadItem di){
        View alertView = View.inflate(this, R.layout.link_alert, null);
        TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(getString(R.string.downloader_download_alert_message) + di.getDescription()));

        new AlertDialog.Builder(this)
            .setView(alertView)
            .setTitle(getString(R.string.downloader_download_alert) + di.getTitle())
            .setPositiveButton(getString(R.string.yes_text), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1){
                    mProgressDialog = ProgressDialog.show(DownloaderFE.this, getString(R.string.loading_please_wait), getString(R.string.loading_downloading));
                    new Thread(){
                        public void run(){
                            try{
                                downloadDatabase(di);
                                mHandler.post(new Runnable(){
                                    public void run(){
                                        mProgressDialog.dismiss();
                                        String dbpath = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_dir);
                                        new AlertDialog.Builder(DownloaderFE.this)
                                            .setTitle(R.string.downloader_download_success)
                                            .setMessage(getString(R.string.downloader_download_success_message) + dbpath + di.getTitle() + ".db")
                                            .setPositiveButton(R.string.ok_text, null)
                                            .create()
                                            .show();
                                    }
                                });

                            }
                            catch(final Exception e){
                                Log.e(TAG, "Error downloading", e);
                                mHandler.post(new Runnable(){
                                    public void run(){
                                        mProgressDialog.dismiss();
                                        new AlertDialog.Builder(DownloaderFE.this)
                                            .setTitle(R.string.downloader_download_fail)
                                            .setMessage(getString(R.string.downloader_download_fail_message) + " " + e.toString())
                                            .setPositiveButton(R.string.ok_text, null)
                                            .create()
                                            .show();
                                    }
                                });
                            }
                        }
                    }.start();
                }
            })
            .setNegativeButton(getString(R.string.no_text), null)
            .show();
    }

    private List<DownloadItem> retrieveList(String criterion) throws Exception{
        HttpClient httpclient = new DefaultHttpClient();
        String url;
        if(validateEmail(criterion)){
            url = FE_API_EMAIL;
        }
        else{
            url = FE_API_TAG;
        }
        url += URLEncoder.encode(criterion);
        Log.i(TAG, "Url: " + url);
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        response = httpclient.execute(httpget);
        Log.i(TAG, "Response: " + response.getStatusLine().toString());
        HttpEntity entity = response.getEntity();

        if(entity == null){
            throw new Exception("Null entity error");
        }

        InputStream instream = entity.getContent();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document doc = documentBuilder.parse(instream);
        NodeList nodes = doc.getElementsByTagName("cardSet");
        nodes = doc.getElementsByTagName("error");

        if(nodes.getLength() > 0){
            throw new Exception("Invalid user name or no such tag to search!");
        }

        List<DownloadItem> diList = new LinkedList<DownloadItem>();
        nodes = doc.getElementsByTagName("cardSet");
            

        int nodeNumber = nodes.getLength();
        for(int i = 0;i < nodeNumber; i++){
            Node node = nodes.item(i);
            if(!node.hasChildNodes()){
                continue;
            }
            NodeList childNodes = node.getChildNodes();

            int childNodeNumber = childNodes.getLength();
            DownloadItem di = new DownloadItem();
            for(int j = 0; j < childNodeNumber; j++){
                Node childNode = childNodes.item(j);
                if(childNode.hasChildNodes()){
                    di.setType(DownloadItem.TYPE_DATABASE);
                    if(childNode.getNodeName().equals("title")){
                        di.setTitle(childNode.getFirstChild().getNodeValue());
                    }
                    else if(childNode.getNodeName().equals("cardSetId")){
                        di.setAddress(FE_API_FLASHCARDS + childNode.getFirstChild().getNodeValue());
                    }
                    else if(childNode.getNodeName().equals("description")){
                        di.setDescription(childNode.getFirstChild().getNodeValue());
                    }
                }
            }
            if(!di.getTitle().equals("")){
                diList.add(di);
            }
        }
        instream.close();
        return diList;
    }

    private void downloadDatabase(DownloadItem di) throws Exception{
        HttpClient httpclient = new DefaultHttpClient();
        String url = di.getAddress();
        Log.i(TAG, "Url: " + url);
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        response = httpclient.execute(httpget);
        Log.i(TAG, "Response: " + response.getStatusLine().toString());
        HttpEntity entity = response.getEntity();

        if(entity == null){
            throw new Exception("Null entity error");
        }

        InputStream instream = entity.getContent();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document doc = documentBuilder.parse(instream);
        NodeList nodes = doc.getElementsByTagName("cardSet");
        nodes = doc.getElementsByTagName("error");

        if(nodes.getLength() > 0){
            throw new Exception("Invalid ID to retrieve the database!");
        }

        List<Item> itemList = new LinkedList<Item>();
        nodes = doc.getElementsByTagName("flashcard");

        int nodeNumber = nodes.getLength();
        for(int i = 0;i < nodeNumber; i++){
            Node node = nodes.item(i);
            if(!node.hasChildNodes()){
                continue;
            }
            NodeList childNodes = node.getChildNodes();

            int childNodeNumber = childNodes.getLength();
            Item item = new Item();
            item.setId(i + 1);
            for(int j = 0; j < childNodeNumber; j++){
                Node childNode = childNodes.item(j);
                if(childNode.hasChildNodes()){
                    if(childNode.getNodeName().equals("question")){
                        item.setQuestion(childNode.getFirstChild().getNodeValue());
                    }
                    else if(childNode.getNodeName().equals("answer")){
                        item.setAnswer(childNode.getFirstChild().getNodeValue());
                    }
                }
            }
            if(!item.getQuestion().equals("")){
                itemList.add(item);
            }
        }
        instream.close();
        /* Now create the databased from the itemList */

        String dbname = di.getTitle() + ".db";
        /* Replace illegal characters */
        dbname = dbname.replaceAll("[`~!#@%&*{};:'\"]", "_");
        String dbpath = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_dir);
        DatabaseHelper.createEmptyDatabase(dbpath, dbname);
        DatabaseHelper dbHelper = new DatabaseHelper(this, dbpath, dbname);
        dbHelper.insertListItems(itemList);
        dbHelper.close();
    }

    private boolean validateEmail(String testString){
        Pattern p = Pattern.compile("^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Za-z]{2,4}$");
        Matcher m = p.matcher(testString);
        return m.matches();
    }

}
