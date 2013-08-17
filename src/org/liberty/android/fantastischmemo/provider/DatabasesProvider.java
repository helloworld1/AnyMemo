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

package org.liberty.android.fantastischmemo.provider;

import java.io.File;
import java.io.FilenameFilter;

import org.liberty.android.fantastischmemo.AMEnv;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * Query a list of database names from the default
 * AnyMemo directory.
 */
public class DatabasesProvider extends ContentProvider {

    public static final String AUTHORITY = "org.liberty.android.fantastischmemo.databasesprovider";

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.org.liberty.android.fantastischmemo.provider.database";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        File anymemoDir = new File(AMEnv.DEFAULT_ROOT_PATH);
        String[] files = anymemoDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".db");
            }
        });
        return buildCursorFromDbNames(files);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }

    private Cursor buildCursorFromDbNames(String[] dbNames) {
        String[] columnNames = {"_id", "dbname"};
        MatrixCursor cursor = new MatrixCursor(columnNames, dbNames.length);
        for (int i = 0; i < dbNames.length; i++) {
            cursor.addRow(new String[] {Integer.toString(i), dbNames[i]});
        }
        return cursor;
    }
}

