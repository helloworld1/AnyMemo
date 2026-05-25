package org.liberty.android.fantastischmemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.R;

import java.io.File;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ExportAnyMemoDbFragment extends FileBrowserFragment {
    private static final String TAG = ExportAnyMemoDbFragment.class.getSimpleName();
    private static final int REQUEST_CODE_EXPORT_DB = 1002;

    private CompositeDisposable disposables;
    private File selectedDbFile;

    public ExportAnyMemoDbFragment() { }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        disposables = new CompositeDisposable();

        setOnFileClickListener(new FileBrowserFragment.OnFileClickListener() {
            @Override
            public void onFileBrowserFileClick(File file) {
                exportDatabase(file);
            }
        });
    }

    private void exportDatabase(File file) {
        selectedDbFile = file;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, file.getName());
        startActivityForResult(intent, REQUEST_CODE_EXPORT_DB);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposables != null) {
            disposables.dispose();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXPORT_DB && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            final Uri uri = data.getData();
            disposables.add(Observable.fromCallable(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    if (selectedDbFile == null) return false;
                    try (OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri)) {
                        FileUtils.copyFile(selectedDbFile, outputStream);
                        return true;
                    }
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean success) {
                    if (success) {
                        Toast.makeText(getContext(), R.string.success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Export failed", Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    Log.e(TAG, "Error exporting DB", throwable);
                    Toast.makeText(getContext(), "Error exporting DB", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE_EXPORT_DB) {
                dismiss();
            }
        }
    }
}
