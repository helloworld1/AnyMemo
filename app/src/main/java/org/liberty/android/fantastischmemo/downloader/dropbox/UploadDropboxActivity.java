package org.liberty.android.fantastischmemo.downloader.dropbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.Subscribe;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.databinding.UploadDropboxScreenBinding;
import org.liberty.android.fantastischmemo.ui.FileBrowserFragment;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class UploadDropboxActivity extends BaseActivity {

    public static final String EXTRA_AUTH_TOKEN = "authToken";

    private static final String ANYMEMO_FOLDER = "AnyMemo";

    private CompositeDisposable disposables = new CompositeDisposable();

    private UploadDropboxScreenBinding binding;

    private String authToken;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = DataBindingUtil.setContentView(this, R.layout.upload_dropbox_screen);

        Bundle extras = getIntent().getExtras();
        authToken = extras.getString(EXTRA_AUTH_TOKEN);

        setTitle(R.string.upload_text);
        setSupportActionBar(binding.toolbar);

        Fragment fileFragment = new FileBrowserFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.file_list_dropbox, fileFragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Override
    public void onStart() {
        super.onStart();
        appComponents().eventBus().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        appComponents().eventBus().unregister(this);
    }

    @Subscribe
    public void onFileClickEvent(FileBrowserFragment.FileClickEvent event) {
        showUploadDialog(event.clickedFile);
    }

    private void showUploadDialog(final File file) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.upload_text)
            .setMessage(String.format(getString(R.string.dropbox_upload_text), file.getName()))
            .setPositiveButton(R.string.ok_text,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    uploadToDropbox(file);
                    setResult(Activity.RESULT_OK);
                }
             }).setNegativeButton(R.string.cancel_text, null).show();
    }


    private void uploadToDropbox(@NonNull final File file) {
        final ProgressDialog progressDialog = new ProgressDialog(UploadDropboxActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(getString(R.string.loading_please_wait));
        progressDialog.setMessage(getString(R.string.upload_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        disposables.add(appComponents().dropboxApiHelper().uploadDropbox(authToken, file,
                String.format("/%1$s/%2$s", ANYMEMO_FOLDER, file.getName()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        progressDialog.dismiss();
                        new AlertDialog.Builder(UploadDropboxActivity.this)
                                .setTitle(R.string.successfully_uploaded_text)
                                .setMessage(R.string.dropbox_successfully_uploaded_message)
                                .setPositiveButton(R.string.ok_text, null)
                                .show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        progressDialog.dismiss();
                        AMGUIUtility.displayException(UploadDropboxActivity.this, getString(R.string.error_text), getString(R.string.error_text), throwable);
                    }
                }));
    }
}
