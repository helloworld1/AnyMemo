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
import java.text.SimpleDateFormat;

import android.content.Context;

public class DBImporter{

    private String filePath;
    private String fileName;
    private Context mContext;
    private final static String TAG = "org.liberty.android.fantastischmemo.DBImporter";

    public DBImporter(Context context, String path, String name){
        filePath = path;
        fileName = name;
        mContext = context;
    }

    public void ImportMnemosyneXML() throws Exception{
        MnemosyneXMLConverter conv = new MnemosyneXMLConverter(filePath, fileName);
        DatabaseHelper.createEmptyDatabase(filePath, fileName.replace(".xml", ".db"));
        DatabaseHelper dbHelper =  new DatabaseHelper(mContext, filePath, fileName.replace(".xml", ".db"));
        dbHelper.insertListItems(conv.outputList());
        dbHelper.close();
    }

    public void ImportTabTXT() throws Exception{
        String fullpath = filePath + "/" + fileName;
        DatabaseHelper.createEmptyDatabase(filePath, fileName.replace(".txt", ".db"));
        DatabaseHelper dbHelper =  new DatabaseHelper(mContext, filePath, fileName.replace(".txt", ".db"));
        BufferedReader txtfile = new BufferedReader(new FileReader(fullpath));
        String line;
        int count = 1;
        LinkedList<Item> itemList = new LinkedList<Item>();
        while((line = txtfile.readLine()) != null){
            String[] split = line.split("\t");
            Item item = new Item();
            item.setId(count);
            if(split.length < 2){
                continue;
            }

            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("question", split[0]);
            hm.put("answer", split[1]);
            if(split.length > 2){
                hm.put("category", split[2]);
            }
            item.setData(hm);
            itemList.add(item);
            count += 1;
        }
        if(!itemList.isEmpty()){
            dbHelper.insertListItems(itemList);
        }
        txtfile.close();
        dbHelper.close();
    }
}



