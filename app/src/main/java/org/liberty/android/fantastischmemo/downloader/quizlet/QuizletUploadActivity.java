package org.liberty.android.fantastischmemo.downloader.quizlet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.FileBrowserFragment;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;

import java.io.File;

import javax.inject.Inject;

public class QuizletUploadActivity extends QuizletAccountActivity {

    private String authToken = null;

    private QuizletUploadHelper quizletUploadHelper;

    @Inject
    public void setQuizletUploadHelper(QuizletUploadHelper quizletUploadHelper) {
        this.quizletUploadHelper = quizletUploadHelper;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.upload_quizlet_screen);
    }

    @Override
    protected void onAuthenticated(final String[] authTokens) {
        this.authToken = authTokens[0];
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FileBrowserFragment fragment = new FileBrowserFragment();
        fragment.setOnFileClickListener(fileClickListener);
        ft.add(R.id.file_list, fragment);
        ft.commit();
    }

    private FileBrowserFragment.OnFileClickListener fileClickListener = new FileBrowserFragment.OnFileClickListener() {

        @Override
        public void onClick(File file) {
            showUploadDialog(file);
        }
    };

    private void showUploadDialog(final File file) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.upload_text)
                .setMessage(
                        String.format(
                                getString(R.string.upload_quizlet_message),
                                file.getName()))
                .setPositiveButton(R.string.ok_text,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                UploadTask task = new UploadTask();
                                task.execute(file);
                            }
                        }).setNegativeButton(R.string.cancel_text, null).show();
    }

    private class UploadTask extends AsyncTask<File, Void, Exception> {

        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(QuizletUploadActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.upload_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public Exception doInBackground(File... files) {
            File file = files[0];
            try {
                quizletUploadHelper.uploadToQuizlet(file, authToken);
            } catch (Exception e) {
                Log.e(TAG, "Error uploading ", e);
                return e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Exception e) {
            if (e != null) {
                AMGUIUtility.displayException(QuizletUploadActivity.this,
                        getString(R.string.error_text),
                        getString(R.string.error_text), e);
            } else {
                setResult(Activity.RESULT_OK, new Intent());
                new AlertDialog.Builder(QuizletUploadActivity.this)
                        .setTitle(R.string.successfully_uploaded_text)
                        .setMessage(
                                R.string.quizlet_successfully_uploaded_message)
                        .setPositiveButton(R.string.ok_text, null).show();
            }
            progressDialog.dismiss();
        }
    }
}
