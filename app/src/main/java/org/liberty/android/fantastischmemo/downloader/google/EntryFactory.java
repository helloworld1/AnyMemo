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

import android.net.Uri;
import android.util.JsonReader;
import android.util.JsonToken;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Parse the JSON format like this:
     * "kind": "drive#fileList",
     * "etag": "\"dM4Z0GasI3ekQlrgb3F8B4ytx24/bhMYFq5sxQUJKZ2H3LNeBmBcr2E\"",
     * "selfLink": "https://www.googleapis.com/drive/v2/files?q=title+%3D+'IMG_5652.jpg'",
     * "items": [..ITEM TYPE HERE.]
     */
    public static <T extends Entry> List<T> getEntriesFromDriveApi(Class<T> clazz, InputStream inputStream) throws IOException {
        List<T> entryList = new ArrayList<T>(50);
        JsonReader jsonReader = null;
        String name = "";

        try {
            jsonReader  = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() != JsonToken.NAME) {
                    jsonReader.skipValue();
                    continue;
                }
                name = jsonReader.nextName();
                if (name.equals("items") && jsonReader.peek() != JsonToken.NULL) {
                    jsonReader.beginArray();

                    while (jsonReader.hasNext()) {
                        entryList.add(getEntryFromJsonReader(clazz, jsonReader));
                    }

                    jsonReader.endArray();
                    

                }

            }
            jsonReader.endObject();
            

        } finally {
            if (jsonReader != null) {
                jsonReader.close();
            }
        }
        return entryList;
    }

    public static <T extends Entry> T getEntryFromDriveApi(Class<T> clazz, InputStream inputStream) throws IOException {
        JsonReader jsonReader = null;

        try {
            jsonReader  = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            return getEntryFromJsonReader(clazz, jsonReader);
        } finally {
            if (jsonReader != null) {
                jsonReader.close();
            }
        }
    }

    /**
     * Parse single entry from json reader
     *
     * The entry is like below:
     * {
     *   "kind": "drive#file",
     *   "id": "0B2Ipwyo9TOxYVmEwZWlLdlFCN1E",
     *   "etag": "\"dM4Z0GasI3ekQlrgb3F8B4ytx24/MTQyNDg0NTE5OTM0MA\"",
     *   "selfLink": "https://www.googleapis.com/drive/v2/files/0B2Ipwyo9TOxYVmEwZWlLdlFCN1E",
     *   "webContentLink": "https://docs.google.com/uc?id=0B2Ipwyo9TOxYVmEwZWlLdlFCN1E&export=download",
     *   "alternateLink": "https://docs.google.com/file/d/0B2Ipwyo9TOxYVmEwZWlLdlFCN1E/edit?usp=drivesdk",
     *   "iconLink": "https://ssl.gstatic.com/docs/doclist/images/icon_11_image_list.png",
     *   "thumbnailLink": "https://lh4.googleusercontent.com/XpCSGp9RcABx5ST3bKKb1HWcugDA-jnJiy8vIe04WF_sD$ 5z0sLX11PRHkydxdHoI5ylgQ=s220",
     *   "title": "IMG_5652.jpg",
     *   "mimeType": "image/jpeg",
     *   "labels": {
     *    "starred": false,
     *    "hidden": false,
     *    "trashed": false,
     *    "restricted": false,
     *    "viewed": false
     *   },
     *   "createdDate": "2015-02-25T06:19:59.340Z",
     *   "modifiedDate": "2015-02-25T06:19:59.340Z",
     *   "modifiedByMeDate": "2015-02-25T06:19:59.340Z",
     *   "markedViewedByMeDate": "1970-01-01T00:00:00.000Z",
     *   "version": "133505",
     *   ,....
     * }
     */

    private static <T extends Entry> T getEntryFromJsonReader(Class<T> clazz, JsonReader jsonReader) throws IOException {
        String name = "";
        T entry;
        try {
            entry = clazz.newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }

        jsonReader.beginObject();
        // item object
        while (jsonReader.hasNext()) {
            name = jsonReader.nextName();
            if (name.equals("id") && jsonReader.peek() != JsonToken.NULL) {
                entry.setId(jsonReader.nextString());
            } else if (name.equals("title") && jsonReader.peek() != JsonToken.NULL) {
                entry.setTitle(jsonReader.nextString());
            } else if (name.equals("modifiedDate") && jsonReader.peek() != JsonToken.NULL) {
                try {
                    entry.setUpdateDate(ISO8601_FORMATTER.parse(jsonReader.nextString()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                jsonReader.skipValue();
            }

        }
        jsonReader.endObject();
        return entry;
    }

}
