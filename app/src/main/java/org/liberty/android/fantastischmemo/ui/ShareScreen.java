/*
Copyright (C) 2012 Haowen Ning

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
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
 * This class is invoked when the user share the card from other
 * apps like ColorDict
 */
public class ShareScreen extends AMActivity implements View.OnClickListener{
    private TextView dbnameView;
    private TextView questionView;
    private TextView answerView;
    private TextView noteView;
    private Button saveButton;
    private Button savePrevButton;
    private Button cancelButton;
    private SharedPreferences settings;
    private final int ACTIVITY_FB = 1;

    private RecentListUtil recentListUtil;

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_screen);
        dbnameView = (TextView)findViewById(R.id.share_db_name_entry);
        questionView = (TextView)findViewById(R.id.share_question_entry);
        answerView = (TextView)findViewById(R.id.share_answer_entry);
        noteView = (TextView)findViewById(R.id.share_note_entry);
        noteView.setText("");
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
            String dbPath = settings.getString(AMPrefKeys.getRecentPathKey(0), "");
            dbnameView.setText(dbPath);
        } else {
            finish();
        }
    }
    @Override
    public void onClick(View v){
        try{
            String dbpath = dbnameView.getText().toString();
            Log.v(TAG, dbpath);

            if(v == saveButton || v == savePrevButton){
                AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(this, dbpath);
                CardDao cardDao = helper.getCardDao();
                try {
                    Card card = new Card();
                    card.setQuestion(questionView.getText().toString());
                    card.setAnswer(answerView.getText().toString());
                    card.setNote(noteView.getText().toString());
                    card.setCategory(new Category());
                    card.setLearningData(new LearningData());
                    cardDao.createCard(card);

                    if(v == savePrevButton){
                        Intent myIntent = new Intent(this, PreviewEditActivity.class);
                        /* This should be the newly created id */
                        myIntent.putExtra("id", card.getId());
                        myIntent.putExtra(PreviewEditActivity.EXTRA_DBPATH, dbpath);
                        startActivity(myIntent);
                    }
                    finish();
                } finally {
                    AnyMemoDBOpenHelperManager.releaseHelper(helper);
                }
            }
            else if(v == cancelButton){
                finish();
            }
            else if(v == dbnameView){
                Intent myIntent = new Intent(this, FileBrowserActivity.class);
                myIntent.putExtra(FileBrowserActivity.EXTRA_FILE_EXTENSIONS, ".db");
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
                String fullpath = data.getStringExtra(FileBrowserActivity.EXTRA_RESULT_PATH);
                recentListUtil.addToRecentList(fullpath);
                dbnameView.setText(fullpath);
            }
            break;
        }
    }

}
