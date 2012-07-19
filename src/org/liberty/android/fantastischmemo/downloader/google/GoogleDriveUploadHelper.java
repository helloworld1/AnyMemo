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

import java.net.URL;

import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.liberty.android.fantastischmemo.AMEnv;

import org.liberty.android.fantastischmemo.downloader.google.Cells;

import org.liberty.android.fantastischmemo.utils.AMUtil;

import android.content.Context;

public class GoogleDriveUploadHelper {
    private Context mContext;

    private final String authToken;

    public GoogleDriveUploadHelper(Context context, String authToken) {
        this.authToken = authToken;
        mContext = context;
    }

    public Spreadsheet createSpreadsheet(String title) throws Exception {

        // Find the spreadsheet we want to work on

        // Delete the worksheets that has already exists
        SpreadsheetFactory.deleteSpreadsheet(title, authToken);
        

        // Create worksheets
        

        // Use batch API to update the rows


        //Spreadsheet s = SpreadsheetFactory.createSpreadsheet(title, authToken);

        //Worksheet w = WorksheetFactory.createWorksheet(s, "hahahahah", authToken);

        return null;
    }

    private Spreadsheet searchSpreadsheetTitle(String title) throws Exception {
        List<Spreadsheet> sl = SpreadsheetFactory.getSpreadsheets(authToken);
        for (Spreadsheet s : sl) {
            if (title.equals(s.getTitle())) {
                return s;
            }
        }
        return null;
    }

}
