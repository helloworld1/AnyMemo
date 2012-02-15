/*
Copyright (C) 2011 Haowen Ning

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

import java.util.Deque;
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

    private Deque<Card> learnQueue;
    private List<Card> newCache;
    private List<Card> reviewCache;
    private Set<Card> dirtyCache;

    private int learnQueueSize;

    private int cacheSize;

    private int maxNewCacheOrdinal = 0;

    private int maxReviewCacheOrdinal = 0; 

    private final String TAG = getClass().getSimpleName();

    public LearnQueueManager(int learnQueueSize, int cacheSize) {
        learnQueue = new LinkedList<Card>();
        newCache = new LinkedList<Card>();
        reviewCache = new LinkedList<Card>();
        dirtyCache = new HashSet<Card>();
        this.learnQueueSize = learnQueueSize;
        this.cacheSize = cacheSize;
    }

	public CardDao getCardDao() {
		return cardDao;
	}
	public void setCardDao(CardDao cardDao) {
		this.cardDao = cardDao;
	}
	public LearningDataDao getLearningDataDao() {
		return learningDataDao;
	}
	public void setLearningDataDao(LearningDataDao learningDataDao) {
		this.learningDataDao = learningDataDao;
	}

	public Category getFilterCategory() {
		return filterCategory;
	}

	public void setFilterCategory(Category filterCategory) {
		this.filterCategory = filterCategory;
	}

	@Override
	public synchronized Card dequeue() {
        refill();
        if (!learnQueue.isEmpty()) {

            Card c = learnQueue.removeFirst();
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
            learnQueue.addLast(reviewCache.get(0));
            reviewCache.remove(0);
        }
        while (learnQueue.size() < learnQueueSize && !newCache.isEmpty()) {
            learnQueue.addLast(newCache.get(0));
            newCache.remove(0);
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
        }
	}
}

