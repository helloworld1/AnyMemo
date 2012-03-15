package org.liberty.android.fantastischmemo.test.queue;

import org.liberty.android.fantastischmemo.InstrumentationActivity;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import org.liberty.android.fantastischmemo.queue.LearnQueueManager;
import org.liberty.android.fantastischmemo.queue.QueueManager;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

public class QueuingTest extends AbstractExistingDBTest<InstrumentationActivity> {

    public QueuingTest() {
        super("org.liberty.android.fantastischmemo", InstrumentationActivity.class);
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

    public void testQueuingPosition() throws Exception {
        CardDao cardDao = helper.getCardDao();
        LearningDataDao learningDataDao = helper.getLearningDataDao();
        QueueManager queueManager = new LearnQueueManager.Builder()
            .setCardDao(cardDao)
            .setLearningDataDao(learningDataDao)
            .setLearnQueueSize(10)
            .setFilterCategory(null)
            .setCacheSize(50)
            .build();
        queueManager.position(5);
        Card cqueue = queueManager.dequeue();
        assertEquals(5, (int)cqueue.getId());
    }
}
