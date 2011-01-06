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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.io.Serializable;


import android.util.Log;

public final class Item implements Cloneable, Serializable{
	private int _id;
	private String date_learn;
	private Date date_learn_as_date;
	private int interval;
	private int grade;
	private double easiness;
	private int acq_reps;
	private int ret_reps;
	private int lapses;
	private int acq_reps_since_lapse;
	private int ret_reps_since_lapse;
	private String question;
	private String answer;
	private String note;
    private String category;
    public final static String TAG = "org.liberty.android.fantastischmemo.Item";
    // make the formatter final static - created once instead of each time
    // which caused some delay.
    private final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private final static CachedCalendar now = new CachedCalendar();
	
	public Item(){
		this._id = 0;
		setDateLearn("2010-01-01");
		this.interval = 0;
		this.grade = 0;
		this.easiness = 2.5;
		this.acq_reps = 0;
		this.ret_reps = 0;
		this.lapses = 0;
		this.acq_reps_since_lapse = 0;
		this.ret_reps_since_lapse = 0;
		this.question = "";
		this.answer = "";
		this.note = "";
		this.category = "";
	}
	public String getQuestion(){
		return this.question;
	}

    public void setQuestion(String q){
        this.question = q;
    }
	
	public String getAnswer(){
		return this.answer;
	}

    public void setAnswer(String a){
        this.answer = a;
    }

    
    public int getInterval(){
        return this.interval;
    }

    public int getGrade(){
        return grade;
    }

    public long getDatelearnUnix() throws ParseException{
        // Get the datelearn in unix time * 1000
		Date date = formatter.parse(this.getDateLearn());
        return date.getTime() / 1000;
    }



	public String getNote(){
		return this.note;
	}

    public void setNote(String n){
        this.note = n;
    }

    public String getCategory(){
        return this.category;
    }

    public void setCategory(String c){
        this.category = c;
    }
	
	public int getId(){
		return this._id;
	}

    public void setId(int id){
        this._id = id;
    }

	public boolean isNew(){
		return acq_reps == 0 ? true : false;
	}

    public void skip(){
        /* This will set a super long interval so it will never appear */
        interval = 100000;
        grade = 5;
        /* I have seen it */
        acq_reps = 1;
        easiness = 10.0;
    }

    public Item clone(){
        Item itemClone = new Item();
        itemClone._id = this._id;
        itemClone.date_learn = this.getDateLearn();
        itemClone.date_learn_as_date = this.getDateLearnAsDate();
        itemClone.interval = this.interval;
        itemClone.grade = this.grade;
        itemClone.easiness = this.easiness;
        itemClone.acq_reps = this.acq_reps;
        itemClone.ret_reps = this.ret_reps;
        itemClone.lapses = this.lapses;
        itemClone.acq_reps_since_lapse = this.acq_reps_since_lapse;
        itemClone.ret_reps_since_lapse = this.ret_reps_since_lapse;
        itemClone.question = this.question;
        itemClone.answer = this.answer;
        itemClone.note = this.note;
        itemClone.category = this.category;

        return itemClone;

    }
	
	public String[] getLearningData(){
		// the string array is in the sequence that is required in the DatabaseHelper.updateItem
		return new String[]{getDateLearn(), new Integer(interval).toString(), new Integer(grade).toString(), new Double(easiness).toString(), new Integer(acq_reps).toString(), new Integer(ret_reps).toString(), new Integer(lapses).toString(), new Integer(acq_reps_since_lapse).toString(), new Integer(ret_reps_since_lapse).toString(), new Integer(_id).toString()};
	}
	
