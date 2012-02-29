package org.liberty.android.fantastischmemo.test;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import org.liberty.android.fantastischmemo.queue.LearnQueueManager;
import org.liberty.android.fantastischmemo.queue.QueueManager;

public class QueuingTest extends AbstractExistingDBTest {
    public QueuingTest () {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetNewCardQueuingWithCategory() throws Exception {
        CardDao cardDao = helper.getCardDao();
        LearningDataDao learningDataDao = helper.getLearningDataDao();
        Card c10 = cardDao.queryForId(10);
        assertNotNull(c10);
        Category cat = new Category();
        cat.setName("tt");
        c10.setCategory(cat);
        cardDao.update(c10);
        QueueManager queueManager = new LearnQueueManager.Builder()
            .setCardDao(cardDao)
            .setLearningDataDao(learningDataDao)
            .setLearnQueueSize(10)
            .setFilterCategory(cat)
            .setCacheSize(50)
            .build();
        Card cqueue = queueManager.dequeue();
        assertEquals(10, (int)cqueue.getId());
    }

    public void testGetNewCardQueuingWithoutCategory() throws Exception {
        CardDao cardDao = helper.getCardDao();
        LearningDataDao learningDataDao = helper.getLearningDataDao();
        QueueManager queueManager = new LearnQueueManager.Builder()
            .setCardDao(cardDao)
            .setLearningDataDao(learningDataDao)
            .setLearnQueueSize(10)
            .setFilterCategory(null)
            .setCacheSize(50)
            .build();
        Card cqueue = queueManager.dequeue();
        assertEquals(1, (int)cqueue.getId());
    }
}
