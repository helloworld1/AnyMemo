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
package org.liberty.android.fantastischmemo.converter;

import org.liberty.android.fantastischmemo.*;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.MalformedURLException;
import android.database.SQLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.HashMap;
import android.util.Log;
import java.text.SimpleDateFormat;
import au.com.bytecode.opencsv.CSVReader;

import android.content.Context;

public class CSVImporter implements AbstractConverter{
    private Context mContext;

    public CSVImporter(Context context){
        mContext = context;
    }

    public void convert(String filePath, String fileName) throws Exception{
        String fullname = filePath + "/" + fileName;
        CSVReader reader = new CSVReader(new FileReader(fullname));
        String[] nextLine;
        int count = 0;
        LinkedList<Item> itemList = new LinkedList<Item>();
        while((nextLine = reader.readNext()) != null){
            if(nextLine.length < 2){
                throw new Exception("Malformed CSV file. Please make sure the CSV's first column is question, second one is answer and the optinal third one is category");
            }
            count++;
            String note = "";
            String category = "";
            if(nextLine.length >= 3){
                category = nextLine[2];
            }
            if(nextLine.length >= 4){
                note = nextLine[3];
            }
            Item item = new Item.Builder()
                .setId(count)
                .setQuestion(nextLine[0])
                .setAnswer(nextLine[1])
                .setCategory(category)
                .setNote(note)
                .build();
            itemList.add(item);
        }
        DatabaseHelper.createEmptyDatabase(filePath, fileName.replace(".csv", ".db"));
        DatabaseHelper dbHelper =  new DatabaseHelper(mContext, filePath, fileName.replace(".csv", ".db"));
        dbHelper.insertListItems(itemList);
        dbHelper.close();
    }
}

