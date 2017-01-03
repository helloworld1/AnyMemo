package org.liberty.android.fantastischmemo.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.modules.PerActivity;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;

@PerActivity
public class DatabaseOperationDialogUtil {
    private static final String TAG = DatabaseOperationDialogUtil.class.getName();
    private final Activity activity;
    private final AMFileUtil amFileUtil;
    private final RecentListUtil recentListUtil;

    @Inject
    public DatabaseOperationDialogUtil(@NonNull final Activity activity,
                                       @NonNull final AMFileUtil amFileUtil,
                                       @NonNull final RecentListUtil recentListUtil) {
        this.activity = activity;
        this.amFileUtil = amFileUtil;
        this.recentListUtil = recentListUtil;
    }

    public Maybe<File> showCreateDbDialog(@NonNull final String directoryPath) {
        final EditText input = new EditText(activity);
        return Maybe.create(new MaybeOnSubscribe<File>() {
            @Override
            public void subscribe(final MaybeEmitter<File> emitter) throws Exception {
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.fb_create_db))
                        .setMessage(activity.getString(R.string.fb_create_db_message))
                        .setView(input)
                        .setPositiveButton(activity.getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which ){
                                String value = input.getText().toString();
                                if(!value.endsWith(".db")){
                                    value += ".db";
                                }
                                File newDbFile = new File(directoryPath + "/" + value);
                                try {
                                    if (newDbFile.exists()) {
                                        amFileUtil.deleteFileWithBackup(newDbFile.getAbsolutePath());
                                    }

                                    amFileUtil.createDbFileWithDefaultSettings(newDbFile);
                                    emitter.onSuccess(newDbFile);
                                } catch(IOException e){
                                    Log.e(TAG, "Fail to create file", e);
                                }
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                emitter.onComplete();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                emitter.onComplete();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    public Maybe<File> showDeleteDbDialog(final File clickedFile) {
        return Maybe.create(new MaybeOnSubscribe<File>() {
            @Override
            public void subscribe(final MaybeEmitter<File> emitter) throws Exception {
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.delete_text))
                        .setMessage(activity.getString(R.string.fb_delete_message))
                        .setPositiveButton(activity.getString(R.string.delete_text), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which ){
                                amFileUtil.deleteDbSafe(clickedFile.getAbsolutePath());
                                emitter.onSuccess(clickedFile);
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                emitter.onComplete();

                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                emitter.onComplete();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    public Maybe<File> showCloneDbDialog(final File clickedFile) {
        return Maybe.create(new MaybeOnSubscribe<File>() {
            @Override
            public void subscribe(MaybeEmitter<File> emitter) throws Exception {
                String srcDir = clickedFile.getAbsolutePath();
                String destDir = srcDir.replaceAll(".db", ".clone.db");
                try {
                    FileUtils.copyFile(new File(srcDir), new File(destDir));
                    emitter.onSuccess(new File(destDir));
                } catch(IOException e){
                    new AlertDialog.Builder(activity)
                            .setTitle(activity.getString(R.string.fail))
                            .setMessage(activity.getString(R.string.fb_fail_to_clone) + "\nError: " + e.toString())
                            .setPositiveButton(activity.getString(R.string.ok_text), null)
                            .create()
                            .show();
                    emitter.onComplete();
                }
            }
        });
    }

    public Maybe<File> showRenameDbDialog(final File clickedFile) {
        return Maybe.create(new MaybeOnSubscribe<File>() {
            @Override
            public void subscribe(final MaybeEmitter<File> emitter) throws Exception {
                final EditText input = new EditText(activity);
                input.setText(clickedFile.getAbsolutePath());
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.fb_rename))
                        .setMessage(activity.getString(R.string.fb_rename_message))
                        .setView(input)
                        .setPositiveButton(activity.getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String value = input.getText().toString();
                                if (!value.equals(clickedFile.getAbsolutePath())) {
                                    try {
                                        FileUtils.copyFile(clickedFile, new File(value));
                                        amFileUtil.deleteDbSafe(clickedFile.getAbsolutePath());
                                        recentListUtil.deleteFromRecentList(clickedFile.getAbsolutePath());
                                        emitter.onSuccess(new File(value));
                                    } catch (IOException e) {
                                        new AlertDialog.Builder(activity)
                                                .setTitle(activity.getString(R.string.fail))
                                                .setMessage(activity.getString(R.string.fb_rename_fail) + "\nError: " + e.toString())
                                                .setNeutralButton(activity.getString(R.string.ok_text), null)
                                                .create()
                                                .show();
                                        emitter.onComplete();
                                    }
                                }
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                emitter.onComplete();
                            }
                        })
                        .show();

            }
        });
    }

    public Maybe<File> showCreateFolderDialog(@NonNull final File currentDirectory) {
        return Maybe.create(new MaybeOnSubscribe<File>() {
            @Override
            public void subscribe(final MaybeEmitter<File> emitter) throws Exception {
                final EditText input = new EditText(activity);
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.fb_create_dir)
                        .setMessage(R.string.fb_create_dir_message)
                        .setView(input)
                        .setPositiveButton(activity.getString(R.string.ok_text), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which ){
                                String value = input.getText().toString();
                                File newDir = new File(currentDirectory.getAbsolutePath() + "/" + value);
                                newDir.mkdir();
                                emitter.onSuccess(newDir);
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                emitter.onComplete();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                emitter.onComplete();

                            }
                        })
                        .create()
                        .show();

            }
        });
    }
}
