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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;


/*
 * This class include the most low level database operation
 * the DatabaseUtility wrap this class to provide more database
 * global operations
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private final String dbPath;
	private final String dbName;
	private SQLiteDatabase myDatabase;
	private final Context mContext;
    private static final String TAG = "org.liberty.android.fantastischmemo.DatabaseHelper";
		
	public DatabaseHelper(Context context, String path, String name){
		super(context, name, null, 1);
		dbPath = path;
		dbName = name;
		mContext = context;
        /* So if the database does not exist, it will create a new one */
        openDatabase();
        if(!checkDatabase()){
            throw new SQLException("Database check failed.");
        }
	}
	
	public void createDatabase() throws IOException{
        File dbfile = new File(dbPath + "/" + dbName);
		if(dbfile.exists()){
		}
		else{
			this.getReadableDatabase();
            copyDatabase();
		}
	}
	
	public static void createEmptyDatabase(String path, String name) throws IOException, SQLException{
        File dbfile = new File(path + "/" + name);
		if(dbfile.exists()){
			// throw new IOException("DB already exist");
            /* Create a backup and overwrite  it instead poping up an error */
            File backupFile = new File(dbfile.getAbsolutePath().replaceAll(".db$", ".old.db"));
            if(backupFile.exists()){
                backupFile.delete();
            }
            dbfile.renameTo(backupFile);
		}
        SQLiteDatabase database = SQLiteDatabase.openDatabase(path + "/" + name, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
		
		database.execSQL("CREATE TABLE dict_tbl(_id INTEGER PRIMARY KEY ASC AUTOINCREMENT, question TEXT, answer TEXT, note TEXT, category TEXT)");
		database.execSQL("CREATE TABLE learn_tbl(_id INTEGER PRIMARY KEY ASC AUTOINCREMENT, date_learn, interval, grade INTEGER, easiness REAL, acq_reps INTEGER, ret_reps INTEGER, lapses INTEGER, acq_reps_since_lapse INTEGER, ret_reps_since_lapse INTEGER)");
		database.execSQL("CREATE TABLE control_tbl(ctrl_key TEXT, value TEXT)");
		
		database.beginTransaction();
		try{
			database.execSQL("DELETE FROM learn_tbl");
			database.execSQL("INSERT INTO learn_tbl(_id) SELECT _id FROM dict_tbl");
			database.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 2.5, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
			database.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('question_locale', 'US')");
			database.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('answer_locale', 'US')");
			database.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('question_align', 'center')");
			database.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('answer_align', 'center')");
			database.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('question_font_size', '24')");
			database.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('answer_font_size', '24')");
			database.setTransactionSuccessful();
		}
		finally{
			database.endTransaction();
            database.close();
		}
		
	}
	public void createDatabaseFromList(List<String> questionList, List<String> answerList, List<String> categoryList, List<String> datelearnList, List<Integer> intervalList, List<Double> easinessList, List<Integer> gradeList, List<Integer> lapsesList, List<Integer> acrpList, List<Integer> rtrpList, List<Integer> arslList, List<Integer> rrslList) throws IOException{
		
		ListIterator<String> liq = questionList.listIterator();
		ListIterator<String> lia = answerList.listIterator();
		ListIterator<String> lic = categoryList.listIterator();
		ListIterator<String> liDatelearn = datelearnList.listIterator();
		ListIterator<Integer> liInterval = intervalList.listIterator();
		ListIterator<Double> liEasiness = easinessList.listIterator();
		ListIterator<Integer> liGrade = gradeList.listIterator();
		ListIterator<Integer> liLapses = lapsesList.listIterator();
		ListIterator<Integer> liAcrp = acrpList.listIterator();
		ListIterator<Integer> liRtrp = rtrpList.listIterator();
		ListIterator<Integer> liArsl = arslList.listIterator();
		ListIterator<Integer> liRrsl = rrslList.listIterator();
        String date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse;

		myDatabase.beginTransaction();
		try{
			while(liq.hasNext() && lia.hasNext()){
				String category;
				if(lic.hasNext()){
					category = lic.next();
				}
				else{
					category = "";
				}
                if(questionList.size() == arslList.size()){
                    date_learn = liDatelearn.next();
                    interval = liInterval.next().toString();
                    grade = liGrade.next().toString();
                    easiness = liEasiness.next().toString();
                    acq_reps = liAcrp.next().toString();
                    ret_reps = liRtrp.next().toString();
                    lapses = liLapses.next().toString();
                    acq_reps_since_lapse = liArsl.next().toString();
                    ret_reps_since_lapse = liRrsl.next().toString();
                }
                else{
                    date_learn = "2010-01-01";
                    interval = "0";
                    grade = "0";
                    easiness = "2.5";
                    acq_reps = "0";
                    ret_reps = "0";
                    lapses = "0";
                    acq_reps_since_lapse = "0";
                    ret_reps_since_lapse = "0";
                }
				myDatabase.execSQL("INSERT INTO dict_tbl(question,answer,category) VALUES(?, ?, ?)", new String[]{liq.next(), lia.next(), category});

			    myDatabase.execSQL("INSERT INTO learn_tbl(date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse});
				
			}
			
			myDatabase.setTransactionSuccessful();
		}
		finally{
			myDatabase.endTransaction();
		}
		
	}

    public void insertListItems(List<Item> itemList){
		ListIterator<Item> li = itemList.listIterator();
		myDatabase.beginTransaction();
		try{
			while(li.hasNext()){
                addOrReplaceItem(li.next());
            }
			myDatabase.setTransactionSuccessful();
        }
        finally{
			myDatabase.endTransaction();
        }
    }
	
	public boolean checkDatabase(){
        boolean checkDB = false;
        try{
            /* Check dict_tbl */
            getNewId();
            /* Check learn_tbl */
            getTotalCount();
            checkDB = true;
        }
        catch(Exception e){
            checkDB = false;
        }

		return checkDB; 
	}
	
	private void copyDatabase() throws IOException{
		InputStream myInput = mContext.getAssets().open(dbName);
		String outFilename = dbPath + "/" + dbName;
		OutputStream myOutput = new FileOutputStream(outFilename);
		byte[] buffer = new byte[1024];
		int length;
		while((length = myInput.read(buffer)) > 0){
			myOutput.write(buffer, 0, length);
		}
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	public void openDatabase() throws SQLException{
		String myPath = dbPath + "/" + dbName;
		Cursor result;
		int count_dict = 0, count_learn = 0;
		myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        result = myDatabase.rawQuery("SELECT _id FROM dict_tbl", null);
        count_dict = result.getCount();
        result.close();
		
		if(count_dict == 0){
			return;
		}
		result = myDatabase.rawQuery("SELECT _id FROM learn_tbl", null);
		count_learn = result.getCount();
		result.close();
		if(count_learn != count_dict){
            /* Reconstruct learn_tbl if error found */
			this.myDatabase.execSQL("DELETE FROM learn_tbl");
			this.myDatabase.execSQL("INSERT INTO learn_tbl(_id) SELECT _id FROM dict_tbl");
			this.myDatabase.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 2.5, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
		}
		
	}

	public synchronized void close(){
		if(myDatabase != null){
			myDatabase.close();
		}
		super.close();
	}

	public void onCreate(SQLiteDatabase db){
		
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		
	}

	public List<Item> getListItems(int id, int windowSize, int flag, String filter){
        /* id: from which ID
         * list: the return list
         * ret: only ret items
         * flag = 0 means no condition
         * flag = 1 means new items, the items user have never seen (acq=0)
         * flag = 2 means item due, they need to be reviewed. (ret)
         * flag = 3 means items that is ahead of time (cram)
         * flag = 4 means both ret and acq items, but ret comes first
         * flag = 5: shuffle items no other condition
         */

		HashMap<String, String> hm = new HashMap<String, String>();
        List<Item> list = new ArrayList<Item>();
		Cursor result;

		String query = "SELECT learn_tbl._id, date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse, question, answer, note, category FROM dict_tbl INNER JOIN learn_tbl ON dict_tbl._id=learn_tbl._id WHERE dict_tbl._id >= " + id + " ";
		if(flag == 1){
			query += "AND acq_reps = 0 ";
		}
		else if(flag == 2 || flag == 4){
			query += "AND round((julianday(date('now', 'localtime')) - julianday(date_learn))) - interval >= 0 AND acq_reps > 0 ";
		}
        else if (flag == 3){
		    query = "SELECT learn_tbl._id, date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse, question, answer, note, category FROM dict_tbl INNER JOIN learn_tbl ON dict_tbl._id=learn_tbl._id WHERE round((julianday(date('now', 'localtime')) - julianday(date_learn))) - interval < 0 AND acq_reps > 0 ";
        }
        if(filter != null){
            if(Pattern.matches("#\\d+-\\d+", filter)){
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(filter);
                m.find();
                String min = m.group();
                m.find();
                String max = m.group();
                query += "AND learn_tbl._id >=" + min + " AND learn_tbl._id <= " + max + " ";
            }
            else if(Pattern.matches("#\\d+", filter)){
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(filter);
                m.find();
                String min = m.group();
                query += "AND learn_tbl._id >= " + min + " ";
            }
            else if(!filter.equals("")){
                /* Replace * and ? to % and _ used in SQL */
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");

                /* First remove white spaces at beginning */
                String realFilter = filter.replaceAll("^\\s+", "");

                String control = filter.length() >= 2 ? filter.substring(0, 2) : "";

                /* Also remove the control text */
                realFilter = realFilter.replaceAll("^%\\w\\s+", "");
                Log.v(TAG, "Control " + control);
                Log.v(TAG, "Filter " + realFilter);

                
                if(control.equals("%q")){
                    query += "AND ((question LIKE '" + realFilter + "')) ";
                }
                else if(control.equals("%a")){
                    query += "AND ((answer LIKE '" + realFilter + "')) ";
                }
                else if(control.equals("%n")){
                    query += "AND ((note LIKE '" + realFilter + "')) ";
                }
                else if(control.equals("%c")){
                    query += "AND ((category LIKE '" + realFilter + "')) ";
                }
                else{
                    query += "AND ((question LIKE '" + realFilter + "') OR (answer LIKE '" + realFilter + "') OR (note LIKE '" + realFilter + "') OR (category LIKE '" + realFilter +"')) ";
                }
            }
        }

        if(flag == 3 || flag == 5){
            query += "ORDER BY RANDOM() ";
        }
        if(windowSize >= 0){
            query += "LIMIT " + windowSize;
        }

		try {
			result = myDatabase.rawQuery(query, null);
		} catch (Exception e) {
			Log.e(TAG, "Query list items error", e);
			return null;
		}
		if (result.getCount() > 0) {
			result.moveToFirst();
			do {
				Item resultItem = new Item.Builder()
                    .setId(result.getInt(result.getColumnIndex("_id")))
				    .setQuestion(result.getString(result.getColumnIndex("question")))
				    .setAnswer(result.getString(result.getColumnIndex("answer")))
                    .setNote(result.getString(result.getColumnIndex("note")))
                    .setCategory(result.getString(result.getColumnIndex("category")))
				    .setDateLearn(result.getString(result.getColumnIndex("date_learn")))
                    .setInterval(result.getInt(result.getColumnIndex("interval")))
                    .setGrade(result.getInt(result.getColumnIndex("grade")))
                    .setEasiness(result.getDouble(result.getColumnIndex("easiness")))
                    .setAcqReps(result.getInt(result.getColumnIndex("acq_reps")))
                    .setRetReps(result.getInt(result.getColumnIndex("ret_reps")))
                    .setLapses(result.getInt(result.getColumnIndex("lapses")))
                    .setAcqRepsSinceLapse(result.getInt(result.getColumnIndex("acq_reps_since_lapse")))
			        .setRetRepsSinceLapse(result.getInt(result.getColumnIndex("ret_reps_since_lapse")))
                    .build();
				list.add(resultItem);
			} while (result.moveToNext());
		}
        result.close();
        if(flag == 4){
		    int remainingSize = windowSize - list.size();
            if(remainingSize > 0){
                /* Get the new items (acq = 0) */
                List<Item> retList = getListItems(id, remainingSize, 1, filter);
                list.addAll(retList);
            }
        }
        return list;
	}
	
	public Item getItemById(int id, int flag, boolean forward, String filter){
		// These function are related to read db operation
		// flag = 0 means no condition
		// flag = 1 means new items, the items user have never seen
		// flag = 2 means item due, they need to be reviewed.
        // flag = 3 means items that is ahead of time
        // filter = null or filter = "": no filter
        // filter = #numA-#numB, items between numA and numB
		HashMap<String, String> hm = new HashMap<String, String>();
		String query = "SELECT learn_tbl._id, date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse, question, answer, note, category FROM dict_tbl INNER JOIN learn_tbl ON dict_tbl._id=learn_tbl._id WHERE dict_tbl._id " + (forward ? ">=" : "<=") +  id + " ";
		if(flag == 1){
			query += "AND acq_reps = 0 ";
		}
		else if(flag == 2){
			query += "AND round((julianday(date('now', 'localtime')) - julianday(date_learn))) - interval >= 0 AND acq_reps > 0 ";
		}
        else if (flag == 3){
		    query = "SELECT learn_tbl._id, date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse, question, answer, note, category FROM dict_tbl INNER JOIN learn_tbl ON dict_tbl._id=learn_tbl._id WHERE round((julianday(date('now', 'localtime')) - julianday(date_learn))) - interval < 0 AND acq_reps > 0 ";
        }
        if(filter != null){
            if(Pattern.matches("#\\d+-\\d+", filter)){
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(filter);
                m.find();
                String min = m.group();
                m.find();
                String max = m.group();
                query += "AND learn_tbl._id >=" + min + " AND learn_tbl._id <= " + max + " ";
            }
            else if(Pattern.matches("#\\d+", filter)){
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(filter);
                m.find();
                String min = m.group();
                query += "AND learn_tbl._id >= " + min + " ";
            }
            else if(!filter.equals("")){
                /* Replace * and ? to % and _ used in SQL */
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");
                
                /* First remove white spaces at beginning */
                String realFilter = filter.replaceAll("^\\s+", "");

                String control = filter.length() >= 2 ? filter.substring(0, 2) : "";

                /* Also remove the control text */
                realFilter = realFilter.replaceAll("^%\\w\\s+", "");
                Log.v(TAG, "Control " + control);
                Log.v(TAG, "Filter " + realFilter);

                
                if(control.equals("%q")){
                    query += "AND ((question LIKE '" + realFilter + "')) ";
                }
                else if(control.equals("%a")){
                    query += "AND ((answer LIKE '" + realFilter + "')) ";
                }
                else if(control.equals("%n")){
                    query += "AND ((note LIKE '" + realFilter + "')) ";
                }
                else if(control.equals("%c")){
                    query += "AND ((category LIKE '" + realFilter + "')) ";
                }
                else{
                    query += "AND ((question LIKE '" + realFilter + "') OR (answer LIKE '" + realFilter + "') OR (note LIKE '" + realFilter + "') OR (category LIKE '" + realFilter +"')) ";
                }
            }
        }

        if(flag == 3){
            query += "ORDER BY RANDOM() ";
        }
        else{
            query += "ORDER BY learn_tbl._id " + (forward ? "ASC " : "DESC ");
        }
        query += "LIMIT 1";

		Cursor result;
		//result = myDatabase.query(true, "dict_tbl", null, querySelection, null, null, null, "_id", null);
		//result = myDatabase.query("dict_tbl", null, querySelection, null, null, null, "_id");
		//result = myDatabase.query(true, "dict_tbl", null, querySelection, null, null, null, null, "1");
		try{
			result = myDatabase.rawQuery(query, null);
		}
		catch(Exception e){
			Log.e("Query item error", e.toString());
			return null;
		}
		
		//System.out.println("The result is: " + result.getString(0));
		//return result.getString(1);
		if(result.getCount() == 0){
			result.close();
			return null; 
		}
		
		result.moveToFirst();
		//int resultId =	result.getInt(result.getColumnIndex("_id"));
        Item resultItem = new Item.Builder()
            .setId(result.getInt(result.getColumnIndex("_id")))
            .setQuestion(result.getString(result.getColumnIndex("question")))
            .setAnswer(result.getString(result.getColumnIndex("answer")))
            .setNote(result.getString(result.getColumnIndex("note")))
            .setCategory(result.getString(result.getColumnIndex("category")))
            .setDateLearn(result.getString(result.getColumnIndex("date_learn")))
            .setInterval(result.getInt(result.getColumnIndex("interval")))
            .setGrade(result.getInt(result.getColumnIndex("grade")))
            .setEasiness(result.getDouble(result.getColumnIndex("easiness")))
            .setAcqReps(result.getInt(result.getColumnIndex("acq_reps")))
            .setRetReps(result.getInt(result.getColumnIndex("ret_reps")))
            .setLapses(result.getInt(result.getColumnIndex("lapses")))
            .setAcqRepsSinceLapse(result.getInt(result .getColumnIndex("acq_reps_since_lapse")))
            .setRetRepsSinceLapse(result.getInt(result.getColumnIndex("ret_reps_since_lapse")))
            .build();
		result.close();
		return resultItem;
	}
	
	public int getScheduledCount(){
		Cursor result = myDatabase.rawQuery("SELECT count(_id) FROM learn_tbl WHERE round((julianday(date('now', 'localtime')) - julianday(date_learn))) - interval >= 0 AND acq_reps > 0", null);
		result.moveToFirst();
		int res = result.getInt(0);
		result.close();
		return res;
		
	}
	
	public int getNewCount(){
		Cursor result = myDatabase.rawQuery("SELECT count(_id) FROM learn_tbl WHERE acq_reps = 0", null);
		result.moveToFirst();
		int res = result.getInt(0);
		result.close();
		return res;
	}
	
	public int getTotalCount(){
		Cursor result = myDatabase.rawQuery("SELECT count(_id) FROM learn_tbl",  null);
		result.moveToFirst();
		int res = result.getInt(0);
		result.close();
		return res;
	}
	
	
	public HashMap<String, String> getSettings(){
		// Dump all the key/value pairs from the learn_tbl
		String key;
		String value;
		HashMap<String, String> hm = new HashMap<String, String>();
		
		
		Cursor result = myDatabase.rawQuery("SELECT * FROM control_tbl", null);
		int count = result.getCount();
		for(int i = 0; i < count; i++){
			if(i == 0){
				result.moveToFirst();
			}
			else{
				result.moveToNext();
			}
			key = result.getString(result.getColumnIndex("ctrl_key"));
			value = result.getString(result.getColumnIndex("value"));
			hm.put(key, value);
		}
		result.close();
		return hm;
	}
	
	public void deleteItem(Item item){
		myDatabase.execSQL("DELETE FROM learn_tbl where _id = ?", new String[]{"" + item.getId()});
		myDatabase.execSQL("DELETE FROM dict_tbl where _id = ?", new String[]{"" + item.getId()});
		myDatabase.execSQL("UPDATE dict_tbl SET _id = _id - 1 where _id > ?", new String[]{"" + item.getId()});
		myDatabase.execSQL("UPDATE learn_tbl SET _id = _id - 1 where _id > ?", new String[]{"" + item.getId()});
	}
	
	public void setSettings(HashMap<String, String> hm){
		// Update the control_tbl in database using the hm
		Set<Map.Entry<String, String>> set = hm.entrySet();
		Iterator<Map.Entry<String, String> > i = set.iterator();
		while(i.hasNext()){
			Map.Entry<String, String> me = i.next();
			myDatabase.execSQL("REPLACE INTO control_tbl values(?, ?)", new String[]{me.getKey().toString(), me.getValue().toString()});
		} 
	}
	
	public void wipeLearnData(){
		this.myDatabase.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 2.5, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
	}

    public void shuffleDatabase(){
        List<Item> itemList = getListItems(0, -1, 5, null);
        int count = 1;
        myDatabase.beginTransaction();
        try{
            for(Item item : itemList){
                Item newItem = new Item.Builder(item)
                    .setId(count)
                    .build();
                addOrReplaceItem(newItem);
                count += 1;
            }
			myDatabase.setTransactionSuccessful();
        }
        finally{
			myDatabase.endTransaction();
        }
    }

	public int getNewId(){
		Cursor result = this.myDatabase.rawQuery("SELECT _id FROM dict_tbl ORDER BY _id DESC LIMIT 1", null);
		if(result.getCount() != 1){
			result.close();
			return 1;
		}
		result.moveToFirst();
		int res = result.getInt(result.getColumnIndex("_id"));
		res += 1;
		result.close();
		return res;
	}
	
	public void addOrReplaceItem(Item item){
		this.myDatabase.execSQL("REPLACE INTO dict_tbl(_id, question, answer, note, category) VALUES(?, ?, ?, ?, ?)", new String[]{"" + item.getId(), item.getQuestion(), item.getAnswer(), item.getNote(), item.getCategory()});
		this.myDatabase.execSQL("REPLACE INTO learn_tbl(date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse, _id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{item.getDateLearn(), Integer.toString(item.getInterval()), Integer.toString(item.getGrade()), Double.toString(item.getEasiness()), Integer.toString(item.getAcqReps()), Integer.toString(item.getRetReps()), Integer.toString(item.getLapses()), Integer.toString(item.getAcqRepsSinceLapse()), Integer.toString(item.getRetRepsSinceLapse()), Integer.toString(item.getId())});
	}


    public void inverseQA(){
        List<Item> itemList = getListItems(0, -1, 0, null);
        myDatabase.beginTransaction();
        try{
            /* First inverse QA */
            for(Item item : itemList){
                Item newItem = item.inverseQA();
                addOrReplaceItem(newItem);
            }
            /* Then inverse control table */
            HashMap<String, String> hm = getSettings();
            String qLocale = hm.get("question_locale");
            String aLocale = hm.get("answer_locale");
            String qAlign = hm.get("question_align");
            String aAlign = hm.get("answer_align");
            String qFont = hm.get("question_font_size");
            String aFont = hm.get("answer_font_size");
            hm.put("question_locale", aLocale);
            hm.put("answer_locale", qLocale);
            hm.put("question_align", aAlign);
            hm.put("answer_align", qAlign);
            hm.put("question_font_size", aFont);
            hm.put("answer_font_size", qFont);
            setSettings(hm);

			myDatabase.setTransactionSuccessful();
        }
        finally{
			myDatabase.endTransaction();
        }
    }

    /* Add all swapped card to the back of database*/
    public void swapDuplicate(){
        List<Item> itemList = getListItems(0, -1, 0, null);
        myDatabase.beginTransaction();
        try{
            int newId = getNewId();
            
            for(Item item : itemList){
                /* Insert swapped item to the backQA */
                Item newItem = new Item.Builder(item)
                    .setId(newId++)
                    .build()
                    .inverseQA();
                addOrReplaceItem(newItem);
            }

			myDatabase.setTransactionSuccessful();
        }
        finally{
			myDatabase.endTransaction();
        }
    }
    public int searchItem(int currentId, String text, boolean forward){
        /* based on currentID, this method can search one item
         * forward or backward
         */
        Cursor result;
        text = "%" + text + "%";
        if(forward == true){
            result = this.myDatabase.rawQuery("SELECT _id FROM dict_tbl where (question LIKE ? OR answer LIKE ?) AND _id > ? LIMIT 1 ", new String[]{text, text, "" + currentId});
        }
        else{
            /* search backward */
            result = this.myDatabase.rawQuery("SELECT _id FROM dict_tbl where (question LIKE ? OR answer LIKE ?) AND _id < ? ORDER BY _id DESC LIMIT 1 ", new String[]{text, text, "" + currentId});
        }

		if(result.getCount() != 1){
			result.close();
			return -1;
		}
		result.moveToFirst();
		int res = result.getInt(result.getColumnIndex("_id"));
		result.close();
		return res;
    }

    public void setRecentFilters(String newFilter){
        /* Store from the newest to the oldest*/
        List<String> filterList = getRecentFilters();
        JSONArray jsonFilters = null;
        String storedFilter;
        if(filterList == null){
            filterList = new ArrayList<String>();
        }

        /* Remove duplicates */
        for(int i = 0; i < filterList.size(); i++){
            if(newFilter.equals(filterList.get(i))){
                filterList.remove(i);
                break;
            }
        }
        filterList.add(0, newFilter);

        /* Save to database */
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("recent_filters_json", (new JSONArray(filterList)).toString());
        setSettings(hm);
    }

    public List<String> getRecentFilters(){
        HashMap<String, String> hm = getSettings();
        String jsonStr = hm.get("recent_filters_json");
        List<String> filterList;
        if(jsonStr == null){
            return null;
        }
        JSONArray jsonFilters = null;
        try{
            jsonFilters = new JSONArray(jsonStr);
        }
        catch(JSONException e){
            return null;
        }

        filterList = new ArrayList<String>();

        for(int i = 0; i < jsonFilters.length(); i++){
            try{
                filterList.add(jsonFilters.getString(i));
            }
            catch(JSONException e){
                Log.e(TAG, "JSON parse error. ", e);
            }
        }
        return filterList;
    }
    
    public void deleteFilters(){
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("recent_filters_json", "");
        setSettings(hm);
    }

    public void mergeDatabase(String dbpath, String dbname){
        int fromId = getNewId() - 1;

        DatabaseHelper dbHelper2 = new DatabaseHelper(mContext, dbpath, dbname);
        final List<Item> items2 = dbHelper2.getListItems(-1, -1, 0, null); 

        dbHelper2.close();


        for(int i = 0; i < items2.size(); i++){
            Item tmpItem = items2.get(i);
            tmpItem = new Item.Builder(tmpItem)
                .setId(tmpItem.getId() + fromId)
                .build();
            items2.set(i, tmpItem);
        }
        insertListItems(items2);
    }

    public void insertItem(Item item, int id){
        /* Insert item after id */
        /* This is a tricky part! UPDATE dict_tbl SET +id = _id - 1 won't work
         * because it will conflict with the unique constraint
         */
        myDatabase.execSQL("UPDATE dict_tbl SET _id = _id - 99999 WHERE _id > ?", new String[]{"" + id});
        myDatabase.execSQL("UPDATE learn_tbl SET _id = _id - 99999 WHERE _id > ?", new String[]{"" + id});
        myDatabase.execSQL("UPDATE dict_tbl SET _id = _id + 100000 WHERE _id < 0");
        myDatabase.execSQL("UPDATE learn_tbl SET _id = _id + 100000 WHERE _id < 0");
        Item newItem = new Item.Builder(item)
            .setId(id + 1)
            .build();
        addOrReplaceItem(newItem);
    }

    public void removeDuplicates(){
        /* Delete duplicate items */
        myDatabase.execSQL("DELETE FROM dict_tbl WHERE _id NOT IN (SELECT MIN(_id) FROM dict_tbl GROUP BY question)");
        myDatabase.execSQL("DELETE FROM learn_tbl WHERE _id NOT IN (SELECT _id FROM dict_tbl)");
        maintainID();
    }

    /* Maintain the ID to be from 1 to n continuously */
    private void maintainID(){
        /* Reconstruct the ID by creating a temporary table 
         * and drop it at last*/
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tmp_count (id INTEGER PRIMARY KEY AUTOINCREMENT, _id INTEGER)");
        myDatabase.execSQL("INSERT INTO tmp_count(_id) SELECT _id FROM dict_tbl;");
        myDatabase.execSQL("UPDATE dict_tbl SET _id = (SELECT tmp_count.id FROM tmp_count WHERE tmp_count._id = dict_tbl._id)");
        myDatabase.execSQL("UPDATE learn_tbl SET _id = (SELECT tmp_count.id FROM tmp_count WHERE tmp_count._id = learn_tbl._id);");
        myDatabase.execSQL("DROP TABLE IF EXISTS tmp_count;");
    }



    public boolean checkFilterValidity(String filter){
        if(getItemById(0, 0, true, filter) == null){
            return false;
        }
        else{
            return true;
        }

    }

    public String getDbName(){
        return dbName;
    }

    public String getDbPath(){
        return dbPath;
    }

}
