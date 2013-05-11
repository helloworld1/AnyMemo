package org.liberty.android.fantastischmemo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mycommons.io.FilenameUtils;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.DaoManager;

/*
 * Keep the reference count of each AnyMemoDBOpenHelper.
 */
public class AnyMemoDBOpenHelperManager {
    private static Map<String, AnyMemoDBOpenHelper> helpers = new ConcurrentHashMap<String, AnyMemoDBOpenHelper>();
    private static Map<String, Integer> refCounts = new ConcurrentHashMap<String, Integer>();

    private static String TAG = "AnyMemoDBOpenHelperManager";
    private static ReentrantLock bigLock = new ReentrantLock();

    /* Get a db open helper and return a cached one if it was called before for the same db */
    public static AnyMemoDBOpenHelper getHelper(Context context, String dbpath) {
        bigLock.lock();
        try {
            assert dbpath != null : "dbpath should not be null";
            dbpath = FilenameUtils.normalize(dbpath);
            if (!helpers.containsKey(dbpath)) {
                Log.i(TAG, "Call get AnyMemoDBOpenHelper for first time."); 
                AnyMemoDBOpenHelper helper = new AnyMemoDBOpenHelper(context, dbpath);
                helpers.put(dbpath, helper);
                refCounts.put(dbpath, 1);
                return helpers.get(dbpath);
            } else {
                Log.i(TAG, "Call get AnyMemoDBOpenHelper for " + dbpath + " again, return existing helper."); 
                refCounts.put(dbpath, refCounts.get(dbpath) + 1);
                return helpers.get(dbpath);
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
                Log.w(TAG, "Release a wrong db path or release an already been released helper!");
                return;
            }

            Log.i(TAG, "Release AnyMemoDBOpenHelper: " + dbpath + " Ref count: " + refCounts.get(dbpath)); 

            refCounts.put(dbpath, refCounts.get(dbpath) - 1);

            if (refCounts.get(dbpath) == 0) {
                helper.close();
                DaoManager.clearCache();
                DaoManager.clearDaoCache();
                helpers.remove(dbpath);
                Log.i(TAG, "All connection released. Close helper. DB: " + dbpath); 
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
                Log.w(TAG, "Force release a file that is not opened yet. Do nothing");
                return;
            }

            helpers.get(dbpath).close();
            DaoManager.clearCache();
            DaoManager.clearDaoCache();
            helpers.remove(dbpath);
            Log.i(TAG, "Force release a db file. DB: " + dbpath); 
        } finally {
            bigLock.unlock();
        }

    }
}
