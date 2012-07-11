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
import java.util.List;

import org.liberty.android.fantastischmemo.downloader.google.GoogleAccountActivity;

import android.accounts.Account;

import android.app.Activity;
import android.app.ProgressDialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;

import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbstractDownloaderFragment extends Fragment {

    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderBase";

    private Activity mActivity;
    
    private ListView listView;

    private DownloadListAdapter dlAdapter;

    /*
     * Retrieve the data when the user first open the
     * Downloader
     */
    abstract protected List<DownloadItem> initialRetrieve() throws Exception;

    /*
     * Retrieve the data when the user has clicked a category
     */
    abstract protected void openCategory(DownloadItem di);

    /*
     * Go back to the previous list
     */
    abstract protected void goBack();
    
    /*
     * Download the database based on the info
     */
    abstract protected void fetchDatabase(DownloadItem di);

    /*
     * Get specific item from the Adapter or else
     */
    protected DownloadItem getDownloadItem(int position) {
        return dlAdapter.getItem(position);
    };

    protected int getDownloadItemCount() {
        return dlAdapter.getCount();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The file browser's is reused here because they are similar
        View v = inflater.inflate(R.layout.file_browser, container, false);

        listView = (ListView)v.findViewById(R.id.file_list);
        listView.setOnItemClickListener(itemClickListener);
        dlAdapter = new DownloadListAdapter(mActivity, R.layout.filebrowser_item);
        listView.setAdapter(dlAdapter);
        InitRetrieveTask task = new InitRetrieveTask();
        task.execute();
        return v;
    }

	private class InitRetrieveTask extends AsyncTask<Void, Void, Exception> {
        private ProgressDialog progressDialog;
        private List<DownloadItem> downloadItems;

		@Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_save));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public Exception doInBackground(Void... nothing) {
            try {
                downloadItems = initialRetrieve();
            } catch (Exception e) {
                return e;
            }
            return null;
        }

        
        @Override
        public void onPostExecute(Exception e){
            progressDialog.dismiss();
            if (e != null) {
                // TODO: handle it nicely
                e.printStackTrace();
                return;
            }
            dlAdapter.addList(downloadItems);
        }

    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parentView, View childView, int position, long id){
            DownloadItem di = getDownloadItem(position);
            if(di == null){
                Log.e(TAG, "NULL Download Item");
                return;
            }
            if(di.getType() == DownloadItem.TYPE_CATEGORY){
                openCategory(di);
            }
            else if(di.getType() == DownloadItem.TYPE_UP){
                goBack();
            }
            else if(di.getType() == DownloadItem.TYPE_DATABASE){
                fetchDatabase(di);
            }
        }
    };

    /* 
     * Helper class to provide the info for each items 
     * that could be categories, databases and other
     * special things
     */
    protected class DownloadListAdapter extends ArrayAdapter<DownloadItem>{

        public DownloadListAdapter(Context context, int textViewResourceId){
            super(context, textViewResourceId);

        }


        public void addList(List<DownloadItem> list){
            for(DownloadItem di : list){
                add(di);
            }
        }

        public ArrayList<DownloadItem> getList(){
            ArrayList<DownloadItem> list = new ArrayList<DownloadItem>();
            int count = getCount();

            for(int i = 0; i < count; i++){
                list.add(getItem(i));
            }
            return list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            if(v == null){
                LayoutInflater li = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                /* Reuse the filebrowser's resources */
                v = li.inflate(R.layout.filebrowser_item, null);
            }
            DownloadItem item = getItem(position);
            if(item != null){
                TextView tv = (TextView)v.findViewById(R.id.file_name);
                ImageView iv = (ImageView)v.findViewById(R.id.file_icon);
                if(item.getType() == DownloadItem.TYPE_CATEGORY){
                    iv.setImageResource(R.drawable.dir);
                }
                else if(item.getType() == DownloadItem.TYPE_UP){
                    iv.setImageResource(R.drawable.back);
                }
                else{
                    iv.setImageResource(R.drawable.database);
                }
                tv.setText(item.getTitle());
            }
            return v;
        }
        
    }
}

