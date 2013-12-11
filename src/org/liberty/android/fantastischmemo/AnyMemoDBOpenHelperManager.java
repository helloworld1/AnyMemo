package org.liberty.android.fantastischmemo;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FilenameUtils;

import roboguice.util.Ln;
import android.content.Context;

import com.j256.ormlite.dao.DaoManager;

/*
 * Keep the reference count of each AnyMemoDBOpenHelper.
 */
public class AnyMemoDBOpenHelperManager {

    // Comparator for determining if two files are the same
    private static Comparator<String> filenameComparator = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            if (FilenameUtils.equalsNormalizedOnSystem(lhs, rhs)) {
                return 0;
            } else {
                return lhs.compareTo(rhs);
            }
        }
    };

    private static volatile Map<String, WeakReference<AnyMemoDBOpenHelper>> helpers = Collections.synchronizedMap(new TreeMap<String, WeakReference<AnyMemoDBOpenHelper>>(filenameComparator));

    private static volatile Map<String, Integer> refCounts = Collections.synchronizedMap(new TreeMap<String, Integer>(filenameComparator));

    /* Used to synchronize different method, i. e. creating and releasing helper. */
    private static volatile ReentrantLock bigLock = new ReentrantLock();

    /**
     * Get a DBOpenHelper with application context.
     */
    public static AnyMemoDBOpenHelper getHelper(String dbpath) {
        return getHelper(AMApplication.getCurrentApplicationContext(), dbpath);
    }

    /* Get a db open helper and return a cached one if it was called before for the same db */
    public static AnyMemoDBOpenHelper getHelper(Context context, String dbpath) {
        bigLock.lock();
        try {
            assert dbpath != null : "dbpath should not be null";
            if (!helpers.containsKey(dbpath)) {
                Ln.i("Call get AnyMemoDBOpenHelper for first time.");
                AnyMemoDBOpenHelper helper = new AnyMemoDBOpenHelper(context, dbpath);
                helpers.put(dbpath, new WeakReference<AnyMemoDBOpenHelper>(helper));
                refCounts.put(dbpath, 1);
                return helpers.get(dbpath).get();
            } else {
                Ln.i("Call get AnyMemoDBOpenHelper for " + dbpath + " again, return existing helper.");
                refCounts.put(dbpath, refCounts.get(dbpath) + 1);
                return helpers.get(dbpath).get();
            }
        } finally {
            bigLock.unlock();
        }
    }

    /* Release a db open helper if there is no open connection to it */
    public static void releaseHelper(AnyMemoDBOpenHelper helper) {
        bigLock.lock();
        try {
            String dbpath = helper.getDbPath();
            if (!helpers.containsKey(dbpath)) {
                Ln.w("Release a wrong db path or release an already been released helper!");
                return;
            }

            Ln.i("Release AnyMemoDBOpenHelper: " + dbpath + " Ref count: " + refCounts.get(dbpath));

            refCounts.put(dbpath, refCounts.get(dbpath) - 1);

            if (refCounts.get(dbpath) == 0) {
                helper.close();
                DaoManager.clearCache();
                DaoManager.clearDaoCache();
                helpers.remove(dbpath);
                Ln.i("All connection released. Close helper. DB: " + dbpath);
            }
        } finally {
            bigLock.unlock();
        }
    }

    public static void forceRelease(String dbpath) {
        bigLock.lock();
        try {
            if (!helpers.containsKey(dbpath)) {
                Ln.w("Force release a file that is not opened yet. Do nothing");
                return;
            }

            AnyMemoDBOpenHelper helper = helpers.get(dbpath).get();

            Ln.i("force releasing " + dbpath + " It contains " + refCounts.get(dbpath) + " refs");
            // Weak reference can return null, so we must check here.
            if (helper != null) {
                helper.close();
            } else {
                Ln.w("forceRelease a path that has already been released by GC.");
            }
            DaoManager.clearCache();
            DaoManager.clearDaoCache();
            helpers.remove(dbpath);
            refCounts.get(dbpath);
            Ln.i("Force released a db file. DB: " + dbpath);
        } finally {
            bigLock.unlock();
        }

    }
}
