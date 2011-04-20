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
package org.liberty.android.fantastischmemo;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.io.Serializable;


import android.util.Log;
import android.os.Parcelable;
import android.os.Parcel;

/* 
 * This class representating the card item is immutable
 */
public final class Item implements Parcelable, Comparable<Item>{
	private final int _id;
	private final String date_learn;
	private final int interval;
	private final int grade;
	private final double easiness;
	private final int acq_reps;
	private final int ret_reps;
	private final int lapses;
	private final int acq_reps_since_lapse;
	private final int ret_reps_since_lapse;
	private final String question;
	private final String answer;
	private final String note;
    private final String category;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private final static String TAG = "org.liberty.android.fantastischmemo.Item";
	
	public static class Builder{
		private int _id = 0;
		private String date_learn = "2010-01-01";
		private int interval = 0;
		private int grade = 0;
		private double easiness = 2.5;
		private int acq_reps = 0;
		private int ret_reps = 0;
		private int lapses = 0;
		private int acq_reps_since_lapse = 0;
		private int ret_reps_since_lapse = 0;
		private String  question = "";
		private String  answer = "";
		private String  note = "";
		private String  category = "";

        public Builder(){}
        
        public Builder(Item item){
            _id = item._id;
            date_learn = item.date_learn;
            interval = item.interval;
            grade = item.grade;
            easiness = item.easiness;
            acq_reps = item.acq_reps;
            ret_reps = item.ret_reps;
            lapses = item.lapses;
            acq_reps_since_lapse = item.acq_reps_since_lapse;
            ret_reps_since_lapse = item.ret_reps_since_lapse;
            question = item.question;
            answer = item.answer;
            note = item.note;
            category = item.category;
        }

        public Builder setId(int v){
            _id = v;
            return this;
        }

        public Builder setDateLearn(String v){
            date_learn = v;
            return this;
        }

        public Builder setInterval(int v){
            interval = v;
            return this;
        }

        public Builder setGrade(int v){
            grade = v;
            return this;
        }

        public Builder setEasiness(double v){
            easiness = v;
            return this;
        }

        public Builder setAcqReps(int v){
            acq_reps = v;
            return this;
        }

        public Builder setRetReps(int v){
            ret_reps = v;
            return this;
        }

        public Builder setLapses(int v){
            lapses = v;
            return this;
        }

        public Builder setAcqRepsSinceLapse(int v){
            acq_reps_since_lapse = v;
            return this;
        }

        public Builder setRetRepsSinceLapse(int v){
            ret_reps_since_lapse = v;
            return this;
        }

        public Builder setQuestion(String v){
            question = v;
            return this;
        }

        public Builder setAnswer(String v){
            answer = v;
            return this;
        }

        public Builder setNote(String v){
            note = v;
            return this;
        }

        public Builder setCategory(String v){
            category = v;
            return this;
        }
        public Item build(){
            return new Item(this);
        }
    }

