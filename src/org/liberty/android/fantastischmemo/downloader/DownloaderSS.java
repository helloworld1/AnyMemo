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

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

/*
 * This is the downloader class for StudyStack
 */
public class DownloaderSS extends DownloaderBase implements ListView.OnScrollListener{
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderSS";
    private static final String SS_API_GET_DECK = "http://www.studystack.com/servlet/json?studyStackId=";
    private static final String SS_API_GET_CATEGORIES = "http://www.studystack.com/servlet/categoryListJson";
    private static final String SS_API_GET_CATEGORY_CONTENT = "http://www.studystack.com/servlet/categoryStackListJson?sortOrder=stars&categoryId=";
    private DownloadListAdapter dlAdapter;
    private Stack<List<DownloadItem>> dlStack;
    private Stack<String> categoryIdStack;
    private ListView listView;
    private ProgressDialog mProgressDialog;
    private Handler mHandler;
    private List<DownloadItem> categoryList = null;

    private DownloaderUtils downloaderUtils;

    private RecentListUtil recentListUtil;

    @Inject
    public void setDownloaderUtils(DownloaderUtils downloaderUtils) {
        this.downloaderUtils = downloaderUtils;
    }

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    @Override
    protected void initialRetrieve(){
        dlAdapter = new DownloadListAdapter(this, R.layout.filebrowser_item);
        dlStack = new Stack<List<DownloadItem>>();
        categoryIdStack = new Stack<String>();
        mHandler = new Handler();
        listView = (ListView)findViewById(R.id.file_list);
        listView.setOnScrollListener(this);
        listView.setAdapter(dlAdapter);
        mProgressDialog = ProgressDialog.show(DownloaderSS.this, getString(R.string.loading_please_wait), getString(R.string.loading_connect_net), true, true, new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                finish();
            }
        });
        new Thread(){
            public void run(){
                try{
                    categoryList = retrieveCategories();
                    mHandler.post(new Runnable(){
                        public void run(){
                            showRootCategories();
                            mProgressDialog.dismiss();
                        }
                    });
                }
                catch(final Exception e){
                    mHandler.post(new Runnable(){
                        public void run(){
                            mProgressDialog.dismiss();
                            new AlertDialog.Builder(DownloaderSS.this)
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
                    final List<DownloadItem> databaseList = retrieveDatabaseList(di);
                    dlStack.push(dlAdapter.getList());
                    categoryIdStack.push(di.getExtras("id"));
                    mHandler.post(new Runnable(){
                        public void run(){
                            dlAdapter.clear();
                            for(DownloadItem i : categoryList){
                                if(i.getExtras("pid").equals(di.getExtras("id"))){
                                    dlAdapter.add(i);
                                }
                            }
                            dlAdapter.addList(databaseList);
                            listView.setSelection(0);
                            mProgressDialog.dismiss();
                        }
                    });

                }
                catch(final Exception e){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Log.e(TAG, "Error obtaining databases", e);
                            new AlertDialog.Builder(DownloaderSS.this)
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
            if(!categoryIdStack.empty()){
                categoryIdStack.pop();
            }
        }
    }

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
                    mProgressDialog = ProgressDialog.show(DownloaderSS.this, getString(R.string.loading_please_wait), getString(R.string.loading_downloading));
                    new Thread(){
                        public void run(){
                            try{
                                downloadDatabase(di);
                                mHandler.post(new Runnable(){
                                    public void run(){
                                        mProgressDialog.dismiss();
                                        String dbpath = AMEnv.DEFAULT_ROOT_PATH;
                                        new AlertDialog.Builder(DownloaderSS.this)
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
                                        new AlertDialog.Builder(DownloaderSS.this)
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



    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
        if(totalItemCount <= 0 ){
            return;
        }
        if(totalItemCount >= 25 && (firstVisibleItem + visibleItemCount == totalItemCount)){
            DownloadItem di = dlAdapter.getItem(totalItemCount - 1);
            if(di.getType() == DownloadItem.ItemType.Database){
                try{
                    int page = Integer.parseInt(di.getExtras("page"));
                    page += 1;
                    DownloadItem nextPage = new DownloadItem();
                    nextPage.setExtras("id", categoryIdStack.peek());
                    nextPage.setExtras("page", Integer.toString(page));
                    List<DownloadItem> nextPageItems = retrieveDatabaseList(nextPage);
                    dlAdapter.addList(nextPageItems);

                }
                catch(Exception e){
                    Log.e(TAG, "Error to scroll", e);
                }
            }

        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    private List<DownloadItem> retrieveCategories() throws Exception{
        List<DownloadItem> diList = new LinkedList<DownloadItem>();
        JSONArray jsonArray = new JSONArray(downloaderUtils.downloadJSONString(SS_API_GET_CATEGORIES));
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            DownloadItem di = new DownloadItem();
            di.setType(DownloadItem.ItemType.Category);
            di.setTitle(jsonItem.getString("name"));
            di.setExtras("id", jsonItem.getString("id"));
            di.setExtras("pid", jsonItem.getString("parentId"));
            di.setExtras("page", "1");

            if(di.getTitle() != null){
                diList.add(di);
            }
        }
        return diList;
    }

    private List<DownloadItem> retrieveDatabaseList(DownloadItem category) throws Exception{
        List<DownloadItem> diList = new LinkedList<DownloadItem>();
        String url = SS_API_GET_CATEGORY_CONTENT + category.getExtras("id");
        String page = category.getExtras("page");
        if(page != null){
            url += "&page=" + page;
        }
        else{
            page = "1";
        }


        JSONArray jsonArray = new JSONArray(downloaderUtils.downloadJSONString(url));
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            DownloadItem di = new DownloadItem();
            di.setType(DownloadItem.ItemType.Database);
            di.setTitle(jsonItem.getString("stackName"));
            di.setDescription(jsonItem.getString("description"));
            di.setExtras("id", jsonItem.getString("id"));
            di.setAddress(SS_API_GET_DECK + jsonItem.getString("id"));
            di.setExtras("page", page);
            if(di.getTitle() != null){
                diList.add(di);
            }
        }
        return diList;
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

    private void downloadDatabase(DownloadItem di) throws Exception{
        String url = di.getAddress();
        String jsonString = downloaderUtils.downloadJSONString(url);
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonDataArray = jsonObject.getJSONArray("data");
        List<Card> cardList = new LinkedList<Card>();
        for(int i = 0; i < jsonDataArray.length(); i++){
            JSONArray jsonItemArray = jsonDataArray.getJSONArray(i);
            String question = jsonItemArray.getString(0);
            String answer = jsonItemArray.getString(1);
            if(question != null && !question.equals("")){
                Card card = new Card();
                card.setQuestion(question);
                card.setAnswer(answer);
                card.setOrdinal(i + 1);
                card.setCategory(new Category());
                card.setLearningData(new LearningData());
                cardList.add(card);
            }

        }
        String dbname = di.getTitle() + ".db";
        String dbpath = AMEnv.DEFAULT_ROOT_PATH;
        String fullpath = dbpath + dbname;
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(DownloaderSS.this, fullpath);
        try {
            CardDao cardDao = helper.getCardDao();
            cardDao.createCards(cardList);
            long count = helper.getCardDao().getTotalCount(null);
            if (count <= 0L) {
                throw new RuntimeException("Downloaded empty db.");
            }
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
        recentListUtil.addToRecentList(fullpath);
    }

}

