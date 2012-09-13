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

public class QuizQueueManager implements QueueManager {
    private CardDao cardDao;
    
    private LearningDataDao learningDataDao;

    /* 
     * The scheduler to determine whether a card should reimain
     * in the learn queue
     */
    private Scheduler scheduler;

    private Category filterCategory;

    private List<Card> newCache;
    private List<Card> reviewCache;
    private Set<Card> dirtyCache;

    private int learnQueueSize;

    private int cacheSize;

    private int maxNewCacheOrdinal = 0;

    private int maxReviewCacheOrdinal = 0; 
    
    private boolean shuffle;

    private final String TAG = getClass().getSimpleName();

    private QuizQueueManager(Builder builder) {
        this.filterCategory = builder.filterCategory;
        this.shuffle = builder.shuffle;
        this.scheduler = builder.scheduler;
        newCache = new LinkedList<Card>();
        reviewCache = new LinkedList<Card>();
        dirtyCache = new HashSet<Card>();
    }

	@Override
	public synchronized Card dequeue() {
        
        return null;
	}
	
	@Override
	public synchronized Card dequeuePosition(int cardId) {
        return null;
	}
	
	@Override
	public synchronized void remove(Card card) {
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
                        for (Card card : dirtyCache) {
                            Log.i(TAG, "Flushing: " + card.getLearningData());
                            learningDataDao.update(card.getLearningData());
                            cardDao.update(card);
                        }
                        dirtyCache.clear();
                        return null;
                    }
                });
        } catch (Exception e) {
            throw new RuntimeException("Queue flushing get exception!", e);
        }
	}

    private synchronized void refill(long startOrd, long size) {
        newCache.addAll(cardDao.getCardsByOrdinalAndSize(startOrd, size));
    }


    private synchronized void shuffle() {
    	// Shuffle all the caches
    	if (shuffle) {
    		Collections.shuffle(newCache);
    		Collections.shuffle(reviewCache);
    	}
    }

	@Override
	public synchronized void update(Card card) {
        if (!scheduler.isCardLearned(card.getLearningData())) {
            // Add to the back of the queue
            reviewCache.add(card);
        }
        dirtyCache.add(card);
	}

    public static class Builder {

        private CardDao cardDao;

        private Scheduler scheduler;

        private LearningDataDao learningDataDao;

        private Category filterCategory;

        private boolean shuffle = false;

        private AnyMemoDBOpenHelper dbOpenHelper;

        private long startOrd = 1;

        private long quizSize = 50;

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

        public Builder setStartCardOrd(long startOrd) {
            this.startOrd = startOrd;
            return this;
        }

        public Builder setQuizSize(long quizSize) {
            this.quizSize = quizSize;
            return this;
        }
        
        public Builder setShuffle(boolean shuffle) {
            this.shuffle = shuffle;
            return this;
        }

        public QueueManager build() {
            this.cardDao = dbOpenHelper.getCardDao();
            this.learningDataDao = dbOpenHelper.getLearningDataDao();
            if (cardDao == null || learningDataDao == null) {
                throw new AssertionError("cardDao and learningDataDao must set");
            }

            QuizQueueManager qm = new QuizQueueManager(this);
            qm.refill(startOrd, quizSize);
            if (shuffle) {
                qm.shuffle();
            }
            return qm;
        }
    }
}
