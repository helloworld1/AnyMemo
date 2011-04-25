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

import android.content.Context;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.IOException;

public class TabTxtExporter implements AbstractConverter{
    private Context mContext;

    public TabTxtExporter(Context context){
        mContext = context;
    }

    public void convert(String dbPath, String dbName) throws Exception{
        String fullpath = dbPath + "/" + dbName.replaceAll(".db", ".txt");
        DatabaseHelper dbHelper = new DatabaseHelper(mContext, dbPath, dbName);

        CSVWriter writer = new CSVWriter(new FileWriter(fullpath), '\t');
        List<Item> itemList = new ArrayList<Item>();
        itemList = dbHelper.getListItems(1, -1, 0, null);
        if(itemList == null || itemList.size() == 0){
            throw new IOException("Can't retrieve items for database: " + dbPath + "/" + dbName);
        }
        String[] entries = new String[4];
        for(Item item : itemList){
            entries[0] = item.getQuestion();
            entries[1] = item.getAnswer();
            entries[2] = item.getCategory();
            entries[3] = item.getNote();
            writer.writeNext(entries);
        }
        writer.close();
        dbHelper.close();
    }
}




