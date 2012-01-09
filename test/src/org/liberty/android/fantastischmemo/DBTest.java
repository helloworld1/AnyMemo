package org.liberty.android.fantastischmemo;

import java.sql.SQLException;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.Item;
import org.liberty.android.fantastischmemo.MainTabs;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.DeckDao;
import org.liberty.android.fantastischmemo.dao.FilterDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Setting;

import org.liberty.android.fantastischmemo.queue.LearnQueueManager;

import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;

import android.os.Parcel;

import android.test.ActivityInstrumentationTestCase2;

import android.widget.TextView;

public class DBTest extends ActivityInstrumentationTestCase2<MiscTab> {
    private MiscTab mActivity;  // the activity under test

    public DBTest() {
      super("org.liberty.android.fantastischmemo", MiscTab.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
    }

    public void testQueuing() {
        try {
            AnyMemoDBOpenHelper helper =
                AnyMemoDBOpenHelperManager.getHelper(mActivity, "/sdcard/french-body-parts.db");
            assertNotNull("Helper should not null!", helper);
            CardDao cardDao = helper.getCardDao();
            DeckDao deckDao = helper.getDeckDao();

            SettingDao settingDao = helper.getSettingDao();
            FilterDao filterDao = helper.getFilterDao();
            CategoryDao categoryDao = helper.getCategoryDao();
            LearningDataDao learningDataDao = helper.getLearningDataDao();

            assertNotNull("cardDao should not null!", cardDao);
            assertNotNull("deckDao should not null!", deckDao);
            assertNotNull("learningDataDao should not null!", learningDataDao);

            // Test Settings
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

            // Test learning queue
            LearnQueueManager manager = new LearnQueueManager(10, 50);
            manager.setLearningDataDao(learningDataDao);
            manager.setCardDao(cardDao);
            List<Card> lc = manager.getCardForReview(10);
            for(Card card : lc) {
               System.out.println("Card id" + card.getId());
               System.out.println("Card id" + card.getQuestion());
               System.out.println("Card id" + card.getAnswer());
               System.out.println("Card id" + card.getCategory().getName());
            } 
            System.out.println("# of total cards" + learningDataDao.getTotalCount());
            System.out.println("# of new cards" + learningDataDao.getNewCardCount());
            System.out.println("# of scheduled cards" + learningDataDao.getScheduledCardCount());
            DefaultScheduler scheduler = new DefaultScheduler();
            int[] s = {0,1,2,3,4,5};
            System.out.println("This is the first");
            while (true) {
                Card c = manager.dequeue();
                if (c == null) {
                    System.out.println("This is the end");
                    break;
                }
                LearningData newld = new LearningData();
                newld.setId(c.getId());
                newld.setGrade(3);
                newld.setUpdateDate(new Date());
                newld.setLastLearnDate(new Date());
                newld.setEasiness(100.0f);
                learningDataDao.createOrUpdate(newld);
            }
            AnyMemoDBOpenHelperManager.releaseHelper("/sdcard/french-body-parts.db");
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
