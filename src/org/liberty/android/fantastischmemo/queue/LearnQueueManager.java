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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.utils.AnyMemoExecutor;

import roboguice.util.Ln;
import android.content.Context;

public class LearnQueueManager implements QueueManager {
    /*
     * The scheduler to determine whether a card should reimain
     * in the learn queue
     */
    private Scheduler scheduler;

    private Category filterCategory;

    private List<Card> learnQueue;
    private List<Card> newCache;
    private List<Card> reviewCache;
    private BlockingQueue<Card> dirtyCache;

    private int learnQueueSize;

    private int cacheSize;

    private boolean shuffle;

    private Context context;

    private String dbPath;

    private LearnQueueManager(Builder builder) {
        this.filterCategory = builder.filterCategory;
        this.learnQueueSize = builder.learnQueueSize;
        this.cacheSize = builder.cacheSize;
        this.shuffle = builder.shuffle;
        this.scheduler = builder.scheduler;
        this.context = builder.context;
        this.dbPath = builder.dbPath;

        learnQueue = Collections.synchronizedList(new LinkedList<Card>());
        newCache = Collections.synchronizedList(new LinkedList<Card>());
        reviewCache = Collections.synchronizedList(new LinkedList<Card>());

        // Make sure the dirtyCache is thread safe because multiple threads will access
        // the set
        dirtyCache = new LinkedBlockingQueue<Card>();
    }

    @Override
    public synchronized Card dequeue() {
        shuffle();
        if (!learnQueue.isEmpty()) {
            Card c = learnQueue.get(0);
            Ln.d("Dequeue card: " + c.getId());
            return c;
        } else {
            return null;
        }
    }

    @Override
    public synchronized Card dequeuePosition(int cardId) {
        position(cardId);

        if (!learnQueue.isEmpty()) {
            Card c = learnQueue.get(0);
            Ln.d("Dequeue card: " + c.getId());
            return c;
        } else {
            return null;
        }

    }

    @Override
    public synchronized void remove(Card card) {
        learnQueue.remove(card);
        reviewCache.remove(card);
        newCache.remove(card);
    }

    @Override
    public synchronized void release() {
        // Make sure the cache is flushed.
        flushDirtyCache();
        AnyMemoExecutor.waitAllTasks();
    }

    private synchronized void refill() {
        final AnyMemoDBOpenHelper dbOpenHelper = AnyMemoDBOpenHelperManager
                .getHelper(context, dbPath);
        final CardDao cardDao = dbOpenHelper.getCardDao();
        dumpLearnQueue();
        List<Card> exclusionList = new ArrayList<Card>(learnQueue.size()
                + dirtyCache.size());
        exclusionList.addAll(learnQueue);
        exclusionList.addAll(dirtyCache);

        try {
            if (newCache.size() == 0) {
                List<Card> cs = cardDao.getNewCards(filterCategory,
                        exclusionList, cacheSize);
                if (cs.size() > 0) {
                    newCache.addAll(cs);
                }
            }

            if (reviewCache.size() == 0) {
                List<Card> cs = cardDao.getCardsForReview(filterCategory,
                        exclusionList, cacheSize);
                if (cs.size() > 0) {
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
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        }
        flushDirtyCache();
        dumpLearnQueue();
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
        // Make sure to remove the stale cache first.
        // Set.add(Object) will not overwrite object.
        remove(card);
        if (!scheduler.isCardLearned(card.getLearningData())) {
            // Add to the back of the queue
            learnQueue.add(card);
        }
        try {
            dirtyCache.put(card);
        } catch (InterruptedException e) {
            Ln.e(e, "Updating card is interrupted");
            assert false : "The update should not be interrupted";
        }
        refill();
    }

    private synchronized void position(int cardId) {
        Iterator<Card> learnIterator= learnQueue.iterator();
        Iterator<Card> reviewCacheIterator = reviewCache.iterator();
        Iterator<Card> newCacheIterator = newCache.iterator();

        int learnQueueRotateDistance = 0;
        while (learnIterator.hasNext()) {
            Card c =learnIterator.next();
            if (c.getId() == cardId) {
                int index = learnQueue.indexOf(c);
                learnQueueRotateDistance = -index;
                Ln.i("Rotate index: " + index);
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

        final AnyMemoDBOpenHelper dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(context, dbPath);
        final CardDao cardDao = dbOpenHelper.getCardDao();
        final LearningDataDao learningDataDao = dbOpenHelper.getLearningDataDao();
        try {
            Card headCard = null;
            headCard = cardDao.queryForId(cardId);
            learningDataDao.refresh(headCard.getLearningData());

            learnQueue.add(0, headCard);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        }
    }

    private synchronized void flushDirtyCache() {
        AnyMemoExecutor.submit(new Runnable() {
            public void run() {
                // Update the queue
                final AnyMemoDBOpenHelper dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(context, dbPath);
                final CardDao cardDao = dbOpenHelper.getCardDao();
                final LearningDataDao learningDataDao = dbOpenHelper.getLearningDataDao();

                try {
                    learningDataDao.callBatchTasks (
                        new Callable<Void>() {
                            public Void call() throws Exception {
                                Ln.i("Flushing dirty cache. # of cards to flush: " + dirtyCache.size());
                                while (!dirtyCache.isEmpty()) {
                                    Card card = dirtyCache.take();
                                    Ln.i("Flushing card id: " + card.getId() + " with learning data: " + card.getLearningData());
                                    if (learningDataDao.update(card.getLearningData()) == 0) {
                                        Ln.w("LearningDataDao update failed for : " + card.getLearningData());
                                        throw new RuntimeException("LearningDataDao update failed! LearningData to update: " + card.getLearningData() + " current value: " + learningDataDao.queryForId(card.getLearningData().getId()));
                                    }
                                    if (cardDao.update(card) == 0) {
                                        Ln.w("CardDao update failed for : " + card.getLearningData());
                                        throw new RuntimeException("CardDao update failed. Card to update: " + card);
                                    }
                                }
                                Ln.i("Flushing dirty cache done.");
                                return null;
                            }
                        });
                } finally {
                    AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
                }
            }
        });
    }

    private void dumpLearnQueue() {
        StringBuilder sb = new StringBuilder();
        sb.append("LearnQueue : card ids[");
        for (Card c : learnQueue) {
            sb.append("" + c.getId() + ", ");
        }
        sb.append("]\n");

        // sb.append("LearnQueue: card learning data[");
        // for (Card c : learnQueue) {
        //     sb.append("" + c.getLearningData() + "\n ");
        // }
        // sb.append("]\n");

        sb.append("Dirty cache: card ids[");
        for (Card c : dirtyCache) {
            sb.append("" + c.getId() + ", ");
        }
        sb.append("]\n");

        // sb.append("Dirty cache: card learning data[");
        // for (Card c : dirtyCache) {
        //     sb.append("" + c.getLearningData() + "\n ");
        // }
        // sb.append("]\n");
        Ln.v(sb.toString());
    }

    public static class Builder {
        private Scheduler scheduler;

        private Category filterCategory;

        private int learnQueueSize = 10;

        private int cacheSize = 50;

        private boolean shuffle = false;

        private String dbPath;

        private Context context;

        public Builder(Context context, String dbPath) {
            this.dbPath = dbPath;
            this.context = context;
        }

        public Builder setScheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
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
            LearnQueueManager qm = new LearnQueueManager(this);
            qm.refill();
            return qm;
        }
    }
}

