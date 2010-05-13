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
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;
import android.util.Log;


public class MemoScreen extends Activity implements View.OnClickListener{
	private ArrayList<Item> learnQueue;
	private DatabaseHelper dbHelper = null;
	private String dbName;
	private String dbPath;
	private boolean showAnswer;
	private Item currentItem;
    // prevItem is used to undo
    private Item prevItem = null;
    private int prevScheduledItemCount;
    private int prevNewItemCount;
    // How many words to learn at a time (rolling)
	private final int WINDOW_SIZE = 10;
	private boolean queueEmpty;
	private int idMaxSeen;
	private int scheduledItemCount;
	private int newItemCount;
	private double questionFontSize = 23.5;
	private double answerFontSize = 23.5;
	private String questionAlign = "center";
	private String answerAlign = "center";
	private String questionLocale = "US";
	private String answerLocale = "US";
	private String htmlDisplay = "none";
	private String qaRatio = "50%";
    private String textColor = "Default";
    private String bgColor = "Default";
	private TTS questionTTS;
	private TTS answerTTS;
	private boolean autoaudioSetting = true;
    private ProgressDialog mProgressDialog = null;
	private boolean questionUserAudio = false;
	private boolean answerUserAudio = false;
	private SpeakWord mSpeakWord = null;
    private Context mContext;
    private Handler mHandler;
    private AlertDialog.Builder mAlert;
    // Six grading buttons
	private Button[] btns = {null, null, null, null, null, null}; 

	private int returnValue = 0;
	private boolean initFeed;

