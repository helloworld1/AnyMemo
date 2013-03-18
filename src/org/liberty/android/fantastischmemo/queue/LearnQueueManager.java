/*
Copyright (C) 2012 Haowen Ning

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

package org.liberty.android.fantastischmemo.queue;

import java.sql.SQLException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import org.liberty.android.fantastischmemo.scheduler.Scheduler;

import android.util.Log;

public class LearnQueueManager implements QueueManager {
    private CardDao cardDao;

    private CategoryDao categoryDao;
    
    private LearningDataDao learningDataDao;

    /* 
     * The scheduler to determine whether a card should reimain
     * in the learn queue
     */
    private Scheduler scheduler;

    private Category filterCategory;

    private List<Card> learnQueue;
    private List<Card> newCache;
    private List<Card> reviewCache;
    private Set<Card> dirtyCache;

    private int learnQueueSize;

    private int cacheSize;

    private int maxNewCacheOrdinal = 0;

    private int maxReviewCacheOrdinal = 0; 
    
    private boolean shuffle;

    private final String TAG = getClass().getSimpleName();

    private LearnQueueManager(Builder builder) {
        this.cardDao = builder.cardDao;
        this.categoryDao = builder.categoryDao;
        this.learningDataDao = builder.learningDataDao;
        this.filterCategory = builder.filterCategory;
        this.learnQueueSize = builder.learnQueueSize;
        this.cacheSize = builder.cacheSize;
        this.shuffle = builder.shuffle;
        this.scheduler = builder.scheduler;
        learnQueue = new LinkedList<Card>();
        newCache = new LinkedList<Card>();
        reviewCache = new LinkedList<Card>();
        dirtyCache = new HashSet<Card>();
    }

	@Override
	public synchronized Card dequeue() {
        refill();
        shuffle();
        if (!learnQueue.isEmpty()) {
            Card c = learnQueue.get(0);
            learnQueue.remove(0);
            Log.d(TAG, "Dequeue card: " + c.getId());
            return c;
        } else {
            return null;
        }
	}
	
	@Override
	public synchronized Card dequeuePosition(int cardId) {
		position(cardId);
		refill();
		
		if (!learnQueue.isEmpty()) {
            Card c = learnQueue.get(0);
            learnQueue.remove(0);
            Log.d(TAG, "Dequeue card: " + c.getId());
            return c;
        } else {
            return null;
        }
		
	}
	
	@Override
	public synchronized void remove(Card card) {
        learnQueue.remove(card);
        dirtyCache.remove(card);
        reviewCache.remove(card);
        newCache.remove(card);
	}

	@Override
	public synchronized void flush() {
        // Update the queue
        
        try {
            learningDataDao.callBatchTasks (
                new Callable<Void>() {
                    public Void call() throws Exception {
                        Log.i(TAG, "Cards to flush: " + dirtyCache.size());
                        for (Card card : dirtyCache) {
                            Log.i(TAG, "Flushing: " + card.getLearningData());
                            learningDataDao.update(card.getLearningData());
                            cardDao.update(card);
                        }
                       return null;
                    }
                });
            dirtyCache.clear();
        } catch (Exception e) {
            Log.e(TAG, "Error encounter when flushing: ", e);
            throw new RuntimeException("Queue flushing get exception!", e);
        }
	}

    private synchronized void refill() {
        if (newCache.size() == 0) {
            List<Card> cs = cardDao.getNewCards(filterCategory, maxNewCacheOrdinal, cacheSize - newCache.size());
            categoryDao.populateCategory(cs);
            if (cs.size() > 0) {
                maxNewCacheOrdinal = cs.get(cs.size() - 1).getOrdinal();
                newCache.addAll(cs);
            }
        }

        if (reviewCache.size() == 0) {
            List<Card> cs = cardDao.getCardForReview(filterCategory, maxReviewCacheOrdinal, cacheSize - reviewCache.size());
            categoryDao.populateCategory(cs);
            if (cs.size() > 0) {
                maxReviewCacheOrdinal = cs.get(cs.size() - 1).getOrdinal();
                reviewCache.addAll(cs);
            }
        }

        while (learnQueue.size() < learnQueueSize && !reviewCache.isEmpty()) {
            learnQueue.add(reviewCache.get(0));
            reviewCache.remove(0);
        }
        while (learnQueue.size() < learnQueueSize && !newCache.isEmpty()) {
            learnQueue.add(newCache.get(0));
            newCache.remove(0);
        }
    }


    private synchronized void shuffle() {
    	// Shuffle all the caches
    	if (shuffle) {
    		Collections.shuffle(newCache);
    		Collections.shuffle(reviewCache);
    		Collections.shuffle(learnQueue);
    	}
    }

	@Override
	public synchronized void update(Card card) {
        learnQueue.remove(card);
        if (!scheduler.isCardLearned(card.getLearningData())) {
            // Add to the back of the queue
            learnQueue.add(card);
        }
        dirtyCache.add(card);
	}
	
    private void position(int cardId) {
        Iterator<Card> learnIterator= learnQueue.iterator();
        Iterator<Card> reviewCacheIterator = reviewCache.iterator();
        Iterator<Card> newCacheIterator = newCache.iterator();

        int learnQueueRotateDistance = 0;
        while (learnIterator.hasNext()) {
            Card c =learnIterator.next();
            if (c.getId() == cardId) {
                int index = learnQueue.indexOf(c);
                learnQueueRotateDistance = -index;
                Log.i(TAG, "Rotate index: " + index);
            }
        }
        Collections.rotate(learnQueue, learnQueueRotateDistance);

        while (reviewCacheIterator.hasNext()) {
            Card c =reviewCacheIterator.next();
            if (c.getId() == cardId) {
                reviewCacheIterator.remove();
            }
        }

        while (newCacheIterator.hasNext()) {
            Card c = newCacheIterator.next();
            if (c.getId() == cardId) {
                newCacheIterator.remove();
            }
        }

        Card headCard = null;
        try {
            headCard = cardDao.queryForId(cardId);
            learningDataDao.refresh(headCard.getLearningData());
        } catch (SQLException e) {
            throw new RuntimeException("Position a wrong card", e);
        }

        learnQueue.add(0, headCard);
    }

    public static class Builder {

        private AnyMemoDBOpenHelper dbOpenHelper;

        private CardDao cardDao;

        private CategoryDao categoryDao;

        private LearningDataDao learningDataDao;

        private Scheduler scheduler;

        private Category filterCategory;

        private int learnQueueSize = 10;

        private int cacheSize = 50;

        private boolean shuffle = false;

        public Builder setScheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public Builder setDbOpenHelper(AnyMemoDBOpenHelper helper) {
            dbOpenHelper = helper;
            return this;
        }

		public Builder setFilterCategory(Category filterCategory) {
			this.filterCategory = filterCategory;
            return this;
		}
		public Builder setLearnQueueSize(int learnQueueSize) {
			this.learnQueueSize = learnQueueSize;
            return this;
		}
		public Builder setCacheSize(int cacheSize) {
			this.cacheSize = cacheSize;
            return this;
		}
        public Builder setShuffle(boolean shuffle) {
            this.shuffle = shuffle;
            return this;
        }

        public QueueManager build() {
            cardDao = dbOpenHelper.getCardDao();

            learningDataDao = dbOpenHelper.getLearningDataDao();

            categoryDao = dbOpenHelper.getCategoryDao();

            if (cardDao == null || learningDataDao == null) {
                throw new AssertionError("cardDao and learningDataDao must set");
            }
            LearnQueueManager qm = new LearnQueueManager(this);
            qm.refill();
            return qm;
        }
    }
}

