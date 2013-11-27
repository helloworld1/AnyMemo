/*
Copyright (C) 2013 Haowen Ning

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

package org.liberty.android.fantastischmemo.ui.loader;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;

import roboguice.util.Ln;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

/**
 * This class manages multiple loader and handle the completion of all
 * of them.
 */
public class MultipleLoaderManager {

    private volatile int runningLoaderCount = 0;

    private Handler handler = new Handler();

    private Runnable onAllLoaderCompletedRunnable = null;

    private ProgressDialog progressDialog;
    
    private Map<Integer, LoaderCallbacks<?>> loaderCallbackMap
        = new HashMap<Integer, LoaderCallbacks<?>>();

    private Map<Integer, Boolean> loaderReloadOnStartMap
        = new HashMap<Integer, Boolean>();

    private AMActivity activity;

    @Inject
    public MultipleLoaderManager(Activity activity) {
        this.activity = (AMActivity) activity;
    }

    public void registerLoaderCallbacks(int id, LoaderCallbacks<?> callbacks, boolean reloadOnStart) {
        loaderCallbackMap.put(id, callbacks);
        loaderReloadOnStartMap.put(id, reloadOnStart);
    }

    public void setOnAllLoaderCompletedRunnable(Runnable onAllLoaderCompletedRunnable) {
        this.onAllLoaderCompletedRunnable = onAllLoaderCompletedRunnable;
    }

    /**
     * @param forceReload if it is true, all loader will be reloaded, if not, the
     * loader will only be reloadeed if it is registered with reloadOnStart = true.
     */
    public void startLoading(boolean forceReload) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(R.string.loading_please_wait);
        progressDialog.setMessage(activity.getString(R.string.loading_database));
        progressDialog.setCancelable(false);
        progressDialog.show();

        LoaderManager.enableDebugLogging(true);
        LoaderManager loaderManager = activity.getSupportLoaderManager();
        for (int id : loaderCallbackMap.keySet()) {
            if (loaderReloadOnStartMap.get(id) || forceReload) {
                loaderManager.restartLoader(id, null, loaderCallbackMap.get(id));
            } else {
                loaderManager.initLoader(id, null, loaderCallbackMap.get(id));
            }
            runningLoaderCount++;
        }
        // runningLoaderCount = loaderCallbackMap.size();
    }

    public void startLoading() {
        startLoading(false);
    }

    public synchronized void checkAllLoadersCompleted() {
        runningLoaderCount--;

        // The onPostInit is running on UI thread.
        if (runningLoaderCount <= 0 && onAllLoaderCompletedRunnable != null) {
            Ln.v("Dismiss dialog");
            progressDialog.dismiss();
            handler.post(onAllLoaderCompletedRunnable);
        }
    }

    /**
     * This method needs to be called in Activity's onDestroy.
     */
    public synchronized void destroy() {
        // The handler needs to remove the callbacks to avoid the race condition
        // that onPostInitRunnable is running after onDestroy.

        if (onAllLoaderCompletedRunnable != null) {
            handler.removeCallbacks(onAllLoaderCompletedRunnable);
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        // Don't keep a reference of activity
        activity = null;
    }
}
