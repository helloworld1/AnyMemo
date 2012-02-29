package org.liberty.android.fantastischmemo.test;

import java.io.File;

import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.InstrumentationActivity;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.utils.DatabaseUtils;


public class DatabaseUtilsTest extends AbstractExistingDBTest {
    private InstrumentationActivity mActivity;  // the activity under test

    AnyMemoDBOpenHelper helper;

    String dbPath = "/sdcard/french-body-parts.db";
    public DatabaseUtilsTest() {
        super();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
        helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, dbPath);
    }

    public void testGetDefaultSetting() throws Exception {
        Setting setting = DatabaseUtils.readDefaultSetting(mActivity);
        assertNotNull(setting);
    }

    public void testMergeDatabase() throws Exception {
        // Create testing DB to merge
        String path2 = "/sdcard/testdb2.db";
        new File(path2).delete();
        AnyMemoDBOpenHelper helper2 = AnyMemoDBOpenHelperManager.getHelper(mActivity, path2);
        CardDao cd2 = helper2.getCardDao();
        cd2.create(new Card() {{setQuestion("c1");}});
        cd2.create(new Card() {{setQuestion("c2");}});
        cd2.create(new Card() {{setQuestion("c3");}});

        DatabaseUtils.mergeDatabases(mActivity, dbPath, path2);
        CardDao cd = helper.getCardDao();
        List<Card> all = cd.queryForAll();
        assertEquals(31, all.size());
        List<Card> cs = cd.queryForEq("question", "c1");
        Card c = cs.get(0);
        assertEquals(29, (int)c.getOrdinal());
        c = cd.queryLastOrdinal();
        assertEquals("c3", c.getQuestion());
        assertEquals(31, (int)c.getOrdinal());
        assertEquals("", c.getCategory().getName());
        assertNotNull(c.getLearningData());

        AnyMemoDBOpenHelperManager.releaseHelper(helper2);
        new File(path2).delete();
    }
}
