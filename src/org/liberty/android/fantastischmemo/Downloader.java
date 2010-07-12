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

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;
import android.util.Log;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Downloader extends Activity implements OnItemClickListener{
    private TextView topText;
    private Handler mHandler;
    private ArrayList<HashMap<String, String>> mDatabaseList;
    private ArrayList<HashMap<String, String>> mFilterDatabaseList;
    private ArrayList<String> categoryArray;
    private ArrayList<String> databaseArray;
    private ListView mListView;
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private AlertDialog alertDialog;
    private Thread downloadThread;
    private int mDownloadProgress = 0;
            

    /* mStage = 1: category selection
     * mStage = 2: database selection
     */ 
    private int mStage = 1;
    private int clickPosition;
    private boolean downloadStatus;
    public final static String TAG = "org.liberty.android.fantastischmemo.Downloader";
    
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloader);
        mDatabaseList = new ArrayList<HashMap<String, String>>();
        mHandler = new Handler();
        mProgressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_connect_net), true, true, new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                finish();
            }
        });

        topText = (TextView)findViewById(R.id.downloader_top_text);
        mListView = (ListView)findViewById(R.id.downloader_list);
        mContext = this;


        Thread connectThread = new Thread(){
            @Override
            public void run(){
                if(obtainJSON(getString(R.string.website_json))){
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            refreshCategoryList();
                            mProgressDialog.dismiss();
                        }
                    });

                }
                else{
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            mProgressDialog.dismiss();
                            DialogInterface.OnClickListener exitButtonListener = new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface arg0, int arg1){
                                    finish();
                                }
                            };
                            alertDialog = new AlertDialog.Builder(mContext).create();
                            alertDialog.setTitle(getString(R.string.downloader_connection_error));
                            alertDialog.setMessage(getString(R.string.downloader_connection_error_message));
                            alertDialog.setButton(getString(R.string.back_menu_text), exitButtonListener);
                            alertDialog.show();
                        }
                    });
                }
            }

        };
        connectThread.start();
        
    }

    private void refreshCategoryList(){
        mStage = 1;
        Set<String> categorySet = new TreeSet<String>();
        categoryArray = new ArrayList<String>();
        for(HashMap<String, String> hm : mDatabaseList){
            categorySet.add(hm.get("DBCategory"));
        }
        for(String s : categorySet){
            categoryArray.add(s);
        }
        mListView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryArray));
        mListView.setOnItemClickListener(this);
        topText.setText(getString(R.string.downloader_categories));
    }

    private void refreshDatabaseList(String category){
        mStage = 2;
        databaseArray = new ArrayList<String>();
        mFilterDatabaseList = new ArrayList<HashMap<String, String>>();

        for(HashMap<String, String> hm : mDatabaseList){
            if(hm.get("DBCategory").equals(category)){
                mFilterDatabaseList.add(hm);
                databaseArray.add(hm.get("DBName"));
            }
        }

        mListView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, databaseArray));
        mListView.setOnItemClickListener(this);
        topText.setText(getString(R.string.downloader_databases));
    }


    private boolean obtainJSON(String url){
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        try{
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
                    String dbtype = jsonItem.getString("DBType");
                    String dbnote = jsonItem.getString("DBNote");
                    String dbcategory = jsonItem.getString("DBCategory");
                    String filename = jsonItem.getString("FileName");
                    HashMap<String, String> hm = new HashMap<String, String>();
                    hm.put("DBName", dbname);
                    hm.put("DBType", dbtype);
                    hm.put("DBNote", dbnote);
                    hm.put("DBCategory", dbcategory);
                    hm.put("FileName", filename);
                    mDatabaseList.add(hm);

                }



                instream.close();
                return true;
            }
            else{
            Log.e(TAG, "Empty entity");
                return false;
            }
        }
        catch(Exception e){
            Log.e(TAG, "Error connect", e);
            return false;
        }
    }

    private void downloadFile(String url, String filename) throws Exception{
        String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_dir);
        File outFile = new File(sdpath + filename);
        mHandler.post(new Runnable(){
            public void run(){
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMessage(getString(R.string.loading_downloading));
                mProgressDialog.show();

            }
        });
        try{
            OutputStream out;
            if(outFile.exists()){
                throw new IOException("Database already exist!");
            }
            try{
                outFile.createNewFile();
                out  =new FileOutputStream(outFile);

                URL myURL = new URL(url);
                URLConnection ucon = myURL.openConnection();
                final int fileSize = ucon.getContentLength();
                mDownloadProgress = 0;
                mHandler.post(new Runnable(){
                    public void run(){
                        mProgressDialog.setMax(fileSize);
                    }
                });

                byte[] buf = new byte[8192];

                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is, 8192);
                Runnable increaseProgress = new Runnable(){
                    public void run(){
                        mProgressDialog.setProgress(mDownloadProgress);
                    }
                };
                int len = 0;
                int lenSum = 0;
                while((len = bis.read(buf)) != -1){
                    out.write(buf, 0, len);
                    lenSum += len;
                    if(lenSum > fileSize / 50){
                        /* This is tricky.
                         * The UI thread can not be updated too often.
                         * So we update it only 50 times
                         */
                        mDownloadProgress += lenSum;
                        lenSum = 0;
                        mHandler.post(increaseProgress);
                    }
                }
                out.close();
                is.close();
                /* Uncompress the zip file that contains images */
                if(filename.endsWith(".zip")){
                    mHandler.post(new Runnable(){
                        public void run(){
                            mProgressDialog.setProgress(fileSize);
                            mProgressDialog.setMessage(getString(R.string.downloader_extract_zip));
                        }
                    });

                    BufferedOutputStream dest = null;
                    BufferedInputStream ins = null;
                    ZipEntry entry;
                    ZipFile zipfile = new ZipFile(outFile);
                    Enumeration<?> e = zipfile.entries();
                    while(e.hasMoreElements()) {
                        entry = (ZipEntry) e.nextElement();
                        Log.v(TAG, "Extracting: " +entry);
                        if(entry.isDirectory()){
                            new File(sdpath + "/" + entry.getName()).mkdir();
                        }
                        else{
                            ins = new BufferedInputStream
                                (zipfile.getInputStream(entry), 8192);
                            int count;
                            byte data[] = new byte[8192];
                            FileOutputStream fos = new FileOutputStream(sdpath + "/" + entry.getName());
                            dest = new BufferedOutputStream(fos, 8192);
                            while ((count = ins.read(data, 0, 8192)) != -1) {
                                dest.write(data, 0, count);
                            }
                            dest.flush();
                            dest.close();
                            ins.close();
                        }
                    }
                    /* Delete the zip file if it is successfully decompressed */
                    outFile.delete();
                }
                /* Check if the db is correct */
                filename = filename.replace(".zip", ".db");
                DatabaseHelper dh = new DatabaseHelper(mContext, sdpath, filename);
                dh.close();
            }
            catch(Exception e){
                if(outFile.exists()){
                    outFile.delete();
                }
                throw new Exception(e);
            }

        }
        catch(Exception e){
            Log.e(TAG, "Error downloading", e);
            throw new Exception(e);
        }
        finally{
            mHandler.post(new Runnable(){
                public void run(){
                    mProgressDialog.dismiss();
                }
            });
        }
    }



    @Override
    public void onItemClick(AdapterView<?> parentView, View childView, int position, long id){
        /* Category list */
        if(mStage == 1){
            refreshDatabaseList(categoryArray.get(position));

        }
        else if(mStage == 2){
            clickPosition = position;
            downloadThread = new Thread(){
                @Override
                public void run(){
                    String filename = mFilterDatabaseList.get(clickPosition).get("FileName");
                    try{
                        downloadFile(getString(R.string.website_download_head)+ URLEncoder.encode(filename, "UTF-8"), filename);
                        filename = filename.replace(".zip", ".db");
                        String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_dir);
                        final File dbFile = new File(sdpath + filename);
                        mHandler.post(new Runnable(){
                            public void run(){
                                new AlertDialog.Builder(Downloader.this)
                                    .setTitle(R.string.downloader_download_success)
                                    .setMessage(getString(R.string.downloader_download_success_message) + dbFile.toString())
                                    .setPositiveButton(R.string.ok_text, null)
                                    .create()
                                    .show();
                            }
                        });
                    }
                    catch(final Exception e){
                        mHandler.post(new Runnable(){
                            public void run(){
                                new AlertDialog.Builder(Downloader.this)
                                    .setTitle(R.string.downloader_download_fail)
                                    .setMessage(getString(R.string.downloader_download_fail_message) + " " + e.toString())
                                    .setPositiveButton(R.string.ok_text, null)
                                    .create()
                                    .show();
                            }
                        });
                    }
                }
            };
            View alertView = View.inflate(Downloader.this, R.layout.link_alert, null);
            TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(Html.fromHtml(getString(R.string.downloader_download_alert_message) + mFilterDatabaseList.get(clickPosition).get("DBNote")));

            new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle(getString(R.string.downloader_download_alert) + mFilterDatabaseList.get(clickPosition).get("FileName"))
                .setPositiveButton(getString(R.string.yes_text), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1){
                        downloadThread.start();
                    }
                })
                .setNegativeButton(getString(R.string.no_text), null)
                .show();


        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(mStage == 2){
                refreshCategoryList();
            }
            else{
                finish();
            }
            return true;
        }
        return false;
    }

}



