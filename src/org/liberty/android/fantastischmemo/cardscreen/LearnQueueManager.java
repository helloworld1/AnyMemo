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

import java.util.Collections;
import java.util.List;

import org.liberty.android.fantastischmemo.DatabaseHelper;
import org.liberty.android.fantastischmemo.Item;

import android.content.Context;
import android.database.SQLException;

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

    // this method is extracted from updateAndNext because it is invoked not by ui thread
    public Item getFirstItemFromQueue() {
    	if(learnQueue == null || learnQueue.size() == 0){
            return null;
        }
    	return learnQueue.get(0).clone();
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
        
        // assert item never null

        
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

        /* To shuffle card, the first item must be maintained */
        if(shuffleCards && learnQueue.size() >= 2){
            Item first = learnQueue.get(0);
            learnQueue.remove(0);
            Collections.shuffle(learnQueue);
            learnQueue.add(0, first);
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
        
        if(learnQueue.size() > 0){
            /* Return the clone to resolve the reference problem
             * i.e. updating the item will also item the one
             * in the queue */
            return learnQueue.get(0).clone();
        }

        return null;
    }


	public void updateInBackground(Item item) {
		dbHelper.beginTransaction();
		dbHelper.addOrReplaceItem(item);
		dbHelper.endSuccessfullTransaction();

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
        if(dbHelper != null){
        	//if (dbHelper.inTransaction()) 
        	//	dbHelper.endSuccessfullTransaction();
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

