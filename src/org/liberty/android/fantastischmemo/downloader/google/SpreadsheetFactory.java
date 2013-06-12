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

import java.io.IOException;

import java.net.URL;

import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import org.xmlpull.v1.XmlPullParserException;

public class SpreadsheetFactory {
    private SpreadsheetFactory() {
        throw new AssertionError("Don't call constructor");
    }

    public static List<Spreadsheet> getSpreadsheets(String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full?access_token="+authToken);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        //conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new IOException(s);
        }

        List<Spreadsheet> spreadsheetList = EntryFactory.getEntries(Spreadsheet.class, conn.getInputStream());
        return spreadsheetList;
    }

}
