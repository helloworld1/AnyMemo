package org.liberty.android.fantasisichmemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.http.ParseException;

import android.util.Log;

public final class Item {
	private int _id;
	private String date_learn;
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
	
	public Item(){
		this._id = 0;
		this.date_learn = "2010-01-01";
		this.interval = 0;
		this.grade = 0;
		this.easiness = 0.0;
		this.acq_reps = 0;
		this.ret_reps = 0;
		this.lapses = 0;
		this.acq_reps_since_lapse = 0;
		this.ret_reps_since_lapse = 0;
		this.question = "";
		this.answer = "";
		this.note = "";
	}
	public String getQuestion(){
		return this.question;
	}
	
	public String getAnswer(){
		return this.answer;
	}
	
	public String getNote(){
		return this.note;
	}
	
	public int getId(){
		return this._id;
	}
	public boolean isNew(){
		return acq_reps == 0 ? true : false;
	}
	
	public String[] getLearningData(){
		// the string array is in the sequence that is required in the DatabaseHelper.updateItem
		return new String[]{date_learn, new Integer(interval).toString(), new Integer(grade).toString(), new Double(easiness).toString(), new Integer(acq_reps).toString(), new Integer(ret_reps).toString(), new Integer(lapses).toString(), new Integer(acq_reps_since_lapse).toString(), new Integer(ret_reps_since_lapse).toString(), new Integer(_id).toString()};
	}
	
	public void setData(HashMap hm){
		Set set = hm.entrySet();
		Iterator i = set.iterator();
		while(i.hasNext()){
			Map.Entry me = (Map.Entry)i.next();
			if(((String)me.getKey()) == "_id"){
				this._id = ((Integer)hm.get("_id")).intValue();
			}
			if(((String)me.getKey()) == "date_learn"){
				this.date_learn = (String)hm.get("date_learn");
			}
			if(((String)me.getKey()) == "interval"){
				this.interval = ((Integer)hm.get("interval")).intValue();
			}
			if(((String)me.getKey()) == "grade"){
				this.grade = ((Integer)hm.get("grade")).intValue();
			}
			if(((String)me.getKey()) == "grade"){
				this.easiness = ((Double)hm.get("easiness")).doubleValue();
				
			}
			if(((String)me.getKey()) == "acq_reps_since_lapse"){
				this.acq_reps_since_lapse = ((Integer)hm.get("acq_reps_since_lapse")).intValue();
				
			}
			if(((String)me.getKey()) == "ret_reps_since_lapse"){
				this.ret_reps_since_lapse = ((Integer)hm.get("ret_reps_since_lapse")).intValue();
				
			}
			if(((String)me.getKey()) == "question"){
				this.question = (String)hm.get("question");
				
			}
			if(((String)me.getKey()) == "answer"){
				this.answer = (String)hm.get("answer");
				
			}
			if(((String)me.getKey()) == "note"){
				this.note = (String)hm.get("note");
				
			}
			
		}
		
	}
	
	private int randomNumber(int min, int max){
		return min + (new Random()).nextInt(max - min);
	}
	private int calculateIntervalNoise(int interval){
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
		final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date d1, d2;
		int difference = 0;
		try{
			d1 = formatter.parse(date1);
			d2 = formatter.parse(date2);
			difference = (int)((d1.getTime() - d2.getTime()) / MILLSECS_PER_DAY);
		}
		catch(Exception e){
			Log.e("diffDate parse error!", e.toString());
		}
		return difference;
		
		
	}

	public boolean processAnswer(int newGrade){
		Date currentDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String now = formatter.format(currentDate);
		int actualInterval = diffDate(now, this.date_learn);
		int scheduleInterval = this.interval;
		int newInterval = 0;
		boolean returnValue = false;
		if(actualInterval == 0){
			actualInterval = 1;
		}
		if(this.acq_reps == 0){
			this.acq_reps = 1;
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
		else if(this.grade >= 2 && newGrade <= 2){
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
				if(grade == 2){
					this.easiness -= 0.16;
				}
				if(grade == 3){
					this.easiness -= 0.14;
				}
				if(grade == 5){
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
		int noise = calculateIntervalNoise(newInterval);
		this.interval = newInterval + noise;
		this.grade = newGrade;
		this.date_learn = now;
		return returnValue;
	}
	
}
