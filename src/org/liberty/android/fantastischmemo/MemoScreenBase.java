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

import org.amr.arabic.ArabicUtilities;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.gesture.GestureOverlayView;
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.util.Log;
import android.os.SystemClock;
import android.os.Environment;
import android.graphics.Typeface;
import android.text.Html.TagHandler;
import android.text.Html.ImageGetter;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;


public abstract class MemoScreenBase extends Activity implements TagHandler, ImageGetter{
	protected DatabaseHelper dbHelper = null;
	protected String dbName;
	protected String dbPath;
	protected boolean showAnswer;
	protected Item currentItem;
    private int prevScheduledItemCount;
    private int prevNewItemCount;

	protected double questionFontSize = 23.5;
	protected double answerFontSize = 23.5;
	protected String questionAlign = "center";
	protected String answerAlign = "center";
	protected String questionLocale = "US";
	protected String answerLocale = "US";
	protected String htmlDisplay = "none";
	protected String qaRatio = "50%";
    protected String btnStyle = "";
    protected String speechCtl = "";
	protected boolean questionUserAudio = false;
	protected boolean answerUserAudio = false;
    protected boolean copyClipboard = true;
    protected String questionTypeface = "";
    protected String answerTypeface = "";
    protected String activeFilter = "";
    protected String audioLocation = "";
    /* The colors for various elements
     * null means default color */
    protected ArrayList<Integer> colors = null;

    protected volatile Handler mHandler;

	protected int returnValue = 0;
	//private boolean initFeed;

    private final static String TAG = "org.liberty.android.fantastischmemo.MemoScreenBase";
    protected final static int DIALOG_EDIT = 30;

	abstract protected boolean prepare();

	abstract public boolean onCreateOptionsMenu(Menu menu);
	
	abstract public boolean onOptionsItemSelected(MenuItem item);
	
    abstract protected void createButtons();

	abstract protected void buttonBinding();

    abstract protected void restartActivity();	

    abstract protected void refreshAfterEditItem();

