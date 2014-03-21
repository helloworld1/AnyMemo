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
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.xmlpull.v1.XmlPullParserException;

import com.google.common.xml.XmlEscapers;

public class DocumentFactory {
    private DocumentFactory() {
        throw new AssertionError("Don't call constructor");
    }

    public static Document createSpreadsheet(String title, String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://docs.google.com/feeds/default/private/full?access_token=" + authToken);

        String payload = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns='http://www.w3.org/2005/Atom'>"+
            "<category scheme='http://schemas.google.com/g/2005#kind'"+
            " term='http://schemas.google.com/docs/2007#spreadsheet'/>"+
            "<title>"+ XmlEscapers.xmlAttributeEscaper().escape(title) +"</title>"+
            "</entry>";

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        //conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);
        conn.addRequestProperty("GData-Version", "3.0");
        conn.addRequestProperty("Content-Type", "application/atom+xml");
        conn.setRequestProperty("Content-Length", Integer.toString(payload.getBytes("UTF-8").length));


        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(payload);
        out.close();

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }

        List<Document> documentList = EntryFactory.getEntries(Document.class, conn.getInputStream());

        return documentList.get(0);
    }

    public static void deleteDocument(Document document, String authToken) throws IOException {
        URL url = new URL("https://docs.google.com/feeds/default/private/full/" + document.getId() + "?access_token=" + authToken);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("GData-Version", "3.0");
        conn.addRequestProperty("If-Match", "*");

        conn.setRequestMethod("DELETE");

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }
    }

    public static List<Document> findDocuments(String title, String authToken) throws Exception {
        URL url = new URL("https://docs.google.com/feeds/default/private/full?title=" + URLEncoder.encode(title, "UTF-8") + "&title-exact=true&access_token=" + authToken);

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);
        conn.addRequestProperty("GData-Version", "3.0");

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }

        List<Document> documentList = EntryFactory.getEntries(Document.class, conn.getInputStream());
        return documentList;
    }
}
