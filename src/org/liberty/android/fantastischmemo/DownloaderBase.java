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
import java.util.List;
import java.util.HashMap;

import android.os.Bundle;
import android.content.Context;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ImageView;
import android.util.Log;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.preference.PreferenceManager;


public abstract class DownloaderBase extends Activity implements OnItemClickListener{

    private static final String TAG = "org.liberty.android.fantastischmemo.DownloaderBase";



    /*
     * Retrieve the data when the user first open the
     * Downloader
     */
    abstract protected void initialRetrieve();

    /*
     * Retrieve the data when the user has clicked a category
     */
    abstract protected void openCategory(DownloadItem di);

    /*
     * Get specific item from the Adapter or else
     */
    abstract protected DownloadItem getDownloadItem(int position);

    /*
     * Go back to the previous list
     */

    abstract protected void goBack();
    
    /*
     * Download the database based on the info
     */
    abstract protected void fetchDatabase(DownloadItem di);

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        /* The file browser's is reused here because they are similar*/
        setContentView(R.layout.file_browser);
        ListView listView = (ListView)findViewById(R.id.file_list);
        listView.setOnItemClickListener(this);
        initialRetrieve();
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        /* set if the orientation change is allowed */
        if(!settings.getBoolean("allow_orientation", true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }



    
    @Override
	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id){
        /* Click to go back to EditScreern with specific card cliced */
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

    /*
     * go back when the user has pressed back key
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            goBack();
        }
        return false;
    }

    
    /* 
     * Helper class to provide the info for each items 
     * that could be categories, databases and other
     * special things
     */
    protected class DownloadItem{
        public static final int TYPE_CATEGORY = 1;
        public static final int TYPE_DATABASE = 2;
        public static final int TYPE_UP= 3;
        private int type;
        private String title = "";
        private String description = "";
        private String address = "";
        private HashMap<String, String> extras;

        public DownloadItem(){
            extras = new HashMap<String, String>();
        }

        public DownloadItem(int type, String title, String description, String address){
            this.type = type;
            this.title = title;
            this.description = description;
            this.address = address;
            extras = new HashMap<String, String>();
        }

        public void setType(int type){
            this.type = type;
        }

        public void setTitle(String title){
            this.title = title;
        }

        public void setDescription(String description){
            this.description = description;
        }

        public void setAddress(String address){
            this.address = address;
        }

        public void setExtras(String key, String item){
            this.extras.put(key, item);
        }

        public int getType(){
            return type;
        }

        public String getTitle(){
            return title;
        }

        public String getDescription(){
            return description;
        }

        public String getAddress(){
            return address;
        }

        public String getExtras(String key){
            return this.extras.get(key);
        }


    }
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
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                    iv.setImageResource(R.drawable.database24);
                }
                tv.setText(item.getTitle());
            }
            return v;
        }
        
    }
}

