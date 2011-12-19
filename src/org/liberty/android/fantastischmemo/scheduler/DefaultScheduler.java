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

import org.liberty.android.fantastischmemo.domain.LearningData;

import android.util.Log;

public class DefaultScheduler {
    final double MILLSECS_PER_DAY = 86400000.0;

    /*
     * Return the interval of the after schedule the new card
     */
	public LearningData schedule(LearningData oldData, int newGrade, boolean includeNoise) {
		Date currentDate = new Date();
		double actualInterval = diffDate(oldData.getLastLearnDate(), currentDate);
		double scheduleInterval = diffDate(oldData.getLastLearnDate(), oldData.getNextLearnDate());
		double newInterval = 0.0;
        int oldGrade = oldData.getGrade();
        double oldEasiness = oldData.getEasiness();
        int newLapses = oldData.getLapses();
        int newAcqReps = oldData.getAcqReps();
        int newRetReps = oldData.getRetReps();
        int newAcqRepsSinceLapse = oldData.getAcqRepsSinceLapse();
        int newRetRepsSinceLapse = oldData.getRetRepsSinceLapse();
        float newEasiness = oldData.getEasiness();

		if(actualInterval == 0){
			actualInterval = 1;
		}
        // new item (unseen = 1 in mnemosyne)
		if(newAcqReps == 0) {
			newAcqReps = 1;
            // 2.5 is 40% difficult.
            // Taken from Mnemosyne
            newEasiness = 2.5f;
            newAcqRepsSinceLapse = 1;
			newInterval = calculateInitialInterval(newGrade);
		} else if(oldGrade <= 1 && newGrade <= 1){
			newAcqReps += 1;
			newAcqRepsSinceLapse += 1;
			newInterval = 0;
		} else if(oldGrade <= 1 && newGrade >= 2){
			newAcqReps += 1;
			newAcqRepsSinceLapse += 1;
			newInterval = 1;
		} else if(oldGrade >= 2 && newGrade <= 1){
			newRetReps += 1;
			newLapses += 1;
			newAcqRepsSinceLapse = 0;
			newRetRepsSinceLapse = 0;
		} else if(oldGrade >= 2 && newGrade >= 2){
			newRetReps += 1;
			newRetRepsSinceLapse += 1;
			if(actualInterval >= scheduleInterval){
				if(newGrade == 2){
					newEasiness -= 0.16;
				}
				if(newGrade == 3){
					newEasiness -= 0.14;
				}
				if(newGrade == 5){
				    newEasiness += 0.10;
				}
				if(oldEasiness < 1.3){
					newEasiness = 1.3f;
				}
			}
			newInterval = 0;
			if(newRetRepsSinceLapse == -1){
				newInterval = 6;
			}
			else{
				if(newGrade == 2 || newGrade == 3){
					if(actualInterval <= scheduleInterval){
						newInterval = (int)Math.round(actualInterval * newEasiness);
					}
					else{
						newInterval = scheduleInterval;
					}
				}
				if(newGrade == 4){
					newInterval = (int)Math.round(actualInterval * newEasiness);
				}
				if(newGrade == 5){
					if(actualInterval < scheduleInterval){
						newInterval = scheduleInterval;
					}
					else{
						newInterval = (int)Math.round(actualInterval * newEasiness);
					}
				}
			}
			if(newInterval == 0){
				Log.e("Interval error", "Interval is 0 in wrong place");
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
         * By default the noise is included. However, 
         * the estimation of days should not include noise
         */ 
        if(includeNoise){
            newInterval = newInterval + calculateIntervalNoise(newInterval);
        }
        return null;
	}

    /*
     * interval is in Day.
     */
	private double calculateIntervalNoise(double interval){
        // Noise value based on Mnymosyne
		double noise;
		if(interval < 0.999999){
			noise = 0.0;
		}
		else if(interval >= 0.999999){
			noise = randomNumber(0.0, 1.0);
		}
		else if(interval <= 10.0){
			noise = randomNumber(-1.0, 1.0);
		}
		else if(interval <= 60.0){
			noise = randomNumber(-3.0, 3.0);
		}
		else{
			noise = randomNumber(-0.05 * interval, 0.05 * interval);
		}
		return noise;
	}
	
	private int calculateInitialInterval(int grade){
		int interval = 0;
		switch(grade){
		case 0:
		case 1:
			interval = 0;
			break;
		case 2:
			interval = 1;
			break;
		case 3:
			interval = 3;
			break;
		case 4:
			interval = 4;
			break;
		case 5:
			interval = 5;
			break;
		}
		return interval;
	}
	

    /* Difference in days between date1 and date2*/
	private double diffDate(Date date1, Date date2){
        double date1s = date1.getTime();
        double date2s = date2.getTime();
        return (date2s - date1s) / MILLSECS_PER_DAY; 
	}

	private double randomNumber(double min, double max){
		return min + (new Random()).nextGaussian() * (max - min);
	}

    private Date afterDays(Date date, double days) {
        long time = date.getTime() + Math.round(days * MILLSECS_PER_DAY);
        return new Date(time);
    }

}
