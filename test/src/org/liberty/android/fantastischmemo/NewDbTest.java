package org.liberty.android.fantastischmemo;

import java.io.File;

import java.util.EnumSet;
import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.Setting;

import org.liberty.android.fantastischmemo.queue.LearnQueueManager;

import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;

import android.test.ActivityInstrumentationTestCase2;

public class NewDbTest extends ActivityInstrumentationTestCase2<InstrumentationActivity> {
    private InstrumentationActivity mActivity;  // the activity under test
    AnyMemoDBOpenHelper helper;
    public static final String dbPath = "/sdcard/newtestdb.db";

    public NewDbTest() {
        super("org.liberty.android.fantastischmemo", InstrumentationActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
        File newdbFile = new File(dbPath);
        newdbFile.delete();
        helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, dbPath);
    }

    @Override
    protected void tearDown() throws Exception {
        AnyMemoDBOpenHelperManager.releaseHelper(dbPath);
        File newdbFile = new File(dbPath);
        newdbFile.delete();
        mActivity.finish();
    }

    public void testCreateFirstCardWithCorrectOrdinal() throws Exception {
        CardDao cardDao = helper.getCardDao();
        // Create card has null ordinal, append to the end
        Card nc = new Card();
        assertNull(nc.getOrdinal());
        cardDao.create(nc);
        assertEquals(1, (int)nc.getOrdinal());
    }
}
