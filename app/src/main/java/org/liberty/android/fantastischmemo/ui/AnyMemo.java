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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Objects;

import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.BuildConfig;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.common.AMPrefKeys;
import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.databinding.MainTabsBinding;
import org.liberty.android.fantastischmemo.receiver.SetAlarmReceiver;
import org.liberty.android.fantastischmemo.service.AnyMemoService;
import org.liberty.android.fantastischmemo.ui.loader.MultipleLoaderManager;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AboutUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;
import org.liberty.android.fantastischmemo.utils.RecentListActionModeUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;
import org.liberty.android.fantastischmemo.widget.AnyMemoWidgetProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class AnyMemo extends BaseActivity {
    private static final String WEBSITE_VERSION="https://anymemo.org/versions-view";

    private SharedPreferences settings;

    private CompositeDisposable disposables;

    private MainTabsBinding binding;

    @Inject AMFileUtil amFileUtil;

    @Inject RecentListUtil recentListUtil;

    @Inject DatabaseUtil databaseUtil;

    @Inject MultipleLoaderManager multipleLoaderManager;

    @Inject AboutUtil aboutUtil;

    @Inject RecentListActionModeUtil recentListActionModeUtil;

    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponents().inject(this);
        disposables = new CompositeDisposable();

        binding = DataBindingUtil.setContentView(this, R.layout.main_tabs);

        // Request storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_EXTERNAL_STORAGE);
        } else {
            loadUiComponents();
        }
        recentListActionModeUtil.registerForActivity();
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
            }
        }
    }

    private void loadUiComponents() {
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        initDrawer();
        initCreateDbFab();

        prepareStoreage();
        prepareFirstTimeRun();
        prepareNotification();

        if (getIntent() != null && Objects.equal(getIntent().getAction(), Intent.ACTION_VIEW)) {
            handleOpenIntent();

            // Set the action null when the intent is handled to prevent the logic being executed
            // again when the screen is rotated
            getIntent().setAction(null);
        }
    }

    /**
     * Initialize the Navigation drawer UI.
     */
    private void initDrawer() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        FragmentPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        final TabLayout tabLayout = binding.tabs;
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.clock);
        tabLayout.getTabAt(1).setIcon(R.drawable.cabinet);
        tabLayout.getTabAt(2).setIcon(R.drawable.download_tab);
        tabLayout.getTabAt(3).setIcon(R.drawable.misc);

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
                            tabLayout.getTabAt(3).select();
                            break;
                        case R.id.option_tab_menu:
                            startActivity(new Intent(tabLayout.getContext(), OptionScreen.class));
                            break;
                        case R.id.about_tab_menu:
                            aboutUtil.createAboutDialog();
                            break;
                    }
                    menuItem.setChecked(true);
                    binding.drawerLayout.closeDrawers();
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

                  // Only add db FAB show in the file browser fragment
                  if (position == 1) {
                      binding.addDbFab.setVisibility(View.VISIBLE);
                  } else {
                      binding.addDbFab.setVisibility(View.GONE);
                  }
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
            if (BuildConfig.DEBUG) {
                throw new RuntimeException("The version code can not be retrieved. Is it defined in build.gradle?");
            }
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

    /**
     * Handle the "VIEW" action for other app to open a db
     */
    private void handleOpenIntent() {
        multipleLoaderManager.registerLoaderCallbacks(1, new HandleOpenIntentLoaderCallbacks(getIntent().getData()), false);
        multipleLoaderManager.startLoading();
    }

    /**
     * We create the FAB only at Activity level to make sure the FAB is not scrolling up and down
     * with appbar_scrolling_view_behavior. The behavior will make the FAB half visible if the FAB is
     * inside a ViewPager fragment.
     */
    private void initCreateDbFab() {
        // Make sure the addDbFab is only shown on FileBrowser fragment
        if (binding.tabs.getSelectedTabPosition() != 1) {
            binding.addDbFab.setVisibility(View.GONE);
        }
        binding.addDbFab.setOnClickListener(new CardFragment.OnClickListener() {
            @Override
            public void onClick(View v) {
                disposables.add(activityComponents().databaseOperationDialogUtil().showCreateDbDialog(AMEnv.DEFAULT_ROOT_PATH)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        appComponents().eventBus().post(new FileBrowserFragment.RefreshFileListEvent(file.getParentFile()));
                    }
                }));
            }
        });
    }

    @Override
    public void onDestroy() {
        recentListActionModeUtil.unregisterForActivity();
        disposables.dispose();

        super.onDestroy();
        // Update the widget and cancel the notification.
        AnyMemoWidgetProvider.updateWidget(this);
        Intent myIntent = new Intent(this, AnyMemoService.class);
        myIntent.putExtra("request_code", AnyMemoService.CANCEL_NOTIFICATION);
        startService(myIntent);

        if (multipleLoaderManager != null) {
            multipleLoaderManager.destroy();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * The loader to copy the db from temporary location to AnyMemo folder
     */
    private class HandleOpenIntentLoaderCallbacks implements LoaderManager.LoaderCallbacks<File> {

        private final Uri contentUri;

        public HandleOpenIntentLoaderCallbacks(Uri contentUri) {
            this.contentUri = contentUri;
        }

        @Override
        public Loader<File> onCreateLoader(int id, Bundle args) {
            Loader<File> loader = new AsyncTaskLoader<File>(AnyMemo.this) {
                @Override
                public File loadInBackground() {
                    String[] splittedUri = contentUri.toString().split("/");
                    String newFileName = splittedUri[splittedUri.length - 1];
                    if (!newFileName.endsWith(".db")) {
                        newFileName += ".db";
                    }

                    File newFile = new File(AMEnv.DEFAULT_ROOT_PATH + "/" + newFileName);
                    // First detect if the db with the same name exists.
                    // And back kup the db if
                    try {
                        amFileUtil.deleteFileWithBackup(newFile.getAbsolutePath());
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to delete the exisitng db with backup", e);
                    }

                    InputStream inputStream;

                    try {
                        inputStream = AnyMemo.this.getContentResolver().openInputStream(contentUri);
                        FileUtils.copyInputStreamToFile(inputStream, newFile);
                    } catch (IOException e) {
                        Log.e(TAG, "Error opening file from intent", e);
                        return null;
                    }

                    if (!databaseUtil.checkDatabase(newFile.getAbsolutePath())) {
                        Log.e(TAG, "Database is corrupted: " + newFile.getAbsolutePath());
                        newFile.delete();
                        return null;
                    };

                    return newFile;
                }
            };
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<File> loader, File newFile) {
            if (newFile == null) {
                Snackbar.make(binding.drawerLayout, R.string.db_file_is_corrupted_text, Snackbar.LENGTH_LONG)
                        .show(); // Don’t forget to show!
                Log.e(TAG, "Could not load db from intent");
                return;
            }

            recentListUtil.addToRecentList(newFile.getAbsolutePath());

            Intent intent = new Intent();
            intent.setClass(AnyMemo.this, PreviewEditActivity.class);
            intent.putExtra(PreviewEditActivity.EXTRA_DBPATH, newFile.getAbsolutePath());
            startActivity(intent);
            multipleLoaderManager.checkAllLoadersCompleted();
        }

        @Override
        public void onLoaderReset(Loader<File> loader) {
            // Nothing
        }
    }


    private static class MainPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] fragments = new Fragment[]{
                new RecentListFragment(),
                new OpenTabFragment(),
                new DownloadTabFragment(),
                new MiscTabFragment()
        };

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);

            // Set arguments for the OpenTabFragment fragment
            // to show the create action
            Bundle bundle = new Bundle();
            bundle.putBoolean(FileBrowserFragment.EXTRA_SHOW_CREATE_DB_BUTTON, true);
            fragments[1].setArguments(bundle);
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
    }
}
