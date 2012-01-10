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

public class ExistingDbTest extends ActivityInstrumentationTestCase2<InstrumentationActivity> {
    private InstrumentationActivity mActivity;  // the activity under test

    AnyMemoDBOpenHelper helper;

    public ExistingDbTest() {
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

    public void testAddCategories() throws Exception {
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

    public void testRemoveCategories() throws Exception {
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        Category c1 = categoryDao.createOrReturn("c1");
        categoryDao.create(c1);
        Card nc = new Card();
        nc.setCategory(c1);
        cardDao.create(nc);
        categoryDao.refresh(nc.getCategory());
        assertEquals("c1", nc.getCategory().getName());
        categoryDao.removeCategory(c1);
        nc = cardDao.queryForId(nc.getId());
        categoryDao.refresh(nc.getCategory());
        assertEquals("", nc.getCategory().getName());
    }

    public void testDeleteCardMaintainOrdinal() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c13 = cardDao.queryForId(13);
        Card c14 = cardDao.queryForId(14);
        Card c15 = cardDao.queryForId(15);
        assertEquals(13, (int)c13.getOrdinal());
        assertEquals(14, (int)c14.getOrdinal());
        assertEquals(15, (int)c15.getOrdinal());
        cardDao.delete(c14);
        c13 = cardDao.queryForId(13);
        c15 = cardDao.queryForId(15);
        assertEquals(13, (int)c13.getOrdinal());
        assertEquals(14, (int)c15.getOrdinal());
    }

    public void testCreateCardMaintainOrdinal() throws Exception {
        CardDao cardDao = helper.getCardDao();
        // Create card has null ordinal, append to the end
        Card nc = new Card();
        assertNull(nc.getOrdinal());
        cardDao.create(nc);
        assertEquals(29, (int)nc.getOrdinal());

        // Create card with an ordinal
        nc = new Card();
        nc.setOrdinal(14);
        cardDao.create(nc);

        Card c13 = cardDao.queryForId(13);
        Card c14 = cardDao.queryForId(14);
        Card c15 = cardDao.queryForId(15);
        assertEquals(13, (int)c13.getOrdinal());
        assertEquals(14, (int)nc.getOrdinal());
        assertEquals(15, (int)c14.getOrdinal());
        assertEquals(16, (int)c15.getOrdinal());
    }

    public void testSwapQA() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c14 = cardDao.queryForId(14);
        String question = c14.getQuestion();
        String answer = c14.getAnswer();
        cardDao.swapQA(c14);
        c14 = cardDao.queryForId(14);
        assertEquals(answer, c14.getQuestion());
        assertEquals(question, c14.getAnswer());
    }

    public void testRemoveDuplicates() throws Exception {
        CardDao cardDao = helper.getCardDao();
        long originalSize = cardDao.countOf();
        Card nc = new Card();
        nc.setQuestion("whatever");
        nc.setAnswer("and whatever");
        cardDao.create(nc);
        cardDao.create(nc);
        cardDao.create(nc);
        cardDao.create(nc);
        List<Card> cards = cardDao.queryForEq("question", "whatever");
        assertEquals(4, cards.size());
        assertEquals(originalSize + 4, cardDao.countOf());
        cardDao.removeDuplicates();
        assertEquals(originalSize + 1, cardDao.countOf());
        cards = cardDao.queryForEq("question", "whatever");
        assertEquals(1, cards.size());
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
