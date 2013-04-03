package org.liberty.android.fantastischmemo.test.queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.queue.QueueManager;
import org.liberty.android.fantastischmemo.queue.QuizQueueManager;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

import android.test.suitebuilder.annotation.SmallTest;

public class QuizQueuingManagerTest extends AbstractExistingDBTest {

    @SmallTest
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

    @SmallTest
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

    @SmallTest
    public void testUpdateCard() throws Exception {
        Scheduler mockScheduler = mock(Scheduler.class);

        // Create a quiz start the quiz size 3 from card 5
        QueueManager queueManager = new QuizQueueManager.Builder()
            .setDbOpenHelper(helper)
            .setQuizSize(3)
            .setStartCardOrd(5)
            .setScheduler(mockScheduler)
            .build();

        Card card5 = queueManager.dequeue();
        assertEquals(5, (int)card5.getOrdinal());
        when(mockScheduler.isCardLearned(card5.getLearningData()))
            .thenReturn(true);
        queueManager.update(card5);

        Card card6 = queueManager.dequeue();
        assertEquals(6, (int)card6.getOrdinal());
        when(mockScheduler.isCardLearned(card6.getLearningData()))
            .thenReturn(false);
        queueManager.update(card6);

        Card card7 = queueManager.dequeue();
        assertEquals(7, (int)card7.getOrdinal());
        when(mockScheduler.isCardLearned(card7.getLearningData()))
            .thenReturn(true);
        queueManager.update(card7);

        // Now cards in the queue is the card we failed
        // and we succeed this time
        Card card6Again = queueManager.dequeue();
        assertEquals(6, (int)card6Again.getOrdinal());
        when(mockScheduler.isCardLearned(card6.getLearningData()))
            .thenReturn(true);
        queueManager.update(card6Again);

        // No cards in the queue
        Card nullCard= queueManager.dequeue();
        assertNull(nullCard);
    }

}
