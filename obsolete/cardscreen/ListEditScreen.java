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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ListEditScreen extends AMActivity implements OnItemClickListener{
    private String dbPath = "/mnt/sdcard/anymemo"; //testing db
    private String dbName = "5000_Collegiate_Words_SAT_Volcabulary.db"; //testing db 
    private static String TAG = "org.liberty.android.fantastischmemo.ListEditScreen";
    private ItemListAdapter mAdapter;
    private Handler mHandler;
    /* Initial position in the list */
    private int initPosition = 1;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_edit_screen);
		Log.v(TAG,"xinxin_test\n");
		Bundle extras = getIntent().getExtras();
//		Log.i(TAG, "xinxin: "+ Arrays.toString(extras.keySet().toArray())+ "\n\n");
		mHandler = new Handler();
		if (extras != null) {
//            dbPath = extras.getString("dbpath");
//            dbName = extras.getString("dbname");
            initPosition = extras.getInt("openid", 1) - 1;

		}
		
//    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_database), true);
        
        /* Load the items into memory to display in the list */
        DatabaseHelper dbHelper = new DatabaseHelper(ListEditScreen.this, dbPath, dbName);
        final List<Item> items = dbHelper.getListItems(-1, -1, 0, null);
        dbHelper.close();
        new Thread(){
            public void run(){
                
        
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

        
        final Button button_col1= (Button)findViewById(R.id.by_id);
        button_col1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "xinxin testing button_col1\n\n");
				new Thread(){
		            public void run(){
		            	Collections.sort(items, new Comparator<Item>(){
							@Override
							public int compare(Item a, Item b) {
								return a.getId() - b.getId();
							}
						});
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
		});

        final Button button_col2= (Button)findViewById(R.id.by_question);
        button_col2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "xinxin testing button_col2\n\n");
				new Thread(){
		            public void run(){
		            	Collections.sort(items, new Comparator<Item>(){
							@Override
							public int compare(Item a, Item b) {
								return a.getQuestion().compareTo(b.getQuestion());
							}
						});

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
		});


        final Button button_col3= (Button)findViewById(R.id.by_answer);
        button_col3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "xinxin testing button_col3\n\n");
				new Thread(){
		            public void run(){
		            	Collections.sort(items, new Comparator<Item>(){
							@Override
							public int compare(Item a, Item b) {
								return a.getAnswer().compareTo(b.getAnswer());
							}
						});
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
		});

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

