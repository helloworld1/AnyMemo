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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.utils.AMZipUtils;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class DownloaderAnyMemo extends DownloaderBase{
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderAnyMemo";
    private DownloadListAdapter dlAdapter;
    /*
     * dlStack caches the previous result so user can press
     * back button to go back
     */
    private Stack<ArrayList<DownloadItem>> dlStack;
    private ListView listView;
    private ProgressDialog mProgressDialog;
    private int mDownloadProgress;
    private Handler mHandler;
    private final static String WEBSITE_JSON = "http://anymemo.org/pages/json.php";
    private final static String WEBSITE_DOWNLOAD= "http://anymemo.org/pages/download.php?wordlistname=DatabasesTable&filename=";
    private static final int BUFFER_SIZE = 8192;

    private RecentListUtil recentListUtil;

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    @Override
    protected void initialRetrieve(){
        dlAdapter = new DownloadListAdapter(this, R.layout.filebrowser_item);
        dlStack = new Stack<ArrayList<DownloadItem>>();
        mHandler = new Handler();
        listView = (ListView)findViewById(R.id.file_list);
        listView.setAdapter(dlAdapter);
        mProgressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_connect_net), true, true, new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                finish();
            }
        });
        new Thread(){
            public void run(){
                try{
                    final ArrayList<DownloadItem> list = obtainCategories();
                    mHandler.post(new Runnable(){
                        public void run(){
                            dlAdapter.addList(list);
                            sortAdapter();
                            mProgressDialog.dismiss();
                        }
                    });

                }
                catch(final Exception e){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Log.e(TAG, "Error obtaining categories", e);
                            new AlertDialog.Builder(DownloaderAnyMemo.this)
                                .setTitle(getString(R.string.downloader_connection_error))
                                .setMessage(getString(R.string.downloader_connection_error_message) + e.toString())
                                .setNeutralButton(getString(R.string.back_menu_text), new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1){
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

    @Override
    protected void openCategory(final DownloadItem di){
        mProgressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_connect_net), true, true, new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                finish();
            }
        });
        new Thread(){
            public void run(){
                try{
                    final ArrayList<DownloadItem> list = obtainDatabases(di);
                    dlStack.push(dlAdapter.getList());
                    mHandler.post(new Runnable(){
                        public void run(){
                            dlAdapter.clear();
                            dlAdapter.addList(list);
                            sortAdapter();
                            listView.setSelection(0);
                            mProgressDialog.dismiss();
                        }
                    });

                }
                catch(final Exception e){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Log.e(TAG, "Error obtaining databases", e);
                            new AlertDialog.Builder(DownloaderAnyMemo.this)
                                .setTitle(getString(R.string.downloader_connection_error))
                                .setMessage(getString(R.string.downloader_connection_error_message) + e.toString())
                                .setNeutralButton(getString(R.string.back_menu_text), new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1){
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

    @Override
    protected DownloadItem getDownloadItem(final int position){
        return dlAdapter.getItem(position);
    }

    @Override
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

    @Override
    protected void fetchDatabase(final DownloadItem di){
            final Thread downloadThread = new Thread(){
                @Override
                public void run(){
                    String filename = di.getExtras("filename");
                    try{
                        downloadDatabase(di);
                        filename = filename.replace(".zip", ".db");
                        String sdpath = AMEnv.DEFAULT_ROOT_PATH;
                        final File dbFile = new File(sdpath + filename);
                        mHandler.post(new Runnable(){
                            public void run(){
                                new AlertDialog.Builder(DownloaderAnyMemo.this)
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
                                new AlertDialog.Builder(DownloaderAnyMemo.this)
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
            View alertView = View.inflate(DownloaderAnyMemo.this, R.layout.link_alert, null);
            TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(Html.fromHtml(getString(R.string.downloader_download_alert_message) + di.getDescription()));

            new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle(getString(R.string.downloader_download_alert) + di.getExtras("filename"))
                .setPositiveButton(getString(R.string.yes_text), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1){
                        downloadThread.start();
                    }
                })
                .setNegativeButton(getString(R.string.no_text), null)
                .show();

    }

    private void downloadDatabase(final DownloadItem di) throws Exception{
        String filename = di.getExtras("filename");
        if(filename == null){
        throw new Exception("Could not get filename");
        }

        String sdpath = AMEnv.DEFAULT_ROOT_PATH;
        File outFile = new File(sdpath + filename);
        mHandler.post(new Runnable(){
            public void run(){
                mProgressDialog = new ProgressDialog(DownloaderAnyMemo.this);
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

                URL myURL = new URL(di.getAddress());
                Log.v(TAG, "URL IS: " + myURL);
                URLConnection ucon = myURL.openConnection();
                final int fileSize = ucon.getContentLength();
                mHandler.post(new Runnable(){
                    public void run(){
                        mProgressDialog.setMax(fileSize);
                    }
                });

                byte[] buf = new byte[BUFFER_SIZE];

                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
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
                    AMZipUtils.unZipFile(outFile, new File(AMEnv.DEFAULT_ROOT_PATH));
                    /* Delete the zip file if it is successfully decompressed */
                    outFile.delete();

                }
                /* We do not check ttf file as db */
                if(!filename.toLowerCase().endsWith(".ttf")){
                    /* Check if the db is correct */
                    filename = filename.replace(".zip", ".db");
                    String fullpath = sdpath + filename;
                    AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(DownloaderAnyMemo.this, fullpath);
                    try {
                        long count = helper.getCardDao().getTotalCount(null);
                        if (count <= 0L) {
                            throw new RuntimeException("Downloaded empty db.");
                        }
                    } finally {
                        AnyMemoDBOpenHelperManager.releaseHelper(helper);
                    }

                    /* Add downloaded item to file list */
                    recentListUtil.addToRecentList(fullpath);
                }
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


    private ArrayList<DownloadItem> obtainCategories() throws Exception{
        ArrayList<DownloadItem> categoryList = new ArrayList<DownloadItem>();
        HttpClient httpclient = new DefaultHttpClient();
        String url = WEBSITE_JSON;
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
                di.setType(DownloadItem.ItemType.Category);
                di.setTitle(dbcategory);
                di.setAddress(WEBSITE_JSON + "?action=getdb&category=" + URLEncoder.encode(dbcategory, "UTF-8"));
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
                String filename = jsonItem.getString("FileName");
                DownloadItem di = new DownloadItem();
                di.setType(DownloadItem.ItemType.Database);
                di.setTitle(dbname);
                di.setDescription(dbnote);
                di.setAddress(WEBSITE_DOWNLOAD + URLEncoder.encode(filename, "UTF-8"));
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

    private void sortAdapter(){
        dlAdapter.sort(new Comparator<DownloadItem>(){
            public int compare(DownloadItem di1, DownloadItem di2){
                return (di1.getTitle().toLowerCase()).compareTo(di2.getTitle().toLowerCase());
            }
        });
    }


}
