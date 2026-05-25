package org.liberty.android.fantastischmemo.utils;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMEnv;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FolderImportExportUtil {

    @Inject
    public FolderImportExportUtil() {}

    public void importFolder(final Activity activity, final Uri treeUri, final CompositeDisposable disposables) {
        disposables.add(Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                DocumentFile pickedDir = DocumentFile.fromTreeUri(activity, treeUri);
                if (pickedDir == null) return 0;
                int count = 0;
                for (DocumentFile file : pickedDir.listFiles()) {
                    if (file.isFile()) {
                        File dest = new File(AMEnv.DEFAULT_ROOT_PATH + file.getName());
                        try (InputStream in = activity.getContentResolver().openInputStream(file.getUri())) {
                            if (in != null) {
                                FileUtils.copyInputStreamToFile(in, dest);
                                count++;
                            }
                        }
                    }
                }
                return count;
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer count) {
                Toast.makeText(activity, "Imported " + count + " files", Toast.LENGTH_SHORT).show();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Toast.makeText(activity, "Error importing folder: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public void exportFolder(final Activity activity, final Uri treeUri, final CompositeDisposable disposables) {
        disposables.add(Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                DocumentFile pickedDir = DocumentFile.fromTreeUri(activity, treeUri);
                if (pickedDir == null) return 0;
                int count = 0;
                File[] sourceFiles = new File(AMEnv.DEFAULT_ROOT_PATH).listFiles();
                if (sourceFiles != null) {
                    for (File file : sourceFiles) {
                        if (file.isFile()) {
                            DocumentFile newFile = pickedDir.findFile(file.getName());
                            if (newFile != null) {
                                newFile.delete(); // Delete existing file with same name
                            }
                            newFile = pickedDir.createFile("application/octet-stream", file.getName());
                            if (newFile != null) {
                                try (OutputStream out = activity.getContentResolver().openOutputStream(newFile.getUri())) {
                                    if (out != null) {
                                        try (InputStream in = new java.io.FileInputStream(file)) {
                                            org.apache.commons.io.IOUtils.copy(in, out);
                                            count++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return count;
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer count) {
                Toast.makeText(activity, "Exported " + count + " files", Toast.LENGTH_SHORT).show();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Toast.makeText(activity, "Error exporting folder: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }
}
