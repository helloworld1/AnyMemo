package org.liberty.android.fantasisichmemo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.TextView;

public class MemoScreen extends Activity {
	//
	private ArrayList<Item> learnQueue;
	private DatabaseHelper dbHelper;
	private String dbName;
	private String dbPath;
	private String mode;
	private boolean showAnswer;
	private int newGrade = -1;
	private Item currentItem;
	private final int WINDOW_SIZE = 10;
	private boolean queueEmpty;
	private int idMaxSeen;
	private int scheduledItemCount;
	private int newItemCount;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memo_screen);
		
		// The extra mode field is passed from intent.
		// acq and rev should be different processes in different learning algorithm
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
		}
		this.prepare();
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
			alertDialog.setTitle("No item");
			alertDialog.setMessage("There is no items for now.");
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Back",
					backButtonListener);
			alertDialog.show();
			
		}
		else{
			this.updateMemoScreen();
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
	    case R.id.menudetail:
	        return true;
	    }
	    return false;
	}

	public boolean onTouchEvent(MotionEvent event) {
		// When the screen is touched, it will uncover answer
		int eventAction = event.getAction();
		switch (eventAction) {
		case MotionEvent.ACTION_DOWN:
			this.showAnswer ^= true;
			updateMemoScreen();

		}
		return true;

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
	}

	private int feedData() {
		// Feed the 10 items to acq queue
		// or feed all items to rev queue
		// from the database
		Item item;
		this.setTitle("Scheduled: " + dbHelper.getScheduledCount() + " / New: " + dbHelper.getNewCount());
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
			alertDialog.setTitle("No item");
			alertDialog.setMessage("There is no items for now.");
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Back",
					backButtonListener);
			alertDialog.show();
			
		}
		
	}


	private void displayQA(Item item) {
		// Display question and answer according to item
		TextView questionView = (TextView) findViewById(R.id.question);
		TextView answerView = (TextView) findViewById(R.id.answer);
		questionView.setText(new StringBuilder().append(item.getQuestion()));
		answerView.setText(new StringBuilder().append(item.getAnswer()));
		questionView.setGravity(Gravity.CENTER);
		answerView.setGravity(Gravity.CENTER);
		this.buttonBinding();

	}

	private void clickHandling() {
		
		// When user click on the button of grade, it will update the item information
		// according to the grade.
		// If the return value is success, the user will not need to see this item today.
		// If the return value is failure, the item will be appended to the tail of the queue.

		boolean isNewItem = currentItem.isNew();
		boolean success = currentItem.processAnswer(newGrade);
		if (success == true) {
			learnQueue.remove(0);
			if(isNewItem){
				this.newItemCount -= 1;
			}
			else{
				this.scheduledItemCount -= 1;
			}
			if(queueEmpty != true){
				dbHelper.updateItem(currentItem);
			}
		} else {
			learnQueue.remove(0);
			learnQueue.add(currentItem);
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
			answer.setText(new StringBuilder().append("?\n Show answer"));

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
