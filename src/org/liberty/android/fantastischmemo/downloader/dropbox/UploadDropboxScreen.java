package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.File;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.downloader.google.GoogleDriveUploadHelper;
import org.liberty.android.fantastischmemo.ui.FileBrowserFragment;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class UploadDropboxScreen extends DropboxAccountActivity{
    
    private String authToken;
    private String authTokenSecret;
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.upload_dropbox_screen);
//        onAuthenticated(authToken, authTokenSecret);
        
//        super.onCreate(bundle);
    }

    protected void onAuthenticated(final String authToken, final String authTokenSecret) {
        this.authToken=authToken;
        this.authTokenSecret=authTokenSecret;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FileBrowserFragment fragment = new FileBrowserFragment();
        fragment.setOnFileClickListener(fileClickListener);
        ft.add(R.id.file_list_dropbox, fragment);
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
//                .setMessage(String.format(getString(R.string.upload_gdrive_message), file.getName()))
                .setMessage("Upload" + file.getName() + " to dropbox?")
                .setPositiveButton(R.string.ok_text,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        UploadTask task = new UploadTask();
                        task.execute(file);
                    }
                 }).setNegativeButton(R.string.cancel_text, null).show();
    }
    
    
    private void uploadToDropbox(File file) {
        try {
            DropboxUploadHelper uploadHelper = new DropboxUploadHelper(this, authToken, authTokenSecret);
//            uploadHelper.createSpreadsheet(file.getName(), file.getAbsolutePath());
            uploadHelper.upload(file.getName(), file.getAbsolutePath());
            setResult(Activity.RESULT_OK, new Intent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private class UploadTask extends AsyncTask<File, Void, Exception> {

        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(UploadDropboxScreen.this);
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
                uploadToDropbox(file);
            } catch (Exception e) {
                Log.e(TAG, "Error uploading ", e);
                return e;
            }
            return null;
        }

        
        @Override
        public void onPostExecute(Exception e){
            if (e != null) {
                AMGUIUtility.displayException(UploadDropboxScreen.this, getString(R.string.error_text), getString(R.string.error_text), e);
            } else {
                new AlertDialog.Builder(UploadDropboxScreen.this)
                    .setTitle(R.string.successfully_uploaded_text)
                    .setMessage(R.string.gdrive_successfully_uploaded_message)
                    .setPositiveButton(R.string.ok_text, null)
                    .show();
            }
            progressDialog.dismiss();
        }
    }

}
