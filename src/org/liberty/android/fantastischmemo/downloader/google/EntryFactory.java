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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.Uri;

public class EntryFactory {
    private static SimpleDateFormat ISO8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private EntryFactory() {
        throw new AssertionError("Don't call constructor");
    }

    /*
     * Get a list of entries of the correct type.
     * clazz: the class type to return
     * inputStream, input stream from the URLConnection
     */
    public static <T extends Entry> List<T> getEntries(Class<T> clazz, InputStream inputStream) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new BufferedReader(new InputStreamReader(inputStream)));

        int eventType = xpp.getEventType();

        List<T> entryList = new ArrayList<T>(50);
        T entry = null;
        String lastTag = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {

            if(eventType == XmlPullParser.START_DOCUMENT) {
            } else if(eventType == XmlPullParser.START_TAG) {
                lastTag = xpp.getName();
                if(xpp.getName().equals("entry")) {
                    try {
                        entry = clazz.newInstance();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
					}
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("entry")) {
                    entryList.add(entry);
                    entry = null;
                }
            } else if(eventType == XmlPullParser.TEXT) {
                if(entry != null && lastTag.equals("id")) {
                    // Get the last part of the uri, as it is the id.
                    entry.setId(Uri.parse(xpp.getText()).getLastPathSegment());
                }
                if(entry != null && lastTag.equals("title")) {
                    entry.setTitle(xpp.getText());
                }
                if(entry != null && lastTag.equals("updated")) {
                    try {
                        entry.setUpdateDate(ISO8601_FORMATTER.parse(xpp.getText()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            eventType = xpp.next();
        }
        return entryList;
    }

}
