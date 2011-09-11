/*
Copyright (C) 2011 Haowen Ning

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

import org.liberty.android.fantastischmemo.downloader.QuizletLauncher;
import org.liberty.android.fantastischmemo.downloader.QuizletLauncher;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.app.AlertDialog;

import android.widget.TextView;

public class DropboxLauncher extends AMActivity implements OnClickListener{
    private Button loginButton;
    private Button downloadButton;
    private Button uploadButton;
    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.DropboxLauncher";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropbox_launcher);
        loginButton = (Button)findViewById(R.id.dropbox_login_button);
        downloadButton = (Button)findViewById(R.id.dropbox_download_button);
        uploadButton = (Button)findViewById(R.id.dropbox_upload_button);
        loginButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onClick(View v){
        if(v == loginButton){
            showLoginDialog();
        }
        if(v == downloadButton){
        }
        if(v == uploadButton){
        }
    }

    private void showLoginDialog(){
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View loginDialog = inflater.inflate(R.layout.login_dialog, null);
        ((TextView)loginDialog.findViewById(R.id.user_name_title)).setText(R.string.user_name_text);
        ((TextView)loginDialog.findViewById(R.id.password_title)).setText(R.string.password_text);
        final EditText username = (EditText)loginDialog.findViewById(R.id.user_name_entry);
        final EditText password = (EditText)loginDialog.findViewById(R.id.password_entry);

        new AlertDialog.Builder(this)
            .setTitle(R.string.dropbox_login_text)
            .setMessage(R.string.dropbox_login_message)
            .setView(loginDialog)
            .setPositiveButton(R.string.fe_login_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }
}

