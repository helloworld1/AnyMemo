package org.liberty.android.fantastischmemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MemoScreen extends Activity{
	//
	private ArrayList<Item> learnQueue;
	private DatabaseHelper dbHelper;
	private String dbName;
	private String dbPath;
	private boolean showAnswer;
	private int newGrade = -1;
	private Item currentItem;
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
	private TTS questionTTS;
	private TTS answerTTS;
	private boolean autoaudioSetting = true;
	private AlertDialog loadingDialog = null;
	
	private int returnValue = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memo_screen);
		//Debug.startMethodTracing("memo");
		
		// The extra mode field is passed from intent.
		// acq and rev should be different processes in different learning algorithm
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
		}
		
		/*
		OnDismissListener dismissListener = new OnDismissListener(){
			public void onDismiss(DialogInterface arg0){
				//
				if(autoaudioSetting == true){
				}
				updateMemoScreen();
			}
		};
		OnClickListener okButtonListener = new OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1){
				updateMemoScreen();
			}
		};
			
		
		
		prepare();
		
		if(questionTTS.sayText("") != TextToSpeech.SUCCESS && autoaudioSetting == true){
			loadingDialog = new AlertDialog.Builder(this).create();
			loadingDialog.setTitle("Alert");
			loadingDialog.setMessage("The automatic question and answer speaking is now on!");
			//loadingDialog.setOnDismissListener(dismissListener);
			loadingDialog.setButton("OK", okButtonListener);
			loadingDialog.show();
		}
		else{
			this.updateMemoScreen();
			
		}*/
		OnDismissListener dismissListener = new OnDismissListener(){
			public void onDismiss(DialogInterface arg0){
				//
				updateMemoScreen();
			}
		};
		
		loadingDialog = new AlertDialog.Builder(this).create();
		loadingDialog.setMessage("Loading");
		loadingDialog.setOnDismissListener(dismissListener);
		loadingDialog.show();
		prepare();
	}
	public void onResume(){
		super.onResume();
		if(returnValue == 1){
			OnDismissListener dismissListener = new OnDismissListener(){
				public void onDismiss(DialogInterface arg0){
					//
					updateMemoScreen();
				}
			};
			
			loadingDialog = new AlertDialog.Builder(this).create();
			loadingDialog.setMessage("Loading");
			loadingDialog.setOnDismissListener(dismissListener);
			loadingDialog.show();
			prepare();
			returnValue = 0;
		}
		else{
			returnValue = 0;
		}
		
		//this.updateMemoScreen();
	}
	
	public void onDestroy(){
		super.onDestroy();
		dbHelper.close();
		questionTTS.shutdown();
		answerTTS.shutdown();
		//Debug.stopMethodTracing();
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
	    	questionTTS.sayText(this.currentItem.getQuestion());
	    	return true;
	    	
	    case R.id.menuspeakanswer:
	    	answerTTS.sayText(this.currentItem.getAnswer());
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
		dbHelper = new DatabaseHelper(this, dbPath, dbName);
		learnQueue = new ArrayList<Item>();
		this.newGrade = -1;
		this.queueEmpty = true;
		this.idMaxSeen = -1;
		this.scheduledItemCount = dbHelper.getScheduledCount();
		this.newItemCount = dbHelper.getNewCount();
		this.loadSettings();
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
		else{
			ql = Locale.US;
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
		else{
			al = Locale.US;
		}
		this.questionTTS = new TTS(this, ql);
		this.answerTTS = new TTS(this, al);
		
		if(this.feedData() == 2){ // The queue is still empty
			OnClickListener backButtonListener = new OnClickListener() {
				// Finish the current activity and go back to the last activity.
				// It should be the main screen.
				public void onClick(DialogInterface arg0, int arg1) {
					finish();
				}
			};
			AlertDialog alertDialog = new AlertDialog.Builder(this) 
			.create();
			alertDialog.setTitle(this.getString(R.string.memo_no_item_title));
			alertDialog.setMessage(this.getString(R.string.memo_no_item_message));
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Back",
					backButtonListener);
			alertDialog.show();
			
		}
		else{

			
			//this.updateMemoScreen();
			if(loadingDialog != null){
				loadingDialog.dismiss();
			}
		}
		
	}

	private int feedData() {
		// Feed the 10 items to acq queue
		// or feed all items to rev queue
		// from the database
		Item item;
		//this.setTitle(this.getString(R.string.stat_scheduled) + dbHelper.getScheduledCount() + " / " + this.getString(R.string.stat_new) + dbHelper.getNewCount());
		this.setTitle(this.getString(R.string.stat_scheduled) + this.scheduledItemCount + " / " + this.getString(R.string.stat_new) + this.newItemCount);
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
			
			

	private void updateMemoScreen() {
		// update the main screen according to the shcurrentItem
		
		OnClickListener backButtonListener = new OnClickListener() {
			// Finish the current activity and go back to the last activity.
			// It should be the main screen.
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		};
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
		questionView.setText(new StringBuilder().append(item.getQuestion()));
		answerView.setText(new StringBuilder().append(item.getAnswer()));
		if(questionAlign.equals("center")){
			questionView.setGravity(Gravity.CENTER);
		}
		else if(questionAlign.equals("right")){
			questionView.setGravity(Gravity.RIGHT);
			
		}
		else{
			questionView.setGravity(Gravity.LEFT);
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
				status = questionTTS.sayText(currentItem.getQuestion());
			}
			else{
				status = answerTTS.sayText(currentItem.getAnswer());
			}
		}
		if(status == 0 || status != 0){
			status = status + 1 -1;
		}
		this.buttonBinding();

	}

	private void clickHandling() {
		
		// When user click on the button of grade, it will update the item information
		// according to the grade.
		// If the return value is success, the user will not need to see this item today.
		// If the return value is failure, the item will be appended to the tail of the queue.

		boolean scheduled = currentItem.isScheduled();
		boolean success = currentItem.processAnswer(newGrade);
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
	}

	private void buttonBinding() {
		// This function will bind the button event and show/hide button
		// according to the showAnswer varible.
		Button btn0 = (Button) findViewById(R.id.But00);
		Button btn1 = (Button) findViewById(R.id.But01);
		Button btn2 = (Button) findViewById(R.id.But02);
		Button btn3 = (Button) findViewById(R.id.But03);
		Button btn4 = (Button) findViewById(R.id.But04);
		Button btn5 = (Button) findViewById(R.id.But05);
		TextView answer = (TextView) findViewById(R.id.answer);
		if (showAnswer == false) {
			btn0.setVisibility(View.INVISIBLE);
			btn1.setVisibility(View.INVISIBLE);
			btn2.setVisibility(View.INVISIBLE);
			btn3.setVisibility(View.INVISIBLE);
			btn4.setVisibility(View.INVISIBLE);
			btn5.setVisibility(View.INVISIBLE);
			answer.setText(new StringBuilder().append(this.getString(R.string.memo_show_answer)));

		} else {
			View.OnClickListener btn0Listener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					newGrade = 0;
					clickHandling();
				}
			};
			View.OnClickListener btn1Listener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					newGrade = 1;
					clickHandling();
				}
			};
			View.OnClickListener btn2Listener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					newGrade = 2;
					clickHandling();
				}
			};
			View.OnClickListener btn3Listener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					newGrade = 3;
					clickHandling();
				}
			};
			View.OnClickListener btn4Listener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					newGrade = 4;
					clickHandling();
				}
			};
			View.OnClickListener btn5Listener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					newGrade = 5;
					clickHandling();
				}
			};
			btn0.setVisibility(View.VISIBLE);
			btn1.setVisibility(View.VISIBLE);
			btn2.setVisibility(View.VISIBLE);
			btn3.setVisibility(View.VISIBLE);
			btn4.setVisibility(View.VISIBLE);
			btn5.setVisibility(View.VISIBLE);
			btn0.setOnClickListener(btn0Listener);
			btn1.setOnClickListener(btn1Listener);
			btn2.setOnClickListener(btn2Listener);
			btn3.setOnClickListener(btn3Listener);
			btn4.setOnClickListener(btn4Listener);
			btn5.setOnClickListener(btn5Listener);

		}
	}

}
