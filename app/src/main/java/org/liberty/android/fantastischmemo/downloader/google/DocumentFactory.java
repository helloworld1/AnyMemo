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

import org.apache.commons.io.IOUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class DocumentFactory {
    private DocumentFactory() {
        throw new AssertionError("Don't call constructor");
    }

    public static Document createSpreadsheet(String title, String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://www.googleapis.com/drive/v2/files");

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + authToken);
        conn.setRequestProperty("Content-Type", "application/json");


        String payload = "{\"title\":\"" + title + "\",\"mimeType\":\"application/vnd.google-apps.spreadsheet\"}";

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
        outputStreamWriter.write(payload);
        outputStreamWriter.close();

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }

        return EntryFactory.getEntryFromDriveApi(Document.class, conn.getInputStream());
    }

    public static void deleteDocument(Document document, String authToken) throws IOException {
        URL url = new URL("https://www.googleapis.com/drive/v2/files/" + document.getId());
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + authToken);

        conn.setRequestMethod("DELETE");

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }
    }

    public static List<Document> findDocuments(String title, String authToken) throws Exception {
        URL url = new URL("https://www.googleapis.com/drive/v2/files?q=" + URLEncoder.encode("title = '" + title + "'", "UTF-8"));

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + authToken);

        if (conn.getResponseCode() >= 300) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }

        List<Document> documentList = EntryFactory.getEntriesFromDriveApi(Document.class, conn.getInputStream());
        return documentList;
    }
}
