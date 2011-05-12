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

import org.liberty.android.fantastischmemo.cardscreen.*;
import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.util.Log;
import android.preference.PreferenceManager;

import java.io.File;

/* 
 * This class is invoked when the user share the card from other
 * apps like ColorDict 
 */
public class ShareScreen extends Activity implements View.OnClickListener{
    private static final String TAG = "org.liberty.android.fantastischmemo.ShareReceiver";
    private TextView dbnameView;
    private TextView questionView;
    private TextView answerView;
    private TextView categoryView;
    private TextView noteView;
    private Button saveButton;
    private Button savePrevButton;
    private Button cancelButton;
    private SharedPreferences settings;
    private final int ACTIVITY_FB = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_screen);
        dbnameView = (TextView)findViewById(R.id.share_db_name_entry);
        questionView = (TextView)findViewById(R.id.share_question_entry);
        answerView = (TextView)findViewById(R.id.share_answer_entry);
        categoryView = (TextView)findViewById(R.id.share_category_entry);
        noteView = (TextView)findViewById(R.id.share_note_entry);
        noteView.setText("");
        categoryView.setText("");
        saveButton = (Button)findViewById(R.id.share_button_save);
        savePrevButton = (Button)findViewById(R.id.share_button_saveprev);
        cancelButton = (Button)findViewById(R.id.share_button_cancel);
        saveButton.setOnClickListener(this);
        savePrevButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        dbnameView.setOnClickListener(this);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_SEND)) {
            Bundle extras = intent.getExtras();
            String subject = extras.getString(Intent.EXTRA_SUBJECT);
            String text = extras.getString(Intent.EXTRA_TEXT);
            questionView.setText(subject);
            answerView.setText(text);
    		dbnameView.setText(settings.getString("recentdbpath0", "") + "/" + settings.getString("recentdbname0", ""));
        } else {
            finish();
        }
    }
    @Override
    public void onClick(View v){
        try{
            File dbFile = new File(dbnameView.getText().toString());
            String dbpath = dbFile.getParent();
            String dbname = dbFile.getName();
            Log.v(TAG, dbpath);
            Log.v(TAG, dbname);

            if(v == saveButton || v == savePrevButton){
                ItemManager im = new ItemManager.Builder(this, dbpath, dbname)
                    .build();
                Item currentItem = new Item.Builder()
                    .setQuestion(questionView.getText().toString())
                    .setAnswer(answerView.getText().toString())
                    .setCategory(categoryView.getText().toString())
                    .setNote(noteView.getText().toString())
                    .build();
                im.insertBack(currentItem);

                if(v == savePrevButton){
                    Intent myIntent = new Intent(this, EditScreen.class);
                    /* This should be the newly created id */
                    myIntent.putExtra("id", im.getStatInfo()[0]);
                    myIntent.putExtra("dbname", dbname);
                    myIntent.putExtra("dbpath", dbpath);
                    startActivity(myIntent);
                }
                im.close();
                finish();
            }
            else if(v == cancelButton){
                finish();
            }
            else if(v == dbnameView){
                Intent myIntent = new Intent(this, FileBrowser.class);
                myIntent.putExtra("file_extension", ".db");
                startActivityForResult(myIntent, ACTIVITY_FB);
            }
        }
        catch (Exception e){
            AMGUIUtility.displayError(this, getString(R.string.error_text), "", e);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode ==Activity.RESULT_CANCELED){
            return;
        }
        /* Refresh the activity according to activities */
        switch(requestCode){
            case ACTIVITY_FB:
            {
                String dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                String dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                if(dbName != null && dbPath != null){
                    String fullname = dbPath + "/" + dbName;
                    RecentListUtil.addToRecentList(this, dbPath, dbName);
                    dbnameView.setText(fullname);
                }
                break;
            }

        }
    }
}
