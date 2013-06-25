package org.liberty.android.fantastischmemo;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FilenameUtils;

import roboguice.util.Ln;

import android.content.Context;

import com.j256.ormlite.dao.DaoManager;

/*
 * Keep the reference count of each AnyMemoDBOpenHelper.
 */
public class AnyMemoDBOpenHelperManager {
    private static Map<String, WeakReference<AnyMemoDBOpenHelper>> helpers = new ConcurrentHashMap<String, WeakReference<AnyMemoDBOpenHelper>>();
    private static Map<String, Integer> refCounts = new ConcurrentHashMap<String, Integer>();

    /* Used to synchronize different method, i. e. creating and releasing helper. */
    private static ReentrantLock bigLock = new ReentrantLock();

    /* Get a db open helper and return a cached one if it was called before for the same db */
    public static AnyMemoDBOpenHelper getHelper(Context context, String dbpath) {
        bigLock.lock();
        try {
            assert dbpath != null : "dbpath should not be null";
            dbpath = FilenameUtils.normalize(dbpath);
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
            String dbpath = FilenameUtils.normalize(helper.getDbPath());

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

    public static void forceRelease(String path) {
        bigLock.lock();
        try {
            String dbpath = FilenameUtils.normalize(path);
            if (!helpers.containsKey(dbpath)) {
                Ln.w("Force release a file that is not opened yet. Do nothing");
                return;
            }

            AnyMemoDBOpenHelper helper = helpers.get(dbpath).get();

            Ln.i("force releasing " + path + " It contains " + refCounts.get(dbpath) + " refs");
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
