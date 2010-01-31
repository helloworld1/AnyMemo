package org.liberty.android.fantastischmemo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private final String DB_PATH;
	private final String DB_NAME;
	private SQLiteDatabase myDatabase;
	private final Context myContext;
		
	public DatabaseHelper(Context context, String dbPath, String dbName){
		super(context, dbName, null, 1);
		DB_PATH = dbPath;
		DB_NAME = dbName;
		this.myContext = context;
		this.openDatabase();
	}
	
	public DatabaseHelper(Context context, String dbPath, String dbName, int noOpen){
		super(context, dbName, null, 1);
		DB_PATH = dbPath;
		DB_NAME = dbName;
		this.myContext = context;
		if(noOpen == 0){
			this.openDatabase();
		}
	}
	
	public void createDatabase() throws IOException{
		boolean dbExist = checkDatabase();
		if(dbExist){
		}
		else{
			this.getReadableDatabase();
			try{
				copyDatabase();
			}
			catch(IOException e){
				throw new Error("Error copying database");
			}
			
		}
	}
	
	public void createEmptyDatabase() throws IOException{
		boolean dbExist = checkDatabase();
		if(dbExist){
			throw new IOException("DB already exist");
		}
		try{
		myDatabase = SQLiteDatabase.openDatabase(DB_PATH + "/" + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
		}
		catch(Exception e){
			Log.e("DB open error here", e.toString());
		}
		
		myDatabase.execSQL("CREATE TABLE dict_tbl(_id INTEGER PRIMARY KEY ASC AUTOINCREMENT, question TEXT, answer TEXT, note TEXT, category TEXT)");
		myDatabase.execSQL("CREATE TABLE learn_tbl(_id INTEGER PRIMARY KEY ASC AUTOINCREMENT, date_learn, interval, grade INTEGER, easiness REAL, acq_reps INTEGER, ret_reps INTEGER, lapses INTEGER, acq_reps_since_lapse INTEGER, ret_reps_since_lapse INTEGER)");
		myDatabase.execSQL("CREATE TABLE control_tbl(ctrl_key TEXT, value TEXT)");
		
		myDatabase.beginTransaction();
		try{
			this.myDatabase.execSQL("DELETE FROM learn_tbl");
			this.myDatabase.execSQL("INSERT INTO learn_tbl(_id) SELECT _id FROM dict_tbl");
			this.myDatabase.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 0.0, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('question_locale', 'US')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('answer_locale', 'US')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('question_align', 'center')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('answer_align', 'center')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('question_font_size', '24')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('answer_font_size', '24')");
			myDatabase.setTransactionSuccessful();
		}
		finally{
			myDatabase.endTransaction();
		}
		
	}
	public void createDatabaseFromList(List<String> questionList, List<String> answerList, List<String> categoryList) throws IOException{
		boolean dbExist = checkDatabase();
		if(dbExist){
			throw new IOException("DB already exist");
		}
		try{
		myDatabase = SQLiteDatabase.openDatabase(DB_PATH + "/" + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
		}
		catch(Exception e){
			Log.e("DB open error here", e.toString());
		}
		
		myDatabase.execSQL("CREATE TABLE dict_tbl(_id INTEGER PRIMARY KEY ASC AUTOINCREMENT, question TEXT, answer TEXT, note TEXT, category TEXT)");
		myDatabase.execSQL("CREATE TABLE learn_tbl(_id INTEGER PRIMARY KEY ASC AUTOINCREMENT, date_learn, interval, grade INTEGER, easiness REAL, acq_reps INTEGER, ret_reps INTEGER, lapses INTEGER, acq_reps_since_lapse INTEGER, ret_reps_since_lapse INTEGER)");
		myDatabase.execSQL("CREATE TABLE control_tbl(ctrl_key TEXT, value TEXT)");
		ListIterator<String> liq = questionList.listIterator();
		ListIterator<String> lia = answerList.listIterator();
		ListIterator<String> lic = categoryList.listIterator();
		
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
				myDatabase.execSQL("INSERT INTO dict_tbl(question,answer,category) VALUES(?, ?, ?)", new String[]{liq.next(), lia.next(), category});
				
				
			}
			this.myDatabase.execSQL("DELETE FROM learn_tbl");
			this.myDatabase.execSQL("INSERT INTO learn_tbl(_id) SELECT _id FROM dict_tbl");
			this.myDatabase.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 0.0, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('question_locale', 'US')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('answer_locale', 'US')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('question_align', 'center')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('answer_align', 'center')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('question_font_size', '24')");
			this.myDatabase.execSQL("INSERT INTO control_tbl(ctrl_key, value) VALUES('answer_font_size', '24')");
			
			myDatabase.setTransactionSuccessful();
		}
		finally{
			myDatabase.endTransaction();
		}
		
	}
	
	public boolean checkDatabase(){
		SQLiteDatabase checkDB = null;
		try{
			String myPath = DB_PATH + "/" + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		}
		catch(SQLiteException e){  
			checkDB = null;
		}
		
		if(checkDB != null){
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}
	
	private void copyDatabase() throws IOException{
		InputStream myInput = myContext.getAssets().open(DB_NAME);
		String outFilename = DB_PATH + "/" + DB_NAME;
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
		String myPath = DB_PATH + "/" + DB_NAME;
		Cursor result;
		int count_dict = 0, count_learn = 0;
		try{
		myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
		}
		catch(Exception e){
			Log.e("First", "Database error first here!: " + e.toString());
			throw new SQLException();
			
		}
		try{
			result = myDatabase.rawQuery("SELECT _id FROM dict_tbl", null);
			count_dict = result.getCount();
			result.close();
		}
		catch(Exception e){
			//new AlertDialog.Builder(this).setMessage(e.toString()).show();	
			Log.e("Second", "Database error here!: " + e.toString());
		}
		
		if(count_dict == 0){
			return;
		}
		result = myDatabase.rawQuery("SELECT _id FROM learn_tbl", null);
		count_learn = result.getCount();
		result.close();
		if(count_learn != count_dict){
			this.myDatabase.execSQL("DELETE FROM learn_tbl");
			this.myDatabase.execSQL("INSERT INTO learn_tbl(_id) SELECT _id FROM dict_tbl");
			this.myDatabase.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 0.0, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
		}
		
		
	}
	//@Override
	public synchronized void close(){
		if(myDatabase != null){
			myDatabase.close();
		}
		super.close();
	}
	//@Override
	public void onCreate(SQLiteDatabase db){
		
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		
	}
	
	public Item getItemById(int id, int flag){
		// These function are related to read db operation
		// flag = 0 means no condition
		// flag = 1 means new items, the items user have never seen
		// flag = 2 means item due, they need to be reviewed.
		HashMap<String, String> hm = new HashMap<String, String>();
		//ArrayList<String> list = new ArrayList<String>();
		String query = "SELECT learn_tbl._id, date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse, question, answer, note FROM dict_tbl INNER JOIN learn_tbl ON dict_tbl._id=learn_tbl._id WHERE dict_tbl._id >= " + id + " ";
		if(flag == 1){
			query += "AND acq_reps = 0 LIMIT 1";
		}
		else if(flag == 2){
			query += "AND round((julianday(date('now', 'localtime')) - julianday(date_learn))) - interval >= 0 AND acq_reps > 0 LIMIT 1";
		}
		else{
			query += "LIMIT 1";
			
		}
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
		hm.put("_id", Integer.toString(result.getInt(result.getColumnIndex("_id"))));
		hm.put("question", result.getString(result.getColumnIndex("question")));
		hm.put("answer", result.getString(result.getColumnIndex("answer")));
		hm.put("note", result.getString(result.getColumnIndex("note")));
		
		//querySelection = " _id = " + resultId;
		//result = myDatabase.query(true, "learn_tbl", null, querySelection, null, null, null, null, "1");
		//if(result.getCount() == 0){
		//	return null;
		//}
		//result.moveToFirst();
		hm.put("date_learn", result.getString(result.getColumnIndex("date_learn")));
		hm.put("interval", Integer.toString(result.getInt(result.getColumnIndex("interval"))));
		hm.put("grade", Integer.toString(result.getInt(result.getColumnIndex("grade"))));
		hm.put("easiness", Double.toString(result.getDouble(result.getColumnIndex("grade"))));
		hm.put("acq_reps", Integer.toString(result.getInt(result.getColumnIndex("acq_reps"))));
		hm.put("ret_reps", Integer.toString(result.getInt(result.getColumnIndex("ret_reps"))));
		hm.put("lapses", Integer.toString(result.getInt(result.getColumnIndex("lapses"))));
		hm.put("acq_reps_since_lapse", Integer.toString(result.getInt(result.getColumnIndex("acq_reps_since_lapse"))));
		hm.put("ret_reps_since_lapse", Integer.toString(result.getInt(result.getColumnIndex("ret_reps_since_lapse"))));
		
		
		result.close();
		Item resultItem = new Item();
		resultItem.setData(hm);
		return resultItem;
	}
	
	public void updateItem(Item item){
		// Only update the learn_tbl
		try{
			myDatabase.execSQL("UPDATE learn_tbl SET date_learn = ?, interval = ?, grade = ?, easiness = ?, acq_reps = ?, ret_reps = ?, lapses = ?, acq_reps_since_lapse = ?, ret_reps_since_lapse = ? WHERE _id = ?", item.getLearningData());

		}
		catch(Exception e){
			Log.e("Query error in updateItem!", e.toString());
			
		}
		
	}
	
	public void updateQA(Item item){
		myDatabase.execSQL("UPDATE dict_tbl SET question = ?, answer = ?, note = ? WHERE _id = ?", new String[]{item.getQuestion(), item.getAnswer(), item.getNote(), Integer.toString(item.getId())});
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
		this.myDatabase.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 0.0, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
	}

	
	public int getNewId(){
		Cursor result = this.myDatabase.rawQuery("SELECT _id FROM dict_tbl ORDER BY _id DESC LIMIT 1", null);
		if(result.getCount() != 1){
			result.close();
			return -1;
		}
		result.moveToFirst();
		int res = result.getInt(result.getColumnIndex("_id"));
		res += 1;
		return res;
	}
	
	public void addOrReplaceItem(Item item){
		this.myDatabase.execSQL("REPLACE INTO dict_tbl(_id, question, answer) VALUES(?, ?, ?)", new String[]{"" + item.getId(), item.getQuestion(), item.getAnswer()});
		this.myDatabase.execSQL("REPLACE INTO learn_tbl(_id, date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse) VALUES (?, '2010-01-01', 0, 0, 0.0, 0, 0, 0, 0, 0)", new String[]{"" + item.getId()});
		
	}
	
}
