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
import java.util.ArrayList;
import java.util.List;

import org.liberty.android.fantastischmemo.AMEnv;
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

    public static final String AUTHORITY = "org.liberty.android.fantastischmemo.cardprovider";

    private static final String TAG = CardProvider.class.getName();

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int COUNT_URI = 1;

    private static final int RANDOM_URI= 2;

    private static final int ORD_URI = 3;

    private static final int ID_URI = 4;

    private static final int START_URI = 5;

    private static final int ALL_URI = 6;

    static {
        // Get the count of the cards in the db
        sUriMatcher.addURI(AUTHORITY, "*/count", COUNT_URI);

        // Get a number of random cards
        sUriMatcher.addURI(AUTHORITY, "*/random/#", RANDOM_URI);

        // Get the card with a specific ordinal
        sUriMatcher.addURI(AUTHORITY, "*/ordinal/#", ORD_URI);

        // Get the card with a specific id
        sUriMatcher.addURI(AUTHORITY, "*/id/#", ID_URI);

        // Get a list of cards starting with an ordinal and a specific max count
        sUriMatcher.addURI(AUTHORITY, "*/start_ordinal/#/count/#", START_URI);

        // Get a all cards in a db
        sUriMatcher.addURI(AUTHORITY, "*/all", ALL_URI);
    }


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Does not support now
		return 0;
	}

	@Override
	public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case COUNT_URI:
                return "vnd.android.cursor.dir/vnd.org.liberty.android.fantastischmemo.provider.integer";
            case RANDOM_URI:
            case ORD_URI:
            case ID_URI:
            case START_URI:
            case ALL_URI:
                return "vnd.android.cursor.dir/vnd.org.liberty.android.fantastischmemo.provider.card";
            default:
                return "vnd.android.cursor.dir/vnd.org.liberty.android.fantastischmemo.provider.card";
        }
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

    /**
     * The query returns null if the db is not valid.
     */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

        List<String> uriSegments = uri.getPathSegments();

        String dbPath = AMEnv.DEFAULT_ROOT_PATH + uriSegments.get(0);

        if (!new File(dbPath).exists()) {
            return null;
        }

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(getContext(), dbPath);

        try {
            CardDao cardDao = helper.getCardDao();
            Cursor resultCursor = null;

            switch (sUriMatcher.match(uri)) {
                case COUNT_URI: {
                    long count = cardDao.getTotalCount(null);
                    resultCursor = buildCursorFromCount(count);
                    break;
                }
                case RANDOM_URI: {
                    int count = Integer.valueOf(uriSegments.get(2));
                    List<Card> cards = cardDao.getRandomCards(null, count);
                    resultCursor = buildCursorFromCards(cards);
                    break;
                }

                case ORD_URI: {
                    int ord = Integer.valueOf(uriSegments.get(2));
                    Card card = cardDao.getByOrdinal(ord);
                    resultCursor = buildCursorFromCard(card);
                    break;
                }

                case ID_URI: {
                    int id = Integer.valueOf(uriSegments.get(2));
                    Card card = cardDao.getById(id);
                    resultCursor = buildCursorFromCard(card);
                    break;
                }

                case START_URI: {
                    int start = Integer.valueOf(uriSegments.get(2));
                    int count = Integer.valueOf(uriSegments.get(4));
                    List<Card> cards = cardDao.getCardsByOrdinalAndSize(start, count);
                    resultCursor = buildCursorFromCards(cards);
                    break;
                }

                case ALL_URI: {
                    List<Card> cards = cardDao.getAllCards(null);
                    resultCursor = buildCursorFromCards(cards);
                    break;
                }

                default:
                    throw new IllegalArgumentException("No matching handler for uri: " + uri);
            }
            if (resultCursor == null) {
                Log.e(TAG, "No case matched for uri: " + uri);
            }
            return resultCursor;
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        // Does not support now
		return 0;
	}

    /**
     * Build cursor from a single card.
     */
    private Cursor buildCursorFromCard(Card card) {
        if (card == null) {
            return null;
        }
        List<Card> cards = new ArrayList<Card>(1);
        cards.add(card);
        return buildCursorFromCards(cards);
    }

    /**
     * Build cursor from a list of cards.
     */
    private Cursor buildCursorFromCards(List<Card> cards) {
        String[] columnNames = {"id", "ordinal", "question", "answer", "category"};
        MatrixCursor cursor = new MatrixCursor(columnNames, cards.size());
        for (Card c : cards) {
            cursor.addRow(new String[]{c.getId().toString(), c.getOrdinal().toString(), c.getQuestion(), c.getAnswer(), c.getCategory().getName()});
        }
        return cursor;
    }

    private Cursor buildCursorFromCount(long count) {
        String[] columnNames = {"count"};
        MatrixCursor cursor = new MatrixCursor(columnNames, 1);
        cursor.addRow(new Long[] {count});
        return cursor;
    }
}

