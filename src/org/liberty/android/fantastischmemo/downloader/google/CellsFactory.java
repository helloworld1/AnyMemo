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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.mycommons.io.IOUtils;

import org.apache.mycommons.lang3.StringEscapeUtils;
import org.apache.mycommons.lang3.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class CellsFactory {
    private CellsFactory() {
        throw new AssertionError("Don't call constructor");
    }

    public static Cells getCells(Spreadsheet spreadsheet, Worksheet worksheet, String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://spreadsheets.google.com/feeds/cells/" + spreadsheet.getId() + "/" + worksheet.getId() + "/private/full?access_token=" + authToken);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new BufferedReader(new InputStreamReader(conn.getInputStream())));

        int eventType = xpp.getEventType();

        List<Row> rowList = new ArrayList<Row>(100);
        Row row = null;
        String lastTag = "";
        int currentRow = 1;
        int currentCol = 1;
        Cells cells = new Cells();
        while (eventType != XmlPullParser.END_DOCUMENT) {
                
            if(eventType == XmlPullParser.START_DOCUMENT) {
            } else if(eventType == XmlPullParser.START_TAG) {
                lastTag = xpp.getName();
                if(xpp.getName().equals("cell")) {
                    currentRow = Integer.valueOf(xpp.getAttributeValue(null, "row"));
                    currentCol = Integer.valueOf(xpp.getAttributeValue(null, "col"));
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("entry")) {
                    rowList.add(row);
                    row = null;
                }
            } else if(eventType == XmlPullParser.TEXT) {
                if(lastTag.equals("cell")) {
                    cells.addCell(currentRow, currentCol, xpp.getText());
                }
                if(lastTag.equals("title") && StringUtils.isEmpty(cells.getWorksheetName())) {
                    cells.setWorksheetName(xpp.getText());
                }
            }
            eventType = xpp.next();
        }
        return cells;
    }

    public static void uploadCells(Spreadsheet spreadsheet, Worksheet worksheet, Cells cells, String authToken) throws Exception {
        URL url = new URL("https://spreadsheets.google.com/feeds/cells/" + spreadsheet.getId() + "/" + worksheet.getId() + "/private/full/batch?access_token=" + authToken);
        String urlPrefix = "https://spreadsheets.google.com/feeds/cells/" + spreadsheet.getId() + "/" + worksheet.getId() + "/private/full";
        StringBuilder payloadBuilder = new StringBuilder();
        payloadBuilder.append("<?xml version='1.0' encoding='UTF-8'?>");
        payloadBuilder.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:batch=\"http://schemas.google.com/gdata/batch\" xmlns:gs=\"http://schemas.google.com/spreadsheets/2006\">");
        payloadBuilder.append("<id>" + urlPrefix + "</id>");
        for (int i = 0; i < cells.getRowCounts(); i++) {
            List<String> row = cells.getRow(i);
            for (int j = 0; j < row.size(); j++) {
                addEntryToRequesetString(payloadBuilder, urlPrefix, i + 1, j + 1, row.get(j));
            }
        }
        payloadBuilder.append("</feed>");

        String payload = payloadBuilder.toString();

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.addRequestProperty("Content-Type", "application/atom+xml");
        //conn.addRequestProperty("Content-Length", "" + payload.length());
        conn.addRequestProperty("If-Match", "*");
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(payload);
        out.close();
        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new Exception(s);
        }
    }

    private static void addEntryToRequesetString(StringBuilder payloadBuilder, String urlPrefix, int row, int col, String value) {
        payloadBuilder.append("<entry>");
        payloadBuilder.append("<batch:id>BR" + row + "C"+ col + "</batch:id>");
        payloadBuilder.append("<batch:operation type=\"update\"/>");
        payloadBuilder.append("<id>"+ urlPrefix + "/R" + row + "C" + col + "</id>");
        payloadBuilder.append("<link rel=\"edit\" type=\"application/atom+xml\" href=\"" + urlPrefix + "/R" + row + "C" + col + "\"/>");
        payloadBuilder.append("<gs:cell row=\"" + row + "\" col=\"" + col + "\" inputValue=\"" + StringEscapeUtils.escapeXml(value) + "\"/>");
        payloadBuilder.append("</entry>");
    }
}
