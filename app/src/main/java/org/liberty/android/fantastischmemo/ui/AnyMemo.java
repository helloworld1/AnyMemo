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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
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
import org.liberty.android.fantastischmemo.ui.loader.MultipleLoaderManager;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AboutUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseOperationDialogUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;
import org.liberty.android.fantastischmemo.utils.NotificationUtil;
import org.liberty.android.fantastischmemo.utils.RecentListActionModeUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;
import org.liberty.android.fantastischmemo.widget.AnyMemoWidgetProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

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

    @Inject DatabaseOperationDialogUtil databaseOperationDialogUtil;

    @Inject NotificationUtil notificationUtil;

    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activityComponents().inject(this);
        super.onCreate(savedInstanceState);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        binding.navView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.recent_tab_menu:
                            binding.bottomNavigation.setSelectedItemId(R.id.recent_tab_menu);
                            break;
                        case R.id.open_tab_menu:
                            binding.bottomNavigation.setSelectedItemId(R.id.open_tab_menu);
                            break;
                        case R.id.download_tab_menu:
                            binding.bottomNavigation.setSelectedItemId(R.id.download_tab_menu);
                            break;
                        case R.id.misc_tab_menu:
                            binding.bottomNavigation.setSelectedItemId(R.id.misc_tab_menu);
                            break;
                        case R.id.option_tab_menu:
                            startActivity(new Intent(AnyMemo.this, OptionScreen.class));
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

        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Fragment fragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.recent_tab_menu:
                        fragment = new RecentListFragment();
                        break;

                    case R.id.open_tab_menu:
                        fragment = new OpenTabFragment();
                        Bundle args = new Bundle();
                        args.putBoolean(FileBrowserFragment.EXTRA_SHOW_CREATE_DB_BUTTON, true);
                        fragment.setArguments(args);
                        break;

                    case R.id.download_tab_menu:
                        fragment = new DownloadTabFragment();
                        break;

                    case R.id.misc_tab_menu:
                        fragment = new MiscTabFragment();
                        break;
                }

                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.page_fragment_container, fragment)
                            .commit();
                }

                return true;
            }
        });

        binding.bottomNavigation.setSelectedItemId(R.id.recent_tab_menu);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void prepareStoreage() {
        File sdPath = new File(AMEnv.DEFAULT_ROOT_PATH);
        sdPath.mkdir();
        if (!sdPath.canRead()){
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
            editor.apply();
        }

        /* First time installation! It will install the sample db
         * to /sdcard/AnyMemo
         */
        if (firstTime){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(AMPrefKeys.FIRST_TIME_KEY, false);
            editor.putString(AMPrefKeys.getRecentPathKey(0), AMEnv.DEFAULT_ROOT_PATH + AMEnv.DEFAULT_DB_NAME);
            editor.apply();
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
            editor.apply();

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

    @Override
    public void onDestroy() {
        recentListActionModeUtil.unregisterForActivity();
        disposables.dispose();

        super.onDestroy();
        // Update the widget and cancel the notification.
        AnyMemoWidgetProvider.updateWidget(this);
        notificationUtil.cancelNotification();

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
            Loader<File> loader = new FileAsyncTaskLoader(AnyMemo.this, contentUri);
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

    private static class FileAsyncTaskLoader extends AsyncTaskLoader<File> {
        private static final String TAG = FileAsyncTaskLoader.class.getSimpleName();
        private WeakReference<AnyMemo> anyMemoRef;
        private Uri contentUri;

        public FileAsyncTaskLoader(@NonNull AnyMemo anyMemo, @NonNull Uri contentUri) {
            super(anyMemo);
            this.anyMemoRef = new WeakReference<>(anyMemo);
            this.contentUri = contentUri;
        }

        @Override
        public File loadInBackground() {
            AnyMemo anyMemoActivity = anyMemoRef.get();
            if (anyMemoActivity == null) {
                return null;
            }

            String[] splittedUri = contentUri.toString().split("/");
            String newFileName = splittedUri[splittedUri.length - 1];
            if (!newFileName.endsWith(".db")) {
                newFileName += ".db";
            }

            File newFile = new File(AMEnv.DEFAULT_ROOT_PATH + "/" + newFileName);
            // First detect if the db with the same name exists.
            // And back kup the db if
            try {
                anyMemoActivity.amFileUtil.deleteFileWithBackup(newFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Failed to delete the exisitng db with backup", e);
            }

            InputStream inputStream;

            try {
                inputStream = anyMemoActivity.getContentResolver().openInputStream(contentUri);
                FileUtils.copyInputStreamToFile(inputStream, newFile);
            } catch (IOException e) {
                Log.e(TAG, "Error opening file from intent", e);
                return null;
            }

            if (!anyMemoActivity.databaseUtil.checkDatabase(newFile.getAbsolutePath())) {
                Log.e(TAG, "Database is corrupted: " + newFile.getAbsolutePath());
                newFile.delete();
                return null;
            }

            return newFile;
        }
    }
}
