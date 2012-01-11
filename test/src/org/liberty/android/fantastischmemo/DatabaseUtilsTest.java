package org.liberty.android.fantastischmemo;

import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;

import android.test.ActivityInstrumentationTestCase2;

public class DatabaseUtilsTest extends ActivityInstrumentationTestCase2<InstrumentationActivity> {
    private InstrumentationActivity mActivity;  // the activity under test

    AnyMemoDBOpenHelper helper;
    public DatabaseUtilsTest() {
        super("org.liberty.android.fantastischmemo", InstrumentationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
        AMUtil.copyFile("/sdcard/anymemo/french-body-parts.db", "/sdcard/french-body-parts.db");
        helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, "/sdcard/french-body-parts.db");
    }

    @Override
    protected void tearDown() throws Exception {
        AnyMemoDBOpenHelperManager.releaseHelper("/sdcard/french-body-parts.db");
        mActivity.finish();
    }

    public void testMergeDatabase() throws Exception {
        String path2 = "/sdcard/tesetdb2.db";
        AnyMemoDBOpenHelper helper2 = AnyMemoDBOpenHelperManager.getHelper(mActivity, path2);
        CardDao cd2 = helper2.getCardDao();
        cd2.create(new Card() {{setQuestion("c1");}});
        cd2.create(new Card() {{setQuestion("c2");}});
        cd2.create(new Card() {{setQuestion("c3");}});
        DatabaseUtils.mergeDatabases(mActivity, "/sdcard/french-body-parts.db", path2);
        CardDao cd = helper.getCardDao();
        List<Card> all = cd.queryForAll();
        assertEquals(31, all.size());
    }

}
