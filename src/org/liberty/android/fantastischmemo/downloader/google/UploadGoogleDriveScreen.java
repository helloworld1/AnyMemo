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
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.mycommons.io.IOUtils;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.FileBrowserFragment;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class UploadGoogleDriveScreen extends GoogleAccountActivity {
    /** Called when the activity is first created. */
    private static final String AUTH_TOKEN_TYPE = "writely";

    private String authToken = null;

    @Override
    protected String getAuthTokenType() {
        return AUTH_TOKEN_TYPE;
    }

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
            System.out.println("Listing.....................................>");
            URL url1 = new URL("https://docs.google.com/feeds/default/private/full?title=aaaaa.db.db&title-exact=true");
            HttpsURLConnection conn1 = (HttpsURLConnection) url1.openConnection();
            conn1.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);
            conn1.addRequestProperty("GData-Version", "3.0");
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
            String inputLine1;
            while ((inputLine1 = reader1.readLine()) != null) {
                System.out.println(inputLine1);
            }
            reader1.close();


            System.out.println("Uplading.....................................>");
            //URL url = new URL("https://docs.google.com/feeds/upload/create-session/default/private/full");
            URL url = new URL("https://docs.google.com/feeds/default/private/full");
        // Create a SpreadSheet
        String payload = "<?xml version='1.0' encoding='UTF-8'?>" +
                          "<entry xmlns='http://www.w3.org/2005/Atom'>"+
                          "<category scheme='http://schemas.google.com/g/2005#kind'"+
                          " term='http://schemas.google.com/docs/2007#spreadsheet'/>"+
                          "<title>"+ file.getName() +"</title>"+
                          "</entry>";

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);
            conn.addRequestProperty("GData-Version", "3.0");
            conn.addRequestProperty("Content-Type", "application/atom+xml");
            conn.addRequestProperty("Content-Length", "" + payload.length());
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(payload);
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println(inputLine);
            }
            reader.close();
            System.out.println("Done.....................................>");


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
