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
import java.util.List;
import java.util.Vector;

import org.apache.mycommons.io.FileUtils;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.AnyMemoService;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.SetAlarmReceiver;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMUiUtil;

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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

public class AnyMemo extends AMActivity {
    private static final String WEBSITE_VERSION="http://anymemo.org/index.php?page=version";

    public static final String EXTRA_INITIAL_TAB = "initial_tab";

    private TabHost mTabHost;
    private TabManager mTabManager;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private HorizontalScrollView mHorizontalScrollView;

    private SharedPreferences settings;

    private AMUiUtil amUiUtil;

    private AMFileUtil amFileUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_tabs);

        amUiUtil = new AMUiUtil(this);
        amFileUtil = new AMFileUtil(this);

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontal_scroll_view);

        mTabHost.setup();

        // Page must be initialized before tab hosts
        // because tab host will change tab using pager.
        initViewPager();
        initTabHosts();

        // Find out the initial tab.
        String initialTab = "recent";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            initialTab = extras.getString(EXTRA_INITIAL_TAB);
        } else if (savedInstanceState != null) {
            // This is the default tab.
            initialTab = savedInstanceState.getString("recent");
        }

        mTabHost.setCurrentTabByTag(initialTab);


        // Make sure the widget will fill the screen if the
        // screen size is large while still keep the widget
        // tabs scrollable for small screen.
        TabWidget widget = mTabHost.getTabWidget();

        int display_width_px = this.getWindowManager().getDefaultDisplay().getWidth();
        int display_width_dp = amUiUtil.convertPxToDp(display_width_px);


        // This is the minimal DP of width for the widget title.
        int minimal_dp = 80;

        int minimal_px = amUiUtil.convertDpToPx(minimal_dp);

        int width_px = minimal_px;
        if (minimal_dp * widget.getChildCount() < display_width_dp) {
            width_px = display_width_px / widget.getChildCount();
        } 
        for (int i = 0; i < widget.getChildCount(); ++i) {
            View v = widget.getChildAt(i);
            v.setMinimumWidth(width_px);
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
        String savedVersion = settings.getString(AMPrefKeys.SAVED_VERSION_KEY, "");
        String thisVersion = getResources().getString(R.string.app_version);

        boolean firstTime = settings.getBoolean(AMPrefKeys.FIRST_TIME_KEY, true);

        // Force clean preference for non-compstible versions.
        if ((!savedVersion.startsWith(thisVersion.substring(0,1)) || savedVersion.equals("9.0"))) {
            firstTime = true;
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.commit();
        }

        /* First time installation! It will install the sample db
         * to /sdcard/AnyMemo
         */
        if(firstTime == true){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(AMPrefKeys.FIRST_TIME_KEY, false);
            editor.putString(AMPrefKeys.getRecentPathKey(0), AMEnv.DEFAULT_ROOT_PATH + AMEnv.DEFAULT_DB_NAME);
            editor.commit();
            try {
                amFileUtil.copyFileFromAsset(AMEnv.DEFAULT_DB_NAME,  new File(sdPath + "/" + AMEnv.DEFAULT_DB_NAME));

                InputStream in2 = getResources().getAssets().open(AMEnv.EMPTY_DB_NAME);
                String emptyDbPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + AMEnv.EMPTY_DB_NAME;
                FileUtils.copyInputStreamToFile(in2, new File(emptyDbPath));
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
            editor.putString(AMPrefKeys.SAVED_VERSION_KEY, thisVersion);
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

    @Override
    public void restartActivity() {
        // The restart activity remember the current tab.
        Intent intent = new Intent(this, this.getClass());
        intent.putExtra(EXTRA_INITIAL_TAB, mTabHost.getCurrentTabTag());
        startActivity(intent);
        finish();
    }

    @SuppressWarnings("unused")
    private class TabManager {
        private final FragmentActivity mActivity;
        private final TabHost mTabHost;
        private final int mContainerId;
        private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
        TabInfo mLastTab;

        private final class TabInfo {
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

        private class DummyTabFactory implements TabHost.TabContentFactory {
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
            mTabHost.setOnTabChangedListener(onTabChangeLilstener);
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
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;

        public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

    // Don't forget to add the new tab to view pager
    private void initTabHosts() {
        mTabManager = new TabManager(this, mTabHost, android.R.id.tabcontent);
        Resources res = getResources();
        mTabManager.addTab(mTabHost.newTabSpec("recent").setIndicator(getString(R.string.recent_tab_text),  res.getDrawable(R.drawable.recent)),
                RecentListFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("open").setIndicator(getString(R.string.open_tab_text),  res.getDrawable(R.drawable.open)),
                OpenTabFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("downloader").setIndicator(getString(R.string.download_tab_text),  res.getDrawable(R.drawable.download)),
                DownloadTabFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("misc").setIndicator(getString(R.string.misc_tab_text),  res.getDrawable(R.drawable.misc)),
                MiscTabFragment.class, null);
    }

    private void initViewPager() {
        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, RecentListFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, OpenTabFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, DownloadTabFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, MiscTabFragment.class.getName()));
        mPagerAdapter  = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        mViewPager = (ViewPager)super.findViewById(R.id.viewpager);
        mViewPager.setAdapter(this.mPagerAdapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }

    private TabHost.OnTabChangeListener onTabChangeLilstener =
        new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
                int pos = mTabHost.getCurrentTab();
                mViewPager.setCurrentItem(pos);
				
			}
        };

    private ViewPager.OnPageChangeListener onPageChangeListener = 
        new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mTabHost.setCurrentTab(position);
            }

			@Override
			public void onPageScrollStateChanged(int arg0) {
                // Do nothing
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Scroll the horizontal scroll bar that wrap the tabwidget
                // so the current tab title can be visible.
                View tabView = mTabHost.getTabWidget().getChildAt(position);
                final int width = mHorizontalScrollView.getWidth(); 
                int scrollPos = tabView.getLeft() - (width - tabView.getWidth()) / 2; 

                mHorizontalScrollView.scrollTo(scrollPos, 0);
                mHorizontalScrollView.refreshDrawableState();

			}

        };
}
