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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import android.util.Log;

public class LearnQueueManager implements QueueManager {
    private CardDao cardDao;

    private LearningDataDao learningDataDao;

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
        this.learningDataDao = builder.learningDataDao;
        this.filterCategory = builder.filterCategory;
        this.learnQueueSize = builder.learnQueueSize;
        this.cacheSize = builder.cacheSize;
        this.shuffle = builder.shuffle;
        learnQueue = new LinkedList<Card>();
        newCache = new LinkedList<Card>();
        reviewCache = new LinkedList<Card>();
        dirtyCache = new HashSet<Card>();
    }

	@Override
	public synchronized Card dequeue() {
        refill();
        if (!learnQueue.isEmpty()) {

            Card c = learnQueue.get(0);
            learnQueue.remove(0);
            Log.i(TAG, "Dequeue card: " + c.getId());
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
            Log.e(TAG, "Queue flushing get exception!");
            e.printStackTrace();
        }
		
	}

    private synchronized void refill() {
        if (newCache.size() == 0) {
            List<Card> cs = cardDao.getNewCards(filterCategory, maxNewCacheOrdinal, cacheSize - newCache.size());
            if (cs.size() > 0) {
                maxNewCacheOrdinal = cs.get(cs.size() - 1).getOrdinal();
                newCache.addAll(cs);
            }
        }

        if (reviewCache.size() == 0) {
            List<Card> cs = cardDao.getCardForReview(filterCategory, maxReviewCacheOrdinal, cacheSize - reviewCache.size());
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
        // Shuffle all teh caches
        if (shuffle) {
            Collections.shuffle(newCache);
            Collections.shuffle(reviewCache);
            Collections.shuffle(learnQueue);
        }
    }

	@Override
	public synchronized void update(Card card) {
        // TODO: Should use an scheduling manager to determine it
        if (card.getLearningData().getGrade() >= 2) {
            learnQueue.remove(card);
            dirtyCache.add(card);
        } else {
            // Add to the back of the queue
            learnQueue.remove(card);
            learnQueue.add(card);

            // Only update once if one fail to lear
            if (!dirtyCache.contains(card)) {
                dirtyCache.add(card);
            }
        }
	}

    public static class Builder {

        private CardDao cardDao;

        private LearningDataDao learningDataDao;

        private Category filterCategory;

        private int learnQueueSize = 10;

        private int cacheSize = 50;

        private boolean shuffle = false;

		public Builder setCardDao(CardDao cardDao) {
			this.cardDao = cardDao;
            return this;
		}
		public Builder setLearningDataDao(LearningDataDao learningDataDao) {
			this.learningDataDao = learningDataDao;
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
            if (cardDao == null || learningDataDao == null) {
                throw new AssertionError("cardDao and learningDataDao must set");
            }
            return new LearnQueueManager(this);
        }
    }
}

