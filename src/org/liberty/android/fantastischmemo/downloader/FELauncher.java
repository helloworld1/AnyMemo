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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.app.AlertDialog;

public class FELauncher extends AMActivity implements OnClickListener{
    private Button directoryButton;
    private Button searchTagButton;
    private Button searchUserButton;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fe_launcher);
        directoryButton = (Button)findViewById(R.id.fe_directory);
        searchTagButton = (Button)findViewById(R.id.fe_search_tag);
        searchUserButton = (Button)findViewById(R.id.fe_search_user);
        directoryButton.setOnClickListener(this);
        searchTagButton.setOnClickListener(this);
        searchUserButton.setOnClickListener(this);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
    }

    @Override
    public void onClick(View v){
        if(v == directoryButton){
            Intent myIntent = new Intent(this, FEDirectory.class);
            startActivity(myIntent);
        }
        if(v == searchTagButton){
            showSearchTagDialog();
        }
        if(v == searchUserButton){
            showSearchUserDialog();
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.fe_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.fe_logout:
            editor.putString(AMPrefKeys.FE_SAVED_USERNAME_KEY, "");
            editor.putString(AMPrefKeys.FE_SAVED_OAUTH_TOKEN_KEY, "");
            editor.putString(AMPrefKeys.FE_SAVED_OAUTH_TOKEN_SECRET_KEY, "");
            editor.commit();
            restartActivity();
			return true;

	    }

	    return false;
	}

    private void showSearchTagDialog(){
        final EditText et = new EditText(this);
        et.setText(settings.getString(AMPrefKeys.FE_SAVED_SEARCH_KEY, ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_tag)
            .setMessage(R.string.fe_search_tag_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    String searchText = et.getText().toString();
                    editor.putString(AMPrefKeys.FE_SAVED_SEARCH_KEY, searchText);
                    editor.commit();
                    Intent myIntent = new Intent(FELauncher.this, DownloaderFE.class);
                    myIntent.setAction(DownloaderFE.INTENT_ACTION_SEARCH_TAG);
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
        et.setText(settings.getString(AMPrefKeys.FE_SAVED_USER_KEY, ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_tag)
            .setMessage(R.string.fe_search_user_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    String searchText = et.getText().toString();
                    editor.putString(AMPrefKeys.FE_SAVED_USER_KEY, searchText);
                    editor.commit();
                    Intent myIntent = new Intent(FELauncher.this, DownloaderFE.class);
                    myIntent.setAction(DownloaderFE.INTENT_ACTION_SEARCH_USER);
                    myIntent.putExtra("search_criterion", searchText);
                    startActivity(myIntent);
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }

}

