package org.liberty.android.fantastischmemo;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Context;

import android.util.Log;

/*
 * Keep the reference count of each AnyMemoDBOpenHelper.
 */
public class AnyMemoDBOpenHelperManager {
    private static Map<String, AnyMemoDBOpenHelper> helpers
        = new HashMap<String, AnyMemoDBOpenHelper>();
    private static Map<String, Integer> refCounts
        = new HashMap<String, Integer>();

    private static Map<String, Set<Future<?>>> dbTasks = new HashMap<String, Set<Future<?>>>();

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    private static String TAG = "AnyMemoDBOpenHelperManager";

    public static AnyMemoDBOpenHelper getHelper(Context context, String dbpath) {
        if (helpers.containsKey(dbpath)) {
            // Increase the reference count
            Log.i(TAG, "Call get AnyMemoDBOpenHelper with ref" + refCounts.get(dbpath)); 
            refCounts.put(dbpath, refCounts.get(dbpath) + 1);
            return helpers.get(dbpath);
        } else {
            try {
                Log.i(TAG, "Call get AnyMemoDBOpenHelper for first time."); 
                AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelper.getHelper(context, dbpath);
                helpers.put(dbpath, helper);
                refCounts.put(dbpath, 1);
                return helper;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void releaseHelper(String dbpath) {
        Integer count = refCounts.get(dbpath);
        Log.i(TAG, "Release AnyMemoDBOpenHelper: " + dbpath); 
        if (count > 1) {
            refCounts.put(dbpath, count - 1);
        } else {
            Log.i(TAG, "Close AnyMemoDBOpenHelper since there are 0 connections: " + dbpath); 
            AnyMemoDBOpenHelper helper = helpers.get(dbpath); 
            helper.close();
            helpers.remove(dbpath);
            refCounts.remove(dbpath);
        }
    }

    public static void submitDBTask(String dbpath, Runnable task) {
        Set<Future<?>> futures;
        if (!dbTasks.containsKey(dbpath)) {
            futures = new HashSet<Future<?>>();
            dbTasks.put(dbpath, futures);
        } else {
            futures = dbTasks.get(dbpath);
        }

        Future<?> f = executor.submit(task);
        refCounts.put(dbpath, refCounts.get(dbpath) + 1);
        futures.add(f);
    }

    public static void waitTask(String dbpath) {
        Set<Future<?>> futures = dbTasks.get(dbpath);
        if (futures == null) {
            return;
        }

        for (Future<?> f : futures) {
            try {
                f.get();
                releaseHelper(dbpath);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void waitAllTasks() {
        for (String path : dbTasks.keySet()) {
            waitTask(path);
        }
    }

}

