package org.liberty.android.fantastischmemo.test.queue;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.queue.LearnQueueManager;
import org.liberty.android.fantastischmemo.queue.QueueManager;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

import android.test.suitebuilder.annotation.SmallTest;

public class LearnQueuingManagerTest extends AbstractExistingDBTest {

    @SmallTest
    public void testGetNewCardQueuingWithCategory() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c10 = cardDao.queryForId(10);
        assertNotNull(c10);
        Category cat = new Category();
        cat.setName("tt");
        c10.setCategory(cat);
        cardDao.update(c10);
        QueueManager queueManager = new LearnQueueManager.Builder()
            .setDbOpenHelper(helper)
            .setLearnQueueSize(10)
            .setFilterCategory(cat)
            .setCacheSize(50)
            .build();
        Card cqueue = queueManager.dequeue();
        assertEquals(10, (int)cqueue.getId());
    }

    @SmallTest
    public void testGetNewCardQueuingWithoutCategory() throws Exception {
        System.out.println("##############################################");
        QueueManager queueManager = new LearnQueueManager.Builder()
        	.setDbOpenHelper(helper)
            .setLearnQueueSize(10)
            .setFilterCategory(null)
            .setCacheSize(50)
            .build();
        Card cqueue = queueManager.dequeue();
        assertEquals(1, (int)cqueue.getId());
        System.out.println("##############################################");
    }

    @SmallTest
    public void testQueuingPosition() throws Exception {
        QueueManager queueManager = new LearnQueueManager.Builder()
            .setDbOpenHelper(helper)
            .setLearnQueueSize(10)
            .setFilterCategory(null)
            .setCacheSize(50)
            .build();
        Card cqueue = queueManager.dequeuePosition(5);
        assertEquals(5, (int)cqueue.getId());
    }
}
