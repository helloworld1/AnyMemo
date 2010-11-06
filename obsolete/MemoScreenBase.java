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


public abstract class MemoScreenBase extends Activity {
	protected String dbName;
	protected String dbPath;
	protected boolean showAnswer;
	protected Item currentItem;
    private int prevScheduledItemCount;
    private int prevNewItemCount;

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
        enableThirdPartyArabic = settings.getBoolean("enable_third_party_arabic", true);

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
                Bundle extras = data.getExtras();
                if(extras != null){
                    currentItem = (Item)extras.getSerializable("item");
                }
                refreshAfterEditItem();
    			// returnValue = 0;
    		}
    		else if(resultCode == Activity.RESULT_CANCELED){
    			returnValue = 0;
    		}
            break;
    		
    	}
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



    @Override
    protected Dialog onCreateDialog(int id){

        switch(id){
            /* The edit dialog can be displayed using showDialog(DIALOG_EDIT)*/
            case DIALOG_EDIT:{
                 getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                              WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

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

    public void showNoItemDialog(){
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
            .setOnCancelListener(new DialogInterface.OnCancelListener(){
                public void onCancel(DialogInterface dialog){
                    finish();
                }
            })
            .create()
            .show();
    }

    public void getFromClickboard(){
        if(copyClipboard){
            ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setText(currentItem.getQuestion());
        }
    }

    public void setButtonColors(){
            LinearLayout buttonLayout = (LinearLayout)findViewById(R.id.layout_buttons);
            buttonLayout.setBackgroundColor(colors.get(3));
    }


	
}

