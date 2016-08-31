package org.liberty.android.fantastischmemo.test.queue;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.entity.Category;
import org.liberty.android.fantastischmemo.entity.LearningData;
import org.liberty.android.fantastischmemo.integrationtest.TestHelper;
import org.liberty.android.fantastischmemo.queue.LearnQueueManager;
import org.liberty.android.fantastischmemo.queue.QueueManager;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LearnQueuingManagerTest extends AbstractExistingDBTest {

    @SmallTest
    @Test
    public void testGetNewCardQueuingWithCategory() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c10 = cardDao.queryForId(10);
        assertNotNull(c10);
        Category cat = new Category();
        cat.setName("tt");
        c10.setCategory(cat);
        cardDao.update(c10);
        QueueManager queueManager = new LearnQueueManager.Builder(getContext(), TestHelper.SAMPLE_DB_PATH)
            .setLearnQueueSize(10)
            .setFilterCategory(cat)
            .setCacheSize(50)
            .build();
        Card cqueue = queueManager.dequeue();
        assertEquals(10, (int)cqueue.getId());
        queueManager.release();
    }

    @SmallTest
    @Test
    public void testGetNewCardQueuingWithoutCategory() throws Exception {
        QueueManager queueManager = new LearnQueueManager.Builder(getContext(), TestHelper.SAMPLE_DB_PATH)
            .setLearnQueueSize(10)
            .setFilterCategory(null)
            .setCacheSize(50)
            .build();
        Card cqueue = queueManager.dequeue();
        assertEquals(1, (int)cqueue.getId());
        queueManager.release();
    }

    @SmallTest
    @Test
    public void testQueuingPosition() throws Exception {
        QueueManager queueManager = new LearnQueueManager.Builder(getContext(), TestHelper.SAMPLE_DB_PATH)
            .setLearnQueueSize(10)
            .setFilterCategory(null)
            .setCacheSize(50)
            .build();
        Card cqueue = queueManager.dequeuePosition(5);
        assertEquals(5, (int)cqueue.getId());
        queueManager.release();
    }

    @SmallTest
    @Test
    public void testUpdate1() throws Exception {
        Scheduler mockScheduler = mock(Scheduler.class);

        QueueManager queueManager = new LearnQueueManager.Builder(getContext(), TestHelper.SAMPLE_DB_PATH)
            .setLearnQueueSize(3)
            .setFilterCategory(null)
            .setScheduler(mockScheduler)
            .setCacheSize(50)
            .build();

        Card c1 = queueManager.dequeue();
        queueManager.remove(c1);
        assertEquals(1, (int)c1.getId());
        when(mockScheduler.isCardLearned(c1.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c1);
        queueManager.update(c1);

        Card c2 = queueManager.dequeue();
        queueManager.remove(c2);
        assertEquals(2, (int)c2.getId());
        when(mockScheduler.isCardLearned(c2.getLearningData()))
            .thenReturn(false);
        updateFailureCardLearningData(c2);
        queueManager.update(c2);

        Card c3 = queueManager.dequeue();
        queueManager.remove(c3);
        assertEquals(3, (int)c3.getId());
        when(mockScheduler.isCardLearned(c3.getLearningData()))
            .thenReturn(false);
        updateFailureCardLearningData(c3);
        queueManager.update(c3);

        Card c4 = queueManager.dequeue();
        queueManager.remove(c4);
        assertEquals(4, (int)c4.getId());
        when(mockScheduler.isCardLearned(c4.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c4);
        queueManager.update(c4);

        Card c2Again = queueManager.dequeue();
        queueManager.remove(c2Again);
        assertEquals(2, (int)c2Again.getId());
        when(mockScheduler.isCardLearned(c2Again.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c2Again);
        queueManager.update(c2Again);

        Card c3Again = queueManager.dequeue();
        queueManager.remove(c3Again);
        assertEquals(3, (int)c3Again.getId());
        when(mockScheduler.isCardLearned(c3Again.getLearningData()))
            .thenReturn(false);
        updateFailureCardLearningData(c3Again);
        queueManager.update(c3Again);

        Card c5 = queueManager.dequeue();
        queueManager.remove(c5);
        assertEquals(5, (int)c5.getId());
        when(mockScheduler.isCardLearned(c5.getLearningData()))
            .thenReturn(false);
        updateFailureCardLearningData(c5);
        queueManager.update(c5);

        queueManager.release();
    }

    @SmallTest
    @Test
    public void testUpdate2() throws Exception {
        Scheduler mockScheduler = mock(Scheduler.class);

        QueueManager queueManager = new LearnQueueManager.Builder(getContext(), TestHelper.SAMPLE_DB_PATH)
            .setLearnQueueSize(3)
            .setFilterCategory(null)
            .setScheduler(mockScheduler)
            .setCacheSize(50)
            .build();

        Card c1 = queueManager.dequeue();
        queueManager.remove(c1);
        assertEquals(1, (int)c1.getId());
        when(mockScheduler.isCardLearned(c1.getLearningData()))
            .thenReturn(false);
        updateFailureCardLearningData(c1);
        queueManager.update(c1);

        Card c2 = queueManager.dequeue();
        queueManager.remove(c2);
        assertEquals(2, (int)c2.getId());
        when(mockScheduler.isCardLearned(c2.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c2);
        queueManager.update(c2);

        Card c3 = queueManager.dequeue();
        queueManager.remove(c3);
        assertEquals(3, (int)c3.getId());
        when(mockScheduler.isCardLearned(c3.getLearningData()))
            .thenReturn(false);
        updateFailureCardLearningData(c3);
        queueManager.update(c3);

        Card c1Again = queueManager.dequeue();
        queueManager.remove(c1Again);
        assertEquals(1, (int)c1Again.getId());
        when(mockScheduler.isCardLearned(c1Again.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c1Again);
        queueManager.update(c1Again);

        Card c4 = queueManager.dequeue();
        queueManager.remove(c4);
        assertEquals(4, (int)c4.getId());
        when(mockScheduler.isCardLearned(c4.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c4);
        queueManager.update(c4);


        Card c3Again = queueManager.dequeue();
        queueManager.remove(c3Again);
        assertEquals(3, (int)c3Again.getId());
        when(mockScheduler.isCardLearned(c3Again.getLearningData()))
            .thenReturn(false);
        updateFailureCardLearningData(c3Again);
        queueManager.update(c3Again);

        Card c5 = queueManager.dequeue();
        queueManager.remove(c5);
        assertEquals(5, (int)c5.getId());
        when(mockScheduler.isCardLearned(c5.getLearningData()))
            .thenReturn(false);
        updateFailureCardLearningData(c5);
        queueManager.update(c5);

        queueManager.release();
    }

    @SmallTest
    @Test
    public void testUpdate3() throws Exception {
        Scheduler mockScheduler = mock(Scheduler.class);

        QueueManager queueManager = new LearnQueueManager.Builder(getContext(), TestHelper.SAMPLE_DB_PATH)
            .setLearnQueueSize(10)
            .setFilterCategory(null)
            .setScheduler(mockScheduler)
            .setCacheSize(11)
            .build();

        for (int i = 1; i <= 10; i++) {
            Card c = queueManager.dequeue();
            queueManager.remove(c);
            assertEquals(i, (int)c.getId());
            if (i == 1 || i == 3) {
                when(mockScheduler.isCardLearned(c.getLearningData()))
                    .thenReturn(false);
                updateFailureCardLearningData(c);
            } else {
                when(mockScheduler.isCardLearned(c.getLearningData()))
                    .thenReturn(true);
                updateSucceedCardLearningData(c);
            }
            queueManager.update(c);
        }

        Card c1 = queueManager.dequeue();
        queueManager.remove(c1);
        assertEquals(1, (int)c1.getId());
        when(mockScheduler.isCardLearned(c1.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c1);
        queueManager.update(c1);

        Card c11 = queueManager.dequeue();
        queueManager.remove(c11);
        assertEquals(11, (int)c11.getId());
        when(mockScheduler.isCardLearned(c11.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c11);
        queueManager.update(c11);

        Card c3 = queueManager.dequeue();
        queueManager.remove(c3);
        assertEquals(3, (int)c3.getId());
        when(mockScheduler.isCardLearned(c3.getLearningData()))
            .thenReturn(false);
        updateFailureCardLearningData(c3);
        queueManager.update(c3);

        Card c12 = queueManager.dequeue();
        queueManager.remove(c12);
        assertEquals(12, (int)c12.getId());
        when(mockScheduler.isCardLearned(c12.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c12);
        queueManager.update(c12);

        Card c13 = queueManager.dequeue();
        queueManager.remove(c13);
        assertEquals(13, (int)c13.getId());
        when(mockScheduler.isCardLearned(c13.getLearningData()))
            .thenReturn(true);
        updateSucceedCardLearningData(c13);
        queueManager.update(c13);

        queueManager.release();
    }

    private void updateSucceedCardLearningData(Card card) {
        LearningData ld = card.getLearningData();
        ld.setAcqReps(1);
        ld.setNextLearnDate(new Date(new Date().getTime() + 100000000));
        ld.setGrade(5);
    }

    private void updateFailureCardLearningData(Card card) {
        LearningData ld = card.getLearningData();
        ld.setAcqReps(1);
        // Make sure to review
        ld.setNextLearnDate(new Date(new Date().getTime() - 1));
        ld.setGrade(0);
    }
}
