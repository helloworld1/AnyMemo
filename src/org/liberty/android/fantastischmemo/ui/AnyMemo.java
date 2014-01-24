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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.SetAlarmReceiver;
import org.liberty.android.fantastischmemo.service.AnyMemoService;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AnyMemo extends AMActivity {
    private static final String WEBSITE_VERSION="http://anymemo.org/index.php?page=version";

    public static final String EXTRA_INITIAL_TAB = "initial_tab";

    private ViewPager mViewPager;

    private PagerAdapter mPagerAdapter;

    private ActionBar actionBar;

    private SharedPreferences settings;

    private AMFileUtil amFileUtil;

    private Tab recentTab;

    private Tab openTab;

    private Tab downloadTab;

    private Tab miscTab;

    // Used to enable fast lookup from index of tab to Tab.
    private List<Tab> tabs = new ArrayList<Tab>(4);

    // Used to enable fast lookup from index of fragment to Fragment.
    private List<Fragment> fragments = new ArrayList<Fragment>(4);

    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_tabs);

        actionBar = getSupportActionBar();

        recentTab = actionBar.newTab()
                .setText(R.string.recent_tab_text)
                .setTabListener(tabListener);

        openTab = actionBar.newTab()
                .setText(R.string.open_tab_text)
                .setTabListener(tabListener);

        downloadTab = actionBar.newTab()
                .setText(R.string.download_tab_text)
                .setTabListener(tabListener);

        miscTab = actionBar.newTab()
                .setText(R.string.misc_tab_text)
                .setTabListener(tabListener);

        fragments.add(Fragment.instantiate(this, RecentListFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, OpenTabFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, DownloadTabFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, MiscTabFragment.class.getName()));

        tabs.add(recentTab);
        tabs.add(openTab);
        tabs.add(downloadTab);
        tabs.add(miscTab);

        // Initial ViewPager before action bar because
        // the action bar's tab will use ViewPager to select
        // to achieve smooth animation.
        initViewPager();

        actionBar.addTab(recentTab);
        actionBar.addTab(openTab);
        actionBar.addTab(downloadTab);
        actionBar.addTab(miscTab);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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
        int savedVersionCode = settings.getInt(AMPrefKeys.SAVED_VERSION_CODE_KEY, 1);

        int thisVersionCode; 
        try {
            thisVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            thisVersionCode = 0;
            assert false : "The version code can not be retrieved. Is it defined in build.gradle?";
        }

        boolean firstTime = settings.getBoolean(AMPrefKeys.FIRST_TIME_KEY, true);

        // Force clean preference for non-compstible versions.
        if (savedVersionCode < 154) { // Version 9.0.4
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
            } catch(IOException e){
                Log.e(TAG, "Copy file error", e);

            }
        }
        /* Detect an update */
        if (savedVersionCode != thisVersionCode) {
            SharedPreferences.Editor editor = settings.edit();
            /* save new version number */
            editor.putInt(AMPrefKeys.SAVED_VERSION_CODE_KEY, thisVersionCode);
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
        startActivity(intent);
        finish();
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

    private void initViewPager() {
        mPagerAdapter  = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        mViewPager = (ViewPager)super.findViewById(R.id.viewpager);
        mViewPager.setAdapter(this.mPagerAdapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            actionBar.selectTab(tabs.get(position));
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // Do nothing
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Do nothing

        }

    };

    private ActionBar.TabListener tabListener = new ActionBar.TabListener() {

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            mViewPager.setCurrentItem(tabs.indexOf(tab));
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            // Do nothing
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // Do nothing
        }

    };

}
