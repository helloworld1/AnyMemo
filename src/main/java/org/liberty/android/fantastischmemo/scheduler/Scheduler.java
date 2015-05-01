package org.liberty.android.fantastischmemo.scheduler;

import org.liberty.android.fantastischmemo.domain.LearningData;

public interface Scheduler {

	/*
	 * Return the interval of the after schedule the new card
	 */
	LearningData schedule(LearningData oldData, int newGrade,
			boolean includeNoise);

	/*
	 * This method returns true if the card should not
	 * be repeated immediately. False if it need to be
	 * repeaeted immediately.
	 */
	boolean isCardLearned(LearningData data);

    /*
     * Return true if the card is never studied before.
     */
    boolean isCardNew(LearningData data);

    /*
     * Return true if the card is never studied before.
     */
    boolean isCardForReview(LearningData data);
}
