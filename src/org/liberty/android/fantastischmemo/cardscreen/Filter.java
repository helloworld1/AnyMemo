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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.List;

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

public class Filter extends Activity implements View.OnClickListener{

    private final static String TAG = "org.liberty.android.fantastischmemo.cardscreen.Filter";
    private Context mContext;
    private Dialog filterDialog;
    private EditText filterEdit;
    private ListView filterList;
    private DatabaseHelper dbHelper = null;
    private Button filterButton;
    private Button clearButton;
    private Button cancelButton;
    private String dbPath;
    private String dbName;
    private List<String> filterArray;


    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_dialog);
        filterEdit = (EditText)findViewById(R.id.filter_dialog_edit);
        filterList = (ListView)findViewById(R.id.filter_list);
        filterButton = (Button)findViewById(R.id.filter_button_filter);
        clearButton = (Button)findViewById(R.id.filter_button_clear);
        cancelButton = (Button)findViewById(R.id.filter_button_cancel);


        setTitle(R.string.filter_text);
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
            dbPath = extras.getString("dbpath");
            dbName = extras.getString("dbname");
        }
        try{
            dbHelper = new DatabaseHelper(this, dbPath, dbName);
        }
        catch(Exception e){
            Log.e(TAG, "Error opening the database", e);
            finish();
        }
        filterArray = dbHelper.getRecentFilters();
        if(filterArray != null){
            filterList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filterArray));
            /* Click to set the text edit */
            filterList.setOnItemClickListener(new OnItemClickListener(){
                public void onItemClick(AdapterView<?> parentView, View childView, int position, long id){
                    filterEdit.setText(filterArray.get(position));
                }
            });
        }
        filterButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override 
    public void onDestroy(){
        if(dbHelper != null){
            try{
                dbHelper.close();
            }
            catch(Exception e){
                Log.e(TAG, "Error closing database", e);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v){
        if(v == filterButton){
            String activeFilter = filterEdit.getText().toString();
            if(dbHelper.checkFilterValidity(activeFilter)){
                dbHelper.setRecentFilters(filterEdit.getText().toString());
                Intent resultIntent = new Intent();
                resultIntent.putExtra("filter", activeFilter);
                setResult(Activity.RESULT_OK, resultIntent);	
                finish();
            }
            else{
                showFilterFailureDialog();
            }
        }
        else if(v == clearButton){
            deleteFilterRecentList();
        }
        else if(v == cancelButton){
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, resultIntent);	
            finish();
        }
    }


    protected void showFilterFailureDialog(){
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.filter_failure_title))
            .setMessage(getString(R.string.filter_failure_message))
            .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1){
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
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("filter", "");
                    setResult(Activity.RESULT_OK, resultIntent);	
                    finish();
                }
            })
            .setNegativeButton(getString(R.string.cancel_text), null)
            .create()
            .show();
    }
}
