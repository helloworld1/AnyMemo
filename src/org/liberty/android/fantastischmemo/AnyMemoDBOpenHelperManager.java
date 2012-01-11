package org.liberty.android.fantastischmemo;

import java.sql.SQLException;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
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
    private static Map<String, AnyMemoDBOpenHelper> helpers = new ConcurrentHashMap<String, AnyMemoDBOpenHelper>();
    private static Map<String, DBConnection> dbConnections = new ConcurrentHashMap<String, DBConnection>(); 

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    private static String TAG = "AnyMemoDBOpenHelperManager";

    // TODO: Remove all synchronized and use java.concurrent's map
    public static synchronized AnyMemoDBOpenHelper getHelper(Context context, String dbpath) {
        if (helpers.containsKey(dbpath)) {
            DBConnection conn = dbConnections.get(dbpath);
            conn.acquireConnection();
            return helpers.get(dbpath);
        } else {
            try {
                Log.i(TAG, "Call get AnyMemoDBOpenHelper for first time."); 
                AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelper.getHelper(context, dbpath);
                DBConnection conn = new DBConnection(dbpath);
                dbConnections.put(dbpath, conn);
                helpers.put(dbpath, helper);
                return helper;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static synchronized void releaseHelper(String dbpath) {
        Log.i(TAG, "Release AnyMemoDBOpenHelper: " + dbpath); 
        DBConnection conn = dbConnections.get(dbpath);
        if (conn == null) {
            throw new RuntimeException("release an non-existing db helper");
        }

        if (conn.getDbState().contains(DBState.ACTIVE_CONNECTION)) {
            conn.releaseConnection();
        }

        if (conn.getDbState().isEmpty()) {
            Log.i(TAG, "All connection released. Close helper. DB: " + dbpath); 
            AnyMemoDBOpenHelper helper = helpers.get(dbpath); 
            helper.close();
            helpers.remove(dbpath);
        } else if (conn.getDbState().contains(DBState.TASK_RUNNING)) {
            Log.i(TAG, "There are still task running when releasing helper. Will be released when the task is done"); 
        } 
    }

    public static synchronized Future<?> submitDBTask(String dbpath, Runnable task) {
        DBConnection conn = dbConnections.get(dbpath);
        return conn.submitDBTask(task);
    }

    public static synchronized void waitTask(String dbpath, Future<?> f) {
        DBConnection conn = dbConnections.get(dbpath);
        conn.waitTask(f);
        if (conn.getDbState().isEmpty()) {
            releaseHelper(dbpath);
        }
    }

    public static synchronized void waitAllTasks(String dbpath) {
        DBConnection conn = dbConnections.get(dbpath);
        conn.waitAllTasks();
    }

    public static synchronized void waitAllTasks() {
        for (String path : dbConnections.keySet()) {
            waitAllTasks(path);
        }
    }

    private static class DBConnection {
        private int connections = 0;
        private Set<Future<?>> tasks;
        private EnumSet<DBState> dbState;

        public DBConnection(String dbpath) {
            tasks = new HashSet<Future<?>>();
            dbState = EnumSet.noneOf(DBState.class);
        }

        public synchronized void acquireConnection() {
            connections += 1;
            dbState.add(DBState.ACTIVE_CONNECTION);
        }

        public synchronized void releaseConnection() {
            if (connections > 0) {
                connections -= 1;
            } else {
                Log.e("DBConnection", "Try to release non active connection");
            }
            if (!tasks.isEmpty()) {
                dbState.remove(DBState.TASK_RUNNING);
            } 
            if (connections == 0) {
                dbState.remove(DBState.ACTIVE_CONNECTION);
            }
        }

        public synchronized Future<?> submitDBTask(Runnable task) {
            Future<?> f = executor.submit(task);
            tasks.add(f);
            dbState.add(DBState.TASK_RUNNING);
            return f;
        }

        public synchronized void waitTask(Future<?> f) {
            try {
                f.get();
                tasks.remove(f);
                if (tasks.isEmpty()) {
                    dbState.remove(DBState.TASK_RUNNING);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        public synchronized void waitAllTasks() {
            Iterator<Future<?>> fi = tasks.iterator();
            while (fi.hasNext()) {
                Future<?> f = fi.next();
                try {
                    f.get();
                    fi.remove();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            if (tasks.isEmpty()) {
                dbState.remove(DBState.TASK_RUNNING);
            } else {
                throw new AssertionError("The tasks shoul be empty!");
            }
        }
        
        public EnumSet<DBState> getDbState() {
            return dbState;
        }
    }

    private static enum DBState {
        ACTIVE_CONNECTION,
        TASK_RUNNING
    }

}
