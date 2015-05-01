/*
Copyright (C) 2010 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.scheduler;

import java.util.Date;
import java.util.Random;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.SchedulingAlgorithmParameters;
import org.liberty.android.fantastischmemo.utils.AMDateUtil;

import roboguice.util.Ln;

/*
 * Default scheduler read the algorithm parameters
 * from the preferences and schedul card based on
 * modified mnemosyne algorithm.
 */
public class DefaultScheduler implements Scheduler {
    private final static String TAG = "DefaultScheduler";

    private SchedulingAlgorithmParameters parameters;

    @Inject
    public DefaultScheduler(SchedulingAlgorithmParameters parameters) {
        this.parameters = parameters;
    }
    /*
     * Return the interval of the after schedule the new card
     */
    @Override
    public LearningData schedule(LearningData oldData, int newGrade, boolean includeNoise) {
        Date currentDate = new Date();
        double actualInterval = AMDateUtil.diffDate(oldData.getLastLearnDate(), currentDate);
        double scheduleInterval = oldData.getInterval();
        double newInterval = 0.0;
        int oldGrade = oldData.getGrade();
        float oldEasiness = oldData.getEasiness();
        int newLapses = oldData.getLapses();
        int newAcqReps = oldData.getAcqReps();
        int newRetReps = oldData.getRetReps();
        int newAcqRepsSinceLapse = oldData.getAcqRepsSinceLapse();
        int newRetRepsSinceLapse = oldData.getRetRepsSinceLapse();
        float newEasiness = oldData.getEasiness();

        if(actualInterval <= parameters.getMinimalInterval()){
            actualInterval = parameters.getMinimalInterval();
        }
        // new item (unseen = 1 in mnemosyne)
        if(newAcqReps == 0) {
            newAcqReps = 1;
            newEasiness = parameters.getInitialEasiness();
            newAcqRepsSinceLapse = 1;
            newInterval = calculateInitialInterval(newGrade);
        } else if(!isGradeSuccessful(oldGrade, false)  && !isGradeSuccessful(newGrade, false)) {
            newAcqReps += 1;
            newAcqRepsSinceLapse += 1;
            newInterval = 0;
        } else if(!isGradeSuccessful(oldGrade, false) && isGradeSuccessful(newGrade, false)){
            newAcqReps += 1;
            newAcqRepsSinceLapse += 1;
            newInterval = parameters.getFailedGradingInterval(newGrade);
        } else if(isGradeSuccessful(oldGrade, false) && !isGradeSuccessful(newGrade, false)){
            newRetReps += 1;
            newLapses += 1;
            newAcqRepsSinceLapse = 0;
            newRetRepsSinceLapse = 0;
            newInterval = 0;
        } else if (isGradeSuccessful(oldGrade, false) && isGradeSuccessful(newGrade, false)) {
            newRetReps += 1;
            newRetRepsSinceLapse += 1;
            newInterval = 0;
            if (actualInterval >= scheduleInterval) {
                newEasiness = oldEasiness + parameters.getEasinessIncremental(newGrade);

                if (newEasiness < parameters.getMinimalEasiness()) {
                    newEasiness =  parameters.getMinimalEasiness();
                }

                if(actualInterval <= scheduleInterval){
                    newInterval = actualInterval * newEasiness;
                    // Fix the cram review scheduling problem by using the larger of scheduled interval
                    newInterval = actualInterval * newEasiness > scheduleInterval ? actualInterval * newEasiness : scheduleInterval;
                } else {
                    newInterval = scheduleInterval * newEasiness;
                }
            } else {
                // If learning a card before it is cheduled
                // The old interval is used.
                newInterval = scheduleInterval;
            }

            if (newInterval <= parameters.getMinimalInterval()) {
                Ln.w("Interval " + newInterval + " is less than "
                        + parameters.getMinimalInterval()
                        + " for old data: " + oldData);
                newInterval = parameters.getMinimalInterval();
            }
        }
        /*
         * By default the noise is included. However,
         * the estimation of days should not include noise
         * If the noise is disabled in the algorithm customization.
         */
        if(includeNoise && parameters.getEnableNoise()){
            newInterval = newInterval + calculateIntervalNoise(newInterval);
        }

        LearningData newData = new LearningData();
        newData.setId(oldData.getId());
        newData.setAcqReps(newAcqReps);
        newData.setAcqRepsSinceLapse(newAcqRepsSinceLapse);
        newData.setEasiness(newEasiness);
        newData.setGrade(newGrade);
        newData.setLapses(newLapses);
        newData.setLastLearnDate(currentDate);
        newData.setNextLearnDate(afterDays(currentDate, newInterval));
        newData.setRetReps(newRetReps);
        newData.setRetRepsSinceLapse(newRetRepsSinceLapse);
        return newData;
    }

    /*
     * This method returns true if the card should not
     * be repeated immediately. False if it need to be
     * repeaeted immediately.
     */
    @Override
    public boolean isCardLearned(LearningData data) {
        if (isCardNew(data)) {
            return false;
        } else if (data.getGrade() < 2) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isCardNew(LearningData data) {
        return data.getAcqReps() == 0;
    }

    @Override
    public boolean isCardForReview(LearningData data) {
        if (isCardNew(data)) {
            return false;
        }
        return data.getNextLearnDate().compareTo(new Date()) <= 0;
    }

    /*
     * Whether a grade is considered successful not not.
     * Unsuccessful grade should be put in the queue.
     */
    private boolean isGradeSuccessful(int grade, boolean isNew) {
        if (isNew) {
            return parameters.getInitialInterval(grade) >= parameters.getMinimalInterval();
        } else {
            return parameters.getFailedGradingInterval(grade) >= parameters.getMinimalInterval();
        }
    }

    /*
     * interval is in Day.
     */
    private double calculateIntervalNoise(double interval){
        // Noise value based on Mnymosyne
        double noise = 0.0;

        if(interval <= parameters.getMinimalInterval()){
            noise = 0.0;
        } else if(interval <= 1.99999){
            noise = randomNumber(0.0, 1.0);
        } else if(interval <= 10.0){
            noise = randomNumber(-1.0, 1.0);
        } else if(interval <= 60.0){
            noise = randomNumber(-3.0, 3.0);
        } else {
            noise = randomNumber(-0.05 * interval, 0.05 * interval);
        }
        return noise;
    }

    private float calculateInitialInterval(int grade){
        return parameters.getInitialInterval(grade);
    }

    private double randomNumber(double min, double max){
        return min + (new Random()).nextDouble() * (max - min);
    }

    private Date afterDays(Date date, double days) {
        long time = date.getTime() + Math.round(days * 1 * 60 * 60 * 24 * 1000 /* 1 day */);
        return new Date(time);
    }

}
