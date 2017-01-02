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
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class FolderFactory {
    private FolderFactory() {
        throw new AssertionError("Don't call constructor");
    }

    public static List<Folder> getFolderWithTitle(String title, String authToken) throws XmlPullParserException, IOException {
        List<Folder> resFolders = new ArrayList<Folder>();
        List<Folder> allFolders = getFolders(authToken);
        for (Folder f : allFolders) {
            if (f.getTitle().equals(title)) {
                resFolders.add(f);
            }
        }
        return resFolders;
    }

    public static List<Folder> getFolders(String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://www.googleapis.com/drive/v2/files?q=" + URLEncoder.encode("mimeType = 'application/vnd.google-apps.folder'", "UTF-8"));
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + authToken);
        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new IOException(s);
        }

        List<Folder> folderList = EntryFactory.getEntriesFromDriveApi(Folder.class, conn.getInputStream());

        return folderList;
    }

    public static Folder createOrReturnFolder(String title, String authToken) throws XmlPullParserException, IOException {
        List<Folder> existingFolders = getFolderWithTitle(title, authToken);
        if (existingFolders.size() > 0) {
            return existingFolders.get(0);
        }
        return createFolder(title, authToken);
    }

    public static Folder createFolder(String title, String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://www.googleapis.com/drive/v2/files");

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + authToken);
        conn.setRequestProperty("Content-Type", "application/json");

        // Used to calculate the content length of the multi part
        String payload = "{\"title\":\"" + title + "\",\"mimeType\":\"application/vnd.google-apps.folder\"}";
        conn.setRequestProperty("Content-Length", "" + payload.length());

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
        outputStreamWriter.write(payload);
        outputStreamWriter.close();

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }

        return EntryFactory.getEntryFromDriveApi(Folder.class, conn.getInputStream());
    }

    public static void addDocumentToFolder(Document document, Folder folder, String authToken) throws XmlPullParserException, IOException {
        URL url = new URL("https://www.googleapis.com/drive/v2/files/" + document.getId() + "/parents");

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + authToken);
        conn.setRequestProperty("Content-Type", "application/json");

        String payload = "{\"id\":\"" + folder.getId() + "\"}";
        conn.setRequestProperty("Content-Length", "" + payload.length());

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
        outputStreamWriter.write(payload);
        outputStreamWriter.close();
        

        if (conn.getResponseCode() / 100 >= 3) {
            String s = new String(IOUtils.toByteArray(conn.getErrorStream()));
            throw new RuntimeException(s);
        }
    }
}
