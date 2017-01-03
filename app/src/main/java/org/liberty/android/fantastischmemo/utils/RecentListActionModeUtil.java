package org.liberty.android.fantastischmemo.utils;

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

    public static final String ACTION_DESELECT_ALL = "recent-list-deselect-all";
    public static final String ACTION_REMOVE_SELECTED = "recent-list-remove-selected";
    private static final String SELECTED_ITEM_COUNT = "selectedItemCount";
    private static final String ACTION_STOP_ACTION_MODE = "recent-list-stop-action-mode";
    private static final String ACTION_START_ACTION_MODE = "recent-list-start-action-mode";
    private static final String ACTION_UPDATE_ACTION_MODE = "recent-list-update-action-mode";

    private final BaseActivity mContext;

    private final ActionModeCallback mActionModeCallback = new ActionModeCallback();

    private ActionMode mActionMode;

    private final BroadcastReceiver mStartActionMode = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mActionMode = mContext.startSupportActionMode(mActionModeCallback);
        }
    };

    private final BroadcastReceiver mUpdateActionMode = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Integer selectedItemCount = intent.getIntExtra(SELECTED_ITEM_COUNT, 0);
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

    private final BroadcastReceiver mStopActionMode = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mActionMode != null) {
                mActionMode.finish();
                mActionMode = null;
                Intent signalIntent = new Intent(ACTION_DESELECT_ALL);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(signalIntent);
            }
        }
    };

    @Inject
    public RecentListActionModeUtil(BaseActivity activityContext) {
        this.mContext = activityContext;
    }


    public void registerForActivity() {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mStartActionMode,
                new IntentFilter(ACTION_START_ACTION_MODE));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mUpdateActionMode,
                new IntentFilter(ACTION_UPDATE_ACTION_MODE));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mStopActionMode,
                new IntentFilter(ACTION_STOP_ACTION_MODE));
    }

    public void unregisterForActivity() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mStartActionMode);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdateActionMode);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mStopActionMode);
    }


    public void startActionMode() {
        Intent intent = new Intent(ACTION_START_ACTION_MODE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void updateActionMode(final Integer itemCount) {
        Intent intent = new Intent(ACTION_UPDATE_ACTION_MODE);
        intent.putExtra(SELECTED_ITEM_COUNT, itemCount);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void stopActionMode() {
        Intent intent = new Intent(ACTION_STOP_ACTION_MODE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
                    Intent intent = new Intent(ACTION_REMOVE_SELECTED);
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
            Intent intent = new Intent(ACTION_DESELECT_ALL);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }
}

