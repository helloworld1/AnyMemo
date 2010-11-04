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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import android.graphics.Color;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.util.Log;
import android.os.SystemClock;
import android.net.Uri;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;


public class EditScreen extends MemoScreenBase implements OnGesturePerformedListener, View.OnClickListener{

    private int currentId = -1;
    private int totalItem = -1;
    private int maxId = -1;
    private Context mContext;
    private GestureLibrary mLibrary;
    private Button newButton;
    private Button nextButton;
    private Button prevButton;
    private Item copyItem = null;
    private boolean searchInflated = false;
    private final int ACTIVITY_MERGE = 10;
    private final int ACTIVITY_LIST = 100;
    

    private static final String TAG = "org.liberty.android.fantastischmemo.EditScreen";

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.memo_screen_gesture);
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
            if(currentId < 0){
                currentId = extras.getInt("openid", 1);
            }
        }
		
        mContext = this;

        mHandler = new Handler();
        /* Initiate the gesture */
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!mLibrary.load()) {
            finish();
        }
        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gesture_overlay);
        gestures.addOnGesturePerformedListener(this);
        createButtons();
        buttonBinding();
        if(prepare() == false){
            new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.open_database_error_title))
                .setMessage(getString(R.string.open_database_error_message))
                .setPositiveButton(getString(R.string.back_menu_text), new OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.help_button_text), new OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent myIntent = new Intent();
                        myIntent.setAction(Intent.ACTION_VIEW);
                        myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                        myIntent.setData(Uri.parse(getString(R.string.website_help_error_open)));
                        startActivity(myIntent);
                        finish();

                    }
                })
                .create()
                .show();
        }
    }

    @Override
	public void onDestroy(){
        super.onDestroy();
        try{
            dbHelper.close();
        }
        catch(Exception e){
        }
    }

    @Override 
    public void onSaveInstanceState(Bundle outState) 
    {
        
        outState.putInt("id", currentId);
        super.onSaveInstanceState(outState); 
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) 
    {
        super.onRestoreInstanceState(savedInstanceState);
        currentId = savedInstanceState.getInt("id", 1);
        prepare();
    }

    @Override
    public void onClick(View v){
        if(v == newButton){
            createNewItem();
        }
        else if(v == nextButton){
            getNextItem();
        }
        else if(v == prevButton){
            getPreviousItem();
        }
        else if(v == (ImageButton)findViewById(R.id.search_close_btn)){
            dismissSearchOverlay();
        }
        else if(v == (ImageButton)findViewById(R.id.search_next_btn)){
            doSearch(true);
        }
        else if(v == (ImageButton)findViewById(R.id.search_previous_btn)){
            doSearch(false);
        }

    }

    @Override
    protected boolean prepare(){
        if(dbHelper == null){
            try{
                dbHelper = new DatabaseHelper(mContext, dbPath, dbName);
            }
            catch(Exception e){
                Log.e(TAG, "Error" + e.toString(), e);
                return false;
            }

        }
		loadSettings();
        maxId = dbHelper.getNewId() - 1;
        totalItem = dbHelper.getTotalCount();
        if(totalItem <= 0){
            /* Ask user to create a new card when the db is empty */
            createNewItem();
        
        }
        else{
            if(currentId < 1){
                currentId = 1;
            }
            else if(currentId > maxId){
                currentId = maxId;
            }

            currentItem = dbHelper.getItemById(currentId, 0, true, activeFilter);
            /* Re-fetch the id in case that the item with id 1 is 
             * deleted.
             */
            if(currentItem == null){
                currentItem = dbHelper.getItemById(currentId, 0, true, null);
            }
            if(currentItem != null){
                currentId = currentItem.getId();
                updateMemoScreen();
            }
        }
        return true;
    }

    @Override
    public  boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_screen_menu, menu);
		return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent();
	    switch (item.getItemId()) {
            case R.id.editmenu_help:
                myIntent.setAction(Intent.ACTION_VIEW);
                myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                myIntent.setData(Uri.parse(getString(R.string.website_help_edit)));
                startActivity(myIntent);
                return true;

            case R.id.editmenu_search_id:
                createSearchOverlay();
                return true;

	        case R.id.editmenu_edit_id:
                doEdit(currentItem);
                return true;

            case R.id.editmenu_delete_id:
                doDelete();
                return true;

            case R.id.editmenu_detail_id:
                myIntent.setClass(this, DetailScreen.class);
                myIntent.putExtra("dbname", dbName);
                myIntent.putExtra("dbpath", dbPath);
                myIntent.putExtra("itemid", currentItem.getId());
                startActivityForResult(myIntent, 2);
                return true;

            case R.id.editmenu_settings_id:
                myIntent.setClass(this, SettingsScreen.class);
                myIntent.putExtra("dbname", dbName);
                myIntent.putExtra("dbpath", dbPath);
                startActivityForResult(myIntent, 1);
                //finish();
                return true;

            case R.id.menu_edit_filter:
                doFilter();
                return true;

            case R.id.editmenu_list_id:
    			myIntent.setClass(this, ListEditScreen.class);
    			myIntent.putExtra("dbname", dbName);
    			myIntent.putExtra("dbpath", dbPath);
    			myIntent.putExtra("openid", currentItem.getId());
    			startActivityForResult(myIntent, ACTIVITY_LIST);
                return true;

            case R.id.editmenu_merge_id:
                myIntent.setClass(this, FileBrowser.class);
                myIntent.putExtra("default_root", dbPath);
                myIntent.putExtra("file_extension", ".db");
                startActivityForResult(myIntent, ACTIVITY_MERGE);

                return true;
            
            case R.id.editmenu_copy_id:
                doCopy();
                return true;

            case R.id.editmenu_paste_id:
                doPaste();
                return true;

            case R.id.editmenu_swap_qa_id:
                currentItem.inverseQA();
                dbHelper.addOrReplaceItem(currentItem);
                restartActivity();
                return true;

            case R.id.editmenu_remove_dup_id:
                final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.removing_dup_title), getString(R.string.removing_dup_summary), true);
                progressDialog.dismiss();
                final Thread removingThread = new Thread(){
                    public void run(){
                        mHandler.post(new Runnable(){
                            public void run(){
                                progressDialog.show();
                            }
                        });
                        dbHelper.removeDuplicates();
                        mHandler.post(new Runnable(){
                            public void run(){
                                progressDialog.dismiss();
                                restartActivity();
                            }
                        });
                    }
                };
                new AlertDialog.Builder(this)
                    .setTitle(R.string.remove_dup_text)
                    .setMessage(R.string.removing_dup_warning)
                    .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface arg0, int arg1) {
                            removingThread.start();
                        }
                    })
                    .setNegativeButton(R.string.cancel_text, null)
                    .create()
                    .show();

                return true;
                
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
        final int request = requestCode;
        if(resultCode == Activity.RESULT_OK){
    		if(requestCode == ACTIVITY_MERGE){
                new AlertDialog.Builder(this)
                    .setTitle(R.string.merge_method_title)
                    .setMessage(R.string.merge_method_message)
                    .setPositiveButton(R.string.merge_method_here, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            doMerge(data);
                        }
                    })
                    .setNeutralButton(R.string.merge_method_end, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            /* set the current item to the last one */
                            currentItem = dbHelper.getItemById(maxId, 0, true, activeFilter);
                            currentId = currentItem.getId();
                            doMerge(data);
                        }
                    })
                    .create()
                    .show();
                returnValue = 0;
            }
            if(requestCode == ACTIVITY_LIST){
                currentId = data.getIntExtra("id", 1);

                /* In case the id for the current item is null
                 * which is unlikely to happen */
                Item savedCurrent = currentItem.clone();
                currentItem = dbHelper.getItemById(currentId, 0, true, activeFilter);

                if(currentItem == null){
                    currentItem = savedCurrent;
                }
                /* Go to specific card id returned by activity */

                restartActivity();
            }
        }
    }

    @Override
    protected void createButtons(){
        /* Inflate buttons from XML */
        LinearLayout root = (LinearLayout)findViewById(R.id.layout_buttons);
        LayoutInflater.from(this).inflate(R.layout.edit_screen_buttons, root);

        newButton = (Button)findViewById(R.id.edit_screen_btn_new);
        prevButton = (Button)findViewById(R.id.edit_screen_btn_prev);
        nextButton = (Button)findViewById(R.id.edit_screen_btn_next);
    }

    @Override
	protected void buttonBinding(){
        newButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
    }

    @Override
    protected void restartActivity(){
        //loadSettings();
        //updateMemoScreen();
        Intent myIntent = new Intent(this, EditScreen.class);
        myIntent.putExtra("dbname", dbName);
        myIntent.putExtra("dbpath", dbPath);
        if(currentItem != null){
            myIntent.putExtra("openid", currentItem.getId());
        }
        else{
            myIntent.putExtra("openid", currentId);
        }

        myIntent.putExtra("active_filter", activeFilter);
        finish();
        startActivity(myIntent);
    }

    @Override 
    protected void refreshAfterEditItem(){
        int max = dbHelper.getNewId() - 1;
        if(max != maxId){
            currentId = max;
            prepare();
        }
        else if(max == 0){
            /* If user cancel editing,
             * it will exit the activity.
             */
            finish();
        }
        else{
            if(currentItem.isEmpty()){
                prepare();
            }
            else{
                updateMemoScreen();
            }
        }
    }

    @Override
    protected void refreshAfterDeleteItem(){
        updateMemoScreen();
        prepare();
    }

    @Override
	protected void displayQA(Item item) {
        super.displayQA(item);
        setTitle(getString(R.string.stat_total) + totalItem + " / " + this.getString(R.string.memo_current_id) + currentId + " / " + currentItem.getCategory());
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture){
        ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

        // We want at least one prediction
        if (predictions.size() > 0) {
            Prediction prediction = predictions.get(0);
            // We want at least some confidence in the result
            if (prediction.score > 1.0) {
                // Show the spell
                Log.v(TAG, "Gesture: " + prediction.name);
                if(prediction.name.equals("swipe-right")){
                    getPreviousItem();

                }
                else if(prediction.name.equals("swipe-left")){
                    getNextItem();
                }
                else if(prediction.name.equals("o") || prediction.name.equals("o2")){
                    doEdit(currentItem);
                }
                else if(prediction.name.equals("cross")){
                    doDelete();
                }

            }
        }
    }

    private void getNextItem(){
        if(totalItem > 0){
            currentId += 1;
            currentItem = dbHelper.getItemById(currentId, 0, true, activeFilter);
            if(currentItem == null){
                currentItem = dbHelper.getItemById(0, 0, true, activeFilter);
            }
        }
        if(currentItem != null){
            currentId = currentItem.getId();
            updateMemoScreen();
        }
        else{
            showFilterFailureDialog();
        }
        
    }

    private void getPreviousItem(){
        if(totalItem > 0){
            currentId -= 1;
            currentItem = dbHelper.getItemById(currentId, 0, false, activeFilter);
            if(currentItem == null){
                currentItem = dbHelper.getItemById(maxId, 0, false, activeFilter);
            }
        }


        if(currentItem != null){
            currentId = currentItem.getId();
            updateMemoScreen();
        }
        else{
            showFilterFailureDialog();
        }
    }
    
    private void createNewItem(){
        /* Reuse the doEdit to get the edit dialog
         * and display the edit dialog
         */
        Item newItem = new Item();
        newItem.setId(dbHelper.getNewId());
        if(currentItem != null){
            newItem.setCategory(currentItem.getCategory());
        }
        /* the tricky here is that the currentId is not modified.
         * it is only modified the success edit.
         * If not the prepare() will restore the oritinal item 
         */
        doEdit(newItem);
    }

    private void createSearchOverlay(){
        if(searchInflated == false){
            LinearLayout root = (LinearLayout)findViewById(R.id.memo_screen_root);
            LayoutInflater.from(this).inflate(R.layout.search_overlay, root);
            ImageButton close = (ImageButton)findViewById(R.id.search_close_btn);
            close.setOnClickListener(this);
            ImageButton prev = (ImageButton)findViewById(R.id.search_previous_btn);
            prev.setOnClickListener(this);
            ImageButton next = (ImageButton)findViewById(R.id.search_next_btn);
            next.setOnClickListener(this);

            EditText editEntry = (EditText)findViewById(R.id.search_entry);
            editEntry.requestFocus();
            searchInflated = true;

        }
        else{
            LinearLayout layout = (LinearLayout)findViewById(R.id.search_root);
            layout.setVisibility(View.VISIBLE);
        }


    }

    private void dismissSearchOverlay(){
        if(searchInflated == true){
            LinearLayout layout = (LinearLayout)findViewById(R.id.search_root);
            layout.setVisibility(View.GONE);
        }
    }

    private void doSearch(boolean forward){
        EditText et = (EditText)findViewById(R.id.search_entry);
        String text = et.getText().toString();
        boolean processed = false;
        Item searchItem = null;
        if(text == null){
            return;
        }
        else if(text.equals("")){
            return;
        }
        if(text.charAt(0) == '#'){
            String num = text.substring(1);
            int intNum = 0;
            try{
                intNum = Integer.parseInt(num);
                if(intNum > 0 && intNum <= maxId){
                    searchItem = dbHelper.getItemById(intNum, 0, true, activeFilter);
                    if(searchItem != null){
                        currentId = intNum;
                        currentItem = searchItem;
                        prepare();
                        processed = true;
                        return;
                    }
                }

            }
            catch(NumberFormatException e){
            }
        }
        if(processed == false && !text.equals("")){
            text = text.replace('*', '%');
            text = text.replace('?', '_');
            int resId = dbHelper.searchItem(currentItem.getId(), text, forward);
            if(resId > 0){
                searchItem = dbHelper.getItemById(resId, 0, forward, activeFilter);
                if(searchItem != null){
                    currentItem = searchItem;
                    currentId = searchItem.getId();
                    prepare();
                }
            }
        }
    }

    private void doMerge(final Intent data){
        final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.merging_title), getString(R.string.merging_summary), true);
        new Thread(){
            @Override
            public void run(){
                final String name = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                final String path = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                try{
                    dbHelper.mergeDatabase(path, name, currentId);
                }
                catch(final Exception e){
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            progressDialog.dismiss();
                            new AlertDialog.Builder(mContext)
                                .setTitle(R.string.merge_fail_title)
                                .setMessage(getString(R.string.merge_fail_message) + " " + e.toString())
                                .setPositiveButton(R.string.ok_text, null)
                                .create()
                                .show();

                        }
                    });
                }
                mHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        progressDialog.dismiss();
                        new AlertDialog.Builder(EditScreen.this)
                            .setTitle(R.string.merge_success_title)
                            .setMessage(getString(R.string.merge_success_message) + " " + dbName + ", " + name)
                            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    restartActivity();

                                }
                            })
                            .create()
                            .show();

                    }
                });
            }
        }.start();
    }

    private void doCopy(){
        copyItem = currentItem;
    }

    private void doPaste(){
        if(copyItem != null){
            dbHelper.insertItem(copyItem, currentId);
            currentId += 1;
            prepare();
        }
    }

        
}
