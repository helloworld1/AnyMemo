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

import org.amr.arabic.ArabicUtilities;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Collections;

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
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;
import android.database.SQLException;
import java.util.concurrent.atomic.AtomicInteger;


class LearnQueueManager implements QueueManager{
    private Context mContext;
    private String dbPath;
    private String dbName;
    private DatabaseHelper dbHelper;
    private String activeFilter;
    private int queueSize;
    private boolean shuffleCards;
    private List<Item> learnQueue = null;
    private int revCardNo;
    private int newCardNo;
    public static final String TAG = "org.liberty.android.fantastischmemo.cardscreen.LearnQueueManager";


    public static class Builder{
        private Context mContext;
        private String dbPath;
        private String dbName;
        private String activeFilter = "";
        private int queueSize = 10;
        private boolean shuffleCards = false;

        public Builder(Context context, String dbpath, String dbname){
            mContext = context;
            dbPath = dbpath;
            dbName = dbname;
        }

        public Builder setFilter(String filter){
            activeFilter = filter;
            return this;
        }

        public Builder setQueueSize(int sz){
            queueSize = sz;
            return this;
        }

        public Builder setShuffle(boolean shuffle){
            shuffleCards = shuffle;
            return this;
        }

        public LearnQueueManager build(){
            return new LearnQueueManager(this);
        }
    }

    private LearnQueueManager(Builder builder) throws SQLException{
        mContext = builder.mContext;
        dbPath = builder.dbPath;
        dbName = builder.dbName;
        activeFilter = builder.activeFilter;
        queueSize = builder.queueSize;
        shuffleCards = builder.shuffleCards;
        dbHelper = new DatabaseHelper(mContext, dbPath, dbName);
        revCardNo = dbHelper.getScheduledCount();
        newCardNo = dbHelper.getNewCount();
    }


    public boolean initQueue(){
        /* Flag 4 means rev first then acq */
        learnQueue = dbHelper.getListItems(1, queueSize, 4, activeFilter);
        if(learnQueue == null || learnQueue.size() == 0){
            return false;
        }
        else{
            return true;
        }
    }

    /*
     * update current item and remove it in the queue
     * if the current item is not learned, this method will put it at the
     * end of the queue. If not, this method will pull another new card from
     * the database
     * The item parameter is the null, it will return current head of queue
     */
    public Item updateAndNext(Item item){
        if(learnQueue == null || learnQueue.size() == 0){
            return null;
        }
        if(item == null){
            return learnQueue.get(0);
        }

        /* When fail to remember a new card */
        if(learnQueue.get(0).isNew() && item.isScheduled()){
            newCardNo -= 1;
            revCardNo += 1;
        }

        /* When successfully remember a card */
        if(!item.isScheduled()){
            if(learnQueue.get(0).isScheduled()){
                revCardNo -= 1;
            }
            if(learnQueue.get(0).isNew()){
                newCardNo -= 1;
            }
        }

        /* To shuffle card, the first and second item must be maintained */
        if(shuffleCards && learnQueue.size() >= 2){
            Item first = learnQueue.get(0);
            Item second = learnQueue.get(1);
            learnQueue.remove(0);
            learnQueue.remove(0);
            Collections.shuffle(learnQueue);
            learnQueue.add(0, first);
            learnQueue.add(1, second);
        }
        Item orngItem = learnQueue.remove(0);
        if(item.isScheduled()){
            /*
             * If the old item is new, the one in the queue
             * should not remain noew
             * Also if the original one is learned but forget this time
             * we will update the queue
             */
            if(orngItem.isNew() || orngItem.getGrade() >= 2){
                learnQueue.add(item);
            }
            /* We do not repetitively update the original item */
            else{
                learnQueue.add(orngItem);
            }
        }
        dbHelper.addOrReplaceItem(item);
        /* Fill up the queue to its queue size */
        int maxNewId = getMaxQueuedItemId(true);
        int maxRevId = getMaxQueuedItemId(false);
        boolean fetchRevFlag = true;
        /* New item in database */
        while(learnQueue.size() < queueSize){
            if(fetchRevFlag == true){
                Item newItemFromDb = dbHelper.getItemById(maxRevId + 1, 2, true, activeFilter);
                if(newItemFromDb == null){
                    fetchRevFlag = false;
                }
                else{
                    learnQueue.add(newItemFromDb);
                    maxRevId = newItemFromDb.getId();
                }
            }
            else{
                Item newItemFromDb = dbHelper.getItemById(maxNewId + 1, 1, true, activeFilter);
                if(newItemFromDb != null){
                    learnQueue.add(newItemFromDb);
                    maxNewId = newItemFromDb.getId();
                }
                else{
                    break;
                }
            }
        }
        if(learnQueue.size() > 0){
            return learnQueue.get(0);
        }
        else{
            return null;
        }
    }

    /* 
     * Get the next item of the parameter item. Literally, it is the 
     * second item in the queue. 
     */
    public Item getNext(Item item){
        if(learnQueue.size() == 0){
            return null;
        }
        else if(learnQueue.size() > 1){
            return learnQueue.get(1);
        }
        else{
            return item;
        }
    }

    /* 
     * Replace the item in the queue with the same ID and
     * return trueif not, it will do nothing and return false
     */
    public boolean updateQueueItem(Item item){
        int foundIndex = -1;
        for(Item i : learnQueue){
            if(i.getId() == item.getId()){
                foundIndex = learnQueue.lastIndexOf(i);
            }
        }
        if(foundIndex == -1){
            return false;
        }
        else{
            learnQueue.set(foundIndex, item);
            return true;
        }
    }

    /* 
     * If position is -1, the item is inserted into the back of the queue.
     */
    public void insertIntoQueue(Item item, int position){
        if(item == null){
            throw new NullPointerException("The Item inserted into queue is null");
        }
        if(learnQueue == null){
            throw new NullPointerException("The learnQueue is null");
        }

        /* First remove the duplicate item with the same id */
        removeFromQueue(item.getId());

        if(position >= 0 && position <= learnQueue.size()){
            learnQueue.add(position, item);
        }
        else if(position == -1){
            learnQueue.add(item);
        }
        else{
            throw new IndexOutOfBoundsException("Illegal position to insert");
        }
    }

    public int[] getStatInfo(){
        return new int[]{newCardNo, revCardNo};
    }
    public void close(){
        /* First busy wait for the IO job done */
        if(dbHelper != null){
            dbHelper.close();
        }
        /* Release memeory */
        if(learnQueue != null){
            for(Item i : learnQueue){
                i = null;
            }
        }
        learnQueue = null;
    }

    private void removeFromQueue(int id){
        for(Item i : learnQueue){
            if(i.getId() == id){
                learnQueue.remove(i);
            }
        }

    }

    private int getMaxQueuedItemId(boolean isNewItem){
        if(learnQueue == null){
            throw new NullPointerException("The learnQueue is null");
        }
        int maxId = -1;
        int id = -1;
        for(Item item : learnQueue){
            id = item.getId();
            if(id > maxId && (isNewItem ? item.isNew() : !item.isNew())){
                maxId = id;
            }
        }
        return maxId;
    }



}

