/*
Copyright (C) 2011 Haowen Ning

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

package org.liberty.android.fantastischmemo.dao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.liberty.android.fantastischmemo.Item;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ItemDAO extends DAOBase {

    public ItemDAO(String path, String name) {
        super(path, name);
    }

	public void addOrReplaceItem(Item item){
        SQLiteDatabase db = getWritableDatabase();
		db.execSQL("REPLACE INTO dict_tbl(_id, question, answer, note, category) VALUES(?, ?, ?, ?, ?)", new String[]{"" + item.getId(), item.getQuestion(), item.getAnswer(), item.getNote(), item.getCategory()});
		db.execSQL("REPLACE INTO learn_tbl(date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse, _id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{item.getDateLearn(), Integer.toString(item.getInterval()), Integer.toString(item.getGrade()), Double.toString(item.getEasiness()), Integer.toString(item.getAcqReps()), Integer.toString(item.getRetReps()), Integer.toString(item.getLapses()), Integer.toString(item.getAcqRepsSinceLapse()), Integer.toString(item.getRetRepsSinceLapse()), Integer.toString(item.getId())});
	}

	public List<Item> geAllItems() {
        SQLiteDatabase db = getWritableDatabase();
		String query = "SELECT learn_tbl._id, date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse, question, answer, note, category FROM dict_tbl INNER JOIN learn_tbl ON dict_tbl._id=learn_tbl._id";
		Cursor result = db.rawQuery(query, null);
        List<Item> resultList = getItemsFromResult(result);
        return resultList;
    }

    public List<Item> getNewItemsForLearning(int queueSize) {

    }

    private List<Item> getItemsFromResult(Cursor result) {
        List<Item> list = new LinkedList<Item>();
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
        return list;
    }

}

