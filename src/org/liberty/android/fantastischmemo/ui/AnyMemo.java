/*
Copyright (C) 2012 Haowen Ning

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
package org.liberty.android.fantastischmemo.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;

import org.apache.mycommons.io.FileUtils;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoService;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.SetAlarmReceiver;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.res.Resources;

import android.net.Uri;
import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import android.text.Html;

import android.text.method.LinkMovementMethod;

import android.util.Log;

import android.view.Display;
import android.view.View;

import android.widget.TabHost;
import android.widget.TextView;

public class AnyMemo extends AMActivity {
    private final static String WEBSITE_VERSION="http://anymemo.org/index.php?page=version";
    private TabHost mTabHost;
    private TabManager mTabManager;
    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_tabs);
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();

        Resources res = getResources();
        mTabManager = new TabManager(this, mTabHost, android.R.id.tabcontent);

        Bundle b = new Bundle();
        String sdPath = AMEnv.DEFAULT_ROOT_PATH;
        b.putString("default_root", sdPath);
        mTabManager.addTab(mTabHost.newTabSpec("recent").setIndicator(getString(R.string.recent_tab_text),  res.getDrawable(R.drawable.recent)),
                RecentListFragment.class, b);
        mTabManager.addTab(mTabHost.newTabSpec("open").setIndicator(getString(R.string.open_tab_text),  res.getDrawable(R.drawable.open)),
                OpenTabFragment.class, b);
        //mTabManager.addTab(mTabHost.newTabSpec("edit").setIndicator(getString(R.string.edit_tab_text),  res.getDrawable(R.drawable.edit)),
        //        EditTabFragment.class, b);
        mTabManager.addTab(mTabHost.newTabSpec("downloader").setIndicator(getString(R.string.download_tab_text),  res.getDrawable(R.drawable.download)),
                DownloadTabFragment.class, b);

        mTabManager.addTab(mTabHost.newTabSpec("misc").setIndicator(getString(R.string.misc_tab_text),  res.getDrawable(R.drawable.misc)),
                MiscTabFragment.class, null);

        // This is the default tab.
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("recent"));
        }
        
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        prepareStoreage();
        prepareFirstTimeRun();
        prepareNotification();
    }

    private void prepareStoreage() {
        File sdPath = new File(AMEnv.DEFAULT_ROOT_PATH);
        sdPath.mkdir();
        if(!sdPath.canRead()){
            DialogInterface.OnClickListener exitButtonListener = new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1){
                    finish();
                }
            };
            new AlertDialog.Builder(this)
                .setTitle(R.string.sdcard_not_available_warning_title)
                .setMessage(R.string.sdcard_not_available_warning_message)
                .setNeutralButton(R.string.exit_text, exitButtonListener)
                .create()
                .show();
        }
    }
    private void prepareFirstTimeRun() {
        File sdPath = new File(AMEnv.DEFAULT_ROOT_PATH);
        //Check the version, if it is updated from an older version it will show a dialog
        String savedVersion = settings.getString("saved_version", "");
        String thisVersion = getResources().getString(R.string.app_version);

        boolean firstTime = settings.getBoolean("first_time", true);
        /* First time installation! It will install the sample db
         * to /sdcard/AnyMemo
         */
        if(firstTime == true){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("first_time", false);
            editor.putString("recentdbpath0", AMEnv.DEFAULT_ROOT_PATH + AMEnv.DEFAULT_DB_NAME);
            editor.commit();
            try {
                InputStream in = getResources().getAssets().open(AMEnv.DEFAULT_DB_NAME);
                FileUtils.copyInputStreamToFile(in, new File(sdPath + "/" + AMEnv.DEFAULT_DB_NAME));

                InputStream in2 = getResources().getAssets().open(AMEnv.EMPTY_DB_NAME);
                FileUtils.copyInputStreamToFile(in2, getDatabasePath(AMEnv.EMPTY_DB_NAME));
                in.close();
                in2.close();
            }
            catch(IOException e){
                Log.e(TAG, "Copy file error", e);

            }
        }
        /* Detect an update */
        if(!savedVersion.equals(thisVersion)){
            SharedPreferences.Editor editor = settings.edit();
            /* save new version number */
            editor.putString("saved_version", thisVersion);
            /* Save the screen dimension for further use */
            Display display = getWindowManager().getDefaultDisplay();
            editor.putInt("screen_width", display.getWidth());
            editor.putInt("screen_height", display.getHeight());
            editor.commit();

            View alertView = View.inflate(this, R.layout.link_alert, null);
            TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
            textView.setText(Html.fromHtml(getString(R.string.what_is_new_message)));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle(getString(R.string.what_is_new))
                .setPositiveButton(getString(R.string.ok_text), null)
                .setNegativeButton(getString(R.string.about_version), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1){
                        Intent myIntent = new Intent();
                        myIntent.setAction(Intent.ACTION_VIEW);
                        myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                        myIntent.setData(Uri.parse(WEBSITE_VERSION));
                        startActivity(myIntent);
                    }
                })
            .show();
        }
    }

    private void prepareNotification() {
        SetAlarmReceiver.cancelNotificationAlarm(this);
        SetAlarmReceiver.setNotificationAlarm(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Update the widget and cancel the notification.
        Intent myIntent = new Intent(this, AnyMemoService.class);
        myIntent.putExtra("request_code", AnyMemoService.CANCEL_NOTIFICATION | AnyMemoService.UPDATE_WIDGET);
        startService(myIntent);
    }



    public static class TabManager implements TabHost.OnTabChangeListener {
        private final FragmentActivity mActivity;
        private final TabHost mTabHost;
        private final int mContainerId;
        private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
        TabInfo mLastTab;

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
            mActivity = activity;
            mTabHost = tabHost;
            mContainerId = containerId;
            mTabHost.setOnTabChangedListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mActivity));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }

            mTabs.put(tag, info);
            mTabHost.addTab(tabSpec);
        }

        @Override
        public void onTabChanged(String tabId) {
            TabInfo newTab = mTabs.get(tabId);
            if (mLastTab != newTab) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                if (mLastTab != null) {
                    if (mLastTab.fragment != null) {
                        ft.detach(mLastTab.fragment);
                    }
                }
                if (newTab != null) {
                    if (newTab.fragment == null) {
                        newTab.fragment = Fragment.instantiate(mActivity,
                                newTab.clss.getName(), newTab.args);
                        ft.add(mContainerId, newTab.fragment, newTab.tag);
                    } else {
                        ft.attach(newTab.fragment);
                    }
                }

                mLastTab = newTab;
                ft.commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        }
    }


}
