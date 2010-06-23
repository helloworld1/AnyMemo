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

/* This will pick the color */
import org.color.ColorDialog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.AdapterView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.net.Uri;

public class SettingsScreen extends Activity implements View.OnClickListener, ColorDialog.OnClickListener {
	private Spinner questionFontSizeSpinner;
	private Spinner answerFontSizeSpinner;
	private Spinner questionAlignSpinner;
	private Spinner answerAlignSpinner;
	private Spinner questionLocaleSpinner;
	private Spinner answerLocaleSpinner;
	private Spinner htmlSpinner;
	private Spinner ratioSpinner;
    private Spinner questionTextColorSpinner;
    private Spinner answerTextColorSpinner;
    private Spinner questionBgColorSpinner;
    private Spinner answerBgColorSpinner;
	private CheckBox wipeCheckbox;
	private CheckBox shuffleCheckbox;
	private CheckBox inverseCheckbox;
    private CheckBox qTypefaceCheckbox;
    private CheckBox aTypefaceCheckbox;
    private EditText qTypefaceEdit;
    private EditText aTypefaceEdit;
    private TableRow colorRow;
    private Spinner colorSpinner;
    private CheckBox colorCheckbox;
    private Button colorButton;
    private ArrayList<Integer> colors;

	private Button btnSave;
	private Button btnDiscard;
	private DatabaseHelper dbHelper;
    private Handler mHandler;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private final int ACTIVITY_TTF_QUESTION = 3;
    private final int ACTIVITY_TTF_ANSWER = 4;
    

	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);
        mContext = this;
        mHandler = new Handler();
        
        /* Properly set up all the Views */
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

        colorRow = (TableRow)findViewById(R.id.color_row);
        colorRow.setVisibility(View.GONE);
        colorSpinner = (Spinner)findViewById(R.id.color_item_spinner);
        ArrayAdapter<CharSequence> colorItemAdapter = ArrayAdapter.createFromResource(this, R.array.color_item_list, android.R.layout.simple_spinner_item);
        colorItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorItemAdapter);
        


        colorButton = (Button)findViewById(R.id.settings_color_button);
        colorButton.setOnClickListener(this);
        /* Initial the default value of colors */
        int[] ia = getResources().getIntArray(R.array.default_color_list);
        colors = new ArrayList<Integer>();
        for(int i : ia){
            colors.add(i);
        }

        
        /* Get the spinner's initial item */
        
        wipeCheckbox = (CheckBox)findViewById(R.id.checkbox_wipe);
        wipeCheckbox.setOnClickListener(this);

        shuffleCheckbox = (CheckBox)findViewById(R.id.checkbox_shuffle);
        shuffleCheckbox.setOnClickListener(this);

        inverseCheckbox = (CheckBox)findViewById(R.id.checkbox_inverse);
        inverseCheckbox.setOnClickListener(this);
        
        btnSave = (Button)findViewById(R.id.settting_save);
        btnSave.setOnClickListener(this);
        btnDiscard = (Button)findViewById(R.id.setting_discard);
        btnDiscard.setOnClickListener(this);

        qTypefaceCheckbox = (CheckBox)findViewById(R.id.checkbox_typeface_question);
        qTypefaceCheckbox.setOnClickListener(this);

        aTypefaceCheckbox = (CheckBox)findViewById(R.id.checkbox_typeface_answer);
        aTypefaceCheckbox.setOnClickListener(this);
        colorCheckbox = (CheckBox)findViewById(R.id.checkbox_customize_color);
        colorCheckbox.setOnClickListener(this);
        qTypefaceEdit = (EditText)findViewById(R.id.edit_typeface_question);
        aTypefaceEdit = (EditText)findViewById(R.id.edit_typeface_answer);
        qTypefaceEdit.setOnClickListener(this);
        aTypefaceEdit.setOnClickListener(this);
        qTypefaceEdit.setVisibility(View.GONE);
        aTypefaceEdit.setVisibility(View.GONE);
        setInitialPosition();
        
    }
    
    private void setInitialPosition(){
    	String dbPath = null, dbName = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
		}
        try{
            dbHelper = new DatabaseHelper(this, dbPath, dbName);
        }
        catch(Exception e){
            new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.open_database_error_title))
                .setMessage(getString(R.string.open_database_error_message))
                .setPositiveButton(getString(R.string.back_menu_text) + " " + e.toString(), null)
                .create()
                .show();
            finish();
            return;
        }
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
                /* Compatible with old database */
                if(res.equals("Other")){
                    res = "Disabled";
                }
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
                /* Compatible with old database */
				String res = me.getValue().toString();
                if(res.equals("Other")){
                    res = "Disabled";
                }
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
            else if(me.getKey().toString().equals("colors")){
                String res = me.getValue();
                if(!res.equals("")){
                    colorCheckbox.setChecked(true);
                    String[] ca = res.split(" ");
                    for(int j = 0; j < ca.length; j++){
                        colors.add(j, Integer.parseInt(ca[j]));
                    }
                    colorRow.setVisibility(View.VISIBLE);
                }
                else{
                    colorCheckbox.setChecked(false);
                    colorRow.setVisibility(View.GONE);
                }
            }
            else if(me.getKey().toString().equals("question_typeface")){
				String res = me.getValue();
                if(!res.equals("")){
                    qTypefaceCheckbox.setChecked(true);
                    qTypefaceEdit.setText(res);
                    qTypefaceEdit.setVisibility(View.VISIBLE);
                }
                else{
                    qTypefaceEdit.setVisibility(View.GONE);
                }
            }
            else if(me.getKey().toString().equals("answer_typeface")){
				String res = me.getValue();
                if(!res.equals("")){
                    aTypefaceCheckbox.setChecked(true);
                    aTypefaceEdit.setText(res);
                    aTypefaceEdit.setVisibility(View.VISIBLE);
                }
                else{
                    aTypefaceEdit.setVisibility(View.GONE);
                }
            }
			
		}
        
        /* Set the colorSpinner's event to retrieve the color */
        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView adapterView, View view, int position, long id){
                colorButton.setTextColor(colors.get(position));

            }
            public void onNothingSelected(AdapterView adapterView){
            }
        });
    	
    	
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
        /* Store colors using space separated string */
        String colorString = "";
        if(colorCheckbox.isChecked()){
            for(int i = 0; i < colors.size(); i++){
                if(i != 0){
                    colorString += " ";
                }
                colorString += colors.get(i).toString();
            }
            hm.put("colors", colorString);
        }
        else{
            hm.put("colors", "");
        }

        if(qTypefaceCheckbox.isChecked()){
            hm.put("question_typeface", qTypefaceEdit.getText().toString());
        }
        else{
            hm.put("question_typeface", "");
        }
        if(aTypefaceCheckbox.isChecked()){
            hm.put("answer_typeface", aTypefaceEdit.getText().toString());
        }
        else{
            hm.put("answer_typeface", "");
        }
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
            doSave();
    	}
    	if(v == btnDiscard){
        	Intent resultIntent = new Intent();
        	setResult(Activity.RESULT_CANCELED, resultIntent);    			
    		finish();
    	}

        /* Display the warnings when these checkbox are checked
         * The actual events are handled in the btnSave
         */
    	if(v == wipeCheckbox){
    		if(wipeCheckbox.isChecked()){
    			new AlertDialog.Builder(this)
    			    .setTitle(getString(R.string.warning_text))
                    .setIcon(R.drawable.alert_dialog_icon)
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
                    .setIcon(R.drawable.alert_dialog_icon)
    			    .setMessage(getString(R.string.settings_shuffle_warning))
    			    .setPositiveButton(getString(R.string.ok_text), null)
                    .create()
                    .show();
    		}
    	}
    	if(v == inverseCheckbox){
    		if(inverseCheckbox.isChecked()){
    			new AlertDialog.Builder(this)
    			    .setTitle(getString(R.string.warning_text))
                    .setIcon(R.drawable.alert_dialog_icon)
    			    .setMessage(getString(R.string.settings_inverse_warning))
    			    .setPositiveButton(getString(R.string.ok_text), null)
                    .create()
                    .show();
    		}
    	}

        if(v == qTypefaceEdit){
            Intent myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", Environment.getExternalStorageDirectory().getAbsolutePath());
            myIntent.putExtra("file_extension", ".ttf");
            startActivityForResult(myIntent, ACTIVITY_TTF_QUESTION);
        }

        if(v == aTypefaceEdit){
            Intent myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", Environment.getExternalStorageDirectory().getAbsolutePath());
            myIntent.putExtra("file_extension", ".ttf");
            startActivityForResult(myIntent, ACTIVITY_TTF_ANSWER);
        }

        if(v == qTypefaceCheckbox){
            if(qTypefaceCheckbox.isChecked()){
                qTypefaceEdit.setVisibility(View.VISIBLE);
            }
            else{
                qTypefaceEdit.setVisibility(View.GONE);
            }
        }

        if(v == aTypefaceCheckbox){
            if(aTypefaceCheckbox.isChecked()){
                aTypefaceEdit.setVisibility(View.VISIBLE);
            }
            else{
                aTypefaceEdit.setVisibility(View.GONE);
            }
        }

        if(v == colorButton){
            int pos = colorSpinner.getSelectedItemPosition();
            ColorDialog dialog = new ColorDialog(this, colorButton, colors.get(pos), this);
            dialog.show();
        }

        if(v == colorCheckbox){
            if(colorCheckbox.isChecked()){
                colorRow.setVisibility(View.VISIBLE);
            }
            else{
                colorRow.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view, int color){
        int pos = colorSpinner.getSelectedItemPosition();
        colorButton.setTextColor(color);
        colors.set(pos, color);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
        String fontFilename = null;
        String dbName, dbPath;
    	switch(requestCode){
    	    case ACTIVITY_TTF_QUESTION:
                if(resultCode == Activity.RESULT_OK){
                    dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                    dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                    if(dbName != null && dbPath != null){
                        fontFilename = dbPath + "/" + dbName;
                        qTypefaceEdit.setText(fontFilename);
                    }
                }
                break;
    	    case ACTIVITY_TTF_ANSWER:
                if(resultCode == Activity.RESULT_OK){
                    dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                    dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                    if(dbName != null && dbPath != null){
                        fontFilename = dbPath + "/" + dbName;
                        aTypefaceEdit.setText(fontFilename);
                    }
                }
                break;
        }
                
                
    }

	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.settingsmenu_save:
            doSave();
            return true;

        case R.id.settingsmenu_discard:
        	Intent resultIntent = new Intent();
        	setResult(Activity.RESULT_CANCELED, resultIntent);    			
    		finish();
            return true;

        case R.id.settingsmenu_help:
            Intent myIntent = new Intent();
            myIntent.setAction(Intent.ACTION_VIEW);
            myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            myIntent.setData(Uri.parse(getString(R.string.website_help_settings)));
            startActivity(myIntent);
	        return true;

        }
        return false;
    }

    private void doSave(){
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
                if(inverseCheckbox.isChecked()){
                    dbHelper.inverseQA();
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

    
}
