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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.liberty.android.fantastischmemo.downloader.DownloaderUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class SpreadsheetFactory {
    private static SimpleDateFormat ISO8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private SpreadsheetFactory() {
        throw new AssertionError("Don't call constructor");
    }

    public static List<Spreadsheet> getSpreadsheetsFromRequest(String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new BufferedReader(new InputStreamReader(conn.getInputStream())));

        int eventType = xpp.getEventType();

        List<Spreadsheet> spreadsheetList = new ArrayList<Spreadsheet>(50);
        Spreadsheet spreadsheet = null;
        String lastTag = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
                
            if(eventType == XmlPullParser.START_DOCUMENT) {
                System.out.println("Start document");
            } else if(eventType == XmlPullParser.START_TAG) {
                System.out.println("Start tag "+xpp.getName());
                lastTag = xpp.getName();
                if(xpp.getName().equals("entry")) {
                    spreadsheet = new Spreadsheet();
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                System.out.println("End tag "+xpp.getName());
                if(xpp.getName().equals("entry")) {
                    spreadsheetList.add(spreadsheet);
                    spreadsheet = null;
                }
            } else if(eventType == XmlPullParser.TEXT) {
                System.out.println("Text "+xpp.getText());
                if(spreadsheet != null && lastTag.equals("id")) {
                    spreadsheet.setId(DownloaderUtils.getLastPartFromUrl(xpp.getText()));
                }
                if(spreadsheet != null && lastTag.equals("title")) {
                    spreadsheet.setTitle(xpp.getText());
                }
                if(spreadsheet != null && lastTag.equals("updated")) {
                    try {
                        spreadsheet.setUpdateDate(ISO8601_FORMATTER.parse(xpp.getText()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            eventType = xpp.next();
        }
        System.out.println("End document");
        return spreadsheetList;
    }

    public static Spreadsheet createSpreadsheet(String title, String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://docs.google.com/feeds/default/private/full");

        String payload = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns='http://www.w3.org/2005/Atom'>"+
            "<category scheme='http://schemas.google.com/g/2005#kind'"+
            " term='http://schemas.google.com/docs/2007#spreadsheet'/>"+
            "<title>"+ title +"</title>"+
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

        // Now populate the response
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new BufferedReader(new InputStreamReader(conn.getInputStream())));

        int eventType = xpp.getEventType();

        Spreadsheet spreadsheet = null;
        String lastTag = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
                
            if(eventType == XmlPullParser.START_DOCUMENT) {
                System.out.println("Start document");
            } else if(eventType == XmlPullParser.START_TAG) {
                System.out.println("Start tag "+xpp.getName());
                lastTag = xpp.getName();
                if(xpp.getName().equals("entry")) {
                    spreadsheet = new Spreadsheet();
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                System.out.println("End tag "+xpp.getName());
            } else if(eventType == XmlPullParser.TEXT) {
                System.out.println("Text "+xpp.getText());
                if(spreadsheet != null && lastTag.equals("id")) {
                    spreadsheet.setId(DownloaderUtils.getLastPartFromUrl(xpp.getText()));
                }
                if(spreadsheet != null && lastTag.equals("title")) {
                    spreadsheet.setTitle(xpp.getText());
                }
                if(spreadsheet != null && lastTag.equals("updated")) {
                    try {
                        spreadsheet.setUpdateDate(ISO8601_FORMATTER.parse(xpp.getText()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            eventType = xpp.next();
        }
        return spreadsheet;
    }

    public static List<Spreadsheet> findSpreadsheets(String title, String authToken) throws Exception {
        URL url = new URL("https://docs.google.com/feeds/default/private/full?title=" + title + "&title-exact=true");

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);
        conn.addRequestProperty("GData-Version", "3.0");

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new BufferedReader(new InputStreamReader(conn.getInputStream())));

        int eventType = xpp.getEventType();

        List<Spreadsheet> spreadsheetList = new ArrayList<Spreadsheet>(5);
        Spreadsheet spreadsheet = null;
        String lastTag = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
                
            if(eventType == XmlPullParser.START_DOCUMENT) {
                System.out.println("Start document");
            } else if(eventType == XmlPullParser.START_TAG) {
                System.out.println("Start tag "+xpp.getName());
                lastTag = xpp.getName();
                if(xpp.getName().equals("entry")) {
                    spreadsheet = new Spreadsheet();
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                System.out.println("End tag "+xpp.getName());
                if(xpp.getName().equals("entry")) {
                    spreadsheetList.add(spreadsheet);
                    spreadsheet = null;
                }
            } else if(eventType == XmlPullParser.TEXT) {
                System.out.println("Text "+xpp.getText());
                if(spreadsheet != null && lastTag.equals("id")) {
                    spreadsheet.setId(DownloaderUtils.getLastPartFromUrl(xpp.getText()));
                }
                if(spreadsheet != null && lastTag.equals("title")) {
                    spreadsheet.setTitle(xpp.getText());
                }
                if(spreadsheet != null && lastTag.equals("updated")) {
                    try {
                        spreadsheet.setUpdateDate(ISO8601_FORMATTER.parse(xpp.getText()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            eventType = xpp.next();
        }
        return spreadsheetList;
    }
}
