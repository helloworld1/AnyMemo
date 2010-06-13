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
        HashMap<String, String>hm = new HashMap<String, String>();
        hm.put("html_display", "both");
        dbHelper.setSettings(hm);
        dbHelper.close();
    }

    public void ImportSupermemoXML() throws Exception{
        SupermemoXMLConverter conv = new SupermemoXMLConverter(filePath, fileName);
        DatabaseHelper.createEmptyDatabase(filePath, fileName.replace(".xml", ".db"));
        DatabaseHelper dbHelper =  new DatabaseHelper(mContext, filePath, fileName.replace(".xml", ".db"));
        dbHelper.insertListItems(conv.outputList());
        HashMap<String, String>hm = new HashMap<String, String>();
        hm.put("html_display", "both");
        dbHelper.setSettings(hm);
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
        HashMap<String, String>hm = new HashMap<String, String>();
        hm.put("html_display", "both");
        dbHelper.setSettings(hm);
        txtfile.close();
        dbHelper.close();
    }

    public void ImportQATXT() throws Exception{
        String fullpath = filePath + "/" + fileName;
        DatabaseHelper.createEmptyDatabase(filePath, fileName.replace(".txt", ".db"));
        DatabaseHelper dbHelper =  new DatabaseHelper(mContext, filePath, fileName.replace(".txt", ".db"));
        BufferedReader txtfile = new BufferedReader(new FileReader(fullpath));
        String line;
        int count = 0;
        LinkedList<Item> itemList = new LinkedList<Item>();
        boolean isQ = false;
        StringBuffer qBuf = null;
        StringBuffer aBuf = null;
        Item item;
        while((line = txtfile.readLine()) != null){
            String head = "";
            if(line.length() >= 2){
                head = line.substring(0, 2);
            }
            if(line.equals("")){
                continue;
            }
            else if(head.equals("Q:")){
                if(isQ == true){
                    /* next line */
                    qBuf.append("<br />" + line.replaceAll("Q:\\s*", ""));
                }
                else{
                    isQ = true;
                    /* Save item when the Q is after A
                     * because it is a new item */
                    if(count != 0){
                        item = new Item();
                        item.setQuestion(qBuf.toString());
                        item.setAnswer(aBuf.toString());
                        item.setId(count);
                        itemList.add(item);
                    }
                    count += 1;
                    qBuf = new StringBuffer();
                    qBuf.append(line.replaceAll("Q:\\s*", ""));
                }
            }
            else if(head.equals("A:")){
                if(isQ == true){
                    isQ = false;
                    aBuf = new StringBuffer();
                    aBuf.append("<br />" + line.replaceAll("A:\\s*", ""));
                }
                else{
                    aBuf.append(line.replaceAll("A:\\s*", ""));
                }
            }
            else{
                if(isQ){
                    qBuf.append("<br />" + line);
                }
                else{
                    aBuf.append("<br />" + line);
                }
            }
        }
        /* Last item need to be added manually */
        item = new Item();
        item.setQuestion(qBuf.toString());
        item.setAnswer(aBuf.toString());
        item.setId(count);
        count += 1;
        itemList.add(item);

        if(!itemList.isEmpty()){
            dbHelper.insertListItems(itemList);
        }
        HashMap<String, String>hm = new HashMap<String, String>();
        hm.put("html_display", "both");
        dbHelper.setSettings(hm);
        txtfile.close();
        dbHelper.close();
    }
}



