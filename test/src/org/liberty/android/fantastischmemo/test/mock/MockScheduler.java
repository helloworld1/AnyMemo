package org.liberty.android.fantastischmemo.test.mock;

import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;

public class MockScheduler implements Scheduler {
    private LearningData mockedLearningData = new LearningData();

    private boolean mockedCardLearned = true;

    public void setMockedLearningData(LearningData ld) {
        mockedLearningData = ld;
    }

    public void setMockedCardLearned(boolean learned) {
        mockedCardLearned = learned;
    }

    @Override
    public LearningData schedule(LearningData oldData, int newGrade,
            boolean includeNoise) {
        return mockedLearningData; 
    }

    @Override
    public boolean isCardLearned(LearningData data) {
        return mockedCardLearned;
    }

}
