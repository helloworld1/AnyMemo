package org.liberty.android.fantastischmemo;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import org.liberty.android.fantastischmemo.queue.QueueManager;
import org.liberty.android.fantastischmemo.queue.QueueManagerFactory;

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
        QueueManager queueManager = QueueManagerFactory.buildLearnQueueManager(cardDao, learningDataDao, 10, 50, cat);
        Card cqueue = queueManager.dequeue();
        assertEquals(10, (int)cqueue.getId());
    }

    public void testGetNewCardQueuingWithoutCategory() throws Exception {
        CardDao cardDao = helper.getCardDao();
        LearningDataDao learningDataDao = helper.getLearningDataDao();
        QueueManager queueManager = QueueManagerFactory.buildLearnQueueManager(cardDao, learningDataDao, 10, 50, null);
        Card cqueue = queueManager.dequeue();
        assertEquals(1, (int)cqueue.getId());
    }
}