    private Item(Builder builder){
        _id = builder._id;
        date_learn = builder.date_learn;
        interval = builder.interval;
        grade = builder.grade;
        easiness = builder.easiness;
        acq_reps = builder.acq_reps;
        ret_reps = builder.ret_reps;
        lapses = builder.lapses;
        acq_reps_since_lapse = builder.acq_reps_since_lapse;
        ret_reps_since_lapse = builder.ret_reps_since_lapse;
        question = builder.question;
        answer = builder.answer;
        note = builder.note;
        category = builder.category;
    }
    /* Parcelable requirement */
    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeInt(_id);
        out.writeString(date_learn);
        out.writeInt(interval);
        out.writeInt(grade);
        out.writeDouble(easiness);
        out.writeInt(acq_reps);
        out.writeInt(ret_reps);
        out.writeInt(lapses);
        out.writeInt(acq_reps_since_lapse);
        out.writeInt(ret_reps_since_lapse);
        out.writeString(question);
        out.writeString(answer);
        out.writeString(note);
        out.writeString(category);
    }

     public static final Parcelable.Creator<Item> CREATOR
             = new Parcelable.Creator<Item>() {
         public Item createFromParcel(Parcel in) {
             return new Item.Builder()
                 .setId(in.readInt())
                 .setDateLearn(in.readString())
                 .setInterval(in.readInt())
                 .setGrade(in.readInt())
                 .setEasiness(in.readDouble())
                 .setAcqReps(in.readInt())
                 .setRetReps(in.readInt())
                 .setLapses(in.readInt())
                 .setAcqRepsSinceLapse(in.readInt())
                 .setRetRepsSinceLapse(in.readInt())
                 .setQuestion(in.readString())
                 .setAnswer(in.readString())
                 .setNote(in.readString())
                 .setCategory(in.readString())
                 .build();
         }

         public Item[] newArray(int size) {
             return new Item[size];
         }
     };

     /* End of parcelable requirement */

    public int getId(){
        return _id;
    }

    public String getDateLearn(){
        return date_learn;
    }

    public int getInterval(){
        return interval;
    }

    public double getEasiness(){
        return easiness;
    }

    public int getAcqReps(){
        return acq_reps;
    }

    public int getRetReps(){
        return ret_reps;
    }

    public int getLapses(){
        return lapses;
    }

    public int getAcqRepsSinceLapse(){
        return acq_reps_since_lapse;
    }

    public int getRetRepsSinceLapse(){
        return ret_reps_since_lapse;
    }

    public int getGrade(){
        return grade;
    }

    public String getQuestion(){
        return question;
    }

    public String getAnswer(){
        return answer;
    }

    public String getNote(){
        return note;
    }

    public String getCategory(){
        return category;
    }

    public long getDatelearnUnix() throws ParseException{
        // Get the datelearn in unix time * 1000
		Date date = formatter.parse(this.date_learn);
        return date.getTime() / 1000;
    }

	public boolean isNew(){
		return acq_reps == 0 ? true : false;
	}



	public boolean isScheduled(){
		Date currentDate = new Date();
		String now = formatter.format(currentDate);
		int actualInterval = diffDate(now, this.date_learn);
		int scheduleInterval = this.interval;
		//actualInterval = actualInterval == 0 ? actualInterval + 1 : actualInterval;
		if(scheduleInterval <= actualInterval && this.acq_reps > 0){
			return true;
		}
		else{
			return false;
		}
	}

	public Item processAnswer(int newGrade, boolean includeNoise){
        // dryRun will leave the original one intact
        // and return the interval
        // if dryRun is false, the return value only show success or not
		Date currentDate = new Date();
		String now = formatter.format(currentDate);
		int actualInterval = diffDate(now, this.date_learn);
		int scheduleInterval = this.interval;
		int newInterval = 0;
        int newLapses = lapses;
        int newAcqReps = acq_reps;
        int newRetReps = ret_reps;
        int newAcqRepsSinceLapse = acq_reps_since_lapse;
        int newRetRepsSinceLapse = ret_reps_since_lapse;
        double newEasiness = easiness;

		if(actualInterval == 0){
			actualInterval = 1;
		}
        // new item (unseen = 1 in mnemosyne)
		if(this.acq_reps == 0){
			newAcqReps = 1;
            // 2.5 is 40% difficult.
            // Taken from Mnemosyne
            newEasiness = 2.5;
            newAcqRepsSinceLapse = 1;
			newInterval = calculateInitialInterval(newGrade);
		}
		else if(this.grade <= 1 && newGrade <= 1){
			newAcqReps += 1;
			newAcqRepsSinceLapse += 1;
			newInterval = 0;
		}
		else if(this.grade <= 1 && newGrade >= 2){
			newAcqReps += 1;
			newAcqRepsSinceLapse += 1;
			newInterval = 1;
		}
		else if(this.grade >= 2 && newGrade <= 1){
			newRetReps += 1;
			newLapses += 1;
			newAcqRepsSinceLapse = 0;
			newRetRepsSinceLapse = 0;
		}
		else if(this.grade >= 2 && newGrade >= 2){
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
				if(this.easiness < 1.3){
					newEasiness = 1.3;
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
		}
        /* 
         * By default the noise is included. However, 
         * the estimation of days should not include noise
         */ 
        if(includeNoise){
		    int noise = calculateIntervalNoise(newInterval);
            newInterval = newInterval + noise;
        }
        Item retItem = new Builder(this)
            .setInterval(newInterval)
            .setDateLearn(now)
            .setLapses(newLapses)
            .setAcqReps(newAcqReps)
            .setRetReps(newRetReps)
            .setAcqRepsSinceLapse(newAcqRepsSinceLapse)
            .setRetRepsSinceLapse(newRetRepsSinceLapse)
            .setEasiness(newEasiness)
            .setInterval(newInterval)
            .setGrade(newGrade)
            .build();
        return retItem;
	}


    public boolean isEmpty(){
        if(question.equals("") && answer.equals("") && category.equals("")){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean containsHTML(){
        return AMUtil.isHTML(question) || AMUtil.isHTML(answer);
    }

    public Item inverseQA(){
        Item newItem = new Builder(this)
            .setQuestion(this.answer)
            .setAnswer(this.question)
            .build();
        return newItem;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Item)){
            return false;
        }
        Item oi = (Item)o;
        if(_id == oi._id && question.equals(oi.question) && answer.equals(oi.answer)){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public String toString(){
        return "ID: " + _id + " Question: " + question + " Answer: " + answer;
    }

    @Override
    public int compareTo(Item another){
        return _id - another._id;
    }

	private int randomNumber(int min, int max){
		return min + (new Random()).nextInt(max - min + 1);
	}
	private int calculateIntervalNoise(int interval){
        // Noise value based on Mnymosyne
		int noise;
		if(interval == 0){
			noise = 0;
		}
		else if(interval == 1){
			noise = randomNumber(0, 1);
		}
		else if(interval <= 10){
			noise = randomNumber(-1, 1);
		}
		else if(interval <= 60){
			noise = randomNumber(-3, 3);
		}
		else{
			noise = randomNumber((int)Math.round(-0.05 * interval), (int)Math.round(0.05 * interval));
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
	
	private int diffDate(String date1, String date2){
        // The days betwween to date of date1 and date2 in format below.
		final double MILLSECS_PER_DAY = 86400000.0;
		Date d1, d2;
		int difference = 0;
		try{
			d1 = formatter.parse(date1);
			d2 = formatter.parse(date2);
			difference = (int)Math.round((d1.getTime() - d2.getTime()) / MILLSECS_PER_DAY);
		}
		catch(Exception e){
			Log.e("diffDate parse error!", e.toString());
		}
		return difference;
	}


}
