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

import org.amr.arabic.ArabicUtilities;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.view.ContextMenu;
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
import android.net.Uri;
import android.database.SQLException;

public class DatabaseMerger extends AMActivity implements View.OnClickListener{
    private final static String TAG = "org.liberty.android.fantastischmemo.cardscreen.DatabaseMerger";
    private final int ACTIVITY_FB_TARGET = 1;
    private final int ACTIVITY_FB_SOURCE = 2;
    private EditText targetEdit;
    private EditText sourceEdit;
    private Button mergeButton;
    private Button cancelButton;

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.merge_layout);
        Bundle extras = getIntent().getExtras();
        String dbPath = "";
        String dbName = "";
        if (extras != null) {
            dbPath = extras.getString("dbpath");
            dbName = extras.getString("dbname");
        }
        targetEdit = (EditText)findViewById(R.id.target_db_edit);
        sourceEdit = (EditText)findViewById(R.id.source_db_edit);
        mergeButton = (Button)findViewById(R.id.merge_button);
        cancelButton = (Button)findViewById(R.id.cancel_button);
        targetEdit.setOnClickListener(this);
        sourceEdit.setOnClickListener(this);
        mergeButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        targetEdit.setText(dbPath + dbName);
        
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Return activity result");
        if(resultCode ==Activity.RESULT_CANCELED){
            return;
        }
        Log.v(TAG, "Return activity NOT CANCELLED");
        /* Refresh the activity according to activities */
        switch(requestCode){
            case ACTIVITY_FB_TARGET:
            {
                String dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                String dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                if(dbName != null && dbPath != null){
                    String fullname = dbPath + "/" + dbName;
                    targetEdit.setText(fullname);
                }
                break;
            }

            case ACTIVITY_FB_SOURCE:
            {
                String dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                String dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                if(dbName != null && dbPath != null){
                    String fullname = dbPath + "/" + dbName;
                    sourceEdit.setText(fullname);
                }
                break;
            }
                
        }
    }
    
    @Override
    public void onClick(View v){
        if(v == targetEdit){
            Intent myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("file_extension", ".db");
            startActivityForResult(myIntent, ACTIVITY_FB_TARGET);
        }

        if(v == sourceEdit){
            Intent myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("file_extension", ".db");
            startActivityForResult(myIntent, ACTIVITY_FB_SOURCE);
        }

        if(v == mergeButton){
            AMGUIUtility.doProgressTask(this, R.string.merging_title, R.string.merging_summary, new AMGUIUtility.ProgressTask(){
                public void doHeavyTask(){
                    String[] splittedpath1 = splitDBPath(targetEdit.getText().toString());
                    String[] splittedpath2 = splitDBPath(sourceEdit.getText().toString());
                    /* splittedpath1[0] is the dbPath for the source
                     * and 1 is dbName */
                    DatabaseHelper dbHelper = new DatabaseHelper(DatabaseMerger.this, splittedpath1[0], splittedpath1[1]);
                    /* Merge to the back fo source database */
                    dbHelper.mergeDatabase(splittedpath2[0], splittedpath2[1]);
                    dbHelper.close();
                }

                public void doUITask(){
                    new AlertDialog.Builder(DatabaseMerger.this)
                        .setTitle(R.string.merge_success_title)
                        .setMessage(R.string.merge_success_message)
                        .setPositiveButton(R.string.back_menu_text, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface arg0, int arg1){
                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }
                        })
                        .create()
                        .show();
                }
            });

        }
        if(v == cancelButton){
            finish();
        }
    }

    /* 
     * Split the path into dbpath and dbname
     * return value is an array, first element is dbpath
     * second one is dbname
     */
    private String[] splitDBPath(String fullpath){
        int rightMostSlashIndex = -1;
        for(int i = 0; i < fullpath.length(); i++){
            if(fullpath.charAt(i) == '/'){
                rightMostSlashIndex = i;
            }
        }
        if(rightMostSlashIndex == -1){
            throw new IllegalArgumentException("Invalid path string: " + fullpath);
        }
        String path = fullpath.substring(0, rightMostSlashIndex + 1);
        String name = fullpath.substring(rightMostSlashIndex + 1, fullpath.length());
        return new String[]{path, name};
    }

}

