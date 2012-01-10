package org.liberty.android.fantastischmemo;

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

public class DBTest extends ActivityInstrumentationTestCase2<InstrumentationActivity> {
    private InstrumentationActivity mActivity;  // the activity under test
    AnyMemoDBOpenHelper helper;

    public DBTest() {
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

    public void testCategories() throws Exception {
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> categories = categoryDao.queryForAll();
        int initSize = categories.size();
        Category c1 = categoryDao.createOrReturn("c1");
        assertNotNull(c1);
        assertEquals(c1.getName(), "c1");
        categories = categoryDao.queryForAll();
        assertEquals(categories.size(), initSize + 1);
        Category c2 = categoryDao.createOrReturn("c1");
        assertEquals(c2.getName(), "c1");
        assertEquals(categories.size(), initSize + 1);
    }

    public void testSettingCardField() throws Exception {
        SettingDao settingDao = helper.getSettingDao();
        Setting setting = settingDao.queryForId(1);
        EnumSet<Setting.CardField> es = setting.getQuestionFieldEnum();
        assertNotNull("Get enum shou not be null es");
        assertTrue(es.contains(Setting.CardField.QUESTION));
        es.add(Setting.CardField.ANSWER);
        setting.setQuestionFieldEnum(es);
        settingDao.update(setting);
        setting = settingDao.queryForId(1);
        es = setting.getQuestionFieldEnum();
        assertTrue(es.contains(Setting.CardField.ANSWER));
    }

    public void testQueuing() throws Exception {
        CardDao cardDao = helper.getCardDao();
        LearningDataDao learningDataDao = helper.getLearningDataDao();

        // Test learning queue
        LearnQueueManager manager = new LearnQueueManager(10, 50);
        manager.setLearningDataDao(learningDataDao);
        manager.setCardDao(cardDao);
        List<Card> lc = manager.getCardForReview(10);
        assertEquals(learningDataDao.getScheduledCardCount(), lc.size());
        assertEquals(28, learningDataDao.getTotalCount());
        assertEquals(28, learningDataDao.getNewCardCount());

        DefaultScheduler scheduler = new DefaultScheduler();
        int[] s = {0,1,2,3,4,5};
        System.out.println("This is the first");
        while (true) {
            Card c = manager.dequeue();
            if (c == null) {
                System.out.println("This is the end");
                break;
            }
        }
    }
}