    abstract protected void refreshAfterDeleteItem();

	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dbPath = extras.getString("dbpath");
			dbName = extras.getString("dbname");
            activeFilter = extras.getString("active_filter");
		}
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if(settings.getBoolean("fullscreen_mode", false)){
            requestWindowFeature(Window.FEATURE_NO_TITLE);  
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        }
        if(!settings.getBoolean("allow_orientation", true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

	public void onResume(){
        super.onResume();
        mHandler = new Handler();

        /* Refresh depending on where it returns. */
        if(returnValue == 2){
            restartActivity();
        }
        else if(returnValue == 1){
			prepare();
			returnValue = 0;
		}
		else{
			returnValue = 0;
		}
    }


	public void onDestroy(){
        super.onDestroy();
    }

	protected void loadSettings(){
        /* Set a default audio location */
        audioLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_audio_dir);
		/* Here is the global settings from the preferences */
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        speechCtl = settings.getString("speech_ctl", getResources().getStringArray(R.array.speech_ctl_list)[0]);
        btnStyle = settings.getString("button_style", getResources().getStringArray(R.array.button_style_list)[0]);
        copyClipboard = settings.getBoolean("copyclipboard", true);
		
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
			if(me.getKey().toString().equals("question_typeface")){
                this.questionTypeface = me.getValue().toString();
            }
			if(me.getKey().toString().equals("answer_typeface")){
                this.answerTypeface = me.getValue().toString();
            }
            if(me.getKey().toString().equals("colors")){
                String colorString = me.getValue().toString();
                if(colorString.equals("")){
                    colors = null;
                }
                else{
                    colors = new ArrayList<Integer>();
                    // Log.v(TAG, "Color String: " + colorString);
                    String[] ca = colorString.split(" ");
                    for(int j = 0; j < ca.length; j++){
                        colors.add(j, Integer.parseInt(ca[j]));
                    }
                }
            }
            if(me.getKey().toString().equals("audio_location")){
                String loc = me.getValue().toString();
                if(!loc.equals("")){
                    audioLocation = loc;
                }
            }

		}
	}

	
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode){
        
    	
    	case 1:
            
    		if(resultCode == Activity.RESULT_OK){
                /* Restart the Memo activity */
    			returnValue = 2;
    		}
    		else if(resultCode == Activity.RESULT_CANCELED){
    			returnValue = 0;
    		}
            break;
            
    	case 2:
            /* Determine whether to update the screen */
    		if(resultCode == Activity.RESULT_OK){
                /* Just reload data */
    			returnValue = 1;
    		}
    		else if(resultCode == Activity.RESULT_CANCELED){
    			returnValue = 0;
    		}
            break;

        case DIALOG_EDIT:
    		if(resultCode == Activity.RESULT_OK){
                /* In case of creating new items, this should be handled
                 * separated by different screens */
                refreshAfterEditItem();
    			returnValue = 1;
    		}
    		else if(resultCode == Activity.RESULT_CANCELED){
    			returnValue = 0;
    		}
            break;
    		
    	}
    }
	
	protected void updateMemoScreen() {
		/* update the main screen according to the currentItem */
		
        /* The q/a ratio is not as whe it seems
         * It displays differently on the screen
         */
		LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
		LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
		float qRatio = Float.valueOf(qaRatio.substring(0, qaRatio.length() - 1));

        if(qRatio > 99.0f){
            layoutAnswer.setVisibility(View.GONE);
            layoutQuestion.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
            layoutAnswer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
        }
        else if(qRatio < 1.0f){
            layoutQuestion.setVisibility(View.GONE);
            layoutQuestion.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
            layoutAnswer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
        }
        else{

            float aRatio = 100.0f - qRatio;
            qRatio /= 50.0;
            aRatio /= 50.0;
            layoutQuestion.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, qRatio));
            layoutAnswer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, aRatio));
        }
        /* Set both background and text color */
        setScreenColor();
		if(currentItem == null){
			new AlertDialog.Builder(this)
			    .setTitle(this.getString(R.string.memo_no_item_title))
			    .setMessage(this.getString(R.string.memo_no_item_message))
			    .setNeutralButton(getString(R.string.back_menu_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        /* Finish the current activity and go back to the last activity.
                         * It should be the open screen. */
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.learn_ahead), new OnClickListener(){
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                        Intent myIntent = new Intent();
                        myIntent.setClass(MemoScreenBase.this, MemoScreen.class);
                        myIntent.putExtra("dbname", dbName);
                        myIntent.putExtra("dbpath", dbPath);
                        myIntent.putExtra("learn_ahead", true);
                        startActivity(myIntent);
                    }
                })
                .create()
                .show();
			
		}
        else{
            if(copyClipboard){
                ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                cm.setText(currentItem.getQuestion());
            }
			displayQA(currentItem);
        }
		
	}

	protected void displayQA(Item item) {
		/* Display question and answer according to item */
        
		TextView questionView = (TextView) findViewById(R.id.question);
		TextView answerView = (TextView) findViewById(R.id.answer);
        /* Set the typeface of question an d answer */
        if(!questionTypeface.equals("")){
            try{
                Typeface qt = Typeface.createFromFile(questionTypeface);
                questionView.setTypeface(qt);
            }
            catch(Exception e){
                Log.e(TAG, "Typeface error when setting question font", e);
            }

        }
        if(!answerTypeface.equals("")){
            try{
                Typeface at = Typeface.createFromFile(answerTypeface);
                answerView.setTypeface(at);
            }
            catch(Exception e){
                Log.e(TAG, "Typeface error when setting answer font", e);
            }

        }
		
		
		if(this.htmlDisplay.equals("both")){
            /* Use HTML to display */
			CharSequence sq = Html.fromHtml(ArabicUtilities.reshape(item.getQuestion()), this, this);
			CharSequence sa = Html.fromHtml(ArabicUtilities.reshape(item.getAnswer()), this, this);
			//CharSequence sa = Html.fromHtml(item.getAnswer());

			
			//questionView.setText(ArabicUtilities.reshape(sq.toString()));
			//answerView.setText(ArabicUtilities.reshape(sa.toString()));
            questionView.setText(sq);
            answerView.setText(sa);
			
		}
		else if(this.htmlDisplay.equals("question")){
			CharSequence sq = Html.fromHtml(ArabicUtilities.reshape(item.getQuestion()));
			questionView.setText(sq);
            answerView.setText(ArabicUtilities.reshape(item.getAnswer()));
		}
		else if(this.htmlDisplay.equals("answer")){
            questionView.setText(ArabicUtilities.reshape(item.getQuestion()));
			CharSequence sa = Html.fromHtml(ArabicUtilities.reshape(item.getAnswer()));
			answerView.setText(sa);
		}
		else{
			//questionView.setText(new StringBuilder().append(item.getQuestion()));
			//answerView.setText(new StringBuilder().append(item.getAnswer()));
            questionView.setText(ArabicUtilities.reshape(item.getQuestion()));
            answerView.setText(ArabicUtilities.reshape(item.getAnswer()));
		}
		
        /* Here is tricky to set up the alignment of the text */
		if(questionAlign.equals("center")){
			questionView.setGravity(Gravity.CENTER);
			LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
			layoutQuestion.setGravity(Gravity.CENTER);
		}
		else if(questionAlign.equals("right")){
			questionView.setGravity(Gravity.RIGHT);
			LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
			layoutQuestion.setGravity(Gravity.NO_GRAVITY);
		}
		else{
			questionView.setGravity(Gravity.LEFT);
			LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
			layoutQuestion.setGravity(Gravity.NO_GRAVITY);
		}
		if(answerAlign.equals("center")){
			answerView.setGravity(Gravity.CENTER);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutAnswer.setGravity(Gravity.CENTER);
		} else if(answerAlign.equals("right")){
			answerView.setGravity(Gravity.RIGHT);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutAnswer.setGravity(Gravity.NO_GRAVITY);
			
		}
		else{
			answerView.setGravity(Gravity.LEFT);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutAnswer.setGravity(Gravity.NO_GRAVITY);
		}
		questionView.setTextSize((float)questionFontSize);
		answerView.setTextSize((float)answerFontSize);

		buttonBinding();

	}


    
    private void setScreenColor(){
        // Set both text and the background color
        if(colors != null){
            TextView questionView = (TextView) findViewById(R.id.question);
            TextView answerView = (TextView) findViewById(R.id.answer);
            LinearLayout questionLayout = (LinearLayout)findViewById(R.id.layout_question);
            LinearLayout answerLayout = (LinearLayout)findViewById(R.id.layout_answer);
            LinearLayout horizontalLine = (LinearLayout)findViewById(R.id.horizontalLine);
            LinearLayout buttonLayout = (LinearLayout)findViewById(R.id.layout_buttons);
            questionView.setTextColor(colors.get(0));
            answerView.setTextColor(colors.get(1));
            questionLayout.setBackgroundColor(colors.get(2));
            answerLayout.setBackgroundColor(colors.get(3));
            buttonLayout.setBackgroundColor(colors.get(3));
            horizontalLine.setBackgroundColor(colors.get(4));
        }
            TextView questionView = (TextView) findViewById(R.id.question);


    }


    protected void showEditDialog(){
        /* This method will show the dialog after long click 
         * on the screen 
         * */
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.memo_edit_dialog_title))
            .setItems(R.array.memo_edit_dialog_list, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    if(which == 0){
                        /* This is a customized dialog inflated from XML */
                        doEdit(currentItem);
                        //doFilter();
                    }
                    if(which == 1){
                        /* Go to preview/edit screen */
                        Intent myIntent = new Intent(MemoScreenBase.this, EditScreen.class);
                        myIntent.putExtra("dbname", dbName);
                        myIntent.putExtra("dbpath", dbPath);
                        myIntent.putExtra("openid", currentItem.getId());
                        myIntent.putExtra("active_filter", activeFilter);
                        startActivity(myIntent);
                    }
                    if(which == 2){
                        /* Delete current card */
                        doDelete();
                    }
                    if(which == 3){
                        /* Skip this card forever */
                        doSkip();
                    }
                }
            })
            .create()
            .show();
    }

    protected void doEdit(Item item){
        /* Edit current card */
        /* This is a customized dialog inflated from XML */
        //showDialog(DIALOG_EDIT);
        Intent myIntent = new Intent(this, CardEditor.class);
        myIntent.putExtra("item", item);
        myIntent.putExtra("dbpath", dbPath);
        myIntent.putExtra("dbname", dbName);

        startActivityForResult(myIntent, DIALOG_EDIT);
    }

    protected void doDelete(){
        new AlertDialog.Builder(MemoScreenBase.this)
            .setTitle(getString(R.string.detail_delete))
            .setMessage(getString(R.string.delete_warning))
            .setPositiveButton(getString(R.string.yes_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        dbHelper.deleteItem(currentItem);
                        refreshAfterDeleteItem();
                    }
                })
            .setNegativeButton(getString(R.string.no_text), null)
            .create()
            .show();
    }

    protected void doSkip(){

        new AlertDialog.Builder(MemoScreenBase.this)
            .setTitle(getString(R.string.skip_text))
            .setMessage(getString(R.string.skip_warning))
            .setPositiveButton(getString(R.string.yes_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        currentItem.skip();
                        dbHelper.updateItem(currentItem, false);
                        restartActivity();
                    }
                })
            .setNegativeButton(getString(R.string.no_text), null)
            .create()
            .show();
    }

    protected void doFilter(){
        LayoutInflater factory = LayoutInflater.from(MemoScreenBase.this);
        View filterView = factory.inflate(R.layout.filter_dialog, (ViewGroup)findViewById(R.id.filter_dialog_root));
        final EditText filterEdit = (EditText)filterView.findViewById(R.id.filter_dialog_edit);
        final ListView filterList = (ListView)filterView.findViewById(R.id.filter_list);
        final ArrayList<String> filterArray = dbHelper.getRecentFilters();
        if(filterArray != null){
            filterList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filterArray));
            /* Click to set the text edit */
            filterList.setOnItemClickListener(new OnItemClickListener(){
                public void onItemClick(AdapterView<?> parentView, View childView, int position, long id){
                    filterEdit.setText(filterArray.get(position));
                }
            });
        }


        new AlertDialog.Builder(MemoScreenBase.this)
            .setTitle(getString(R.string.filter_text))
            .setView(filterView)
            .setPositiveButton(getString(R.string.filter_text), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    activeFilter = filterEdit.getText().toString();
                    if(!activeFilter.equals("")){
                        dbHelper.setRecentFilters(filterEdit.getText().toString());
                    }
                    restartActivity();
                }
            })
            .setNeutralButton(getString(R.string.clear_filter_text), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    deleteFilterRecentList();
                }
            })
            .setNegativeButton(getString(R.string.cancel_text), null)
            .create()
            .show();
    }

    protected void showFilterFailureDialog(){
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.filter_failure_title))
            .setMessage(getString(R.string.filter_failure_message))
            .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1){
                    activeFilter = "";
                    restartActivity();
                }
            })
            .create()
            .show();
    }

    protected void deleteFilterRecentList(){
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.warning_text))
            .setMessage(getString(R.string.clear_filter_message))
            .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1){
                    dbHelper.deleteFilters();
                    activeFilter = "";
                    restartActivity();
                }
            })
            .setNegativeButton(getString(R.string.cancel_text), null)
            .create()
            .show();
    }

    @Override
    public Drawable getDrawable(String source){
        Log.v(TAG, "Source: " + source);
        /* Try the image in /sdcard/anymemo/images/dbname/myimg.png */
        try{
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_image_dir) + "/" + dbName + "/" + source;
            Drawable d = Drawable.createFromStream(new FileInputStream(filePath), source);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            return d;
        }
        catch(Exception e){
        }

        /* Try the image in /sdcard/anymemo/images/myimg.png */
        try{
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_image_dir) + "/" + source;
            Drawable d = Drawable.createFromStream(new FileInputStream(filePath), source);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            return d;
        }
        catch(Exception e){
        }

        /* Try the image from internet */
        try{
            String url = source;
            String src_name = source; 
            Drawable d = Drawable.createFromStream(((InputStream)new URL(url).getContent()), src_name);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            return d;
        }
        catch(Exception e){
        }

        /* Fallback, display default image */
        Drawable d = getResources().getDrawable(R.drawable.picture);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        return d;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader){
        return;
    }

    @Override
    protected Dialog onCreateDialog(int id){

        switch(id){
            /* The edit dialog can be displayed using showDialog(DIALOG_EDIT)*/
            case DIALOG_EDIT:{
                 getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                              WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);



                LayoutInflater factory = LayoutInflater.from(MemoScreenBase.this);
                final View editView = factory.inflate(R.layout.edit_dialog, null);
                final EditText eq = (EditText)editView.findViewById(R.id.edit_dialog_question_entry);
                final EditText ea = (EditText)editView.findViewById(R.id.edit_dialog_answer_entry);
                final EditText ca = (EditText)editView.findViewById(R.id.edit_dialog_category_entry);
                final Button btnDlgSave = (Button)editView.findViewById(R.id.edit_dialog_button_save);
                final Button btnDlgCancel = (Button)editView.findViewById(R.id.edit_dialog_button_cancel);


                final Dialog dialog = new Dialog(this, R.style.edit_dialog_style);
                dialog.setContentView(editView);
                dialog.setTitle(R.string.memo_edit_dialog_title);

                eq.setText(currentItem.getQuestion());
                ea.setText(currentItem.getAnswer());
                ca.setText(currentItem.getCategory());

                btnDlgSave.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String qText = eq.getText().toString();
                        String aText = ea.getText().toString();
                        String cText = ca.getText().toString();
                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("question", qText);
                        hm.put("answer", aText);
                        hm.put("category", cText);
                        currentItem.setData(hm);
                        dbHelper.addOrReplaceItem(currentItem);
                        updateMemoScreen();
                        try{
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(dialog.findViewById(R.id.edit_dialog_root).getWindowToken(), 0);
                        }
                        catch(Exception e){
                            Log.e(TAG, "Input method problems here", e);
                        }
                        refreshAfterEditItem();
                        removeDialog(DIALOG_EDIT);
                    }
                });
                btnDlgCancel.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        String qText = eq.getText().toString();
                        String aText = ea.getText().toString();
                        String cText = ca.getText().toString();
                        if(!qText.equals(currentItem.getQuestion()) || !aText.equals(currentItem.getAnswer()) || !cText.equals(currentItem.getCategory())){
                            new AlertDialog.Builder(MemoScreenBase.this)
                                .setTitle(R.string.warning_text)
                                .setMessage(R.string.edit_dialog_unsave_warning)
                                .setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface  d, int which){
                                        try{
                                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(dialog.findViewById(R.id.edit_dialog_root).getWindowToken(), 0);
                                        }
                                        catch(Exception e){
                                            Log.e(TAG, "Input method problems here", e);
                                        }
                                        refreshAfterEditItem();
                                        removeDialog(DIALOG_EDIT);

                                    }
                                }) .setNegativeButton(R.string.no_text, null)
                                .create()
                                .show();
                                
                        }
                        else{
                            /* Hide the keyboard before closing
                             * this is a known android bug
                             * issue 7115
                             * */
                            try{
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(dialog.findViewById(R.id.edit_dialog_root).getWindowToken(), 0);
                            }
                            catch(Exception e){
                                Log.e(TAG, "Input method problems here", e);
                            }
                            refreshAfterEditItem();
                            removeDialog(DIALOG_EDIT);

                        }
                    }
                });

                dialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
                    public  boolean onKey(DialogInterface  dialog, int keyCode, KeyEvent  event){
                        if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK){
                            String qText = eq.getText().toString();
                            String aText = ea.getText().toString();
                            String cText = ca.getText().toString();
                            if(!qText.equals(currentItem.getQuestion()) || !aText.equals(currentItem.getAnswer()) || !cText.equals(currentItem.getCategory())){
                                new AlertDialog.Builder(MemoScreenBase.this)
                                    .setTitle(R.string.warning_text)
                                    .setMessage(R.string.edit_dialog_unsave_warning)
                                    .setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface  dialog, int which){
                                refreshAfterEditItem();
                                removeDialog(DIALOG_EDIT);

                                        }
                                    })
                                    .setNegativeButton(R.string.no_text, null)
                                    .create()
                                    .show();
                                    
                            }
                            else{
                                refreshAfterEditItem();
                                removeDialog(DIALOG_EDIT);
                            }
                            return true;
                        }
                        return false;
                    }
                });


                

                return dialog;
            }

            default:
                return super.onCreateDialog(id);

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
	
}

