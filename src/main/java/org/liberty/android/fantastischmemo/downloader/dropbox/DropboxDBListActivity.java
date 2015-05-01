/*
Copyright (C) 2013 Haowen Ning, Xinxin Wang

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
package org.liberty.android.fantastischmemo.downloader.dropbox;

import org.liberty.android.fantastischmemo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

// The activity that display a list of db from Dropbox to download.
public class DropboxDBListActivity extends DropboxAccountActivity {

    private static final int UPLOAD_ACTIVITY = 1;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.spreadsheet_list_screen);
    }

    @Override
    protected void onAuthenticated(final String[] accessTokens) {
        String accessToken = accessTokens[0];
        String accessTokenSecret = accessTokens[1];

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = new DownloadDBFileListFragment();
        Bundle args = new Bundle();
        args.putString(DownloadDBFileListFragment.EXTRA_AUTH_TOKEN, accessToken);
        args.putString(DownloadDBFileListFragment.EXTRA_AUTH_TOKEN_SECRET, accessTokenSecret);
        newFragment.setArguments(args);
        ft.add(R.id.spreadsheet_list, newFragment);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:{
                startActivityForResult(new Intent(this, UploadDropboxScreen.class), UPLOAD_ACTIVITY);
                return true;
            }
            case R.id.logout:{
                invalidateSavedToken();
                finish();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            return;
        }

        switch(requestCode){
            case UPLOAD_ACTIVITY:
            {
                restartActivity();
                break;
            }
        }
    }
}
