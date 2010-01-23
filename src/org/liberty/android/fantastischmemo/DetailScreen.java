package org.liberty.android.fantastischmemo;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DetailScreen extends Activity implements OnClickListener{
	
	private EditText idEntry;
	private EditText questionEntry;
	private EditText answerEntry;
	private EditText noteEntry;
	private EditText dateLearnEntry;
	private EditText intervalEntry;
	private EditText gradeEntry;
	private EditText easinessEntry;
	private EditText acqRepsEntry;
	private EditText retRepsEntry;
	private EditText lapsesEntry;
	private EditText acqRepsSinceLapseEntry;
	private EditText retRepsSinceLapseEntry;
	private DatabaseHelper dbHelper;
	private Item currentItem;
	Button backButton;
	Button updateButton;
	Button deleteButton;
	
	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_screen);	
        idEntry = (EditText)findViewById(R.id.entry__id);
        questionEntry = (EditText)findViewById(R.id.entry_question);
        answerEntry = (EditText)findViewById(R.id.entry_answer);
        noteEntry = (EditText)findViewById(R.id.entry_note);
        dateLearnEntry = (EditText)findViewById(R.id.entry_date_learn);
        intervalEntry = (EditText)findViewById(R.id.entry_interval);
        gradeEntry = (EditText)findViewById(R.id.entry_grade);
        easinessEntry = (EditText)findViewById(R.id.entry_easiness);
        acqRepsEntry = (EditText)findViewById(R.id.entry_acq_reps);
        retRepsEntry = (EditText)findViewById(R.id.entry_ret_reps);
        lapsesEntry = (EditText)findViewById(R.id.entry_lapses);
        acqRepsSinceLapseEntry = (EditText)findViewById(R.id.entry_acq_reps_since_lapse);
        retRepsSinceLapseEntry = (EditText)findViewById(R.id.entry_ret_reps_since_lapse);
		Bundle extras = getIntent().getExtras();
		String dbPath, dbName;
		int itemId;
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
			itemId = extras.getInt("itemid");
			
			dbHelper = new DatabaseHelper(this, dbPath, dbName);
			currentItem = dbHelper.getItemById(itemId, 0);
		}
		else{
			dbHelper = null;
			currentItem = null;
		}
		backButton = (Button)findViewById(R.id.but_detail_back);
		updateButton = (Button)findViewById(R.id.but_detail_update);
		deleteButton = (Button)findViewById(R.id.but_detail_delete);
		backButton.setOnClickListener(this);
		deleteButton.setOnClickListener(this);
		updateButton.setOnClickListener(this);
		
		loadEntries();
    }
		
    
    private void loadEntries(){
    	idEntry.setText("" + currentItem.getId());
    	questionEntry.setText(currentItem.getQuestion());
    	answerEntry.setText(currentItem.getAnswer());
    	noteEntry.setText(currentItem.getNote());
    	
    	String[] learnData = currentItem.getLearningData();
    	dateLearnEntry.setText(learnData[0]);
    	intervalEntry.setText(learnData[1]);
    	gradeEntry.setText(learnData[2]);
    	easinessEntry.setText(learnData[3]);
    	acqRepsEntry.setText(learnData[4]);
    	retRepsEntry.setText(learnData[5]);
    	lapsesEntry.setText(learnData[6]);
    	acqRepsSinceLapseEntry.setText(learnData[7]);
    	retRepsSinceLapseEntry.setText(learnData[8]);
    }
    
    private void saveEntries(){
    	HashMap<String, String> hm=  new HashMap<String, String>();
    	hm.put("_id", idEntry.getText().toString());
    	hm.put("question", questionEntry.getText().toString());
    	hm.put("answer", answerEntry.getText().toString());
    	hm.put("note", noteEntry.getText().toString());
    	hm.put("date_learn", dateLearnEntry.getText().toString());
    	hm.put("interval", intervalEntry.getText().toString());
    	hm.put("grade", gradeEntry.getText().toString());
    	hm.put("easiness", easinessEntry.getText().toString());
    	hm.put("acq_reps", acqRepsEntry.getText().toString());
    	hm.put("ret_reps", retRepsEntry.getText().toString());
    	hm.put("lapses", lapsesEntry.getText().toString());
    	hm.put("acq_reps_since_lapse", acqRepsSinceLapseEntry.getText().toString());
    	hm.put("ret_reps_since_lapse", retRepsSinceLapseEntry.getText().toString());
    	currentItem.setData(hm);
    	dbHelper.updateItem(currentItem);
    	dbHelper.updateQA(currentItem);
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	dbHelper.close();
    	Intent resultIntent = new Intent();
    	setResult(Activity.RESULT_CANCELED, resultIntent);
    	
    }
    
    public void onClick(View v){
    	
    	if(v == backButton){
    		Intent resultIntent = new Intent();
    		setResult(Activity.RESULT_CANCELED, resultIntent);
    		finish();
    	}
    	if(v == deleteButton){
    		AlertDialog alertDialog = new AlertDialog.Builder(this)
			.create();
			alertDialog.setTitle("Warning");
			alertDialog.setMessage("Do you really want to delete this item?");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							dbHelper.deleteItem(currentItem);
							Intent resultIntent = new Intent();
							setResult(Activity.RESULT_OK, resultIntent);
							finish();
						}
					});
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
			alertDialog.show();
    	}
    	if(v == updateButton){
    		AlertDialog alertDialog = new AlertDialog.Builder(this)
			.create();
			alertDialog.setTitle("Warning");
			alertDialog.setMessage("Do you really want to update this item?");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							saveEntries();
							Intent resultIntent = new Intent();
							setResult(Activity.RESULT_OK, resultIntent);
							
							finish();
						}
					});
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
			alertDialog.show();
    		
    	}
    	
    }
	

}
