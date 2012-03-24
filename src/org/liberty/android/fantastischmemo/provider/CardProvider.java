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

import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import android.util.Log;

public class CardProvider extends ContentProvider {
    public static final String AUTHORITY = "org.liberty.android.fantastischmemo.provider";
    private static final String TAG = CardProvider.class.getName();
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "dbpath/*", 1);
        sUriMatcher.addURI(AUTHORITY, "dbpath/*/start/#", 2);
        sUriMatcher.addURI(AUTHORITY, "dbpath/*/limit/#", 3);
        sUriMatcher.addURI(AUTHORITY, "dbpath/*/start/#/limit/#", 4);
    }


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Does not support now
		return 0;
	}

	@Override
	public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.org.liberty.android.fantastischmemo.provider.card";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        // Does not support now
		return null;
	}

	@Override
	public boolean onCreate() {
        // Does not support now
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        switch (sUriMatcher.match(uri)) {
            case 1:
                String dbPathUri = uri.getLastPathSegment();
                return getCards(dbPathUri);
        }

        Log.e(TAG, "No case matched for uri: " + uri);
        return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        // Does not support now
		return 0;
	}

    // The dbPath name should be URI safe.
    private Cursor getCards(String dbPathUri) {
        String dbPath = Uri.decode(dbPathUri);
        // We don't want to create new file if the file does not exist.
        if (!(new File(dbPath).exists())) {
            Log.e(TAG, "DBPath does not exist: " + dbPath);
            return null;
        }
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(getContext(), dbPath);
        String[] columnNames = {"question", "answer"};
        try {
            CardDao cardDao = helper.getCardDao();
            List<Card> cards = cardDao.getRandomCards(null, 50);
            
            MatrixCursor cursor = new MatrixCursor(columnNames, 50);


            for (Card c : cards) {
                cursor.addRow(new String[]{c.getQuestion(), c.getAnswer()});
            }
            return cursor;
        } catch (Exception e) {
            Log.e(TAG, "Excepting getting cards.", e);
            return null;
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }
}

