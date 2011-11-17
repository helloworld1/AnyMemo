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

import java.sql.SQLException;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;

import com.j256.ormlite.dao.Dao;

import com.j256.ormlite.stmt.QueryBuilder;

public class LearnQueueManager implements QueueManager {
    private Dao<Card, Integer> cardDao;

    private Dao<LearningData, Integer> learningDataDao;

    private List<Category> filterCategories;

    private List<Card> learnQueue;
    private List<Card> newCache;
    private List<Card> reviewCache;
    private Set<Card> dirtyCache;

    private int learnQueueSize;

    private int cacheSize;

    public LearnQueueManager(int learnQueueSize, int cacheSize) {
        learnQueue = new LinkedList<Card>();
        newCache = new LinkedList<Card>();
        reviewCache = new LinkedList<Card>();
        dirtyCache = new HashSet<Card>();
        refill();
    }

	public Dao<Card, Integer> getCardDao() {
		return cardDao;
	}
	public void setCardDao(Dao<Card, Integer> cardDao) {
		this.cardDao = cardDao;
	}
	public Dao<LearningData, Integer> getLearningDataDao() {
		return learningDataDao;
	}
	public void setLearningDataDao(Dao<LearningData, Integer> learningDataDao) {
		this.learningDataDao = learningDataDao;
	}

	public List<Category> getFilterCategories() {
		return filterCategories;
	}

	public void setFilterCategories(List<Category> filterCategories) {
		this.filterCategories = filterCategories;
	}

	@Override
	public Card dequeue() {
        if (!learnQueue.isEmpty()) {
            return learnQueue.get(0);
        } else {
            return null;
        }
	}
	@Override
	public void remove(Card card) {
        learnQueue.remove(card);
        dirtyCache.remove(card);
        reviewCache.remove(card);
        newCache.remove(card);
	}

	@Override
	public void refresh() {
        // Update the queue
        for (Card card : dirtyCache) {
            try {
                cardDao.update(card);
                learningDataDao.update(card.getLearningData());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
		
	}

    private void refill() {
        if (newCache.size() <= learnQueueSize) {
            newCache.addAll(getCardForReview(cacheSize - newCache.size()));
        }

        if (reviewCache.size() <= learnQueueSize) {
            reviewCache.addAll(getCardForReview(cacheSize - reviewCache.size()));
        }
        if (learnQueue.size() < learnQueueSize) {
            while (!reviewCache.isEmpty()) {
                learnQueue.add(reviewCache.get(0));
                reviewCache.remove(0);
            }
        }
        if (learnQueue.size() < learnQueueSize) {
            while (!newCache.isEmpty()) {
                learnQueue.add(newCache.get(0));
                newCache.remove(0);
            }
        }
    }

	@Override
	public void update(Card card) {
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

    public List<Card> getCardForReview(int limit) {
        try {
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            learnQb.selectColumns("id");
            learnQb.where().le("nextLearnDate", Calendar.getInstance().getTime())
                .and().gt("acqReps", "0");
            learnQb.limit((long)limit);
            QueryBuilder<Card, Integer> cardQb = cardDao.queryBuilder();
            return cardQb.where().in("learningData_id", learnQb).query();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Card> getNewCard(int limit) {
        try {
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            learnQb.selectColumns("id");
            learnQb.where().eq("acqReps", "0");
            learnQb.limit((long)limit);
            QueryBuilder<Card, Integer> cardQb = cardDao.queryBuilder();
            return cardQb.where().in("learningData_id", learnQb).query();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

