package org.liberty.android.fantastischmemo.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.modules.PerActivity;

import javax.inject.Inject;

@PerActivity
public class RecentListActionModeUtil {

    private final BaseActivity mContext;

    private final ActionModeCallback mActionModeCallback = new ActionModeCallback();

    private ActionMode mActionMode;

    private BroadcastReceiver mStartActionMode;

    private BroadcastReceiver mUpdateActionMode;

    private BroadcastReceiver mStopActionMode;

    @Inject
    public RecentListActionModeUtil(BaseActivity activityContext) {
        this.mContext = activityContext;
    }


    public void register() {
        mStartActionMode = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mActionMode = mContext.startSupportActionMode(mActionModeCallback);
            }
        };
        mUpdateActionMode = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Integer selectedItemCount = intent.getIntExtra("selectedItemCount", 0);
                if (selectedItemCount == 0 && mActionMode != null) {
                    mActionMode.finish();
                    mActionMode = null;
                }
                if (mActionMode != null) {
                    final String selectionText = String.valueOf(selectedItemCount);
                    mActionMode.setTitle(selectionText);
                }
            }
        };
        mStopActionMode = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mActionMode != null) {
                    mActionMode.finish();
                    mActionMode = null;
                    Intent signalIntent = new Intent("recent-list-clear-selection");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(signalIntent);
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mStartActionMode,
                new IntentFilter("recent-list-start-action-mode"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mUpdateActionMode,
                new IntentFilter("recent-list-update-action-mode"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mStopActionMode,
                new IntentFilter("recent-list-stop-action-mode"));
    }

    public void deregister() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mStartActionMode);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdateActionMode);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mStopActionMode);
    }

    public class ActionModeCallback implements ActionMode.Callback {
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.action_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_menu_remove:
                    Intent intent = new Intent("recent-list-remove-selected");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    Log.d(TAG, "menu_remove");
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Intent intent = new Intent("recent-list-clear-selection");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }
}

