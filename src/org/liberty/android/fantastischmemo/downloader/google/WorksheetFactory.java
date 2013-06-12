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
import java.io.OutputStreamWriter;

import java.net.URL;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import org.liberty.android.fantastischmemo.downloader.google.Spreadsheet;

import org.xmlpull.v1.XmlPullParserException;

public class WorksheetFactory {
    private WorksheetFactory() {
        throw new AssertionError("Don't call constructor");
    }

    public static List<Worksheet> getWorksheets(Spreadsheet spreadsheet, String authToken) throws XmlPullParserException, IOException {
        String worksheetAddress = "https://spreadsheets.google.com/feeds/worksheets/" + spreadsheet.getId() + "/private/full?access_token=" + authToken;
        URL url = new URL(worksheetAddress);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }
        List<Worksheet> worksheetList = EntryFactory.getEntries(Worksheet.class, conn.getInputStream());
        return worksheetList;
    }

    public static void deleteWorksheet(Spreadsheet spreadsheet, Worksheet worksheet, String authToken) throws Exception {
        String requestUrl= "https://spreadsheets.google.com/feeds/worksheets/" + spreadsheet.getId() + "/private/full/" + worksheet.getId() + "/0" + "?access_token=" + authToken;
        URL url = new URL(requestUrl);
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.addRequestProperty("If-Match", "*");
        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new Exception(s);
        }
    }

    public static Worksheet createWorksheet(Spreadsheet spreadsheet, String title, int row, int col, String authToken) throws Exception {
        URL url = new URL("https://spreadsheets.google.com/feeds/worksheets/" + spreadsheet.getId() + "/private/full?access_token=" + authToken);

        String payload = "<entry xmlns=\"http://www.w3.org/2005/Atom\""
            + " xmlns:gs=\"http://schemas.google.com/spreadsheets/2006\">"
            + "<title>" + URLEncoder.encode(title, "UTF-8") + "</title>"
            + "<gs:rowCount>" + row + "</gs:rowCount>"
            + "<gs:colCount>" + col + "</gs:colCount>"
            + "</entry>";


        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.addRequestProperty("Content-Type", "application/atom+xml");
        conn.addRequestProperty("Content-Length", "" + payload.getBytes("UTF-8").length);
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(payload);
        out.close();

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new Exception(s);
        }

        List<Worksheet> worksheets = getWorksheets(spreadsheet, authToken);
        for (Worksheet worksheet : worksheets) {
            if (title.equals(worksheet.getTitle())) {
                return worksheet;
            }
        }
        throw new IllegalStateException("Worksheet lookup failed. Worksheet is not created properly.");
    }

    public static List<Worksheet> findWorksheetByTitle(Spreadsheet spreadsheet, String title, String authToken) throws XmlPullParserException, IOException {
        List<Worksheet> allWorksheets = getWorksheets(spreadsheet, authToken);
        List<Worksheet> resWorksheets = new ArrayList<Worksheet>();
        for (Worksheet w : allWorksheets) {
            if (w.getTitle().equals(title)) {
                resWorksheets.add(w);
            }
        }
        return resWorksheets;
    }

}
