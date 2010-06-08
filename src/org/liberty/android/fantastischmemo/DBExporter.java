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

import android.content.Context;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;


class DBExporter{
    private DatabaseHelper dbHelper;
    private String dbPath;
    private String dbName;
    private Context mContext;
    private final static String TAG = "org.liberty.android.fantastischmemo.DBExporter";
    private final int SEC_PER_DAY = 24 * 60 * 60;


    public DBExporter(){}

    public DBExporter(Context context, String path, String name) throws Exception{
        dbPath = path;
        dbName = name;
        mContext = context;
        loadDatabase();
    }

    public void loadDatabase() throws Exception{
        try{
            dbHelper = new DatabaseHelper(mContext, dbPath, dbName);
        }
        catch(Exception e){
            throw e;
        }
    }

    public void writeXML() throws Exception{
        String fullpath = dbPath + "/" + dbName.replaceAll(".db", ".xml");
        PrintWriter outxml = new PrintWriter(new BufferedWriter(new FileWriter(fullpath)));
        if(outxml.checkError()){
            throw new IOException("Can't open: " + fullpath);
        }
        List<Item> itemList = new LinkedList<Item>();
        boolean result = dbHelper.getListItems(1, -1, itemList);
        if(result == false){
            throw new IOException("Can't retrieve items for database: " + dbPath + "/" + dbName);
        }
        int count = 0;
        long timeOfStart = 0L;
        String id, u, gr, e, ac_rp, rt_rp, lps, ac_rp_l, rt_rp_l, l_rp, n_rp, question,  answer;
        // Now write the xml to the file
        for(Item item : itemList){
            // At the first item, we write all metadata
            if(count == 0){
                //timeOfStart = item.getDatelearnUnix();
                // 2000-01-01 12:00
                timeOfStart = 946728000L;
                outxml.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                outxml.print("<mnemosyne core_version=\"1\" time_of_start=\"" + timeOfStart + "\" >\n");
                outxml.print("<category active=\"1\">\n");
                outxml.print("<name>" + dbName + "</name>\n");
                outxml.print("</category>\n");
            }
            // Convert the learning data
            String[] learnData = item.getLearningData();
            id = Integer.toString(item.getId());
            gr = learnData[2];
            e = learnData[3];
            ac_rp = learnData[4];
            rt_rp = learnData[5];
            lps = learnData[6];
            ac_rp_l = learnData[7];
            rt_rp_l = learnData[8];
            if(ac_rp.equals("0")){
                u = "1";
            }
            else{
                u = "0";
            }
            // Add 1 here to avoid rounding problem
            long duration = (item.getDatelearnUnix() - timeOfStart) / SEC_PER_DAY + 1;


            Long interval = new Long(learnData[1]);
            l_rp = Long.toString(duration);
            n_rp = Long.toString(interval + duration);

            // Replace the illegal symbols from the question and answer
            question = item.getQuestion();
            answer = item.getAnswer();
            question = question.replaceAll("<", "&lt;");
            question = question.replaceAll(">", "&gt;");
            question = question.replaceAll("&", "&amp;");
            question = question.replaceAll("'", "&apos;");
            question = question.replaceAll("\"", "&quot;");
            answer = answer.replaceAll("<", "&lt;");
            answer = answer.replaceAll(">", "&gt;");
            answer = answer.replaceAll("&", "&amp;");
            answer = answer.replaceAll("'", "&apos;");
            answer = answer.replaceAll("\"", "&quot;");


            outxml.print("<item id=\"" + id + "\" u=\"" + u +"\" gr=\"" + gr +"\" e=\"" + e + "\" ac_rp=\"" + ac_rp + "\" rt_rp=\"" + rt_rp + "\" lps=\"" + lps + "\" ac_rp_l=\"" + ac_rp_l + "\" rt_rp_l=\"" + rt_rp_l + "\" l_rp=\"" + l_rp + "\" n_rp=\"" + n_rp + "\">\n");
            outxml.print("<cat>" + dbName + "</cat>\n");
            outxml.print("<Q>" + question + "</Q>\n");
            outxml.print("<A>" + answer + "</A>\n");
            outxml.print("</item>\n");
            if(outxml.checkError()){
                throw new IOException("Error writing xml on id: " + id);
            }
            count += 1;
        }
        outxml.print("</mnemosyne>");
        outxml.close();
        dbHelper.close();
    }






                
                

}





