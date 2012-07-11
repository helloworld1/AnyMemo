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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.mycommons.io.IOUtils;

import org.liberty.android.fantastischmemo.R;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class SpreadsheetListScreen extends GoogleAccountActivity {
    /** Called when the activity is first created. */
    private static final String AUTH_TOKEN_TYPE = "wise";

    @Override
    protected String getAuthTokenType() {
        return AUTH_TOKEN_TYPE;
    }

    @Override
    public void onCreate(Bundle bundle) {
        setContentView(R.layout.spreadsheet_list_screen);
        super.onCreate(bundle);
    }


    @Override
    protected void onAuthenticated(final String authToken) {
        //try {
        //    URL url = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
        //    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        //    conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);

        //    String s = new String(IOUtils.toByteArray(conn.getInputStream()));
        //    System.out.println(s);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = new SpreadsheetListFragment(authToken);
        ft.add(R.id.spreadsheet_list, newFragment);
        ft.commit();
    }
}
