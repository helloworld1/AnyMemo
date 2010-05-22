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
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.util.Log;
import android.os.SystemClock;

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
    private Handler mHandler;
    private GestureLibrary mLibrary;
    private Button newButton;
    private Button nextButton;
    private Button prevButton;
    private Item savedItem = null;

    private static final String TAG = "org.liberty.android.fantastischmemo.EditScreen";

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.memo_screen);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
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
        prepare();
    }

    @Override
	public void onDestroy(){
        super.onDestroy();
		dbHelper.close();
    }

    @Override
    public void onClick(View v){
        if(v == newButton){
            createNewItem();
        }
        if(v == nextButton){
            getNextItem();
        }
        if(v == prevButton){
            getPreviousItem();
        }

    }

    @Override
    protected void prepare(){
        if(dbHelper == null){
            dbHelper = new DatabaseHelper(mContext, dbPath, dbName);
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

            currentItem = dbHelper.getItemById(currentId, 0);
            /* Re-fetch the id in case that the item with id 1 is 
             * deleted.
             */
            currentId = currentItem.getId();
            setTitle(getString(R.string.stat_total) + totalItem);
            updateMemoScreen();
        }
    }

    @Override
    protected int feedData(){
        /* Dummy feed Data */
        return 1;
    }

    @Override
    public  boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_screen_menu, menu);
		return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item){
	    switch (item.getItemId()) {
	        case R.id.editmenu_edit_id:
                doEdit();
                return true;

            case R.id.editmenu_delete_id:
                doDelete();
                return true;

            case R.id.editmenu_detail_id:
                Intent myIntent1 = new Intent();
                myIntent1.setClass(this, DetailScreen.class);
                myIntent1.putExtra("dbname", this.dbName);
                myIntent1.putExtra("dbpath", this.dbPath);
                myIntent1.putExtra("itemid", currentItem.getId());
                startActivityForResult(myIntent1, 2);
                return true;

            case R.id.editmenu_settings_id:
                Intent myIntent = new Intent();
                myIntent.setClass(this, SettingsScreen.class);
                myIntent.putExtra("dbname", this.dbName);
                myIntent.putExtra("dbpath", this.dbPath);
                startActivityForResult(myIntent, 1);
                //finish();
                return true;
        }
        return false;
    }

    @Override
    protected void createButtons(){
        /* Make up an id using this base */
        int base = 0x31212;
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.memo_screen_button_layout);
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth(); 

        newButton = new Button(this);
        newButton.setText(getString(R.string.add_screen_new));
        newButton.setId(base);
        RelativeLayout.LayoutParams p1 = new RelativeLayout.LayoutParams(width / 3, RelativeLayout.LayoutParams.WRAP_CONTENT); 
        layout.addView(newButton, p1);


        prevButton = new Button(this);
        prevButton.setText(getString(R.string.add_screen_previous));
        prevButton.setId(base + 1);
        RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(width / 3, RelativeLayout.LayoutParams.WRAP_CONTENT); 
        p2.addRule(RelativeLayout.RIGHT_OF, base);
        layout.addView(prevButton, p2);

        nextButton = new Button(this);
        nextButton.setText(getString(R.string.add_screen_next));
        nextButton.setId(base + 2);
        RelativeLayout.LayoutParams p3 = new RelativeLayout.LayoutParams(width / 3, RelativeLayout.LayoutParams.WRAP_CONTENT); 
        p3.addRule(RelativeLayout.RIGHT_OF, base + 1);
        layout.addView(nextButton, p3);


    }

    @Override
	protected void buttonBinding(){
        newButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
    }

    @Override
    protected boolean fetchCurrentItem(){
        /* Dummy, there is no queue in this activity */
        return true;
    }

    @Override
    protected void restartActivity(){
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
            if(savedItem != null){
                currentItem = savedItem;
            }
        }
    }

    @Override
    protected void refreshAfterDeleteItem(){
        setTitle(getString(R.string.stat_total) + totalItem);
        updateMemoScreen();
        prepare();
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
                    doEdit();
                }
                else if(prediction.name.equals("cross")){
                    doDelete();
                }

            }
        }
    }

    private void getNextItem(){
        if(totalItem > 0){
            do{
                if(currentId < maxId){
                    currentId = currentId + 1;
                }
                else{
                    currentId = 1;
                }
                currentItem = dbHelper.getItemById(currentId, 0);
            }
            while(currentItem.getId() != currentId);
        }
        setTitle(getString(R.string.stat_total) + totalItem);
        updateMemoScreen();
    }




    private void getPreviousItem(){
        if(totalItem > 0){
            do{
                if(currentId > 0){
                    currentId = currentId - 1;
                }
                else{
                    currentId = maxId;
                }
                currentItem = dbHelper.getItemById(currentId, 0);
            }
            while(currentItem.getId() != currentId);
        }
        setTitle(getString(R.string.stat_total) + totalItem);
        updateMemoScreen();
    }
    
    private void createNewItem(){
        /* Reuse the doEdit to get the edit dialog
         * and display the edit dialog
         */
        savedItem = currentItem;
        Item newItem = new Item();
        newItem.setId(dbHelper.getNewId());
        currentItem = newItem;
        doEdit();
    }

        
        
}
