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
import org.liberty.android.fantastischmemo.tts.*;

import org.amr.arabic.ArabicUtilities;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.gesture.GestureOverlayView;
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.util.Log;
import android.os.SystemClock;
import android.os.Environment;
import android.graphics.Typeface;
import android.text.Html.TagHandler;
import android.text.Html.ImageGetter;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;
import android.net.Uri;
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
    

