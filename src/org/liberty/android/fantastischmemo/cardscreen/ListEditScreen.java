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
package org.liberty.android.fantastischmemo.cardscreen;

import org.liberty.android.fantastischmemo.*;

import org.amr.arabic.ArabicUtilities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.app.ProgressDialog;
import android.util.Log;
import android.text.Html;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.res.Configuration;


import java.util.List;


public class ListEditScreen extends AMActivity implements OnItemClickListener{
    private String dbPath = null;
    private String dbName = null;
    private static String TAG = "org.liberty.android.fantastischmemo.ListEditScreen";
    private ItemListAdapter mAdapter;
    private Handler mHandler;
    /* Initial position in the list */
    private int initPosition = 1;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_edit_screen);
		Bundle extras = getIntent().getExtras();
        mHandler = new Handler();
		if (extras != null) {
            dbPath = extras.getString("dbpath");
            dbName = extras.getString("dbname");
            initPosition = extras.getInt("openid", 1) - 1;

		}
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_database), true);
        new Thread(){
            public void run(){
                /* Load the items into memory to display in the list */
                DatabaseHelper dbHelper = new DatabaseHelper(ListEditScreen.this, dbPath, dbName);
                final List<Item> items = dbHelper.getListItems(-1, -1, 0, null);
                dbHelper.close();
                mHandler.post(new Runnable(){
                    public void run(){
                        mAdapter = new ItemListAdapter(ListEditScreen.this, R.layout.list_edit_screen_item, items);
                        ListView listView = (ListView)findViewById(R.id.item_list);
                        listView.setAdapter(mAdapter);
                        listView.setSelection(initPosition);
                        listView.setOnItemClickListener(ListEditScreen.this);
                        progressDialog.dismiss();
                        
                    }
                });
            }
        }.start();

    }

    @Override
	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id){
        /* Click to go back to EditScreern with specific card cliced */
        Intent resultIntent = new Intent();
        resultIntent.putExtra("item", mAdapter.getItem(position));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private class ItemListAdapter extends ArrayAdapter<Item> implements SectionIndexer{
        /* quick index sections */
        private String[] sections;

        public ItemListAdapter(Context context, int textViewResourceId, List<Item> items){
            super(context, textViewResourceId, items);
            int sectionSize = getCount() / 100;
            sections = new String[sectionSize];
            for(int i = 0; i < sectionSize; i++){
                sections[i] = "" + (i * 100);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.list_edit_screen_item, null);
            }
            TextView idView = (TextView)convertView.findViewById(R.id.item_id);
            TextView questionView = (TextView)convertView.findViewById(R.id.item_question);
            TextView answerView = (TextView)convertView.findViewById(R.id.item_answer);

            idView.setText("" + getItem(position).getId());
            questionView.setText(getItem(position).getQuestion());
            answerView.setText(getItem(position).getAnswer());

            return convertView;
        }

        /* Display the quick index when the user is scrolling */
        
        @Override
        public int getPositionForSection(int section){
            return section * 100;
        }

        @Override
        public int getSectionForPosition(int position){
            return position / 100;
        }

        @Override
        public Object[] getSections(){
            return sections;
        }
    }

}

