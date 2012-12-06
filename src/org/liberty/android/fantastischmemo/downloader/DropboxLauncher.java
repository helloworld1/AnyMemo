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
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
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
        /* Retrieve saved login information */
        String dropboxUsername = settings.getString(AMPrefKeys.DROPBOX_USERNAME_KEY, null);
        if (dropboxUsername != null && !dropboxUsername.equals("")) {
            loginButton.setText(getString(R.string.fe_logged_in_text) + ": " + dropboxUsername);
        }
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
            String token = settings.getString(AMPrefKeys.DROPBOX_TOKEN_KEY, "");
            String secret = settings.getString(AMPrefKeys.DROPBOX_SECRET_KEY, "");
            if (token.equals("") || secret.equals("")){
                showNotAuthDialog();
                return;
            }
            Intent myIntent = new Intent(this, DownloaderDropbox.class);
            myIntent.putExtra("dropbox_token", token);
            myIntent.putExtra("dropbox_secret", secret);
            myIntent.putExtra("initial_path", "/");
            startActivity(myIntent);
        }
        if(v == uploadButton){
            String token = settings.getString(AMPrefKeys.DROPBOX_TOKEN_KEY, "");
            String secret = settings.getString(AMPrefKeys.DROPBOX_SECRET_KEY, "");
            if (token.equals("") || secret.equals("")){
                showNotAuthDialog();
                return;
            }
            Intent myIntent = new Intent(this, DropboxUploader.class);
            myIntent.putExtra("dropbox_token", token);
            myIntent.putExtra("dropbox_secret", secret);
            myIntent.putExtra("remote_path", "/AnyMemo");
            myIntent.putExtra("default_root", AMEnv.DEFAULT_ROOT_PATH);

            startActivity(myIntent);

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
                    try {
                        String[] r = DropboxUtils.retrieveToken(username.getText().toString(), password.getText().toString());
                        editor.putString(AMPrefKeys.DROPBOX_USERNAME_KEY, username.getText().toString());
                        editor.putString(AMPrefKeys.DROPBOX_TOKEN_KEY, r[0]);
                        editor.putString(AMPrefKeys.DROPBOX_SECRET_KEY, r[1]);
                        editor.commit();
                        /* Properly display the login status in the button. */
                        restartActivity();
                    }
                    catch(Exception e) {
                        AMGUIUtility.displayException(DropboxLauncher.this, getString(R.string.error_text), "", e);
                    }

                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
    }
    @Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dropbox_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.logout:
            editor.putString(AMPrefKeys.DROPBOX_USERNAME_KEY, "");
            editor.putString(AMPrefKeys.DROPBOX_TOKEN_KEY, "");
            editor.putString(AMPrefKeys.DROPBOX_SECRET_KEY, "");
            editor.commit();
            restartActivity();
			return true;
	    }
	    return false;
	}

    private void showNotAuthDialog(){
        new AlertDialog.Builder(this)
            .setTitle(R.string.fe_not_login)
            .setMessage(R.string.fe_not_login_message)
            .setPositiveButton(R.string.ok_text, null)
            .show();
    }
}

