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


public class EditScreen extends MemoScreenBase implements OnGesturePerformedListener{

    private int currentId = -1;
    private int totalItem = -1;
    private int maxId = -1;
    private Context mContext;
    private Handler mHandler;
    private GestureLibrary mLibrary;
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
        //LinearLayout root = (LinearLayout)findViewById(R.id.memo_screen_root);
        //LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 0);
        //GestureOverlayView goView = new GestureOverlayView(this);
        //goView.addOnGesturePerformedListener(this);
        //root.addView(goView, p);
        //
        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gesture_overlay);
        gestures.addOnGesturePerformedListener(this);
        prepare();
    }

    @Override
	public void onResume(){
        super.onResume();
        /* Refresh depending on where it returns. */
		if(returnValue == 1){
			prepare();
			returnValue = 0;
		}
		else{
			returnValue = 0;
		}
    }

    @Override
	public void onDestroy(){
        super.onDestroy();
		dbHelper.close();
    }

    @Override
    protected void prepare(){
        if(dbHelper == null){
            dbHelper = new DatabaseHelper(mContext, dbPath, dbName);
        }
		loadSettings();
        if(currentId == -1){
            currentId = 1;
        }
        if(maxId == -1){
            maxId = dbHelper.getNewId() - 1;
        }
        totalItem = dbHelper.getTotalCount();
        currentItem = dbHelper.getItemById(currentId, 0);
        if(currentItem == null){
            new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.memo_no_item_title))
                .setMessage(getString(R.string.memo_no_item_message))
                .setPositiveButton(getString(R.string.back_menu_text),new OnClickListener() {
                // Finish the current activity and go back to the last activity.
                // It should be the main screen.
                public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                })
                .create()
                .show();
        }
		setTitle(getString(R.string.stat_total) + totalItem);
        updateMemoScreen();
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
        return false;
    }

    @Override
    protected void createButtons(){
    }

    @Override
	protected void buttonBinding(){
    }

    @Override
    protected boolean fetchCurrentItem(){
        return true;
    }

    @Override
    protected void restartActivity(){
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
                    do{
                        if(currentId <= 1){
                            currentId = 1;
                            break;
                        }
                        currentId -= 1;
                        currentItem = dbHelper.getItemById(currentId, 0);
                    }
                    while(currentItem.getId() != currentId);

                }
                else if(prediction.name.equals("swipe-left")){
                    do{
                        if(currentId >= maxId){
                            currentId = 1;
                            break;
                        }
                        currentId += 1;
                        currentItem = dbHelper.getItemById(currentId, 0);
                    }
                    while(currentItem.getId() != currentId);
                }
                else if(prediction.name.equals("o") || prediction.name.equals("o2")){
                    doEdit();
                }
                else if(prediction.name.equals("cross")){
                    if(currentId == maxId){
                        maxId -= 1;
                    }
                    doDelete();
                    currentId = (currentId != maxId) ? currentId + 1 : maxId;
                    totalItem -= 1;
                }

		        setTitle(getString(R.string.stat_total) + totalItem);
                updateMemoScreen();
            }
        }
    }

}
