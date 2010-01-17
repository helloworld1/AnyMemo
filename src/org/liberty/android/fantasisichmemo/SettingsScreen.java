package org.liberty.android.fantasisichmemo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SettingsScreen extends Activity implements OnClickListener {
	Spinner questionFontSizeSpinner;
	Spinner answerFontSizeSpinner;
	Spinner questionAlignSpinner;
	Spinner answerAlignSpinner;
	Spinner questionLocaleSpinner;
	Spinner answerLocaleSpinner;
	Button btnSave;
	Button btnDiscard;
	DatabaseHelper dbHelper;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);
        
        questionFontSizeSpinner = (Spinner)findViewById(R.id.question_font_size_spinner);
        ArrayAdapter fontSizeAdapter = ArrayAdapter.createFromResource(this, R.array.font_size_list, android.R.layout.simple_spinner_item);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionFontSizeSpinner.setAdapter(fontSizeAdapter);
        answerFontSizeSpinner = (Spinner)findViewById(R.id.answer_font_size_spinner);
        answerFontSizeSpinner.setAdapter(fontSizeAdapter);
        
        questionAlignSpinner = (Spinner)findViewById(R.id.question_align_spinner);
        ArrayAdapter alignAdapter = ArrayAdapter.createFromResource(this, R.array.align_list, android.R.layout.simple_spinner_item);
        alignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionAlignSpinner.setAdapter(alignAdapter);
        answerAlignSpinner = (Spinner)findViewById(R.id.answer_align_spinner);
        answerAlignSpinner.setAdapter(alignAdapter);
        
        questionLocaleSpinner = (Spinner)findViewById(R.id.question_locale_spinner);
        ArrayAdapter localeAdapter = ArrayAdapter.createFromResource(this, R.array.locale_list, android.R.layout.simple_spinner_item);
        localeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionLocaleSpinner.setAdapter(localeAdapter);
        answerLocaleSpinner = (Spinner)findViewById(R.id.answer_locale_spinner);
        answerLocaleSpinner.setAdapter(localeAdapter);
        
        setInitialPosition();
        
        btnSave = (Button)findViewById(R.id.settting_save);
        btnSave.setOnClickListener(this);
        btnDiscard = (Button)findViewById(R.id.setting_discard);
        btnDiscard.setOnClickListener(this);
        
        
    }
    
    private void setInitialPosition(){
    	String dbPath = null, dbName = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
		}
		dbHelper = new DatabaseHelper(this, dbPath, dbName);
		HashMap hm= dbHelper.getSettings();
		Set set = hm.entrySet();
		Iterator i = set.iterator();
		while(i.hasNext()){
			Map.Entry me = (Map.Entry)i.next();
			if((me.getKey().toString()).equals("question_font_size")){
				Double res = new Double(me.getValue().toString());
				String[] fontSizeList = getResources().getStringArray(R.array.font_size_list);
				int index = 0;
				for(String str:fontSizeList){
					if(res.doubleValue() <= (new Double(str)).doubleValue()){
						break;
					}
					index += 1;
				}
				questionFontSizeSpinner.setSelection(index);
				
			}
			if(me.getKey().toString().equals("answer_font_size")){
				
				Double res = new Double(me.getValue().toString());
				String[] fontSizeList = getResources().getStringArray(R.array.font_size_list);
				int index = 0;
				for(String str:fontSizeList){
					if(res.doubleValue() <= (new Double(str)).doubleValue()){
						break;
					}
					index += 1;
				}
				answerFontSizeSpinner.setSelection(index);
				
			}
			if(me.getKey().toString().equals("question_align")){
				String res = me.getValue().toString();
				int index;
				if(res.equals("left")){
					index = 0;
				}
				else if(res.equals("right")){
					index = 2;
				}
				else{
					index = 1;
				}
				questionAlignSpinner.setSelection(index);
			}
			if(me.getKey().toString().equals("answer_align")){
				String res = me.getValue().toString();
				int index;
				if(res.equals("left")){
					index = 0;
				}
				else if(res.equals("right")){
					index = 2;
				}
				else{
					index = 1;
				}
				answerAlignSpinner.setSelection(index);
			}
			if(me.getKey().toString().equals("question_locale")){
				String res = me.getValue().toString();
				String[] localeList = getResources().getStringArray(R.array.locale_list);
				int index = 0;
				for(String str : localeList){
					if(str.equals(res)){
						break;
					}
					index ++;
				}
				questionLocaleSpinner.setSelection(index);
			}
			if(me.getKey().toString().equals("answer_locale")){
				String res = me.getValue().toString();
				String[] localeList = getResources().getStringArray(R.array.locale_list);
				int index = 0;
				for(String str : localeList){
					if(str.equals(res)){
						break;
					}
					index ++;
				}
				answerLocaleSpinner.setSelection(index);
			}
		}
    	
    	
    }
    
    private void updateSettings(){
    	HashMap hm = new HashMap();
    	String[] fontSizeList = getResources().getStringArray(R.array.font_size_list);
    	String[] alignList = getResources().getStringArray(R.array.align_list);
    	String[] localeList = getResources().getStringArray(R.array.locale_list);
    	hm.put("question_font_size", fontSizeList[questionFontSizeSpinner.getSelectedItemPosition()]);
    	hm.put("answer_font_size", fontSizeList[answerFontSizeSpinner.getSelectedItemPosition()]);
    	hm.put("question_align", alignList[questionAlignSpinner.getSelectedItemPosition()]);
    	hm.put("answer_align", alignList[answerAlignSpinner.getSelectedItemPosition()]);
    	hm.put("question_locale", localeList[questionLocaleSpinner.getSelectedItemPosition()]);
    	hm.put("answer_locale", localeList[answerLocaleSpinner.getSelectedItemPosition()]);
    	dbHelper.setSettings(hm);
    	
    }
    
    public void onClick(View v){
    	if(v == btnSave){
    		updateSettings();
    		dbHelper.close();
    		finish();
    	}
    	if(v == btnDiscard){
    		finish();
    	}
    	
    }
    
}
