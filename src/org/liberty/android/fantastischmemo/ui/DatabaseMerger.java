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
package org.liberty.android.fantastischmemo.ui;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DatabaseMerger extends AMActivity implements View.OnClickListener{
    public static final String EXTRA_SRC_PATH = "src_path";
    private final static String TAG = "org.liberty.android.fantastischmemo.ui.DatabaseMerger";

    private final int ACTIVITY_FB_TARGET = 1;
    private final int ACTIVITY_FB_SOURCE = 2;
    private EditText targetEdit;
    private EditText sourceEdit;
    private Button mergeButton;
    private Button cancelButton;

    private DatabaseUtil databaseUtil;

    @Inject
    public void setDatabaseUtil(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.merge_layout);
        Bundle extras = getIntent().getExtras();
        String dbPath = "";
        if (extras != null) {
            dbPath = extras.getString(EXTRA_SRC_PATH);
        }
        targetEdit = (EditText)findViewById(R.id.target_db_edit);
        sourceEdit = (EditText)findViewById(R.id.source_db_edit);
        mergeButton = (Button)findViewById(R.id.merge_button);
        cancelButton = (Button)findViewById(R.id.cancel_button);
        targetEdit.setOnClickListener(this);
        sourceEdit.setOnClickListener(this);
        mergeButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        targetEdit.setText(dbPath);
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
                String dbPath = data.getStringExtra(FileBrowserActivity.EXTRA_RESULT_PATH);
                targetEdit.setText(dbPath);
                break;
            }

            case ACTIVITY_FB_SOURCE:
            {
                String dbPath = data.getStringExtra(FileBrowserActivity.EXTRA_RESULT_PATH);
                sourceEdit.setText(dbPath);
                break;
            }

        }
    }

    @Override
    public void onClick(View v){
        if(v == targetEdit){
            Intent myIntent = new Intent();
            myIntent.setClass(this, FileBrowserActivity.class);
            myIntent.putExtra(FileBrowserActivity.EXTRA_FILE_EXTENSIONS, ".db");
            startActivityForResult(myIntent, ACTIVITY_FB_TARGET);
        }

        if(v == sourceEdit){
            Intent myIntent = new Intent();
            myIntent.setClass(this, FileBrowserActivity.class);
            myIntent.putExtra(FileBrowserActivity.EXTRA_FILE_EXTENSIONS, ".db");
            startActivityForResult(myIntent, ACTIVITY_FB_SOURCE);
        }

        if(v == mergeButton){
            AMGUIUtility.doProgressTask(this, R.string.merging_title, R.string.merging_summary, new AMGUIUtility.ProgressTask(){
                public void doHeavyTask() throws Exception {
                    String targetPath = targetEdit.getText().toString();
                    String sourcePath = sourceEdit.getText().toString();
                    databaseUtil.mergeDatabases(targetPath, sourcePath);
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
}

