package org.liberty.android.fantastischmemo.queue;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Category;

public class QueueManagerFactory {
    public static QueueManager buildLearnQueueManager(
           CardDao cardDao,
          LearningDataDao learningDataDao,
          int learnQueueSize,
          int cacheSize, Category filterCategory) {
        LearnQueueManager manager = new LearnQueueManager(learnQueueSize, cacheSize);
        manager.setCardDao(cardDao);
        manager.setLearningDataDao(learningDataDao);
        manager.setFilterCategory(filterCategory);
        return manager;
   }

   public static QueueManager buildCramQueueManager(
           CardDao cardDao,
           int learnQueueSize,
           Category filterCategory) {

       CramQueueManager manager = new CramQueueManager(learnQueueSize);
       manager.setCardDao(cardDao);
       return manager;
   }

}