    public final static String TAG = "org.liberty.android.fantastischmemo.MemoScreen";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memo_screen);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
		}
		initFeed = true;
		
        mHandler = new Handler();
        mContext = this;
        btns[0] = (Button) findViewById(R.id.But00);
        btns[1] = (Button) findViewById(R.id.But01);
        btns[2] = (Button) findViewById(R.id.But02);
        btns[3] = (Button) findViewById(R.id.But03);
        btns[4] = (Button) findViewById(R.id.But04);
        btns[5] = (Button) findViewById(R.id.But05);
        for(Button btn : btns){
            btn.setOnClickListener(this);
        }


        mProgressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_database), true);

		
            Thread loadingThread = new Thread(){
                public void run(){
                    // Pre load cards (The number is specified in Window size varable)
                    prepare();
                    mHandler.post(new Runnable(){
                        public void run(){
                            mProgressDialog.dismiss();
                        }
                    });
                }
            };
            loadingThread.start();

	}

	public void onResume(){
		super.onResume();
		if(returnValue == 1){
			
			prepare();
			returnValue = 0;
		}
		else{
			returnValue = 0;
		}
		
	}
	
	public void onDestroy(){
		super.onDestroy();
		dbHelper.close();
		if(questionTTS != null){
			questionTTS.shutdown();
		}
		if(answerTTS != null){
			answerTTS.shutdown();
		}
	}
	
	
	private void loadSettings(){
		// Here is the global settings from the preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	autoaudioSetting = settings.getBoolean("autoaudio", true);
		
		HashMap<String, String> hm = dbHelper.getSettings();
		Set<Map.Entry<String, String>> set = hm.entrySet();
		Iterator<Map.Entry<String, String> > i = set.iterator();
		while(i.hasNext()){
			Map.Entry<String, String> me = i.next();
			if((me.getKey().toString()).equals("question_font_size")){
				this.questionFontSize = new Double(me.getValue().toString());
			}
			if(me.getKey().toString().equals("answer_font_size")){
				this.answerFontSize = new Double(me.getValue().toString());
			}
			if(me.getKey().toString().equals("question_align")){
				this.questionAlign = me.getValue().toString();
			}
			if(me.getKey().toString().equals("answer_align")){
				this.answerAlign = me.getValue().toString();
			}
			if(me.getKey().toString().equals("question_locale")){
				this.questionLocale = me.getValue().toString();
			}
			if(me.getKey().toString().equals("answer_locale")){
				this.answerLocale = me.getValue().toString();
			}
			if(me.getKey().toString().equals("html_display")){
				this.htmlDisplay = me.getValue().toString();
			}
			if(me.getKey().toString().equals("ratio")){
				this.qaRatio = me.getValue().toString();
			}
			if(me.getKey().toString().equals("text_color")){
                this.textColor = me.getValue().toString();
            }
			if(me.getKey().toString().equals("bg_color")){
                this.bgColor = me.getValue().toString();
            }
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.memo_screen_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menuback:
	    	finish();
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
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, SettingsScreen.class);
    		myIntent.putExtra("dbname", this.dbName);
    		myIntent.putExtra("dbpath", this.dbPath);
    		startActivityForResult(myIntent, 1);
    		//finish();
    		return true;
	    	
	    case R.id.menudetail:
	    
    		Intent myIntent1 = new Intent();
    		myIntent1.setClass(this, DetailScreen.class);
    		myIntent1.putExtra("dbname", this.dbName);
    		myIntent1.putExtra("dbpath", this.dbPath);
    		myIntent1.putExtra("itemid", currentItem.getId());
    		startActivityForResult(myIntent1, 2);
    		return true;

        case R.id.menuundo:
            if(prevItem != null){
                try{
                    currentItem = (Item)prevItem.clone();
                }
                catch(CloneNotSupportedException e){
                    Log.e(TAG, "Can not clone", e);
                }
                prevItem = null;
                learnQueue.add(0, currentItem);
                if(learnQueue.size() >= WINDOW_SIZE){
                    learnQueue.remove(learnQueue.size() - 1);
                }
                newItemCount = prevNewItemCount;
                scheduledItemCount = prevScheduledItemCount;
                this.showAnswer = false;
                this.updateMemoScreen();
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

	    }
	    	
	    return false;
	}

	public boolean onTouchEvent(MotionEvent event) {
		// When the screen is touched, it will uncover answer
		int eventAction = event.getAction();
		switch (eventAction) {
		case MotionEvent.ACTION_DOWN:
			if(this.showAnswer == false){
				this.showAnswer ^= true;
				updateMemoScreen();
			}

		}
		return true;

	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode){
        
    	
    	case 1:
    	case 2:
            // Determine whether to update the screen
    		if(resultCode == Activity.RESULT_OK){
    			returnValue = 1;
    		}
    		if(resultCode == Activity.RESULT_CANCELED){
    			returnValue = 0;
    		}
    		
    		
    	}
    }
	

	private void prepare() {
		// Empty the queue, init the db
        if(dbHelper == null){
            dbHelper = new DatabaseHelper(mContext, dbPath, dbName);
        }
		learnQueue = new ArrayList<Item>();
		queueEmpty = true;
		idMaxSeen = -1;
		scheduledItemCount = dbHelper.getScheduledCount();
		newItemCount = dbHelper.getNewCount();
		loadSettings();
		// Get question and answer locale
		Locale ql;
		Locale al;
		if(questionLocale.equals("US")){
			ql = Locale.US;
		}
		else if(questionLocale.equals("DE")){
			ql = Locale.GERMAN;
		}
		else if(questionLocale.equals("UK")){
			ql = Locale.UK;
		}
		else if(questionLocale.equals("FR")){
			ql = Locale.FRANCE;
		}
		else if(questionLocale.equals("IT")){
			ql = Locale.ITALY;
		}
		else if(questionLocale.equals("ES")){
			ql = new Locale("es", "ES");
		}
		else if(questionLocale.equals("User Audio")){
			this.questionUserAudio= true;
			ql = null;
		}
		else{
			ql = null;
		}
		if(answerLocale.equals("US")){
			al = Locale.US;
		}
		else if(answerLocale.equals("DE")){
			al = Locale.GERMAN;
		}
		else if(answerLocale.equals("UK")){
			al = Locale.UK;
		}
		else if(answerLocale.equals("FR")){
			al = Locale.FRANCE;
		}
		else if(answerLocale.equals("IT")){
			al = Locale.ITALY;
		}
		else if(answerLocale.equals("ES")){
			al = new Locale("es", "ES");
		}
		else if(answerLocale.equals("User Audio")){
			this.answerUserAudio = true;
			al = null;
		}
		else{
			al = null;
		}
		if(ql != null){
			this.questionTTS = new TTS(this, ql);
		}
		else{
			this.questionTTS = null;
		}
		if(al != null){
			this.answerTTS = new TTS(this, al);
		}
		else{
			this.answerTTS = null;
		}
		if(questionUserAudio || answerUserAudio){
			mSpeakWord = new SpeakWord(this.getString(R.string.default_audio_path));
		}
		
		if(this.feedData() == 2){ // The queue is still empty
            mHandler.post(new Runnable(){
                @Override
                public void run(){
                    mAlert = new AlertDialog.Builder(mContext);
                    OnClickListener backButtonListener = new OnClickListener() {
                        // Finish the current activity and go back to the last activity.
                        // It should be the main screen.
                        public void onClick(DialogInterface arg0, int arg1) {
                            finish();
                        }
                    };
                    mAlert.setPositiveButton(getString(R.string.back_menu_text), backButtonListener );
                    mAlert.setTitle(getString(R.string.memo_no_item_title));
                    mAlert.setMessage(getString(R.string.memo_no_item_message));
                    mAlert.show();
                }
            });
			
		}
		else{
            // When feeding is done, update the screen

			
            mHandler.post(new Runnable(){
                @Override
                public void run(){
			        updateMemoScreen();
                }
            });

		}
		
	}
	

	private int feedData() {
		if(initFeed){
			initFeed = false;
			
			boolean feedResult = dbHelper.getListItems(-1, WINDOW_SIZE, learnQueue);
			if(feedResult == true){
				idMaxSeen = learnQueue.get(learnQueue.size() - 1).getId();
				return 0;
			}
			else{
				return 2;
			}
			
		}
		else{
		
		Item item;
		setTitle(getString(R.string.stat_scheduled) + scheduledItemCount + " / " + getString(R.string.stat_new) + newItemCount);
		for(int i = learnQueue.size(); i < WINDOW_SIZE; i++){
			item = dbHelper.getItemById(idMaxSeen + 1, 2); // Revision first
			if(item == null){
				item = dbHelper.getItemById(idMaxSeen + 1, 1); // Then learn new if no revision.
			}
			if(item != null){
				learnQueue.add(item);
			}
			else{
				break;
			}
			idMaxSeen = item.getId();
			
		}
		switch(learnQueue.size()){
		case 0: // No item in queue
			queueEmpty = true;
			return 2;
		case WINDOW_SIZE: // Queue full
			queueEmpty = false;
			return 0;
		default: // There are some items in the queue
			queueEmpty = false;
			return 1;
				
		}
		}
	}
			
			

	private void updateMemoScreen() {
		// update the main screen according to the currentItem
		
		OnClickListener backButtonListener = new OnClickListener() {
			// Finish the current activity and go back to the last activity.
			// It should be the open screen.
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		};
		LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
		LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
		float qRatio = Float.valueOf(qaRatio.substring(0, qaRatio.length() - 1));
		float aRatio = 100.0f - qRatio;
		qRatio /= 50.0;
		aRatio /= 50.0;
		layoutQuestion.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, qRatio));
		layoutAnswer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, aRatio));
        // Set both background and text color
        setScreenColor();
		feedData();
		if(queueEmpty == false){
			currentItem = learnQueue.get(0);

			this.displayQA(currentItem);
		}
		else{
			AlertDialog alertDialog = new AlertDialog.Builder(this)
			.create();
			alertDialog.setTitle(this.getString(R.string.memo_no_item_title));
			alertDialog.setMessage(this.getString(R.string.memo_no_item_message));
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Back",
					backButtonListener);
			alertDialog.show();
			
		}
		
	}


	private void displayQA(Item item) {
		// Display question and answer according to item
		this.setTitle(this.getTitle() + " / " + this.getString(R.string.memo_current_id) + item.getId() );
		TextView questionView = (TextView) findViewById(R.id.question);
		TextView answerView = (TextView) findViewById(R.id.answer);
		
		
		if(this.htmlDisplay.equals("both")){
			CharSequence sq = Html.fromHtml(item.getQuestion());
			CharSequence sa = Html.fromHtml(item.getAnswer());
			
			questionView.setText(sq);
			answerView.setText(sa);
			
		}
		else if(this.htmlDisplay.equals("question")){
			CharSequence sq = Html.fromHtml(item.getQuestion());
			questionView.setText(sq);
			answerView.setText(new StringBuilder().append(item.getAnswer()));
		}
		else if(this.htmlDisplay.equals("answer")){
			questionView.setText(new StringBuilder().append(item.getQuestion()));
			CharSequence sa = Html.fromHtml(item.getAnswer());
			answerView.setText(sa);
		}
		else{
			questionView.setText(new StringBuilder().append(item.getQuestion()));
			answerView.setText(new StringBuilder().append(item.getAnswer()));
		}
		
		if(questionAlign.equals("center")){
			questionView.setGravity(Gravity.CENTER);
			LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutQuestion.setGravity(Gravity.CENTER);
			layoutAnswer.setGravity(Gravity.CENTER);
		}
		else if(questionAlign.equals("right")){
			questionView.setGravity(Gravity.RIGHT);
			LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutQuestion.setGravity(Gravity.NO_GRAVITY);
			layoutAnswer.setGravity(Gravity.NO_GRAVITY);
		}
		else{
			questionView.setGravity(Gravity.LEFT);
			LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutQuestion.setGravity(Gravity.NO_GRAVITY);
			layoutAnswer.setGravity(Gravity.NO_GRAVITY);
		}
		if(answerAlign.equals("center")){
			answerView.setGravity(Gravity.CENTER);
		} else if(answerAlign.equals("right")){
			answerView.setGravity(Gravity.RIGHT);
			
		}
		else{
			answerView.setGravity(Gravity.LEFT);
		}
		questionView.setTextSize((float)questionFontSize);
		answerView.setTextSize((float)answerFontSize);

		int status= -10;
		if(autoaudioSetting){
			if(this.showAnswer == false){
				if(questionTTS != null){
					status = questionTTS.sayText(currentItem.getQuestion());
				}
				else if(questionUserAudio){
					mSpeakWord.speakWord(currentItem.getQuestion());
					
				}
			}
			else{
				if(answerTTS != null){
					status = answerTTS.sayText(currentItem.getAnswer());
				}
				else if(answerUserAudio){
					mSpeakWord.speakWord(currentItem.getAnswer());
					
				}
			}
		}
		if(status == 0 || status != 0){
			status = status + 1 -1;
		}
		this.buttonBinding();

	}

    @Override
    public void onClick(View v){
        for(int i = 0; i < btns.length; i++){
            if(v == btns[i]){
                // i is also the grade for the button
                int grade = i;
                // When user click on the button of grade, it will update the item information
                // according to the grade.
                // If the return value is success, the user will not need to see this item today.
                // If the return value is failure, the item will be appended to the tail of the queue.

                prevScheduledItemCount = scheduledItemCount;
                prevNewItemCount = newItemCount;

                try{
                    prevItem = (Item)currentItem.clone();
                }
                catch(CloneNotSupportedException e){
                    Log.e(TAG, "Can not clone", e);
                }


                boolean scheduled = currentItem.isScheduled();
                boolean success = currentItem.processAnswer(grade, false) > 0 ? true : false;
                if (success == true) {
                    learnQueue.remove(0);
                    if(queueEmpty != true){
                        dbHelper.updateItem(currentItem);
                    }
                    if(scheduled){
                        this.scheduledItemCount -= 1;
                    }
                    else{
                        this.newItemCount -= 1;
                    }
                } else {
                    learnQueue.remove(0);
                    learnQueue.add(currentItem);
                    dbHelper.updateItem(currentItem);
                    if(!scheduled){
                        this.scheduledItemCount += 1;
                        this.newItemCount -= 1;
                    }
                    
                }

                this.showAnswer = false;
                // Now the currentItem is the next item, so we need to udpate the screen.
                this.updateMemoScreen();
                break;
            }
        }

    }

	private void buttonBinding() {
		// This function will bind the button event and show/hide button
		// according to the showAnswer varible.
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
            // Show all buttons when user has clicked the screen.
            for(Button btn : btns){
			    btn.setVisibility(View.VISIBLE);
            }
            String[] btnsText = {getString(R.string.memo_btn0_text),getString(R.string.memo_btn1_text),getString(R.string.memo_btn2_text),getString(R.string.memo_btn3_text),getString(R.string.memo_btn4_text),getString(R.string.memo_btn5_text)};
            for(int i = 0; i < btns.length; i++){
                // This part will display the days to review
                    btns[i].setText(btnsText[i] + "\n+" + currentItem.processAnswer(i, true));
            }
		}
	}
    
    private void setScreenColor(){
        // Set both text and the background color
		TextView questionView = (TextView) findViewById(R.id.question);
		TextView answerView = (TextView) findViewById(R.id.answer);
        LinearLayout root = (LinearLayout)findViewById(R.id.memo_screen_root);
        int[] colorMap = {Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW};
    	String[] colorList = getResources().getStringArray(R.array.color_list);
        if(!textColor.equals("Default")){
            for(int i = 0; i < colorMap.length; i++){
                if(textColor.equals(colorList[i])){
                    questionView.setTextColor(colorMap[i - 1]);
                    answerView.setTextColor(colorMap[i - 1]);
                    break;
                }
            }
        }
        if(!bgColor.equals("Default")){
            for(int i = 0; i < colorMap.length; i++){
                if(bgColor.equals(colorList[i])){
                    if(root!= null){
                        root.setBackgroundColor(colorMap[i - 1]);
                    }
                    break;
                }
            }
        }
    }

}
