package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class DropboxAccountActivity extends AMActivity {
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private String oatuhAccessToken;
    private String oauthAccessTokenSecret;
    private static final String ACCESS_TOKEN_URL = "https://api.dropbox.com/1/oauth/access_token";
    private static final int UPLOAD_ACTIVITY = 1;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();

        oatuhAccessToken = settings.getString(AMPrefKeys.DROPBOX_AUTH_TOKEN, null);
        oauthAccessTokenSecret = settings.getString(AMPrefKeys.DROPBOX_AUTH_TOKEN_SECRET, null);

         if (oatuhAccessToken == null || oauthAccessTokenSecret == null) {
             showGetTokenDialog();
         } else {
             onAuthenticated(oatuhAccessToken, oauthAccessTokenSecret);
         }
         
         setContentView(R.layout.spreadsheet_list_screen);
    }

    private void showGetTokenDialog() {
        DropboxOAuthTokenRetrievalDialogFragment tokenFetchAuthDialog = new DropboxOAuthTokenRetrievalDialogFragment();
        tokenFetchAuthDialog.setAuthCodeReceiveListener(authCodeReceiveListener);
        tokenFetchAuthDialog.show(getSupportFragmentManager(), TAG);
    }

    private DropboxOAuthTokenRetrievalDialogFragment.AuthCodeReceiveListener authCodeReceiveListener = new DropboxOAuthTokenRetrievalDialogFragment.AuthCodeReceiveListener() {
        public void onRequestTokenNSecretReceived(String[] requestTokenNSecret) {
            GetAccessTokenTask task = new GetAccessTokenTask();
            task.execute(requestTokenNSecret);
        }

        public void onRequestTokenNSecretError(String error) {
             showAuthErrorDialog(error);
        }

        public void onCancelled() {
            finish();
        }
    };

    private class GetAccessTokenTask extends AsyncTask<String, Void, Exception> {
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DropboxAccountActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_auth_text));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public Exception doInBackground(String... requestTokenNSecret) {
            Exception exception = null;
            
            // requestTokenNSecret should be a two elements array containing token and secret 
            if(requestTokenNSecret.length != 2){
                Log.e(TAG, "Error fetching request token and secret");
                exception = new IOException("Error fetching request token and secret");
            } else {
                BufferedReader reader = null;
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(ACCESS_TOKEN_URL);
                httpPost.setHeader("Authorization", DropboxUtils.buildOAuthAccessHeader(requestTokenNSecret[0], requestTokenNSecret[1]));
                
                try {
                    HttpResponse response = httpClient.execute(httpPost);
                    int statusCode = response.getStatusLine().getStatusCode();
                    
                    if(statusCode == 200){
                        HttpEntity entity = response.getEntity();
                        reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                        String[] parsedResult = reader.readLine().split("&");
                        oauthAccessTokenSecret = parsedResult[0].split("=")[1];
                        oatuhAccessToken = parsedResult[1].split("=")[1];
                    } else {
                        throw new IOException("Fetching access token request returns error code: " + statusCode);
                    }
                    
                } catch (ClientProtocolException e) {
                    exception = e;
                } catch (IOException e) {
                    exception = e;
                } finally {
                    if(reader != null){
                        try {
                            reader.close();
                        } catch (IOException e) {}
                    }
                }
            }
            return exception;
        }

        @Override
        public void onPostExecute(Exception e) {
            progressDialog.dismiss();
            if (e == null) {
                editor.putString("dropbox_auth_token", oatuhAccessToken);
                editor.putString("dropbox_auth_token_secret", oauthAccessTokenSecret);
                editor.commit();
                onAuthenticated(oatuhAccessToken, oauthAccessTokenSecret);
            } else {
                showAuthErrorDialog(getString(R.string.dropbox_token_failure_text));
            }
        }
    }
    
    
    private void showAuthErrorDialog(String error) {
        String errorMessage = getString(R.string.auth_error_text);
        if (error != null) {
            errorMessage += " " + error;
        }
        new AlertDialog.Builder(DropboxAccountActivity.this)
            .setTitle(R.string.error_text)
            .setMessage(errorMessage)
            .setPositiveButton(R.string.back_menu_text, new DialogInterface.OnClickListener() { 
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
        .show();
    }
    
    protected void onAuthenticated(final String authToken, final String authTokenSecret) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = new DownloadDBFileListFragment(authToken, authTokenSecret);
        ft.add(R.id.spreadsheet_list, newFragment);
        ft.commit();
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dropbox_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:{
                startActivityForResult(new Intent(this, UploadDropboxScreen.class), UPLOAD_ACTIVITY);
                return true;
            }
            case R.id.logout:{
                invalidateSavedToken();
                finish();
                return true;
            }
        }
        return false;
    }
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            return;
        }

        switch(requestCode){
            case UPLOAD_ACTIVITY:
            {
                restartActivity();
                break;
            }
        }
    }
    
    private void invalidateSavedToken() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AMPrefKeys.DROPBOX_AUTH_TOKEN, null);
        editor.putString(AMPrefKeys.DROPBOX_AUTH_TOKEN_SECRET, null);
        editor.commit();
    }
}
