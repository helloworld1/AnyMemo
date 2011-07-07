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
import java.util.List;


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
import android.os.Environment;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
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
import android.database.SQLException;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;

public class EditScreen extends AMActivity{
    private final static String TAG = "org.liberty.android.fantastischmemo.cardscreen.EditScreen";
    private AnyMemoTTS questionTTS = null;
    private AnyMemoTTS answerTTS = null;
    private boolean searchInflated = false;
    private final int DIALOG_LOADING_PROGRESS = 100;
    private final int ACTIVITY_FILTER = 10;
    private final int ACTIVITY_EDIT = 11;
    private final int ACTIVITY_CARD_TOOLBOX = 12;
    private final int ACTIVITY_DB_TOOLBOX = 13;
    private final int ACTIVITY_GOTO_PREV = 14;
    private final int ACTIVITY_SETTINGS = 15;
    private final int ACTIVITY_LIST = 16;
    private final int ACTIVITY_MERGE = 17;
    private final int ACTIVITY_DETAIL = 18;
    private final static String WEBSITE_HELP_EDIT = "http://anymemo.org/wiki/index.php?title=Editing_screen";

    Handler mHandler;
    Item currentItem = null;
    Item savedItem = null;
    Item prevItem = null;
    String dbPath = "";
    String dbName = "";
    String activeFilter = "";
    FlashcardDisplay flashcardDisplay;
    SettingManager settingManager;
    ControlButtons controlButtons;
    DatabaseUtility databaseUtility;
    private GestureDetector gestureDetector;
    ItemManager itemManager;

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.memo_screen_layout);
        mHandler = new Handler();
        Bundle extras = getIntent().getExtras();
        int currentId = 1;
        if (extras != null) {
            dbPath = extras.getString("dbpath");
            dbName = extras.getString("dbname");
            activeFilter = extras.getString("filter");
            currentId = extras.getInt("id", 1);
        }
        try{
            settingManager = new SettingManager(this, dbPath, dbName);
            if(settingManager.getCardStyle() == SettingManager.CardStyle.DOUBLE_SIDED){
                flashcardDisplay = new DoubleSidedCardDisplay(this, settingManager);
            }
            else{
                flashcardDisplay = new SingleSidedCardDisplay(this, settingManager);
            }
            controlButtons = new EditScreenButtons(this);

            /* databaseUtility is for global db operations */
            databaseUtility =  new DatabaseUtility(this, dbPath, dbName);
            itemManager = new ItemManager.Builder(this, dbPath, dbName)
                .setFilter(activeFilter)
                .build();

            initTTS();
            composeViews();
            currentItem = itemManager.getItem(currentId);
            if(currentItem == null){
                itemManager.getItem(1);
            }
            updateCardFrontSide();
            updateTitle();
            setViewListeners();
            /* Double sided card can't use the flip gesture*/
            if(settingManager.getCardStyle() != SettingManager.CardStyle.DOUBLE_SIDED){
                gestureDetector= new GestureDetector(EditScreen.this, gestureListener);
                flashcardDisplay.setScreenOnTouchListener(viewTouchListener);
            }
            registerForContextMenu(flashcardDisplay.getView());
        }
        catch(Exception e){
            Log.e(TAG, "Error in the onCreate()", e);
            AMGUIUtility.displayError(this, getString(R.string.open_database_error_title), getString(R.string.open_database_error_message), e);
        }

        /* 
         * Currently always set the result to OK
         * to assume there are always some changes.
         * This may be changed in the future to reflect the
         * real changes
         */
        setResult(Activity.RESULT_OK);

    }

    @Override
    public void onDestroy(){
        if(itemManager != null){
            itemManager.close();
        }
        if(settingManager != null){
            settingManager.close();
        }
        if(questionTTS != null){
            questionTTS.shutdown();
        }
        if(answerTTS != null){
            answerTTS.shutdown();
        }

        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode ==Activity.RESULT_CANCELED){
            return;
        }
        /* Refresh the activity according to activities */
        switch(requestCode){
            case ACTIVITY_EDIT:
            {

                Bundle extras = data.getExtras();
                Item item = extras.getParcelable("item");
                if(item != null){
                    currentItem = item;
                }
                restartActivity();
                break;
            }

            case ACTIVITY_FILTER:
            {
                Bundle extras = data.getExtras();
                activeFilter = extras.getString("filter");
                restartActivity();
                break;
            }

            case ACTIVITY_SETTINGS:
            {
                restartActivity();
                break;
            }
            case ACTIVITY_LIST:
            {
                Bundle extras = data.getExtras();
                currentItem = extras.getParcelable("item");
                restartActivity();
                break;
            }
            case ACTIVITY_MERGE:
            {
                restartActivity();
                break;
            }
            case ACTIVITY_DETAIL:
            {
                restartActivity();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuspeakquestion:
            {
                if(questionTTS != null && currentItem != null){
                    questionTTS.sayText(currentItem.getQuestion());
                }
                return true;
            }

            case R.id.menuspeakanswer:
            {
                if(answerTTS != null && currentItem != null){
                    answerTTS.sayText(currentItem.getAnswer());
                }
                return true;
            }

            case R.id.editmenu_settings_id:
            {
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.putExtra("dbname", dbName);
                myIntent.putExtra("dbpath", dbPath);
                startActivityForResult(myIntent, ACTIVITY_SETTINGS);
                return true;
            }

            case R.id.editmenu_delete_id:
            {
                deleteCurrent();
                return true;
            }


            case R.id.editmenu_detail_id:
            {
                if(currentItem != null){
                    Intent myIntent = new Intent(this, DetailScreen.class);
                    myIntent.putExtra("dbname", this.dbName);
                    myIntent.putExtra("dbpath", this.dbPath);
                    myIntent.putExtra("itemid", currentItem.getId());
                    startActivityForResult(myIntent, ACTIVITY_DETAIL);
                }
                return true;
            }
            case R.id.editmenu_list_id:
            {
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.setClass(this, ListEditScreen.class);
                myIntent.putExtra("dbname", dbName);
                myIntent.putExtra("dbpath", dbPath);
                if(currentItem != null){
                    myIntent.putExtra("openid", currentItem.getId());
                }
                startActivityForResult(myIntent, ACTIVITY_LIST);
                return true;
            }

            case R.id.menu_edit_filter:
            {
                Intent myIntent = new Intent(this, Filter.class);
                myIntent.putExtra("dbname", dbName);
                myIntent.putExtra("dbpath", dbPath);
                startActivityForResult(myIntent, ACTIVITY_FILTER);
                return true;
            }
            case R.id.editmenu_search_id:
            {
                createSearchOverlay();
                return true;
            }

            case R.id.editmenu_help:
            {
                Intent myIntent = new Intent();
                myIntent.setAction(Intent.ACTION_VIEW);
                myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                myIntent.setData(Uri.parse(WEBSITE_HELP_EDIT));
                startActivity(myIntent);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editscreen_context_menu, menu);
        menu.setHeaderTitle(R.string.menu_text);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuitem) {
        switch(menuitem.getItemId()) {
            case R.id.menu_context_copy:
            {

                if(currentItem != null){
                    savedItem = new Item.Builder()
                        .setId(currentItem.getId())
                        .setQuestion(currentItem.getQuestion())
                        .setAnswer(currentItem.getAnswer())
                        .setCategory(currentItem.getCategory())
                        .build();
                }
                return true;
            }
            case R.id.menu_context_paste:
            {
                if(savedItem != null){
                    itemManager.insert(savedItem, currentItem.getId());
                    /* Set the Id to the current one */
                    currentItem = new Item.Builder(savedItem)
                        .setId(currentItem.getId() + 1)
                        .build();
                    updateCardFrontSide();
                    updateTitle();
                }

                return true;
            }
            case R.id.menu_context_swap_current:
            {
                if(currentItem != null){
                    databaseUtility.swapSingelItem(currentItem);
                }
                return true;
            }

            case R.id.menu_context_reset_current:
            {
                if(currentItem != null){
                    databaseUtility.resetCurrentLearningData(currentItem);
                }
                return true;
            }

            case R.id.menu_context_wipe:
            {
                databaseUtility.wipeLearningData();
                return true;
            }

            case R.id.menu_context_swap:
            {
                databaseUtility.swapAllQA();
                return true;
            }

            case R.id.menu_context_remove_dup:
            {
                databaseUtility.removeDuplicates();
                return true;
            }

            case R.id.menu_context_merge_db:
            {
                Intent myIntent = new Intent(this, DatabaseMerger.class);
                myIntent.putExtra("dbpath", dbPath);
                myIntent.putExtra("dbname", dbName);
                startActivityForResult(myIntent, ACTIVITY_MERGE);
                return true;
            }

            case R.id.menu_context_shuffle:
            {
                databaseUtility.shuffleDatabase();
                return true;
            }

            default:
            {
                return super.onContextItemSelected(menuitem);
            }
        }
    }

    private void initTTS(){
        String audioDir = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_audio_dir);
        Locale ql = settingManager.getQuestionAudioLocale();
        Locale al = settingManager.getAnswerAudioLocale();
        if(settingManager.getQuestionUserAudio()){
            questionTTS = new AudioFileTTS(audioDir, dbName);
        }
        else if(ql != null){
            questionTTS = new AnyMemoTTSPlatform(this, ql);
        }
        else{
            questionTTS = null;
        }
        if(settingManager.getAnswerUserAudio()){
            answerTTS = new AudioFileTTS(audioDir, dbName);
        }
        else if(al != null){
            answerTTS = new AnyMemoTTSPlatform(this, al);
        }
        else{
            answerTTS = null;
        }
    }

    @Override
    public void restartActivity(){
        Intent myIntent = new Intent(this, EditScreen.class);
        if(currentItem != null){
            myIntent.putExtra("id", currentItem.getId());
        }
        myIntent.putExtra("dbname", dbName);
        myIntent.putExtra("dbpath", dbPath);
        myIntent.putExtra("filter", activeFilter);
        finish();
        startActivity(myIntent);
    }


    private void composeViews(){
        LinearLayout memoRoot = (LinearLayout)findViewById(R.id.memo_screen_root);

        LinearLayout flashcardDisplayView = (LinearLayout)flashcardDisplay.getView();
        LinearLayout controlButtonsView = (LinearLayout)controlButtons.getView();
        /* This li is make the background of buttons the same as answer */
        LinearLayout li = new LinearLayout(this);
        li.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.FILL_PARENT));
        List<Integer> colors = settingManager.getColors();
        if(colors != null){
            li.setBackgroundColor(settingManager.getColors().get(3));
        }
        /* 
         * -1: Match parent -2: Wrap content
         * This is necessary or the view will not be 
         * stetched
         */
        memoRoot.addView(flashcardDisplayView, -1, -1);
        li.addView(controlButtonsView, -1, -2);
        memoRoot.addView(li, -1, -2);
        flashcardDisplayView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
    }

    void setViewListeners(){
        Map<String, Button> bm = controlButtons.getButtons();
        Button newButton = bm.get("new");
        Button editButton = bm.get("edit");
        Button prevButton = bm.get("prev");
        Button nextButton = bm.get("next");
        /* Set button listeners */
        newButton.setOnClickListener(newButtonListener);
        editButton.setOnClickListener(editButtonListener);
        prevButton.setOnClickListener(prevButtonListener);
        nextButton.setOnClickListener(nextButtonListener);
        /* For double sided card, the view can be toggled */
        if(settingManager.getCardStyle() == SettingManager.CardStyle.DOUBLE_SIDED){

            flashcardDisplay.setQuestionLayoutClickListener(toggleCardSideListener);
            flashcardDisplay.setAnswerLayoutClickListener(toggleCardSideListener);
            flashcardDisplay.setQuestionTextClickListener(toggleCardSideListener);
            flashcardDisplay.setAnswerTextClickListener(toggleCardSideListener);
            flashcardDisplay.setQuestionLayoutLongClickListener(popupContextMenuListener);
            flashcardDisplay.setAnswerLayoutLongClickListener(popupContextMenuListener);
        }


    }

    private View.OnLongClickListener popupContextMenuListener = new View.OnLongClickListener(){
        public boolean onLongClick(View v){
            closeContextMenu();
            EditScreen.this.openContextMenu(flashcardDisplay.getView());
            return true;
        }
    };

    private View.OnClickListener newButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            Intent myIntent = new Intent(EditScreen.this, CardEditor.class);
            myIntent.putExtra("item", currentItem); 
            myIntent.putExtra("dbpath", dbPath);
            myIntent.putExtra("dbname", dbName);
            myIntent.putExtra("new", true);
            startActivityForResult(myIntent, ACTIVITY_EDIT);
        }
    };

    private View.OnClickListener editButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            Intent myIntent = new Intent(EditScreen.this, CardEditor.class);
            myIntent.putExtra("item", currentItem);
            myIntent.putExtra("dbpath", dbPath);
            myIntent.putExtra("dbname", dbName);
            myIntent.putExtra("new", false);
            startActivityForResult(myIntent, ACTIVITY_EDIT);
        }
    };

    private void updateTitle(){
        if(currentItem != null){
            int total = itemManager.getStatInfo()[0];
            String titleString = getString(R.string.stat_total) + total + " " + getString(R.string.memo_current_id) + " " + currentItem.getId();
            if(currentItem != null && currentItem.getCategory() != null){
                titleString += "  " + currentItem.getCategory();
            }
            setTitle(titleString);
        }
    }
    
    private void gotoNext(){
        currentItem = itemManager.getNextItem(currentItem);
        updateCardFrontSide();
        updateTitle();
    }

    private void deleteCurrent(){
        if(currentItem != null){
            new AlertDialog.Builder(EditScreen.this)
                .setTitle(getString(R.string.detail_delete))
                .setMessage(getString(R.string.delete_warning))
                .setPositiveButton(getString(R.string.yes_text),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            currentItem = itemManager.deleteItem(currentItem);
                            restartActivity();
                        }
                    })
                .setNegativeButton(getString(R.string.no_text), null)
                .create()
                .show();
        }
    }

    private void gotoPrev(){
        currentItem = itemManager.getPreviousItem(currentItem);
        updateCardFrontSide();
        updateTitle();
    }

    /* 
     * Show the front side of the current card 
     * This method is called instead directly update the flashcard
     * so both single and double sided card will work.
     */
    private void updateCardFrontSide(){
        if(currentItem != null){
            if(settingManager.getCardStyle() == SettingManager.CardStyle.DOUBLE_SIDED){
                /* Double sided card, show front */
                flashcardDisplay.updateView(currentItem, false);
            }
            else{
                /* Single sided, show both answer and questjion. */
                flashcardDisplay.updateView(currentItem, true);
            }
        }
    }

    private void createSearchOverlay(){
        if(searchInflated == false){
            LinearLayout root = (LinearLayout)findViewById(R.id.memo_screen_root);
            LayoutInflater.from(this).inflate(R.layout.search_overlay, root);
            ImageButton close = (ImageButton)findViewById(R.id.search_close_btn);
            close.setOnClickListener(closeSearchButtonListener);
            ImageButton prev = (ImageButton)findViewById(R.id.search_previous_btn);
            prev.setOnClickListener(searchPrevButtonListener);
            ImageButton next = (ImageButton)findViewById(R.id.search_next_btn);
            next.setOnClickListener(searchNextButtonListener);

            EditText editEntry = (EditText)findViewById(R.id.search_entry);
            editEntry.requestFocus();
            searchInflated = true;

        }
        else{
            LinearLayout layout = (LinearLayout)findViewById(R.id.search_root);
            layout.setVisibility(View.VISIBLE);
        }


    }

    private void dismissSearchOverlay(){
        if(searchInflated == true){
            LinearLayout layout = (LinearLayout)findViewById(R.id.search_root);
            layout.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener prevButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            gotoPrev();
            if(questionTTS != null){
                questionTTS.stop();
            }
        }
    };

    private View.OnClickListener nextButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            gotoNext();
            if(answerTTS != null){
                answerTTS.stop();
            }
        }
    };

    private View.OnClickListener closeSearchButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            dismissSearchOverlay();
        }
    };

    private View.OnClickListener searchNextButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            EditText editEntry = (EditText)findViewById(R.id.search_entry);
            String text = editEntry.getText().toString();
            Item item = itemManager.search(text, true, currentItem);
            if(item != null){
                currentItem = item;
                updateCardFrontSide();
                updateTitle();
            }
        }
    };

    private View.OnClickListener searchPrevButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            EditText editEntry = (EditText)findViewById(R.id.search_entry);
            String text = editEntry.getText().toString();
            Item item = itemManager.search(text, false, currentItem);
            if(item != null){
                currentItem = item;
                updateCardFrontSide();
                updateTitle();
            }
        }
    };

    private View.OnTouchListener viewTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event){
            return gestureDetector.onTouchEvent(event);
        }
    };

    private View.OnClickListener toggleCardSideListener = new View.OnClickListener(){
        public void onClick(View v){
            if(currentItem != null){
                /* Double sided card, the click will toggle question and answer */
                if(settingManager.getCardStyle() == SettingManager.CardStyle.DOUBLE_SIDED){
                    if(flashcardDisplay.isAnswerShown()){
                        flashcardDisplay.updateView(currentItem, false);
                    }
                    else{
                        flashcardDisplay.updateView(currentItem, true);
                    }
                }
            }
        }
    };


    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener(){
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override 
        public boolean onDown(MotionEvent e){
            /* Trick: Prevent the menu to popup twice */
            return true;
        }
        @Override 
        public void onLongPress(MotionEvent e){
            closeContextMenu();
            EditScreen.this.openContextMenu(flashcardDisplay.getView());
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    /* Swipe Right to Left event */
                    gotoNext();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    /* Swipe Left to Right event */
                    gotoPrev();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling gesture left/right event", e);
            }
            return false;
        }
    };

}


