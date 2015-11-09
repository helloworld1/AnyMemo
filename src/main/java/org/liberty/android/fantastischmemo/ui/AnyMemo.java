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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.SetAlarmReceiver;
import org.liberty.android.fantastischmemo.service.AnyMemoService;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.widget.AnyMemoWidgetProvider;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class AnyMemo extends AMActivity {
    private static final String WEBSITE_VERSION="http://anymemo.org/index.php?page=version";

    public static final String EXTRA_INITIAL_TAB = "initial_tab";

    private DrawerLayout drawerLayout;

    private ViewPager viewPager;

    private ActionBar actionBar;

    private SharedPreferences settings;

    private AMFileUtil amFileUtil;

    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 1;

    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_tabs);

        // Request storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_EXTERNAL_STORAGE);
        } else {
            loadUiComponents();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadUiComponents();
                } else {
                    Toast.makeText(this, R.string.write_storage_permission_denied_message, Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                return;
            }
        }
    }

    private void loadUiComponents() {
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        actionBar = getSupportActionBar();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        initDrawer();

        prepareStoreage();
        prepareFirstTimeRun();
        prepareNotification();
    }

    /**
     * Initialize the Navigation drawer UI.
     */
    private void initDrawer() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        FragmentPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.clock_dark);
        tabLayout.getTabAt(1).setIcon(R.drawable.cabinet_dark);
        tabLayout.getTabAt(2).setIcon(R.drawable.download_tab_dark);
        tabLayout.getTabAt(3).setIcon(R.drawable.gear_dark);


        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.recent_tab_menu:
                            tabLayout.getTabAt(0).select();
                            break;
                        case R.id.open_tab_menu:
                            tabLayout.getTabAt(1).select();
                            break;
                        case R.id.download_tab_menu:
                            tabLayout.getTabAt(2).select();
                            break;
                        case R.id.misc_tab_menu:
                            tabLayout.getTabAt(2).select();
                            break;
                    }
                    menuItem.setChecked(true);
                    drawerLayout.closeDrawers();
                    return true;
                }
            }
        );

        // Change the selected navigation view
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
              @Override
              public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                  // Nothing
              }

              @Override
              public void onPageSelected(int position) {
                  navigationView.getMenu().getItem(position).setChecked(true);
              }

              @Override
              public void onPageScrollStateChanged(int state) {
                  // Nothing
              }
          }
        );



        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    public void onDestroy() {
        super.onDestroy();
        // Update the widget and cancel the notification.
        AnyMemoWidgetProvider.updateWidget(this);
        Intent myIntent = new Intent(this, AnyMemoService.class);
        myIntent.putExtra("request_code", AnyMemoService.CANCEL_NOTIFICATION);
        startService(myIntent);
    }

    @Override
    public void restartActivity() {
        // The restart activity remember the current tab.
        Intent intent = new Intent(this, this.getClass());
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private static class MainPagerAdapter extends FragmentPagerAdapter {

        private Context context;

        private Fragment[] fragments = new Fragment[]{
                new RecentListFragment(),
                new OpenTabFragment(),
                new DownloadTabFragment(),
                new MiscTabFragment()
        };

        public MainPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Display icon only
            return null;
        }
    };
}
