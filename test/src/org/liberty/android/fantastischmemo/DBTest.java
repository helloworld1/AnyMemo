package org.liberty.android.fantastischmemo;

import java.sql.SQLException;

import java.util.Date;
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

import org.liberty.android.fantastischmemo.queue.LearnQueueManager;

import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;

import android.os.Parcel;

import android.test.ActivityInstrumentationTestCase2;

import android.widget.TextView;

public class DBTest extends ActivityInstrumentationTestCase2<MainTabs> {
    private MainTabs mActivity;  // the activity under test

    public DBTest() {
      super("org.liberty.android.fantastischmemo", MainTabs.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
    }

    public void testAll() {
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
            Card cx = lc.get(0);
            System.out.println("cx" + cx.getQuestion());
            System.out.println("cx" + cx.getAnswer());
            System.out.println("cx" + cx.getCategory().getName());
            
            DefaultScheduler scheduler = new DefaultScheduler();
            int[] s = {0,1,2,3,4,5};
            for (int grade : s) {
                System.out.println("Original LD:  " + cx.getLearningData().toString());
                LearningData af = scheduler.schedule(cx.getLearningData(), grade, true);
                System.out.println("Grade: " + grade + af.toString());
            }

            //Card nc = new Card();
            //nc.setId(1);
            //nc.setQuestion("Test question");
            //nc.setAnswer("Test Answer");
            //Category cc = new Category();
            //cc.setId(154);
            //cc.setName("New category");
            //LearningData ld = new LearningData();
            //ld.setLapses(100);
            //ld.setNextLearnDate(new Date());
            //nc.setCategory(cc);
            //nc.setLearningData(ld);
            //categoryDao.create(cc);
            //learningDataDao.create(ld);
            //cardDao.createOrUpdate(nc);
            //nc = new Card();
            //nc.setQuestion("new card");
            //nc.setAnswer("new answer");
            //nc.setNote("New note");
            //nc.setCreationDate(new java.util.Date());
            //Deck de = new Deck();
            //de.setDescription("Hello");
            //de.setName("my");
            //deckDao.create(de);
            //Setting se = new Setting();
            //se.setName("My set");
            //settingDao.create(se);
            //Filter fe = new Filter();
            //fe.setName("my filter");
            //fe.setExpression("Whatever");
            //filterDao.create(fe);
            //cardDao.create(nc);
            System.out.println("This is the first");
            while (true) {
                Card c = manager.dequeue();
                if (c == null) {
                    System.out.println("This is the end");
                    break;
                }
                System.out.println(c.getId());
            }
            AnyMemoDBOpenHelperManager.releaseHelper("/sdcard/french-body-parts.db");

        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
