package org.liberty.android.fantastischmemo.test.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.integrationtest.TestHelper;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

import static org.junit.Assert.assertTrue;

public class DatabasesProviderTest extends AbstractExistingDBTest {

    private static String AUTHORITY = "org.liberty.android.fantastischmemo.databasesprovider";

    @SmallTest
    @Test
    public void testReturnSampleDb() {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://" + AUTHORITY),null, null, null, null);
        assertTrue(cursor.getCount() >= 1);

        boolean hasSampleDb = false;
        cursor.moveToFirst();
        do {
            String dbName = cursor.getString(cursor.getColumnIndex("dbname"));
            if (dbName.equals(TestHelper.SAMPLE_DB_NAME)) {
                hasSampleDb = true;
            }
        } while (cursor.moveToNext());
        cursor.close();
        assertTrue(hasSampleDb);
    }
}

