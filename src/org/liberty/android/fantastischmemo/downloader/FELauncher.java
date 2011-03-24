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
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.view.View.OnClickListener;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.util.Log;
import oauth.signpost.*;

public class FELauncher extends AMActivity implements OnClickListener{
    private Button directoryButton;
    private Button searchTagButton;
    private Button searchUserButton;
    private Button privateButton;
    private Button uploadButton;
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.FELauncher";
    private static final int OAUTH_PRIVATE_ACTIVITY = 1;
    private static final int OAUTH_UPLOAD_ACTIVITY = 2;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fe_launcher);
        directoryButton = (Button)findViewById(R.id.fe_directory);
        searchTagButton = (Button)findViewById(R.id.fe_search_tag);
        searchUserButton = (Button)findViewById(R.id.fe_search_user);
        privateButton = (Button)findViewById(R.id.fe_private_login);
        uploadButton = (Button)findViewById(R.id.fe_upload);
        directoryButton.setOnClickListener(this);
        searchTagButton.setOnClickListener(this);
        searchUserButton.setOnClickListener(this);
        privateButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
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
        if(v == privateButton){
            Intent myIntent = new Intent(this, FEOauth.class);
            startActivityForResult(myIntent, OAUTH_PRIVATE_ACTIVITY);
        }
        if(v == uploadButton){
            Intent myIntent = new Intent(this, FEUpload.class);
            startActivity(myIntent);
        }

    }

    /* Handle the return of Oauth to access private cards */
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Result: " + requestCode + " " + resultCode + " " + data);
        if(resultCode == Activity.RESULT_OK){
            switch(requestCode){
                case OAUTH_PRIVATE_ACTIVITY:
                {
                    Bundle resultExtras = data.getExtras();
                    if(resultExtras != null){
                        String key = resultExtras.getString("oauth_token");
                        String secret = resultExtras.getString("oauth_token_secret");
                        OAuthConsumer consumer = (OAuthConsumer)resultExtras.getSerializable("consumer");
                        showPrivateSearchUserDialog(key, secret, consumer);
                        break;
                    }
                }
            }
        }
    }

    private void showSearchTagDialog(){
        final EditText et = new EditText(this);
        et.setText(settings.getString("fe_saved_search", ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_tag)
            .setMessage(R.string.fe_search_tag_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    String searchText = et.getText().toString();
                    editor.putString("fe_saved_search", searchText);
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
        et.setText(settings.getString("fe_saved_user", ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_tag)
            .setMessage(R.string.fe_search_user_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    String searchText = et.getText().toString();
                    editor.putString("fe_saved_user", searchText);
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

    /* Oauth information should be passed to this method */
    private void showPrivateSearchUserDialog(final String key, final String secret, final OAuthConsumer consumer){
        final EditText et = new EditText(this);
        et.setText(settings.getString("fe_saved_user", ""));
        new AlertDialog.Builder(this)
            .setTitle(R.string.search_tag)
            .setMessage(R.string.fe_private_login_message)
            .setView(et)
            .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    String searchText = et.getText().toString();
                    editor.putString("fe_saved_user", searchText);
                    editor.commit();
                    Intent myIntent = new Intent(FELauncher.this, DownloaderFE.class);
                    myIntent.setAction(DownloaderFE.INTENT_ACTION_SEARCH_PRIVATE);
                    myIntent.putExtra("search_criterion", "liberty@live.com");
                    myIntent.putExtra("oauth_token", key);
                    myIntent.putExtra("oauth_token_secret", secret);
                    myIntent.putExtra("oauth_consumer", consumer);
                    startActivity(myIntent);
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }



}

