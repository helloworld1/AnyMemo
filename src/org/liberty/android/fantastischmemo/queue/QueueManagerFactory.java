package org.liberty.android.fantastischmemo.queue;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

public class QueueManagerFactory {
    public static QueueManager buildLearnQueueManager(
           CardDao cardDao,
          LearningDataDao learningDataDao,
          int learnQueueSize,
          int cacheSize) {
        LearnQueueManager manager = new LearnQueueManager(learnQueueSize, cacheSize);
        manager.setCardDao(cardDao);
        manager.setLearningDataDao(learningDataDao);
        return manager;
   }

}
