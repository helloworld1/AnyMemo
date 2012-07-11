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

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.mycommons.io.IOUtils;

import org.liberty.android.fantastischmemo.downloader.AbstractDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.DownloadItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

class SpreadsheetListFragment extends AbstractDownloaderFragment {
    private String authToken = null;

    public SpreadsheetListFragment(String authToken) {
        this.authToken = authToken;
    }

	@Override
	protected List<DownloadItem> initialRetrieve() throws Exception {
		// TODO Auto-generated method stub
        URL url = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);

        //String s = new String(IOUtils.toByteArray(conn.getInputStream()));
        //System.out.println(s);

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new BufferedReader(new InputStreamReader(conn.getInputStream())));

        int eventType = xpp.getEventType();

        List<DownloadItem> downloadItems = new ArrayList<DownloadItem>(50);
        DownloadItem downloadItem = null;
        String lastTag = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
                
            if(eventType == XmlPullParser.START_DOCUMENT) {
                System.out.println("Start document");
            } else if(eventType == XmlPullParser.START_TAG) {
                System.out.println("Start tag "+xpp.getName());
                lastTag = xpp.getName();
                if(xpp.getName().equals("entry")) {
                    downloadItem = new DownloadItem();
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                System.out.println("End tag "+xpp.getName());
                if(xpp.getName().equals("entry")) {
                    downloadItems.add(downloadItem);
                    downloadItem = null;
                }
            } else if(eventType == XmlPullParser.TEXT) {
                System.out.println("Text "+xpp.getText());
                if(downloadItem != null && lastTag.equals("id")) {
                    downloadItem.setAddress(xpp.getText());
                }
                if(downloadItem != null && lastTag.equals("title")) {
                    downloadItem.setTitle(xpp.getText());
                }
            }
            eventType = xpp.next();
        }
        System.out.println("End document");
        System.out.println(downloadItems.size());


        return downloadItems;
		
	}

	@Override
	protected void openCategory(DownloadItem di) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void goBack() {
        // Do nothing
	}

	@Override
	protected void fetchDatabase(DownloadItem di) {
		// TODO Auto-generated method stub
		
	}
}
