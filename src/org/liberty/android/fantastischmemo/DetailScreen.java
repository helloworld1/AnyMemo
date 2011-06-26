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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class DetailScreen extends AMActivity implements OnClickListener{
	
	private EditText idEntry;
	private EditText questionEntry;
	private EditText answerEntry;
	private EditText noteEntry;
	private EditText categoryEntry;
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
	Button resetButton;
	
	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.detail_screen);	
        idEntry = (EditText)findViewById(R.id.entry__id);
        questionEntry = (EditText)findViewById(R.id.entry_question);
        answerEntry = (EditText)findViewById(R.id.entry_answer);
        noteEntry = (EditText)findViewById(R.id.entry_note);
        categoryEntry = (EditText)findViewById(R.id.entry_category);
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
			
            try{
                dbHelper = new DatabaseHelper(this, dbPath, dbName);
            }
            catch(Exception e){
                new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.open_database_error_title))
                    .setMessage(getString(R.string.open_database_error_message))
                    .setPositiveButton(getString(R.string.back_menu_text) + " " + e.toString(), null)
                    .create()
                    .show();
                finish();
                return;
            }

			currentItem = dbHelper.getItemById(itemId, 0, true, null);
		}
		else{
			dbHelper = null;
			currentItem = null;
		}
		backButton = (Button)findViewById(R.id.but_detail_back);
		updateButton = (Button)findViewById(R.id.but_detail_update);
		resetButton = (Button)findViewById(R.id.but_detail_reset);
		backButton.setOnClickListener(this);
		resetButton.setOnClickListener(this);
		updateButton.setOnClickListener(this);
		
		loadEntries();
    }
		
    
    private void loadEntries(){
    	idEntry.setText("" + currentItem.getId());
    	questionEntry.setText(currentItem.getQuestion());
    	answerEntry.setText(currentItem.getAnswer());
    	noteEntry.setText(currentItem.getNote());
        categoryEntry.setText(currentItem.getCategory());
    	dateLearnEntry.setText(currentItem.getDateLearn());
    	intervalEntry.setText("" + currentItem.getInterval());
    	gradeEntry.setText("" + currentItem.getGrade());
    	easinessEntry.setText("" + currentItem.getEasiness());
    	acqRepsEntry.setText("" + currentItem.getAcqReps());
    	retRepsEntry.setText("" + currentItem.getRetReps());
    	lapsesEntry.setText("" + currentItem.getLapses());
    	acqRepsSinceLapseEntry.setText("" + currentItem.getAcqRepsSinceLapse());
    	retRepsSinceLapseEntry.setText("" + currentItem.getRetRepsSinceLapse());
    }
    
    private void saveEntries(){
    	HashMap<String, String> hm=  new HashMap<String, String>();
        currentItem = new Item.Builder()
            .setId(Integer.parseInt(idEntry.getText().toString()))
            .setQuestion(questionEntry.getText().toString())
            .setAnswer(answerEntry.getText().toString())
            .setNote(noteEntry.getText().toString())
            .setCategory(categoryEntry.getText().toString())
            .setDateLearn(dateLearnEntry.getText().toString())
            .setInterval(Integer.parseInt(intervalEntry.getText().toString()))
            .setGrade(Integer.parseInt(gradeEntry.getText().toString()))
            .setEasiness(Double.parseDouble(easinessEntry.getText().toString()))
            .setAcqReps(Integer.parseInt(acqRepsEntry.getText().toString()))
            .setRetReps(Integer.parseInt(retRepsEntry.getText().toString()))
            .setLapses(Integer.parseInt(lapsesEntry.getText().toString()))
            .setAcqRepsSinceLapse(Integer.parseInt(acqRepsSinceLapseEntry.getText().toString()))
            .setRetRepsSinceLapse(Integer.parseInt(retRepsSinceLapseEntry.getText().toString()))
            .build();

    	dbHelper.addOrReplaceItem(currentItem);
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
    	if(v == resetButton){
            dateLearnEntry.setText("2010-01-01");
            intervalEntry.setText("0");
            gradeEntry.setText("0");
            easinessEntry.setText("2.5");
            acqRepsEntry.setText("0");
            retRepsEntry.setText("0");
            lapsesEntry.setText("0");
            acqRepsSinceLapseEntry.setText("0");
            retRepsSinceLapseEntry.setText("0");
    	}
    	if(v == updateButton){
    		new AlertDialog.Builder(this)
                .setTitle(R.string.warning_text)
                .setMessage(R.string.item_update_warning)
                .setPositiveButton(R.string.ok_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							saveEntries();
							Intent resultIntent = new Intent();
							setResult(Activity.RESULT_OK, resultIntent);
							
							finish();
						}
					})
                .setNegativeButton(R.string.cancel_text, null)
                .show();
    		
    	}
    	
    }

}
