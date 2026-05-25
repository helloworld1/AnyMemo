package org.liberty.android.fantastischmemo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMEnv;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DatabaseImportUtil {

    private final AMFileUtil amFileUtil;
    private final RecentListUtil recentListUtil;

    @Inject
    public DatabaseImportUtil(AMFileUtil amFileUtil, RecentListUtil recentListUtil) {
        this.amFileUtil = amFileUtil;
        this.recentListUtil = recentListUtil;
    }

    public void handleImportDbResult(final Activity activity, final Uri uri, final CompositeDisposable disposables, final Runnable onSuccess) {
        String newFileName = amFileUtil.getFileNameFromUri(activity, uri);
        if (newFileName == null) {
            newFileName = "imported_db.db";
        }
        if (!newFileName.endsWith(".db")) {
            newFileName += ".db";
        }
        final File newFile = new File(AMEnv.DEFAULT_ROOT_PATH + newFileName);

        if (newFile.exists()) {
            new AlertDialog.Builder(activity)
                .setTitle(R.string.overwrite_db_title)
                .setMessage(activity.getString(R.string.overwrite_db_message, newFileName))
                .setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importDb(activity, uri, newFile, disposables, onSuccess);
                    }
                })
                .setNegativeButton(R.string.no_text, null)
                .show();
        } else {
            importDb(activity, uri, newFile, disposables, onSuccess);
        }
    }

    private void importDb(final Activity activity, final Uri uri, final File newFile, final CompositeDisposable disposables, final Runnable onSuccess) {
        disposables.add(Observable.fromCallable(new Callable<File>() {
            @Override
            public File call() throws Exception {
                InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                FileUtils.copyInputStreamToFile(inputStream, newFile);
                return newFile;
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<File>() {
            @Override
            public void accept(File file) {
                recentListUtil.addToRecentList(file.getAbsolutePath());
                Toast.makeText(activity, R.string.success, Toast.LENGTH_SHORT).show();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Log.e("DatabaseImportUtil", "Error importing DB", throwable);
                Toast.makeText(activity, "Error importing DB", Toast.LENGTH_SHORT).show();
            }
        }));
    }
}
