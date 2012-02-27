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

import java.util.LinkedList;
import java.util.List;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;

public class CramQueueManager implements QueueManager {

    private CardDao cardDao;
    private List<Card> learnQueue; 
    private final int learnQueueSize;


    private CramQueueManager(Builder builder) {
        this.cardDao = builder.cardDao;
        this.learnQueueSize = builder.learnQueueSize;
        learnQueue = new LinkedList<Card>();
    }

	public void setCardDao(CardDao cardDao) {
		this.cardDao = cardDao;
	}

	@Override
	public synchronized void update(Card card) {
        if (card.getLearningData().getGrade() >= 2) {
            learnQueue.remove(card);
            try {
                cardDao.update(card);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Add to the back of the queue
            learnQueue.remove(card);
            learnQueue.add(card);
        }
    }
    
	@Override
	public synchronized void remove(Card card) {
        learnQueue.remove(card);
    }

	@Override
	public synchronized Card dequeue() {
        refill();
        if (learnQueue.size() > 0) {
            Card c = learnQueue.get(0);
            learnQueue.remove(0);
            return c;
        } else {
            return null;
        }
    }

	@Override
	public synchronized void flush() {
        // Do nothing
    }

    public static class Builder {

        private CardDao cardDao;

        private int learnQueueSize;

		public Builder setCardDao(CardDao cardDao) {
			this.cardDao = cardDao;
            return this;
		}

		public Builder setLearnQueueSize(int learnQueueSize) {
			this.learnQueueSize = learnQueueSize;
            return this;
		}

        public QueueManager build() {
            if (cardDao == null) {
                throw new AssertionError("cardDao must set");
            }
            return new CramQueueManager(this);
        }
    }

    private void refill() {
        int limit = learnQueueSize - learnQueue.size();
        if (limit > 0) {
            List<Card> fillCards = cardDao.getRandomReviewedCards(null, limit);
            learnQueue.addAll(fillCards);
        }

    }

}
