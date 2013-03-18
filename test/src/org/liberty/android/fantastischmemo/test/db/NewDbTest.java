package org.liberty.android.fantastischmemo.test.db;

import java.io.File;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

import android.test.suitebuilder.annotation.SmallTest;

public class NewDbTest extends AbstractExistingDBTest {
    AnyMemoDBOpenHelper newDbHelper;
    public static final String dbPath = "/sdcard/newtestdb.db";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File newdbFile = new File(dbPath);
        newdbFile.delete();
        newDbHelper = AnyMemoDBOpenHelperManager.getHelper(getContext(), dbPath);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        AnyMemoDBOpenHelperManager.releaseHelper(newDbHelper);
        File newdbFile = new File(dbPath);
        newdbFile.delete();
    }

    @SmallTest
    public void testCreateFirstCardWithCorrectOrdinal() throws Exception {
        CardDao cardDao = newDbHelper.getCardDao();
        // Create card has null ordinal, append to the end
        Card nc = new Card();
        assertNull(nc.getOrdinal());
        cardDao.create(nc);
        assertEquals(1, (int)nc.getOrdinal());
    }
}
