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

import java.io.File;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.FileBrowserActivity;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;

import android.app.AlertDialog;

import android.os.Bundle;

public class DropboxUploader extends FileBrowserActivity {
    private String oauthToken;
    private String oauthSecret;
    private String remotePath = "/";


    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
        if(extras != null){
            oauthToken = extras.getString("dropbox_token");
            oauthSecret = extras.getString("dropbox_secret");
            remotePath = extras.getString("remote_path");
        }
    }

    @Override
    public void fileClickAction(File file){
        final String uploadFileName = file.getAbsolutePath();
        AMGUIUtility.doProgressTask(this, R.string.loading_please_wait, R.string.upload_wait, new AMGUIUtility.ProgressTask(){
            public void doHeavyTask() throws Exception{
                DropboxUtils.uploadFile(oauthToken, oauthSecret, uploadFileName, remotePath);
            }
            public void doUITask(){
                new AlertDialog.Builder(DropboxUploader.this)
                    .setTitle(R.string.upload_finish)
                    .setMessage(uploadFileName + " " + getString(R.string.upload_finish_message))
                    .setPositiveButton(R.string.ok_text, null)
                    .create()
                    .show();
            }
        });
    }
}

