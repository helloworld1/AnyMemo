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
package org.liberty.android.fantastischmemo.cardscreen;

import org.liberty.android.fantastischmemo.*;
import android.content.Context;
import android.database.SQLException;

/*
 * This class will wrap the DBHelper to provide useful and
 * higher level function for card previewing and editing.
 */
public class ItemManager{
    private Context mContext;
    private String dbPath;
    private String dbName;
    private String activeFilter;
    private int totalItemNo = 0;
    private int maxId;
    private DatabaseHelper dbHelper;

    public static class Builder{
        private Context mContext;
        private String dbPath;
        private String dbName;
        private String activeFilter = "";

        public Builder(Context context, String dbpath, String dbname){
            mContext = context;
            dbPath = dbpath;
            dbName = dbname;
        }

        public Builder setFilter(String filter){
            activeFilter = filter;
            return this;
        }

        public ItemManager build(){
            return new ItemManager(this);
        }
    }

    private ItemManager(Builder builder) throws SQLException{
        mContext = builder.mContext;
        dbPath = builder.dbPath;
        dbName = builder.dbName;
        activeFilter = builder.activeFilter;
        dbHelper = new DatabaseHelper(mContext, dbPath, dbName);
        totalItemNo = dbHelper.getTotalCount();
        maxId = dbHelper.getNewId() - 1;
    }

    public Item getItem(int id){
        Item item = dbHelper.getItemById(id, 0, true, activeFilter);
        return item;
    }

    public Item getNextItem(Item currentItem){
        if(currentItem == null){
            return null;
        }
        Item nextItem = null;
        int currentId = currentItem.getId();

        if(totalItemNo > 0){
            nextItem = dbHelper.getItemById(currentId + 1, 0, true, activeFilter);
            if(nextItem == null){
                nextItem = dbHelper.getItemById(0, 0, true, activeFilter);
            }
        }
        return nextItem;
    }

    public Item getPreviousItem(Item currentItem){
        if(currentItem == null){
            return null;
        }
        Item nextItem = null;
        int currentId = currentItem.getId();
        if(totalItemNo > 0){
            nextItem = dbHelper.getItemById(currentId - 1, 0, false, activeFilter);
            if(nextItem == null){
               nextItem = dbHelper.getItemById(maxId, 0, false, activeFilter);
            }
        }
        return nextItem;
    }

    /* Insert next to the card# = id */
    public Item insert(Item newItem, int id){
        if(newItem != null){
            maxId += 1;
            totalItemNo += 1;
            Item ni = new Item.Builder(newItem)
                .setId(id)
                .build();
            dbHelper.insertItem(ni, id); 
            return ni;
        }
        else{
            return null;
        }
    }
    public Item insertBack(Item newItem){
        return insert(newItem, maxId);
    }

    /* Delete an item and return the next if available */
    public Item deleteItem(Item item){
        dbHelper.deleteItem(item);
        totalItemNo = dbHelper.getTotalCount();
        maxId = dbHelper.getNewId() - 1;
        /* 
         * Only fetch the item of currentId after it is deleted 
         * so it is the next item
         */
        Item nextItem = getItem(item.getId());
        return nextItem;
    }

    public int[] getStatInfo(){
        return new int[]{totalItemNo};
    }


    /*
     * Search the item forward or backward based on criterion
     * wildcard is permitted. Also use can directly go to a card
     * by input the number
     */
    public Item search(String text, boolean forward, Item currentItem){
        boolean processed = false;
        Item searchItem;
        if(text == null){
            return null;
        }
        else if(text.equals("")){
            return null;
        }
        if(text.charAt(0) == '#'){
            String num = text.substring(1);
            int intNum = 0;
            try{
                intNum = Integer.parseInt(num);
                if(intNum > 0 && intNum <= maxId){
                    searchItem = dbHelper.getItemById(intNum, 0, true, activeFilter);
                    if(searchItem != null){
                        return searchItem;
                    }
                }

            }
            catch(NumberFormatException e){
            }
        }
        if(processed == false && !text.equals("")){
            text = text.replace('*', '%');
            text = text.replace('?', '_');
            int resId = dbHelper.searchItem(currentItem.getId(), text, forward);
            if(resId > 0){
                searchItem = dbHelper.getItemById(resId, 0, forward, activeFilter);
                if(searchItem != null){
                    return searchItem;
                }
            }
        }
        return null;
    }

    public void close(){
        if(dbHelper != null){
            dbHelper.close();
        }
    }
}
    

