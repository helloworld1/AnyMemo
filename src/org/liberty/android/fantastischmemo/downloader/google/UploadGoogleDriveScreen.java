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
package org.liberty.android.fantastischmemo.downloader.google;

import java.io.File;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.FileBrowserFragment;

import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;

public class UploadGoogleDriveScreen extends GoogleAccountActivity {
    /** Called when the activity is first created. */

    private String authToken = null;

    @Override
    public void onCreate(Bundle bundle) {
        setContentView(R.layout.upload_google_drive_screen);
        super.onCreate(bundle);
    }


    @Override
    protected void onAuthenticated(final String authToken) {
        this.authToken = authToken;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FileBrowserFragment fragment = new FileBrowserFragment();
        fragment.setOnFileClickListener(fileClickListener);
        ft.add(R.id.file_list, fragment);
        ft.commit();
    }

    private void uploadToGoogleDrive(File file) {
        try {
            GoogleDriveUploadHelper uploadHelper = new GoogleDriveUploadHelper(this, authToken);
            Spreadsheet s = uploadHelper.createSpreadsheet(file.getName());
            System.out.println(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FileBrowserFragment.OnFileClickListener fileClickListener =
        new FileBrowserFragment.OnFileClickListener() {

			@Override
			public void onClick(File file) {
                uploadToGoogleDrive(file);
				
			}
        };

}