	public void setData(HashMap<String, String> hm){
		Set<Map.Entry<String, String>> set = hm.entrySet();
		Iterator<Map.Entry<String, String> > i = set.iterator();
		while(i.hasNext()){
			Map.Entry<String, String> me = (Map.Entry<String, String>)i.next();
			if(((String)me.getKey()) == "_id"){
				this._id = Integer.parseInt(hm.get("_id")); 
			}
			if(((String)me.getKey()) == "date_learn"){
				setDateLearn(hm.get("date_learn"));
			}
			if(((String)me.getKey()) == "interval"){
				this.interval = Integer.parseInt(hm.get("interval")); 
			}
			if(((String)me.getKey()) == "grade"){
				this.grade = Integer.parseInt(hm.get("grade")); 			}
			if(((String)me.getKey()) == "easiness"){
				this.easiness = Double.parseDouble(hm.get("easiness"));
				
			}
			if(((String)me.getKey()) == "lapses"){
				this.lapses = Integer.parseInt(hm.get("lapses")); 
			}
			if(((String)me.getKey()) == "acq_reps"){
				this.acq_reps =Integer.parseInt(hm.get("acq_reps")); 
			}
			if(((String)me.getKey()) == "ret_reps"){
				this.ret_reps = Integer.parseInt(hm.get("ret_reps")); 
			}
			if(((String)me.getKey()) == "acq_reps_since_lapse"){
				this.acq_reps_since_lapse = Integer.parseInt(hm.get("acq_reps_since_lapse")); 
				
			}
			if(((String)me.getKey()) == "ret_reps_since_lapse"){
				this.ret_reps_since_lapse = Integer.parseInt(hm.get("ret_reps_since_lapse")); 
			}
			if(((String)me.getKey()) == "question"){
				this.question = hm.get("question");
			}
			if(((String)me.getKey()) == "answer"){
				this.answer = hm.get("answer");
				
			}
			if(((String)me.getKey()) == "note"){
				this.note = (String)hm.get("note");
				
			}
			if(((String)me.getKey()) == "category"){
				this.category = (String)hm.get("category");
				
			}
			
		}
		
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
	
	// diffDate replaced by this one
	private int getActualInterval(){
		final double MILLSECS_PER_DAY = 86400000.0;
		
		return (int)Math.round((now.getTimeInMillis() - getDateLearnAsDate().getTime()) / MILLSECS_PER_DAY);
	}
	
	public boolean isScheduled(){
		int actualInterval = getActualInterval();
		int scheduleInterval = this.interval;
		//actualInterval = actualInterval == 0 ? actualInterval + 1 : actualInterval;
		if(scheduleInterval <= actualInterval && this.acq_reps > 0){
			return true;
		}
		else{
			return false;
		}
		
	}

	public int processAnswer(int newGrade, boolean dryRun){
        int actualInterval = getActualInterval();
		int scheduleInterval = this.interval;
		int newInterval = 0;
        Item cloneItem = null;
        if(dryRun){
            try{
                cloneItem = (Item)this.clone();
            }
            catch(Exception e){
                Log.e(TAG, "Error cloning", e);
                cloneItem = null;
            }
        }
		boolean returnValue = false;
		if(actualInterval == 0){
			actualInterval = 1;
		}
        // new item (unseen = 1 in mnemosyne)
		if(this.acq_reps == 0){
			this.acq_reps = 1;
            // 2.5 is 40% difficult.
            // Taken from Mnemosyne
            this.easiness = 2.5;
			this.acq_reps_since_lapse = 1;
			newInterval = calculateInitialInterval(newGrade);
			if(newGrade >= 2){
				returnValue = true;
			}
		}
		else if(this.grade <= 1 && newGrade <= 1){
			this.acq_reps += 1;
			this.acq_reps_since_lapse += 1;
			newInterval = 0;
		}
		else if(this.grade <= 1 && newGrade >= 2){
			this.acq_reps += 1;
			this.acq_reps_since_lapse += 1;
			newInterval = 1;
			returnValue = true;
		}
		else if(this.grade >= 2 && newGrade <= 1){
			this.ret_reps += 1;
			this.lapses += 1;
			this.acq_reps_since_lapse = 0;
			this.ret_reps_since_lapse = 0;
			returnValue = false;
		}
		else if(this.grade >= 2 && newGrade >= 2){
			this.ret_reps += 1;
			this.ret_reps_since_lapse += 1;
			returnValue = true;
			if(actualInterval >= scheduleInterval){
				if(newGrade == 2){
					this.easiness -= 0.16;
				}
				if(newGrade == 3){
					this.easiness -= 0.14;
				}
				if(newGrade == 5){
					this.easiness += 0.10;
				}
				if(this.easiness < 1.3){
					this.easiness = 1.3;
				}
			}
			newInterval = 0;
			if(this.ret_reps_since_lapse == 1){
				newInterval = 6;
			}
			else{
				if(newGrade == 2 || newGrade == 3){
					if(actualInterval <= scheduleInterval){
						newInterval = (int)Math.round(actualInterval * this.easiness);
					}
					else{
						newInterval = scheduleInterval;
					}
				}
				if(newGrade == 4){
					newInterval = (int)Math.round(actualInterval * this.easiness);
				}
				if(newGrade == 5){
					if(actualInterval < scheduleInterval){
						newInterval = scheduleInterval;
					}
					else{
						newInterval = (int)Math.round(actualInterval * this.easiness);
					}
				}
			}
			if(newInterval == 0){
				Log.e("Interval error", "Interval is 0 in wrong place");
			}
		}
        if(dryRun == true){
            // dryRun does not include the noise in the return value!!
            if(cloneItem != null){
                this.interval = cloneItem.interval;
                this._id = cloneItem._id;
                this.date_learn = cloneItem.getDateLearn();
                this.date_learn_as_date = cloneItem.getDateLearnAsDate();
                this.interval = cloneItem.interval;
                this.grade = cloneItem.grade;
                this.easiness = cloneItem.easiness;
                this.acq_reps = cloneItem.acq_reps;
                this.ret_reps = cloneItem.ret_reps;
                this.lapses = cloneItem.lapses;
                this.acq_reps_since_lapse = cloneItem.acq_reps_since_lapse;
                this.ret_reps_since_lapse = cloneItem.ret_reps_since_lapse;
                this.question = cloneItem.question;
                this.answer = cloneItem.answer;
                this.note = cloneItem.note;
            }
            return newInterval;
        }
        else{
		    int noise = calculateIntervalNoise(newInterval);
            this.interval = newInterval + noise;
            this.grade = newGrade;
            setDateLearn(formatter.format(new Date()));
            // 1 means success ,0 means fail
            return returnValue ? 1 : 0;
        }
	}

    public void inverseQA(){
        String tmp = answer;
        answer = question;
        question = tmp;
    }

    public boolean isEmpty(){
        if(question.equals("") && answer.equals("") && category.equals("")){
            return true;
        }
        else{
            return false;
        }
    }
	private String getDateLearn() {
		return date_learn;
	}
	
	private void setDateLearn(String date_learn) {
		this.date_learn = date_learn;
		this.date_learn_as_date = null; // invalidate
	}
	
	private Date getDateLearnAsDate() {
		if (date_learn_as_date == null) {
			try{
				date_learn_as_date = formatter.parse(getDateLearn());
			}
			catch(Exception e){
				Log.e("diffDate parse error!", e.toString());
				date_learn_as_date = new Date();
			}
		}

		return date_learn_as_date;
	}

	private static class CachedCalendar {
		private long validTill;
		private long nowInMiliseconds;
		
		public CachedCalendar() {
			calculate();
		}

		public long getTimeInMillis() {
			if (System.currentTimeMillis() > validTill)
				calculate();
			
			return nowInMiliseconds;
		}
		
		private void calculate() {
			Calendar now = Calendar.getInstance();
			now.set(Calendar.HOUR_OF_DAY, 23);
			now.set(Calendar.MINUTE, 59);
			now.set(Calendar.SECOND, 59);
			now.set(Calendar.MILLISECOND,999);
			validTill = now.getTimeInMillis();
			
			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.SECOND, 0);
			nowInMiliseconds = now.getTimeInMillis();
		}
		
	}
	
}
