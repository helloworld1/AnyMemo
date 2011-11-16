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
import java.util.List;
import java.util.PriorityQueue;

import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.FilterDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.Filter;
import org.liberty.android.fantastischmemo.domain.LearningData;

import com.j256.ormlite.dao.Dao;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

public class LearnQueueManager implements QueueManager {
    private Dao<Card, Integer> cardDao;

    private Dao<LearningData, Integer> learningDataDao;

    private List<Category> filterCategories;

    private PriorityQueue<Card> queue;

    private int learnQueueSize;

    private int cacheSize;

    public LearnQueueManager(int learnQueueSize, int cacheSize) {
        queue = new PriorityQueue<Card>();
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
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void remove(Card card) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void update(Card card) {
		// TODO Auto-generated method stub
	}

    public List<Card> getCardForReview() throws SQLException {
        QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
        learnQb.selectColumns("id");
        learnQb.where().le("nextLearnDate", Calendar.getInstance().getTime())
            .and().gt("acqReps", "0");
        QueryBuilder<Card, Integer> cardQb = cardDao.queryBuilder();
        return cardQb.where().in("learningData_id", learnQb).query();

    }

}

