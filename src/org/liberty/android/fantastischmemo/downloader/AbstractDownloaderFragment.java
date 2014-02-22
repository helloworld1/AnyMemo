/*
Copyright (C) 2012 Haowen Ning

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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import roboguice.fragment.RoboFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The abstract class for an acitvity that displays a list of card sets
 * to download.
 */
public abstract class AbstractDownloaderFragment extends RoboFragment {

    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderBase";

    private Activity mActivity;

    private ListView listView;

    private DownloadListAdapter dlAdapter;

    private RecentListUtil recentListUtil;

    private View loadMoreFooter;

    private View loadingProgressFooter;

    /**
     * Retrieve the DownloadItem for the first time.
     * @return a list of download item.
     * @throws Exception exceptions
     */
    abstract protected List<DownloadItem> initialRetrieve() throws Exception;

    /**
     * Retrieve more DownloadItem
     * @return a list of download item.
     * @throws Exception exceptions
     */
    abstract protected List<DownloadItem> loadMore() throws Exception;

    /**
     * Return if there are more cards to load. 
     *
     * This is called after any loading
     *
     * @return true if there are more download item to retrieve
     */
    abstract protected boolean hasMore();

    /**
     * Retrieve the data when the user has clicked a category
     */
    abstract protected void openCategory(DownloadItem di);

    /**
     * Go back to the previous list
     */
    abstract protected void goBack();

    /**
     * Download the database based on the info
     */
    abstract protected String fetchDatabase(DownloadItem di) throws Exception;

    /**
     * Get specific item from the Adapter or else
     * @param position the posotion of the item in the list
     * @return the downloaditem at that position
     */
    protected DownloadItem getDownloadItem(int position) {
        return dlAdapter.getItem(position);
    };

    /**
     * Get the count of the download items
     * @return the download item count.
     */
    protected int getDownloadItemCount() {
        return dlAdapter.getCount();
    }

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
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

        // Initial two footers that is displayed in the bottom of the list
        loadMoreFooter =  ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_load_more_footer, null, false);
        loadingProgressFooter =  ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_loading_progress_footer, null, false);

        loadMoreFooter.setOnClickListener(loadMoreOnClickListener);
        // Must nullify the onclick listener on loadingProgressFooter
        // Or it will be treated a list item and cause index out of bount exception.
        loadingProgressFooter.setOnClickListener(null);

        dlAdapter = new DownloadListAdapter(mActivity, R.layout.filebrowser_item);

        // This is for android 2.3 compatibility. The addFooterView needs to be called
        // before setAdapter.
        listView.addFooterView(loadMoreFooter);
        listView.setAdapter(dlAdapter);
        listView.removeFooterView(loadMoreFooter);

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
            progressDialog.setMessage(getString(R.string.loading_connect_net));
            progressDialog.setCancelable(false);
            progressDialog.show();

            listView.addFooterView(loadingProgressFooter);
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
            listView.removeFooterView(loadingProgressFooter);
            progressDialog.dismiss();
            if (e != null) {
                AMGUIUtility.displayError(mActivity, getString(R.string.downloader_connection_error), getString(R.string.downloader_connection_error_message), e);
                return;
            }

            dlAdapter.addList(downloadItems);
            if (hasMore()) {
                listView.addFooterView(loadMoreFooter);
            }
        }
    }

    private class LoadMoreTask extends AsyncTask<Void, Void, Exception> {
        private List<DownloadItem> downloadItems;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            listView.removeFooterView(loadMoreFooter);
            listView.addFooterView(loadingProgressFooter);
        }

        @Override
        public Exception doInBackground(Void... nothing) {
            try {
                downloadItems = loadMore();
            } catch (Exception e) {
                return e;
            }
            return null;
        }


        @Override
        public void onPostExecute(Exception e){
            // Always remove the progress footer
            listView.removeFooterView(loadingProgressFooter);
            if (e != null) {
                AMGUIUtility.displayException(mActivity, getString(R.string.downloader_connection_error), getString(R.string.downloader_connection_error_message), e);
                return;
            }

            dlAdapter.addList(downloadItems);

            // Add the "loadMoreFooter" only if we successfully retrieve the list
            if (hasMore()) {
                listView.addFooterView(loadMoreFooter);
            }
        }
    }

    protected void showFetchDatabaseDialog(final DownloadItem item) {
        View alertView = View.inflate(mActivity, R.layout.link_alert, null);
        TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(getString(R.string.downloader_download_alert_message) + item.getDescription()));
        new AlertDialog.Builder(mActivity)
            .setView(alertView)
            .setTitle(getString(R.string.downloader_download_alert) + item.getTitle())
            .setPositiveButton(getString(R.string.yes_text), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1){
                    startFetchDatabaseTask(item);
                }
            })
            .setNegativeButton(getString(R.string.no_text), null)
            .show();
    }

    protected void startFetchDatabaseTask(final DownloadItem item) {
        FetchDatabaseTask task = new FetchDatabaseTask();
        task.execute(item);
    }

    private class FetchDatabaseTask extends AsyncTask<DownloadItem, Void, Exception> {
        private ProgressDialog progressDialog;
        private DownloadItem item;
        private String fetchedDbPath;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_downloading));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public Exception doInBackground(DownloadItem... items) {
            try {
                item = items[0];
                fetchedDbPath = fetchDatabase(item);
            } catch (Exception e) {
                Log.e(TAG, "Error fetch db lists", e);
                return e;
            }
            return null;
        }


        @Override
        public void onPostExecute(Exception e){
            progressDialog.dismiss();
            if (e != null) {
                AMGUIUtility.displayException(mActivity, getString(R.string.error_text), getString(R.string.downloader_download_fail_message), e);
                return;
            } else {
                new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.downloader_download_success)
                    .setMessage(getString(R.string.downloader_download_success_message) + fetchedDbPath)
                    .setPositiveButton(R.string.ok_text, null)
                    .show();
                recentListUtil.addToRecentList(fetchedDbPath);
            }
        }

    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parentView, View childView, int position, long id){
            DownloadItem di = getDownloadItem(position);
            if(di == null){
                Log.e(TAG, "NULL Download Item");
                return;
            }
            if(di.getType() == DownloadItem.ItemType.Category){
                openCategory(di);
            } else if(di.getType() == DownloadItem.ItemType.Back){
                goBack();
            } else {
                showFetchDatabaseDialog(di);
            }
        }
    };

    private View.OnClickListener loadMoreOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            LoadMoreTask task = new LoadMoreTask();
            task.execute();

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
            if (v == null){
                LayoutInflater li = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                /* Reuse the filebrowser's resources */
                v = li.inflate(R.layout.filebrowser_item, null);
            }
            DownloadItem item = getItem(position);
            if (item != null){
                TextView tv = (TextView)v.findViewById(R.id.file_name);
                ImageView iv = (ImageView)v.findViewById(R.id.file_icon);
                if (item.getType() == DownloadItem.ItemType.Category) {
                    iv.setImageResource(R.drawable.dir);
                } else if (item.getType() == DownloadItem.ItemType.Back) {
                    iv.setImageResource(R.drawable.back);
                } else if (item.getType() == DownloadItem.ItemType.Spreadsheet) {
                    iv.setImageResource(R.drawable.spreadsheet);
                } else {
                    iv.setImageResource(R.drawable.database);
                }
                tv.setText(item.getTitle());
            }
            return v;
        }

    }
}

