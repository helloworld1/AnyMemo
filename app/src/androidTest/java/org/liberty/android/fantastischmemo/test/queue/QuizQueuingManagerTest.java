package org.liberty.android.fantastischmemo.test.queue;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.entity.Category;
import org.liberty.android.fantastischmemo.queue.QueueManager;
import org.liberty.android.fantastischmemo.queue.QuizQueueManager;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuizQueuingManagerTest extends AbstractExistingDBTest {

    @SmallTest
    @Test
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
    @Test
    public void testFilterByGroup() throws Exception {
        // Create a quiz start the quiz size 3 from card 5
        QueueManager queueManager = new QuizQueueManager.Builder()
            .setDbOpenHelper(helper)
            .setQuizSize(3)
            .setStartCardOrd(5)
            .build();
        Card card5 = queueManager.dequeue();
        queueManager.remove(card5);
        assertEquals(5, (int)card5.getOrdinal());

        Card card6 = queueManager.dequeue();
        queueManager.remove(card6);
        assertEquals(6, (int)card6.getOrdinal());

        Card card7 = queueManager.dequeue();
        queueManager.remove(card7);
        assertEquals(7, (int)card7.getOrdinal());

        // No cards in the queue
        Card nullCard= queueManager.dequeue();
        assertNull(nullCard);
    }

    @SmallTest
    @Test
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
        queueManager.remove(card5);
        assertEquals(5, (int)card5.getOrdinal());
        when(mockScheduler.isCardLearned(card5.getLearningData()))
            .thenReturn(true);
        queueManager.update(card5);

        Card card6 = queueManager.dequeue();
        queueManager.remove(card6);
        assertEquals(6, (int)card6.getOrdinal());
        when(mockScheduler.isCardLearned(card6.getLearningData()))
            .thenReturn(false);
        queueManager.update(card6);

        Card card7 = queueManager.dequeue();
        queueManager.remove(card7);
        assertEquals(7, (int)card7.getOrdinal());
        when(mockScheduler.isCardLearned(card7.getLearningData()))
            .thenReturn(true);
        queueManager.update(card7);

        // Now cards in the queue is the card we failed
        // and we succeed this time
        Card card6Again = queueManager.dequeue();
        queueManager.remove(card6Again);
        assertEquals(6, (int)card6Again.getOrdinal());
        when(mockScheduler.isCardLearned(card6.getLearningData()))
            .thenReturn(true);
        queueManager.update(card6Again);

        // No cards in the queue
        Card nullCard= queueManager.dequeue();
        assertNull(nullCard);
    }

}
