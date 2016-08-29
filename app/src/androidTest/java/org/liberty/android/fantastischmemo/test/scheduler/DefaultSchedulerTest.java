package org.liberty.android.fantastischmemo.test.scheduler;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.entity.LearningData;
import org.liberty.android.fantastischmemo.entity.SchedulingAlgorithmParameters;
import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;
import org.liberty.android.fantastischmemo.test.AbstractPreferencesTest;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultSchedulerTest extends AbstractPreferencesTest {

    private DefaultScheduler defaultScheduler;

    private LearningData newCardLearningData;

    private LearningData failedCardLearningData;

    private LearningData learnedCardLearningData;

    private LearningData forReviewCardLearningData;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        defaultScheduler = new DefaultScheduler(new SchedulingAlgorithmParameters(getContext()));

        newCardLearningData = new LearningData();

        failedCardLearningData = new LearningData();
        failedCardLearningData.setAcqReps(1);
        failedCardLearningData.setNextLearnDate(new Date(new Date().getTime() - 1000000));
        failedCardLearningData.setGrade(0);

        learnedCardLearningData = new LearningData();
        learnedCardLearningData.setAcqReps(1);
        learnedCardLearningData.setNextLearnDate(new Date(new Date().getTime() + 1000000));
        learnedCardLearningData.setGrade(5);

        forReviewCardLearningData = new LearningData();
        forReviewCardLearningData.setAcqReps(2);
        forReviewCardLearningData.setNextLearnDate(new Date(new Date().getTime() - 1000000));
        forReviewCardLearningData.setGrade(5);
    }

    @SmallTest
    @Test
    public void testIsNew() {
        assertTrue(defaultScheduler.isCardNew(newCardLearningData));
        assertFalse(defaultScheduler.isCardNew(failedCardLearningData));
        assertFalse(defaultScheduler.isCardNew(learnedCardLearningData));
        assertFalse(defaultScheduler.isCardNew(forReviewCardLearningData));
    }

    @SmallTest
    @Test
    public void testIsCardLearned() {
        assertFalse(defaultScheduler.isCardLearned(newCardLearningData));
        assertFalse(defaultScheduler.isCardLearned(failedCardLearningData));
        assertTrue(defaultScheduler.isCardLearned(learnedCardLearningData));
        assertTrue(defaultScheduler.isCardLearned(forReviewCardLearningData));
    }

    @SmallTest
    @Test
    public void testIsCardForReview() {
        assertFalse(defaultScheduler.isCardForReview(newCardLearningData));
        assertTrue(defaultScheduler.isCardForReview(failedCardLearningData));
        assertFalse(defaultScheduler.isCardForReview(learnedCardLearningData));
        assertTrue(defaultScheduler.isCardForReview(forReviewCardLearningData));
    }

    @SmallTest
    @Test
    public void testScheduleNewCardSuccess() {
        LearningData ld = new LearningData();
        LearningData newLd = defaultScheduler.schedule(ld, 3, false);
        assertFalse(defaultScheduler.isCardForReview(newLd));
        assertFalse(defaultScheduler.isCardNew(newLd));
        assertTrue(defaultScheduler.isCardLearned(newLd));

        assertEquals(3, (int) newLd.getGrade());
    }

    @SmallTest
    @Test
    public void testScheduleNewCardFailure() {
        LearningData newLd = defaultScheduler.schedule(newCardLearningData, 0, false);

        assertTrue(defaultScheduler.isCardForReview(newLd));
        assertFalse(defaultScheduler.isCardNew(newLd));
        assertFalse(defaultScheduler.isCardLearned(newLd));

        assertEquals(0, (int) newLd.getGrade());
    }

    @SmallTest
    @Test
    public void testScheduleFailedCardSuccess() {
        LearningData newLd = defaultScheduler.schedule(failedCardLearningData, 5, false);

        assertFalse(defaultScheduler.isCardForReview(newLd));
        assertFalse(defaultScheduler.isCardNew(newLd));
        assertTrue(defaultScheduler.isCardLearned(newLd));

    }

    @SmallTest
    @Test
    public void testScheduleFailedCardFailure() {
        LearningData newLd = defaultScheduler.schedule(failedCardLearningData, 0, false);

        assertTrue(defaultScheduler.isCardForReview(newLd));
        assertFalse(defaultScheduler.isCardNew(newLd));
        assertFalse(defaultScheduler.isCardLearned(newLd));
    }

    @SmallTest
    @Test
    public void testScheduleLearnedCardSuccess() {
        LearningData newLd = defaultScheduler.schedule(learnedCardLearningData, 5, false);

        assertFalse(defaultScheduler.isCardForReview(newLd));
        assertFalse(defaultScheduler.isCardNew(newLd));
        assertTrue(defaultScheduler.isCardLearned(newLd));
    }

    @SmallTest
    @Test
    public void testScheduleLearnedCardFailure() {
        LearningData newLd = defaultScheduler.schedule(learnedCardLearningData, 0, false);

        assertTrue(defaultScheduler.isCardForReview(newLd));
        assertFalse(defaultScheduler.isCardNew(newLd));
        assertFalse(defaultScheduler.isCardLearned(newLd));
    }

    @SmallTest
    @Test
    public void testScheduleForReviewardSuccess() {
        LearningData newLd = defaultScheduler.schedule(forReviewCardLearningData, 5, false);

        assertFalse(defaultScheduler.isCardForReview(newLd));
        assertFalse(defaultScheduler.isCardNew(newLd));
        assertTrue(defaultScheduler.isCardLearned(newLd));
    }

    @SmallTest
    @Test
    public void testScheduleForScheduleCardFailure() {
        LearningData newLd = defaultScheduler.schedule(forReviewCardLearningData, 0, false);

        assertTrue(defaultScheduler.isCardForReview(newLd));
        assertFalse(defaultScheduler.isCardNew(newLd));
        assertFalse(defaultScheduler.isCardLearned(newLd));
    }
}
