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
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class LearnQueueManager implements QueueManager {
    private CardDao cardDao;

    private LearningDataDao learningDataDao;

    private List<Category> filterCategories;

    private Deque<Card> learnQueue;
    private List<Card> newCache;
    private List<Card> reviewCache;
    private Set<Card> dirtyCache;

    private int learnQueueSize;

    private int cacheSize;

    private int maxNewCacheOrdinal = 0;

    private int maxReviewCacheOrdinal = 0; 

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

	public List<Category> getFilterCategories() {
		return filterCategories;
	}

	public void setFilterCategories(List<Category> filterCategories) {
		this.filterCategories = filterCategories;
	}

	@Override
	public Card dequeue() {
        refill();
        if (!learnQueue.isEmpty()) {

            Card c = learnQueue.removeFirst();
            return c;
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
        if (newCache.size() == 0) {
            List<Card> cs = getNewCards(cacheSize - newCache.size());
            if (cs.size() > 0) {
                maxNewCacheOrdinal = cs.get(cs.size() - 1).getOrdinal();
                newCache.addAll(cs);
            }
        }

        if (reviewCache.size() == 0) {
            List<Card> cs = getCardForReview(cacheSize - reviewCache.size());
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

    //TODO: change to private
    public List<Card> getCardForReview(int limit) {
        try {
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            learnQb.selectColumns("id");
            learnQb.where().le("nextLearnDate", Calendar.getInstance().getTime())
                .and().gt("acqReps", "0");
            QueryBuilder<Card, Integer> cardQb = cardDao.queryBuilder();
            Where<Card, Integer> where = cardQb.where().in("learningData_id", learnQb)
                .and().gt("ordinal", "" + maxReviewCacheOrdinal);

            cardQb.setWhere(where);
            cardQb.orderBy("ordinal", true);
            cardQb.limit((long)limit);
            List<Card> cs = cardQb.query();
            for (Card c : cs) {
                learningDataDao.refresh(c.getLearningData());
            }
            return cs;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    //TODO: change to private
    public List<Card> getNewCards(int limit) {
        try {
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            learnQb.selectColumns("id");
            learnQb.where().eq("acqReps", "0");
            QueryBuilder<Card, Integer> cardQb = cardDao.queryBuilder();
            Where<Card, Integer> where = cardQb.where().in("learningData_id", learnQb)
                .and().gt("ordinal", "" + maxNewCacheOrdinal);

            cardQb.setWhere(where);
            cardQb.orderBy("ordinal", true);
            cardQb.limit((long)limit);
            List<Card> cs = cardQb.query();
            for (Card c : cs) {
                learningDataDao.refresh(c.getLearningData());
            }
            return cs;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

