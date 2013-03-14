package org.liberty.android.fantastischmemo.test.queue;

import java.util.Date;

import org.liberty.android.fantastischmemo.InstrumentationActivity;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.queue.QueueManager;
import org.liberty.android.fantastischmemo.queue.QuizQueueManager;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.test.mock.MockScheduler;

public class QuizQueuingManagerTest extends AbstractExistingDBTest<InstrumentationActivity> {

    public QuizQueuingManagerTest() {
        super("org.liberty.android.fantastischmemo", InstrumentationActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testFilterCategory() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c10 = cardDao.queryForId(10);
        assertNotNull(c10);
        Category cat = new Category();
        cat.setName("tt");
        c10.setCategory(cat);
        cardDao.update(c10);
        QueueManager queueManager = new QuizQueueManager.Builder()
            .setDbOpenHelper(helper)
            .setFilterCategory(cat)
            .build();
        Card cqueue = queueManager.dequeue();
        assertEquals(10, (int)cqueue.getId());
    }

    public void testFilterByGroup() throws Exception {
        // Create a quiz start the quiz size 3 from card 5
        QueueManager queueManager = new QuizQueueManager.Builder()
            .setDbOpenHelper(helper)
            .setQuizSize(3)
            .setStartCardOrd(5)
            .build();
        Card card5 = queueManager.dequeue();
        assertEquals(5, (int)card5.getOrdinal());

        Card card6 = queueManager.dequeue();
        assertEquals(6, (int)card6.getOrdinal());

        Card card7 = queueManager.dequeue();
        assertEquals(7, (int)card7.getOrdinal());

        // No cards in the queue
        Card nullCard= queueManager.dequeue();
        assertNull(nullCard);
    }

    public void testUpdateCard() throws Exception {
        MockScheduler mockScheduler = new MockScheduler();

        // Create a quiz start the quiz size 3 from card 5
        QueueManager queueManager = new QuizQueueManager.Builder()
            .setDbOpenHelper(helper)
            .setQuizSize(3)
            .setStartCardOrd(5)
            .setScheduler(mockScheduler)
            .build();

        mockScheduler.setMockedCardLearned(true);
        Card card5 = queueManager.dequeue();
        assertEquals(5, (int)card5.getOrdinal());
        queueManager.update(card5);

        mockScheduler.setMockedCardLearned(false);
        Card card6 = queueManager.dequeue();
        assertEquals(6, (int)card6.getOrdinal());
        queueManager.update(card6);

        mockScheduler.setMockedCardLearned(true);
        Card card7 = queueManager.dequeue();
        assertEquals(7, (int)card7.getOrdinal());
        queueManager.update(card7);

        // Now cards in the queue is the card we failed
        // and we succeed this time
        mockScheduler.setMockedCardLearned(true);
        Card card6Again = queueManager.dequeue();
        assertEquals(6, (int)card6Again.getOrdinal());
        queueManager.update(card6Again);

        // No cards in the queue
        Card nullCard= queueManager.dequeue();
        assertNull(nullCard);
    }

}
