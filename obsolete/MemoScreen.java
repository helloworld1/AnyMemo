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
import org.liberty.android.fantastischmemo.tts.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Collections;

import android.graphics.Color;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.KeyEvent;
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import android.os.SystemClock;
import android.net.Uri;


public class MemoScreen extends MemoScreenBase implements View.OnClickListener, View.OnLongClickListener{
	private ArrayList<Item> learnQueue;
    /* prevItem is used to undo */
    private Item prevItem = null;
    private int prevScheduledItemCount;
    private int prevNewItemCount;
    /* How many words to learn at a time (rolling) */
	private int learningQueueSize= 10;
	private int maxNewId;
	private int maxRetId;
	private int scheduledItemCount;
	private int newItemCount;
	private AnyMemoTTS questionTTS;
	private AnyMemoTTS answerTTS;
    private Context mContext;
    private SpeakWord mSpeakWord;
    private boolean learnAhead;
    private boolean enableTTSExtended = false;
    private boolean shufflingCards = false;
    private boolean volumeKeyShortcut = false;
    /* Six grading buttons */
	private Button[] btns = {null, null, null, null, null, null}; 

	private boolean initFeed;
    /* Set an id of the loading dialog */
    private final int DIALOG_LOADING_PROGRESS = 20;

    public final static String TAG = "org.liberty.android.fantastischmemo.MemoScreen";

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

   
		setContentView(R.layout.memo_screen);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
            learnAhead = extras.getBoolean("learn_ahead");
		}
		initFeed = true;
		
        mHandler = new Handler();
        mContext = this;
        createButtons();
        for(Button btn : btns){
            btn.setOnClickListener(this);
            btn.setOnLongClickListener(this);
        }
        /* Load some global settings */
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        LinearLayout root = (LinearLayout)findViewById(R.id.memo_screen_root);

        //root.setOnClickListener(this);
        root.setOnClickListener(this);
        root.setOnLongClickListener(this);
        /* Click the q and a to speak if this option is 
         * enabled */
		TextView answerView = (TextView) findViewById(R.id.answer);
		TextView questionView= (TextView) findViewById(R.id.question);
        questionView.setOnClickListener(this);
        questionView.setOnLongClickListener(this);
        answerView.setOnClickListener(this);
        answerView.setOnLongClickListener(this);



		
        Thread loadingThread = new Thread(){
            public void run(){
                /* Pre load cards (The number is specified in Window size varable) */
                mHandler.post(new Runnable(){
                    public void run(){
                        removeDialog(DIALOG_LOADING_PROGRESS);
                        showDialog(DIALOG_LOADING_PROGRESS);
                    }
                });

                final boolean isPrepared = prepare();
                mHandler.post(new Runnable(){
                    public void run(){
                        removeDialog(DIALOG_LOADING_PROGRESS);
                        if(isPrepared == false){
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
                });
            }
        };
        loadingThread.start();

	}


    @Override
	public void onDestroy(){
		super.onDestroy();
        removeDialog(DIALOG_LOADING_PROGRESS);
        try{
            dbHelper.close();
        }
        catch(Exception e){
        }
		if(questionTTS != null){
			questionTTS.shutdown();
		}
		if(answerTTS != null){
			answerTTS.shutdown();
		}
	}

    @Override
    protected void restartActivity(){
        /* restart the current activity */
        Intent myIntent = new Intent();
        myIntent.setClass(MemoScreen.this, MemoScreen.class);
        myIntent.putExtra("dbname", dbName);
        myIntent.putExtra("dbpath", dbPath);
        myIntent.putExtra("active_filter", activeFilter);
        myIntent.putExtra("learn_ahead", learnAhead);
        finish();
        startActivity(myIntent);
    }
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.memo_screen_menu, menu);
		return true;
	}
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent();
	    switch (item.getItemId()) {
	    case R.id.menu_memo_help:
            myIntent.setAction(Intent.ACTION_VIEW);
            myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            myIntent.setData(Uri.parse(getString(R.string.website_help_memo)));
            startActivity(myIntent);
	        return true;
	    case R.id.menuspeakquestion:
	    	if(questionTTS != null){
	    		questionTTS.sayText(this.currentItem.getQuestion());
	    	}
	    	else if(questionUserAudio){
	    		mSpeakWord.speakWord(currentItem.getQuestion());
	    	}
	    	return true;
	    	
	    case R.id.menuspeakanswer:
	    	if(answerTTS != null){
	    		answerTTS.sayText(this.currentItem.getAnswer());
	    	}
	    	else if(answerUserAudio){
	    		mSpeakWord.speakWord(currentItem.getAnswer());
	    	}
	    	return true;
	    	
	    case R.id.menusettings:
    		myIntent.setClass(this, SettingsScreen.class);
    		myIntent.putExtra("dbname", this.dbName);
    		myIntent.putExtra("dbpath", this.dbPath);
    		startActivityForResult(myIntent, 1);
    		//finish();
    		return true;
	    	
	    case R.id.menudetail:
    		myIntent.setClass(this, DetailScreen.class);
    		myIntent.putExtra("dbname", this.dbName);
    		myIntent.putExtra("dbpath", this.dbPath);
    		myIntent.putExtra("itemid", currentItem.getId());
    		startActivityForResult(myIntent, 2);
    		return true;

        case R.id.menuundo:
            if(prevItem != null){
                currentItem = (Item)prevItem.clone();
                prevItem = null;
                learnQueue.add(0, currentItem);
                if(learnQueue.size() >= learningQueueSize){
                    learnQueue.remove(learnQueue.size() - 1);
                }
                newItemCount = prevNewItemCount;
                scheduledItemCount = prevScheduledItemCount;
                showAnswer = false;
                updateMemoScreen();
                autoSpeak();
            }
            else{
                new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.undo_fail_text))
                    .setMessage(getString(R.string.undo_fail_message))
                    .setNeutralButton(R.string.ok_text, null)
                    .create()
                    .show();
            }
            return true;

        case R.id.menu_memo_filter:
            doFilter();
            return true;

	    }
	    	
	    return false;
	}

    @Override
	protected boolean prepare() {
		/* Empty the queue, init the db */
        if(dbHelper == null){
            try{
                dbHelper = new DatabaseHelper(mContext, dbPath, dbName);
            }
            catch(Exception e){
                Log.e(TAG, "Error" + e.toString(), e);
                return false;
            }

        }
		learnQueue = new ArrayList<Item>();
        maxNewId = -1;
        maxRetId = -1;
		scheduledItemCount = dbHelper.getScheduledCount();
		newItemCount = dbHelper.getNewCount();
		loadSettings();
		Locale ql = new Locale(questionLocale.toLowerCase());
		Locale al = new Locale(answerLocale.toLowerCase());;
		/* Get question and answer locale */
		if(questionLocale.equals("US")){
			ql = Locale.US;
		}
		else if(questionLocale.equals("UK")){
			ql = Locale.UK;
		}
		else if(questionLocale.equals("User Audio")){
			this.questionUserAudio= true;
			ql = null;
		}
		else if(questionLocale.equals("") || questionLocale.toLowerCase().equals("disabled") || questionLocale.toLowerCase().equals("other")){
			ql = null;
		}
		if(answerLocale.equals("US")){
			al = Locale.US;
		}
		else if(answerLocale.equals("UK")){
			al = Locale.UK;
		}
		else if(answerLocale.equals("User Audio")){
			this.answerUserAudio = true;
			al = null;
		}
		else if(answerLocale.equals("") || answerLocale.toLowerCase().equals("disabled") || answerLocale.toLowerCase().equals("other")){
            al = null;
        }
		if(ql != null){
            if(enableTTSExtended){
                questionTTS = new AnyMemoTTSExtended(this ,ql);
            }
            else{
                questionTTS = new AnyMemoTTSPlatform(this, ql);
            }
		}
		else{
			this.questionTTS = null;
		}
		if(al != null){
            if(enableTTSExtended){
                answerTTS = new AnyMemoTTSExtended(this ,al);
            }
            else{
                answerTTS = new AnyMemoTTSPlatform(this, al);
            }
		}
		else{
			this.answerTTS = null;
		}
		if(questionUserAudio || answerUserAudio){
			mSpeakWord = new SpeakWord(audioLocation, dbName);
		}
		
		if(this.feedData() == 2){ // The queue is still empty
            mHandler.post(new Runnable(){
                @Override
                public void run(){
                    new AlertDialog.Builder(mContext)
                        .setTitle(getString(R.string.memo_no_item_title))
                        .setMessage(getString(R.string.memo_no_item_message))
                        .setPositiveButton(getString(R.string.back_menu_text),new OnClickListener() {
                            /* Finish the current activity and go back to the last activity.
                             *It should be the main screen.
                             */
                            public void onClick(DialogInterface arg0, int arg1) {
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.learn_ahead), new OnClickListener(){
                        public void onClick(DialogInterface arg0, int arg1) {
                            finish();
                            Intent myIntent = new Intent();
                            myIntent.setClass(MemoScreen.this, MemoScreen.class);
                            myIntent.putExtra("dbname", dbName);
                            myIntent.putExtra("dbpath", dbPath);
                            myIntent.putExtra("learn_ahead", true);
                            myIntent.putExtra("active_filter", activeFilter);
                            startActivity(myIntent);
                        }
                    })
                        .setOnCancelListener(new DialogInterface.OnCancelListener(){
                            public void onCancel(DialogInterface dialog){
                                finish();
                            }
                        })

                        .create()
                        .show();
                    
                }
            });
		}
		else{
            // When feeding is done, update the screen

			
            mHandler.post(new Runnable(){
                @Override
                public void run(){
			        updateMemoScreen();
                    autoSpeak();
                }
            });

		}
        return true;
		
	}
	

	protected int feedData() {
		if(initFeed){
			initFeed = false;
			
            boolean feedResult;
            if(learnAhead){
                feedResult = dbHelper.getListItems(-1, learningQueueSize, learnQueue, 3, activeFilter);
            }
            else{
                feedResult = dbHelper.getListItems(-1, learningQueueSize, learnQueue, 4, activeFilter);
            }
			if(feedResult == true){
                for(int i = 0; i < learnQueue.size(); i++){
                    Item qItem = learnQueue.get(i);
                    if(qItem.isScheduled()){
                        if(maxRetId < qItem.getId()){
                            maxRetId = qItem.getId();
                        }
                    }
                    else{
                        if(maxNewId < qItem.getId()){
                            maxNewId = qItem.getId();
                        }
                    }

                }
                /* Shuffling the queue */
                if(shufflingCards){
                    Collections.shuffle(learnQueue);
                }
                currentItem = learnQueue.get(0);
				return 0;
			}
			else{
                currentItem = null;
                return 2;
			}
			
		}
		else{
            Item item;
            for(int i = learnQueue.size(); i < learningQueueSize; i++){
                if(learnAhead){
                    /* Flag = 3 for randomly choose item from future */
                    item = dbHelper.getItemById(0, 3, true, activeFilter);
                    learnQueue.add(item);
                }
                else{
                    /* Search out the maxRetId and maxNew Id
                     * before queuing in order to achive consistence
                     */
                    for(int j = 0; j < learnQueue.size(); j++){
                        Item qItem = learnQueue.get(j);
                        if(qItem.isScheduled()){
                            if(maxRetId < qItem.getId()){
                                maxRetId = qItem.getId();
                            }
                        }
                        else{
                            if(maxNewId < qItem.getId()){
                                maxNewId = qItem.getId();
                            }
                        }

                    }
                    item = dbHelper.getItemById(maxRetId + 1, 2, true, activeFilter); // Revision first
                    if(item != null){
                        maxRetId = item.getId();
                    }
                    else{
                        item = dbHelper.getItemById(maxNewId + 1, 1, true, activeFilter); // Then learn new if no revision.
                        if(item != null){
                            maxNewId = item.getId();
                        }
                    }
                    if(item != null){
                        learnQueue.add(item);
                    }
                    else{
                        break;
                    }
                }
            }
            int size = learnQueue.size();
            if(size == 0){
                /* No new items */
                currentItem = null;
                return 2;
            }
            else if(size == learningQueueSize){
                currentItem = learnQueue.get(0);
                return 0;
            }
            else{
                /* Shuffling the queue */
                if(shufflingCards){
                    Collections.shuffle(learnQueue);
                }
                currentItem = learnQueue.get(0);
                return 1;
            }
		}
	}

    
    // 1.6 compatibility issue, this can be enabled in the future
    //@Override
    //public boolean onKeyDown(int keyCode, KeyEvent event){
    //    /* According to http://android-developers.blogspot.com/2009/12/back-and-other-hard-keys-three-stories.html */
    //    if(volumeKeyShortcut){
    //        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
    //            event.startTracking();
    //            return true;
    //        }
    //        else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
    //            event.startTracking();
    //            return true;
    //        }
    //    }
    //    return super.onKeyDown(keyCode, event);
    //}

    //@Override
    //public boolean onKeyLongPress(int keyCode, KeyEvent event){
    //    /* Long press to scroe the card */
    //    if(volumeKeyShortcut){
    //        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
    //            if(showAnswer == false){
    //                TextView answerView = (TextView) findViewById(R.id.answer);
    //                /* Reveal the answer if it is now shown */
    //                this.onClick(answerView);
    //            }
    //            else{
    //                Toast.makeText(this, getString(R.string.grade_text) + " 2", Toast.LENGTH_SHORT).show();
    //                handleGradeButtonClick(2);
    //            }
    //            return true;
    //        }
    //        else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
    //            if(showAnswer == false){
    //                TextView answerView = (TextView) findViewById(R.id.answer);
    //                this.onClick(answerView);
    //            }
    //            else{
    //                Toast.makeText(this, getString(R.string.grade_text) + " 4", Toast.LENGTH_SHORT).show();
    //                handleGradeButtonClick(4);
    //            }
    //            return true;
    //        }
    //    }
    //    return super.onKeyLongPress(keyCode, event);
    //}

    //@Override
    //public boolean onKeyUp(int keyCode, KeyEvent event){
    //    /* Short press to scroe the card */

    //    if(volumeKeyShortcut){
    //        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.isTracking() && !event.isCanceled()){
    //            if(showAnswer == false){
    //                TextView answerView = (TextView) findViewById(R.id.answer);
    //                this.onClick(answerView);
    //            }
    //            else{
    //                Toast.makeText(this, getString(R.string.grade_text) + " 0", Toast.LENGTH_SHORT).show();
    //                handleGradeButtonClick(0);
    //            }
    //            return true;
    //        }
    //        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.isTracking() && !event.isCanceled()){
    //            if(showAnswer == false){
    //                TextView answerView = (TextView) findViewById(R.id.answer);
    //                this.onClick(answerView);
    //            }
    //            else{
    //                Toast.makeText(this, getString(R.string.grade_text) + " 3", Toast.LENGTH_SHORT).show();
    //                handleGradeButtonClick(3);
    //            }
    //            return true;
    //        }
    //    }
    //    return super.onKeyUp(keyCode, event);
    //}
    
    /* This version only handle short click */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(volumeKeyShortcut){
            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                return true;
            }
            else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        /* Short press to scroe the card */

        if(volumeKeyShortcut){
            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                if(showAnswer == false){
                    TextView answerView = (TextView) findViewById(R.id.answer);
                    this.onClick(answerView);
                }
                else{
                    Toast.makeText(this, getString(R.string.grade_text) + " 0", Toast.LENGTH_SHORT).show();
                    handleGradeButtonClick(0);
                }
                return true;
            }
            if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                if(showAnswer == false){
                    TextView answerView = (TextView) findViewById(R.id.answer);
                    this.onClick(answerView);
                }
                else{
                    Toast.makeText(this, getString(R.string.grade_text) + " 3", Toast.LENGTH_SHORT).show();
                    handleGradeButtonClick(3);
                }
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }


			
    @Override
    public void onClick(View v){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        String[] speechCtlList = getResources().getStringArray(R.array.speech_ctl_list);
        String[] touchAreaList = getResources().getStringArray(R.array.touch_area_list);
        String touchArea = settings.getString("touch_area", touchAreaList[0]);
        LinearLayout root = (LinearLayout)findViewById(R.id.memo_screen_root);
		TextView answerView = (TextView) findViewById(R.id.answer);
		TextView questionView= (TextView) findViewById(R.id.question);

        if((speechCtl.equals(speechCtlList[1]) || speechCtl.equals(speechCtlList[3])) && v == questionView ){
            if(questionTTS != null){
                questionTTS.sayText(currentItem.getQuestion());
            }
            else if(questionUserAudio){
                mSpeakWord.speakWord(currentItem.getQuestion());
            }
        }
        else if(touchArea.equals(touchAreaList[0]) && showAnswer == false && (v == root || v == questionView || v == answerView)){
            this.showAnswer = true;
            updateMemoScreen();
            autoSpeak();
        }

        else if(v == answerView){
            if(showAnswer == false){
                this.showAnswer = true;
                updateMemoScreen();
                autoSpeak();
            }

            else if((speechCtl.equals(speechCtlList[1]) || speechCtl.equals(speechCtlList[3]))){
                /* showAnswer is ture so autoSpeak will speak answer */
                if(answerTTS != null){
                    answerTTS.sayText(currentItem.getAnswer());
                }
                else if(answerUserAudio){
                    mSpeakWord.speakWord(currentItem.getAnswer());
                }
            }
        }


        for(int i = 0; i < btns.length; i++){
            if(v == btns[i]){
                /* i is also the grade for the button */
                handleGradeButtonClick(i);
                break;
            }
        }

    }

    private void handleGradeButtonClick(int grade){
        /* When user click on the button of grade, it will update the item information
         * according to the grade.
         * If the return value is success, the user will not need to see this item today.
         * If the return value is failure, the item will be appended to the tail of the queue. 
         * */

        if(currentItem == null){
            /* 
             * Prevent problems when clicking button too fast 
             * because they can click it before the "empty database" dialog
             * showing up.
             */
            return;
        }

        prevScheduledItemCount = scheduledItemCount;
        prevNewItemCount = newItemCount;

        prevItem = (Item)currentItem.clone();


        boolean scheduled = currentItem.isScheduled();
        /* The processAnswer will return the interval
         * if it is 0, it means failure.
         */
        boolean success = currentItem.processAnswer(grade, false) > 0 ? true : false;
        if (success == true) {
            if(learnQueue.size() != 0){
                learnQueue.remove(0);
            }
            dbHelper.updateItem(currentItem, false);
            if(scheduled){
                this.scheduledItemCount -= 1;
            }
            else{
                this.newItemCount -= 1;
            }
        } else {
            if(learnQueue.size() != 0){
                learnQueue.remove(0);
            }
            learnQueue.add(currentItem);
            dbHelper.updateItem(currentItem, false);
            if(!scheduled){
                this.scheduledItemCount += 1;
                this.newItemCount -= 1;
            }
            
        }

        this.showAnswer = false;
        /* Now the currentItem is the next item, so we need to udpate the screen. */
        feedData();
        updateMemoScreen();
        autoSpeak();
    }

    @Override
    public boolean onLongClick(View v){
        LinearLayout root = (LinearLayout)findViewById(R.id.memo_screen_root);
		TextView answerView = (TextView) findViewById(R.id.answer);
		TextView questionView= (TextView) findViewById(R.id.question);
        if(v == root || v == answerView || v == questionView){
            showEditDialog();
            return true;
        }
        String[] helpText = {getString(R.string.memo_btn0_help_text),getString(R.string.memo_btn1_help_text),getString(R.string.memo_btn2_help_text),getString(R.string.memo_btn3_help_text),getString(R.string.memo_btn4_help_text),getString(R.string.memo_btn5_help_text)};
        for(int i = 0; i < 6; i++){
            if(v == btns[i]){
                Toast.makeText(this, helpText[i], Toast.LENGTH_SHORT).show();
            }
        }


        return false;
    }
        

    @Override
	protected void buttonBinding() {
		/* This function will bind the button event and show/hide button
         * according to the showAnswer varible.
         * */
		TextView answer = (TextView) findViewById(R.id.answer);
		if (showAnswer == false) {
            for(Button btn : btns){
                btn.setVisibility(View.INVISIBLE);
            }
			answer.setText(new StringBuilder().append(this.getString(R.string.memo_show_answer)));
			answer.setGravity(Gravity.CENTER);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutAnswer.setGravity(Gravity.CENTER);

		} else {
            String[] btnStyleList = getResources().getStringArray(R.array.button_style_list);

            for(Button btn : btns){
			    btn.setVisibility(View.VISIBLE);
            }
            if(btnStyle.equals(btnStyleList[1])){
                /* Mnemosyne, one line button */
                /* Do we still keep the 0 button? */
                //btns[0].setVisibility(View.GONE);
                String[] btnsText = {getString(R.string.memo_btn0_brief_text),getString(R.string.memo_btn1_brief_text),getString(R.string.memo_btn2_brief_text),getString(R.string.memo_btn3_brief_text),getString(R.string.memo_btn4_brief_text),getString(R.string.memo_btn5_brief_text)};
                for(int i = 0; i < btns.length; i++){
                    btns[i].setText(btnsText[i]);
                }
            }
            else if(btnStyle.equals(btnStyleList[2])){
                /* Anki, no grade 1 and 2 */
                btns[1].setVisibility(View.GONE);
                btns[2].setVisibility(View.GONE);
                String[] btnsText = {getString(R.string.memo_btn0_anki_text),getString(R.string.memo_btn1_anki_text),getString(R.string.memo_btn2_anki_text),getString(R.string.memo_btn3_anki_text),getString(R.string.memo_btn4_anki_text),getString(R.string.memo_btn5_anki_text)};
                for(int i = 0; i < btns.length; i++){
                    btns[i].setText(btnsText[i]);
                    if(!learnAhead){
                        btns[i].setText(btnsText[i] + "\n+" + currentItem.processAnswer(i, true));
                    }
                    else{
                        btns[i].setText(btnsText[i]);
                    }
                }
            }
            else{
            // This is only for two line mode
            // Show all buttons when user has clicked the screen.
                String[] btnsText = {getString(R.string.memo_btn0_text),getString(R.string.memo_btn1_text),getString(R.string.memo_btn2_text),getString(R.string.memo_btn3_text),getString(R.string.memo_btn4_text),getString(R.string.memo_btn5_text)};
                for(int i = 0; i < btns.length; i++){
                // This part will display the days to review
                    if(!learnAhead){
                        btns[i].setText(btnsText[i] + "\n+" + currentItem.processAnswer(i, true));
                    }
                    else{
                        /* In cram review mode, we do not estimate the 
                         * days to learn 
                         */
                        btns[i].setText(btnsText[i]);
                    }
                }
            }
        }
	}
    

    @Override
    protected void createButtons(){
        /* First load the settings */
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String[] btnStyleList = getResources().getStringArray(R.array.button_style_list);
        btnStyle = settings.getString("button_style", btnStyleList[0]);
        LinearLayout root = (LinearLayout)findViewById(R.id.layout_buttons);
        if(btnStyle.equals(btnStyleList[1])){
            LayoutInflater.from(this).inflate(R.layout.grade_buttons_mnemosyne, root);
            btns[0] = (Button)findViewById(R.id.grade_btn_mnemosyne_0);
            btns[1] = (Button)findViewById(R.id.grade_btn_mnemosyne_1);
            btns[2] = (Button)findViewById(R.id.grade_btn_mnemosyne_2);
            btns[3] = (Button)findViewById(R.id.grade_btn_mnemosyne_3);
            btns[4] = (Button)findViewById(R.id.grade_btn_mnemosyne_4);
            btns[5] = (Button)findViewById(R.id.grade_btn_mnemosyne_5);
        }
        else if(btnStyle.equals(btnStyleList[2])){
            LayoutInflater.from(this).inflate(R.layout.grade_buttons_anki, root);
            btns[0] = (Button)findViewById(R.id.grade_btn_anki_0);
            btns[1] = (Button)findViewById(R.id.grade_btn_anki_1);
            btns[2] = (Button)findViewById(R.id.grade_btn_anki_2);
            btns[3] = (Button)findViewById(R.id.grade_btn_anki_3);
            btns[4] = (Button)findViewById(R.id.grade_btn_anki_4);
            btns[5] = (Button)findViewById(R.id.grade_btn_anki_5);
        }
        else{
            LayoutInflater.from(this).inflate(R.layout.grade_buttons_anymemo, root);
            btns[0] = (Button)findViewById(R.id.grade_btn_anymemo_0);
            btns[1] = (Button)findViewById(R.id.grade_btn_anymemo_1);
            btns[2] = (Button)findViewById(R.id.grade_btn_anymemo_2);
            btns[3] = (Button)findViewById(R.id.grade_btn_anymemo_3);
            btns[4] = (Button)findViewById(R.id.grade_btn_anymemo_4);
            btns[5] = (Button)findViewById(R.id.grade_btn_anymemo_5);
        }

    }

    private void autoSpeak(){
        if(currentItem == null){
            return;
        }
        String[] speechCtlList = getResources().getStringArray(R.array.speech_ctl_list);

        if(speechCtl.equals(speechCtlList[2]) || speechCtl.equals(speechCtlList[3])){
            if(this.showAnswer == false){
                if(questionTTS != null){
                    questionTTS.sayText(currentItem.getQuestion());
                }
                else if(questionUserAudio){
                    mSpeakWord.speakWord(currentItem.getQuestion());

                }
            }
            else{
                if(answerTTS != null){
                    answerTTS.sayText(currentItem.getAnswer());
                }
                else if(answerUserAudio){
                    mSpeakWord.speakWord(currentItem.getAnswer());

                }
            }
        }
    }

    @Override
    protected void refreshAfterEditItem(){
        updateMemoScreen();
    }

    @Override
    protected void refreshAfterDeleteItem(){
        restartActivity();
    }

    @Override
    protected Dialog onCreateDialog(int id){
        switch(id){
            case DIALOG_LOADING_PROGRESS:{
                ProgressDialog progressDialog = new ProgressDialog(MemoScreen.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle(getString(R.string.loading_please_wait));
                progressDialog.setMessage(getString(R.string.loading_database));
                progressDialog.setCancelable(false);

                return progressDialog;
            }
            default:
                return super.onCreateDialog(id);

        }
    }

    @Override
	protected void displayQA(Item item) {
        /* Override this method to display the title bar properly */
        super.displayQA(item);
        if(!learnAhead){
            setTitle(getString(R.string.stat_scheduled) + scheduledItemCount + " / " + getString(R.string.stat_new) + newItemCount + " / " + getString(R.string.memo_current_id) + currentItem.getId() + " / " +  currentItem.getCategory());
        }
        else{
            setTitle(getString(R.string.learn_ahead) + " / " + getString(R.string.memo_current_id) + currentItem.getId() + " / " + currentItem.getCategory());
        }

    }


}
