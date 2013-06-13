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
package org.liberty.android.fantastischmemo.downloader;

import org.liberty.android.fantastischmemo.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.app.AlertDialog;

public class QuizletLauncher extends AMActivity implements OnClickListener{
    private Button searchTagButton;
    private Button searchUserButton;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quizlet_launcher);
        searchTagButton = (Button)findViewById(R.id.quizlet_search_tag);
        searchUserButton = (Button)findViewById(R.id.quizlet_search_user);
        searchTagButton.setOnClickListener(this);
        searchUserButton.setOnClickListener(this);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onClick(View v){
        if(v == searchTagButton){
            showSearchTagDialog();
        }
        if(v == searchUserButton){
            showSearchUserDialog();
        }

    }

    private void showSearchTagDialog(){
        final EditText et = new EditText(this);
        et.setText(settings.getString(AMPrefKeys.QUIZLET_SAVED_SEARCH, ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_tag)
            .setMessage(R.string.quizlet_search_tag_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    String searchText = et.getText().toString();
                    editor.putString(AMPrefKeys.QUIZLET_SAVED_SEARCH, searchText);
                    editor.commit();
                    Intent myIntent = new Intent(QuizletLauncher.this, DownloaderQuizlet.class);
                    myIntent.setAction(DownloaderQuizlet.INTENT_ACTION_SEARCH_TAG);
                    myIntent.putExtra("search_criterion", searchText);
                    startActivity(myIntent);
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }

    private void showSearchUserDialog(){
        final EditText et = new EditText(this);
        et.setText(settings.getString(AMPrefKeys.QUIZLET_SAVED_USER, ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_user)
            .setMessage(R.string.quizlet_search_user_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    String searchText = et.getText().toString();
                    editor.putString(AMPrefKeys.QUIZLET_SAVED_USER, searchText);
                    editor.commit();
                    Intent myIntent = new Intent(QuizletLauncher.this, DownloaderQuizlet.class);
                    myIntent.setAction(DownloaderQuizlet.INTENT_ACTION_SEARCH_USER);
                    myIntent.putExtra("search_criterion", searchText);
                    startActivity(myIntent);
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }
}

