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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class SettingsScreen extends Activity implements OnClickListener {
	private Spinner questionFontSizeSpinner;
	private Spinner answerFontSizeSpinner;
	private Spinner questionAlignSpinner;
	private Spinner answerAlignSpinner;
	private Spinner questionLocaleSpinner;
	private Spinner answerLocaleSpinner;
	private Spinner htmlSpinner;
	private Spinner ratioSpinner;
    private Spinner textColorSpinner;
    private Spinner bgColorSpinner;
	private CheckBox wipeCheckbox;
	private CheckBox shuffleCheckbox;
	private Button btnSave;
	private Button btnDiscard;
	private DatabaseHelper dbHelper;
    private Handler mHandler;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    

	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);
        mContext = this;
        mHandler = new Handler();
        
        questionFontSizeSpinner = (Spinner)findViewById(R.id.question_font_size_spinner);
        ArrayAdapter<CharSequence> fontSizeAdapter = ArrayAdapter.createFromResource(this, R.array.font_size_list, android.R.layout.simple_spinner_item);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionFontSizeSpinner.setAdapter(fontSizeAdapter);
        answerFontSizeSpinner = (Spinner)findViewById(R.id.answer_font_size_spinner);
        answerFontSizeSpinner.setAdapter(fontSizeAdapter);
        
        questionAlignSpinner = (Spinner)findViewById(R.id.question_align_spinner);
        ArrayAdapter<CharSequence> alignAdapter = ArrayAdapter.createFromResource(this, R.array.align_list, android.R.layout.simple_spinner_item);
        alignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionAlignSpinner.setAdapter(alignAdapter);
        answerAlignSpinner = (Spinner)findViewById(R.id.answer_align_spinner);
        answerAlignSpinner.setAdapter(alignAdapter);
        
        questionLocaleSpinner = (Spinner)findViewById(R.id.question_locale_spinner);
        ArrayAdapter<CharSequence> localeAdapter = ArrayAdapter.createFromResource(this, R.array.locale_list, android.R.layout.simple_spinner_item);
        localeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionLocaleSpinner.setAdapter(localeAdapter);
        answerLocaleSpinner = (Spinner)findViewById(R.id.answer_locale_spinner);
        answerLocaleSpinner.setAdapter(localeAdapter);
        
        htmlSpinner = (Spinner)findViewById(R.id.html_spinner);
        ArrayAdapter<CharSequence> htmlAdapter = ArrayAdapter.createFromResource(this, R.array.html_list, android.R.layout.simple_spinner_item);
        htmlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        htmlSpinner.setAdapter(htmlAdapter);
        
        ratioSpinner = (Spinner)findViewById(R.id.ratio_spinner);
        ArrayAdapter<CharSequence> ratioAdapter = ArrayAdapter.createFromResource(this, R.array.ratio_list, android.R.layout.simple_spinner_item);
        ratioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ratioSpinner.setAdapter(ratioAdapter);

        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this, R.array.color_list, android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        textColorSpinner = (Spinner)findViewById(R.id.text_color_spinner);
        textColorSpinner.setAdapter(colorAdapter);
        bgColorSpinner = (Spinner)findViewById(R.id.bg_color_spinner);
        bgColorSpinner.setAdapter(colorAdapter);
        
        setInitialPosition();
        
        wipeCheckbox = (CheckBox)findViewById(R.id.checkbox_wipe);
        wipeCheckbox.setOnClickListener(this);

        shuffleCheckbox = (CheckBox)findViewById(R.id.checkbox_shuffle);
        shuffleCheckbox.setOnClickListener(this);
        
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
		HashMap<String, String> hm= dbHelper.getSettings();
		Set<Map.Entry<String, String>> set = hm.entrySet();
		Iterator<Map.Entry<String, String> > i = set.iterator();
		while(i.hasNext()){
			Map.Entry<String, String> me = i.next();
			if((me.getKey().toString()).equals("question_font_size")){
				Double res = new Double(me.getValue().toString());
				String[] fontSizeList = getResources().getStringArray(R.array.font_size_list);
				int index = 0;
				boolean found = false;
				for(String str:fontSizeList){
					if(res.doubleValue() <= (new Double(str)).doubleValue()){
						found = true;
						break;
					}
					index += 1;
				}
				if(found == false){
					index = 0;
				}
				questionFontSizeSpinner.setSelection(index);
				
			}
			else if(me.getKey().toString().equals("answer_font_size")){
				
				Double res = new Double(me.getValue().toString());
				String[] fontSizeList = getResources().getStringArray(R.array.font_size_list);
				int index = 0;
				boolean found = false;
				for(String str:fontSizeList){
					if(res.doubleValue() <= (new Double(str)).doubleValue()){
						found = true;
						break;
					}
					index += 1;
				}
				if(found == false){
					index = 0;
				}
				answerFontSizeSpinner.setSelection(index);
				
			}
			else if(me.getKey().toString().equals("question_align")){
				String res = me.getValue();
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
			else if(me.getKey().toString().equals("answer_align")){
				String res = me.getValue();
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
			else if(me.getKey().toString().equals("question_locale")){
				String res = me.getValue();
				String[] localeList = getResources().getStringArray(R.array.locale_list);
				int index = 0;
				boolean found = false;
				for(String str : localeList){
					if(str.equals(res)){
						found = true;
						break;
					}
					index ++;
				}
				if(found == false){
					index = 0;
				}
				questionLocaleSpinner.setSelection(index);
			}
			else if(me.getKey().toString().equals("answer_locale")){
				String res = me.getValue().toString();
				String[] localeList = getResources().getStringArray(R.array.locale_list);
				int index = 0;
				boolean found = false;
				for(String str : localeList){
					if(str.equals(res)){
						found = true;
						break;
					}
					index ++;
				}
				if(found == false){
					index = 0;
				}
				answerLocaleSpinner.setSelection(index);
			}
			
			else if(me.getKey().toString().equals("html_display")){
				String res = me.getValue();
				String[] htmlList = getResources().getStringArray(R.array.html_list);
				int index = 0;
				boolean found = false;
				for(String str : htmlList){
					if(str.equals(res)){
						found = true;
						break;
					}
					index++;
				}
				if(found == false){
					index = 0;
				}
				htmlSpinner.setSelection(index);
			}
			else if(me.getKey().toString().equals("ratio")){
				String res = me.getValue();
				String[] ratioList = getResources().getStringArray(R.array.ratio_list);
				int index = 0;
				boolean found = false;
				for(String str : ratioList){
					if(str.equals(res)){
						found = true;
						break;
					}
					index++;
				}
				if(found == false){
					index = 0;
				}
				ratioSpinner.setSelection(index);
				
			}
			else if(me.getKey().toString().equals("text_color")){
				String res = me.getValue();
				String[] colorList = getResources().getStringArray(R.array.color_list);
				int index = 0;
				boolean found = false;
				for(String str : colorList){
					if(str.equals(res)){
						found = true;
						break;
					}
					index++;
				}
				if(found == false){
					index = 0;
				}
				textColorSpinner.setSelection(index);
				
			}
			else if(me.getKey().toString().equals("bg_color")){
				String res = me.getValue();
				String[] colorList = getResources().getStringArray(R.array.color_list);
				int index = 0;
				boolean found = false;
				for(String str : colorList){
					if(str.equals(res)){
						found = true;
						break;
					}
					index++;
				}
				if(found == false){
					index = 0;
				}
				bgColorSpinner.setSelection(index);
				
			}
			
		}
    	
    	
    }
    
    private void updateSettings(){
    	HashMap<String, String> hm = new HashMap<String, String>();
    	String[] fontSizeList = getResources().getStringArray(R.array.font_size_list);
    	String[] alignList = getResources().getStringArray(R.array.align_list);
    	String[] localeList = getResources().getStringArray(R.array.locale_list);
    	String[] htmlList = getResources().getStringArray(R.array.html_list);
    	String[] ratioList = getResources().getStringArray(R.array.ratio_list);
    	String[] colorList = getResources().getStringArray(R.array.color_list);
    	hm.put("question_font_size", fontSizeList[questionFontSizeSpinner.getSelectedItemPosition()]);
    	hm.put("answer_font_size", fontSizeList[answerFontSizeSpinner.getSelectedItemPosition()]);
    	hm.put("question_align", alignList[questionAlignSpinner.getSelectedItemPosition()]);
    	hm.put("answer_align", alignList[answerAlignSpinner.getSelectedItemPosition()]);
    	hm.put("question_locale", localeList[questionLocaleSpinner.getSelectedItemPosition()]);
    	hm.put("answer_locale", localeList[answerLocaleSpinner.getSelectedItemPosition()]);
    	hm.put("html_display", htmlList[htmlSpinner.getSelectedItemPosition()]);
    	hm.put("ratio", ratioList[ratioSpinner.getSelectedItemPosition()]);
    	hm.put("text_color", colorList[textColorSpinner.getSelectedItemPosition()]);
    	hm.put("bg_color", colorList[bgColorSpinner.getSelectedItemPosition()]);
    	dbHelper.setSettings(hm);
    	
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	dbHelper.close();
    	Intent resultIntent = new Intent();
    	setResult(Activity.RESULT_CANCELED, resultIntent);
    }
    
    public void onClick(View v){
    	if(v == btnSave){
            mProgressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_save), true);
            Thread savingThread = new Thread(){
                @Override
                public void run(){
                    updateSettings();
                    if(wipeCheckbox.isChecked()){
                        
                        
                        dbHelper.wipeLearnData();
                    }
                    if(shuffleCheckbox.isChecked()){
                        dbHelper.shuffleDatabase();
                    }
                    dbHelper.close();
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            mProgressDialog.dismiss();
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);    			
                            finish();
                        }
                    });
                }
            };
            savingThread.start();
    	}
    	if(v == btnDiscard){
        	Intent resultIntent = new Intent();
        	setResult(Activity.RESULT_CANCELED, resultIntent);    			
    		finish();
    	}
    	
    	if(v == wipeCheckbox){
    		if(wipeCheckbox.isChecked()){
    			new AlertDialog.Builder(this)
    			    .setTitle(getString(R.string.warning_text))
    			    .setMessage(getString(R.string.settings_wipe_warning))
    			    .setPositiveButton(getString(R.string.ok_text), null)
                    .create()
                    .show();
    		}
    	}
    	if(v == shuffleCheckbox){
    		if(shuffleCheckbox.isChecked()){
    			new AlertDialog.Builder(this)
    			    .setTitle(getString(R.string.warning_text))
    			    .setMessage(getString(R.string.settings_shuffle_warning))
    			    .setPositiveButton(getString(R.string.ok_text), null)
                    .create()
                    .show();
    		}
    	}
    	
    }
    
}
