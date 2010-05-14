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

import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AddItemScreen extends Activity implements OnClickListener{
	private EditText entryId;
	private EditText entryQuestion;
	private EditText entryAnswer;
	private Button btnNew;
	private Button btnEdit;
	private Button btnGotoid;
	private Button btnBack;
	private Button btnNext;
	private Button btnPrevious;
	private String dbName;
	private String dbPath;
	private int openId = -1;
	private DatabaseHelper dbHelper;
    private Handler mHandler;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private static final String TAG = "org.liberty.android.fantastischmemo.AddItemScreen";

    /* This is used for multi threading */
    private String tmpId;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_screen);
		Bundle extras = getIntent().getExtras();
        mHandler = new Handler();
        mContext = this;
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
			openId = extras.getInt("openid");
		}
		dbHelper = new DatabaseHelper(this, dbPath, dbName);
		
		
        entryId = (EditText)findViewById(R.id.add_item_id_entry);
        entryQuestion = (EditText)findViewById(R.id.add_item_question_entry);
        entryAnswer = (EditText)findViewById(R.id.add_item_answer_entry);
        btnNew = (Button)findViewById(R.id.add_item_but_new);
        btnEdit = (Button)findViewById(R.id.add_item_but_edit);
        btnGotoid = (Button)findViewById(R.id.add_item_but_gotoid);
        btnBack = (Button)findViewById(R.id.add_item_but_back);
        btnNext = (Button)findViewById(R.id.add_item_but_next);
        btnPrevious = (Button)findViewById(R.id.add_item_but_previous);
        btnNew.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnGotoid.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        if(openId != -1 && dbHelper.getNewId() > openId){
        	entryId.setText("" + openId);
        	Item item = dbHelper.getItemById(openId, 0);
        	entryQuestion.setText(item.getQuestion());
        	entryAnswer.setText(item.getAnswer());
        }
        
    }
    
    private void setEntryById(int id){
    	int maxId = dbHelper.getNewId();
    	if(id == 0){
    		id = 1;
    	}
    	if(maxId == id){
    		entryQuestion.setText("");
    		entryAnswer.setText("");
    		entryId.setText("" + id);
    	}
    	else if(maxId < id){
    		entryId.setText("" + maxId);
    		entryQuestion.setText("");
    		entryAnswer.setText("");
    	}
    	else if(maxId > id && id > 0){
    		Item item = dbHelper.getItemById(id, 0);
    		entryQuestion.setText(item.getQuestion());
    		entryAnswer.setText(item.getAnswer());
    		entryId.setText("" + id);
    	}
    
    	
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	dbHelper.close();
    }

	@Override
	public void onClick(View v) {
		if(v == btnNew){
			entryQuestion.setText("");
			entryAnswer.setText("");
			entryId.setText("" + dbHelper.getNewId());
			
			
		}
		
		if(v == btnEdit){

            mProgressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_save), true);
            Thread savingThread = new Thread(){
                @Override
                public void run(){
                    HashMap<String, String> hm = new HashMap<String, String>();
                    // Check if id is an integer;
                    String myid = entryId.getText().toString();
                    int intId = -1;

                    tmpId = null;

                    try{
                        intId = Integer.parseInt(myid);
                        if(intId < 0){
                            throw new NumberFormatException();
                        }
                    }
                    catch(NumberFormatException e){
                        intId = dbHelper.getNewId();
                        myid = Integer.toString(intId);
                        //entryId.setText(myid);
                        tmpId = myid;
                    }
                    hm.put("_id", myid);
                    hm.put("question", entryQuestion.getText().toString());
                    hm.put("answer", entryAnswer.getText().toString());
                    Item item = new Item();
                    item.setData(hm);
                    dbHelper.addOrReplaceItem(item);
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            if(tmpId != null){
                                entryId.setText(tmpId);
                            }
                                
                        //entryId.setText(myid);
                            mProgressDialog.dismiss();
                        }
                    });
                }
            };
            savingThread.start();
		}
		if(v == btnGotoid){
			int id = -1;
			try{
				id = Integer.parseInt(entryId.getText().toString());
			}
			catch(Exception e){
				return;
			}
			setEntryById(id);
		}
		
		if(v == btnBack){
			finish();
		}
		
		if(v == btnNext){
			int id = -1;
			try{
				id = Integer.parseInt(entryId.getText().toString());
			}
			catch(Exception e){
				return;
			}
			setEntryById(id + 1);
			
		}
		if(v == btnPrevious){
			int id = -1;
			try{
				id = Integer.parseInt(entryId.getText().toString());
			}
			catch(Exception e){
				return;
			}
			setEntryById(id - 1);
			
		}
		
	}
    @Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_screen_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.add_menudetail:
            int intId = -1;
            String myid = entryId.getText().toString();
            try{
                intId = Integer.parseInt(myid);
            }
            catch(NumberFormatException e){
                intId = -1;
            }

            if(intId >= 0 && intId < dbHelper.getNewId()){
                Intent myIntent1 = new Intent();
                myIntent1.setClass(this, DetailScreen.class);
                myIntent1.putExtra("dbname", this.dbName);
                myIntent1.putExtra("dbpath", this.dbPath);
                myIntent1.putExtra("itemid", intId);
                startActivity(myIntent1);
            }
    		return true;
	    case R.id.add_menusettings:
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, SettingsScreen.class);
    		myIntent.putExtra("dbname", this.dbName);
    		myIntent.putExtra("dbpath", this.dbPath);
            startActivity(myIntent);
    		//finish();
    		return true;
        }
        return false;
    }
            

}
