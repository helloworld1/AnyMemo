package org.liberty.android.fantastischmemo;

import java.sql.SQLException;

import java.util.List;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import org.liberty.android.fantastischmemo.queue.LearnQueueManager;

import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;

public class QueuingTest extends AbstractExistingDBTest {
    public QueuingTest () {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
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
