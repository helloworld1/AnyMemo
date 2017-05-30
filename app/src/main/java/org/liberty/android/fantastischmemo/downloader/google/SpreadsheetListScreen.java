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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.downloader.oauth.Oauth2TokenUtil;

public class SpreadsheetListScreen extends BaseActivity {

    public static final String EXTRA_AUTH_TOKEN = "authToken";

    private final static int UPLOAD_ACTIVITY = 1;

    private String authToken;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.spreadsheet_list_screen);

        SpreadsheetListFragment fragment = new SpreadsheetListFragment();
        Bundle args = getIntent().getExtras();
        fragment.setArguments(args);

        authToken = args.getString(EXTRA_AUTH_TOKEN);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.spreadsheet_list, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.google_drive_spreadsheet_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:
            {
                Intent intent = new Intent(this, UploadGoogleDriveScreen.class);
                intent.putExtra(UploadGoogleDriveScreen.EXTRA_AUTH_TOKEN, authToken);
                startActivityForResult(intent, UPLOAD_ACTIVITY);
                return true;
            }
            case R.id.logout:
            {
                activityComponents().oauth2TokenUtil().invalidateSavedToken();
                // After mark saved token to null, we should exit.
                finish();
                return true;
            }

        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        switch (requestCode) {
            case UPLOAD_ACTIVITY:
            {
                restartActivity();
                break;
            }
        }
    }
}
