package org.liberty.android.fantastischmemo.test.provider;

import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.test.TestHelper;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;

public class CardProviderTest extends AbstractExistingDBTest {

    private static String AUTHORITY = "org.liberty.android.fantastischmemo.cardprovider";

    @SmallTest
    public void testNonExistingDbShouldReturnNullCursor() {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://" + AUTHORITY + "/" + "No_such_db_exists.db"+ "/count"),null, null, null, null);
        assertNull(cursor);
    }

    @SmallTest
    public void testCardCount() {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://" + AUTHORITY + "/" + TestHelper.SAMPLE_DB_NAME + "/count"),null, null, null, null);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(28, cursor.getInt(0));
    }

    @SmallTest
    public void testGetRandomCards() {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://" + AUTHORITY + "/" + TestHelper.SAMPLE_DB_NAME + "/random/10"),null, null, null, null);
        assertEquals(10, cursor.getCount());
    }

    @SmallTest
    public void testGetByOrdinal() {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://" + AUTHORITY + "/" + TestHelper.SAMPLE_DB_NAME + "/ordinal/3"),null, null, null, null);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(3, cursor.getInt(cursor.getColumnIndex("ordinal")));
        assertEquals("face", cursor.getString(cursor.getColumnIndex("question")));
        assertEquals("le visage", cursor.getString(cursor.getColumnIndex("answer")));
    }

    @SmallTest
    public void testGetById() {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://" + AUTHORITY + "/" + TestHelper.SAMPLE_DB_NAME + "/id/3"),null, null, null, null);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(3, cursor.getInt(cursor.getColumnIndex("ordinal")));
        assertEquals("face", cursor.getString(cursor.getColumnIndex("question")));
        assertEquals("le visage", cursor.getString(cursor.getColumnIndex("answer")));
    }

    @SmallTest
    public void testGetCardWithStartOrdinalAndCount() {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://" + AUTHORITY + "/" + TestHelper.SAMPLE_DB_NAME + "/start_ordinal/3/count/5"),null, null, null, null);
        assertEquals(5, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(3, cursor.getInt(cursor.getColumnIndex("ordinal")));
        assertEquals("face", cursor.getString(cursor.getColumnIndex("question")));
        assertEquals("le visage", cursor.getString(cursor.getColumnIndex("answer")));
        assertTrue(cursor.moveToNext());
        assertEquals(4, cursor.getInt(cursor.getColumnIndex("ordinal")));
    }

    @SmallTest
    public void testGetAllCards() {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://" + AUTHORITY + "/" + TestHelper.SAMPLE_DB_NAME + "/all"),null, null, null, null);
        assertEquals(28, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(1, cursor.getInt(cursor.getColumnIndex("ordinal")));
        assertEquals("head", cursor.getString(cursor.getColumnIndex("question")));
        assertTrue(cursor.moveToNext());
        assertEquals(2, cursor.getInt(cursor.getColumnIndex("ordinal")));
    }
}
