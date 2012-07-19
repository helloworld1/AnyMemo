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

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.mycommons.lang3.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class CellsFactory {
    private CellsFactory() {
        throw new AssertionError("Don't call constructor");
    }

    public static Cells getCellsFromRequest(String spreadsheetId, String worksheetId, String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://spreadsheets.google.com/feeds/cells/" + spreadsheetId + "/" + worksheetId + "/private/full?access_token=" + authToken);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

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
                System.out.println("Start document");
            } else if(eventType == XmlPullParser.START_TAG) {
                System.out.println("Start tag "+xpp.getName());
                lastTag = xpp.getName();
                if(xpp.getName().equals("cell")) {
                    currentRow = Integer.valueOf(xpp.getAttributeValue(null, "row"));
                    currentCol = Integer.valueOf(xpp.getAttributeValue(null, "col"));
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                System.out.println("End tag "+xpp.getName());
                if(xpp.getName().equals("entry")) {
                    rowList.add(row);
                    row = null;
                }
            } else if(eventType == XmlPullParser.TEXT) {
                System.out.println("Text "+xpp.getText());
                if(lastTag.equals("cell")) {
                    cells.addCell(currentRow, currentCol, xpp.getText());
                }
                if(lastTag.equals("title") && StringUtils.isEmpty(cells.getWorksheetName())) {
                    cells.setWorksheetName(xpp.getText());
                }
            }
            eventType = xpp.next();
        }
        System.out.println("End document");
        return cells;
    }
}
