package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

public abstract class DropboxAccountActivity extends AMActivity {
    private SharedPreferences settings;

    private SharedPreferences.Editor editor;

    private String oauthAccessToken;

    private String oauthAccessTokenSecret;

    private static final String ACCESS_TOKEN_URL = "https://api.dropbox.com/1/oauth/access_token";

    // Use the Account info API to verify the token is valid.
    private static final String ACCOUNT_INFO_URL = "https://api.dropbox.com/1/account/info";

    // Once the token and token secret are authenticated, this method will be called
    abstract protected void onAuthenticated(final String authToken, final String authTokenSecret);

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();

        oauthAccessToken = settings.getString(AMPrefKeys.DROPBOX_AUTH_TOKEN, null);
        oauthAccessTokenSecret = settings.getString(AMPrefKeys.DROPBOX_AUTH_TOKEN_SECRET, null);

         if (oauthAccessToken == null || oauthAccessTokenSecret == null) {
             showGetTokenDialog();
         } else {
             VerifyAndAuthenticTask task = new VerifyAndAuthenticTask();
             task.execute((Void) null);
         }
         
    }

    private void showGetTokenDialog() {
        DropboxOAuthTokenRetrievalDialogFragment tokenFetchAuthDialog = new DropboxOAuthTokenRetrievalDialogFragment();
        tokenFetchAuthDialog.setAuthCodeReceiveListener(authCodeReceiveListener);
        tokenFetchAuthDialog.show(getSupportFragmentManager(), TAG);
    }

    private DropboxOAuthTokenRetrievalDialogFragment.AuthCodeReceiveListener authCodeReceiveListener = new DropboxOAuthTokenRetrievalDialogFragment.AuthCodeReceiveListener() {
        public void onRequestTokenSecretReceived(String[] requestTokenSecret) {
            GetAccessTokenTask task = new GetAccessTokenTask();
            task.execute(requestTokenSecret);
        }

        public void onRequestTokenSecretError(String error) {
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
        public Exception doInBackground(String... requestTokenSecret) {
            Exception exception = null;
            
            // requestTokenSecret should be a two elements array containing token and secret 
            if(requestTokenSecret.length != 2){
                Log.e(TAG, "Error fetching request token and secret");
                exception = new IOException("Error fetching request token and secret");
            } else {
                BufferedReader reader = null;
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(ACCESS_TOKEN_URL);
                httpPost.setHeader("Authorization", DropboxUtils.buildOAuthAccessHeader(requestTokenSecret[0], requestTokenSecret[1]));
                
                try {
                    HttpResponse response = httpClient.execute(httpPost);
                    int statusCode = response.getStatusLine().getStatusCode();
                    
                    if(statusCode == 200){
                        HttpEntity entity = response.getEntity();
                        reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                        String[] parsedResult = reader.readLine().split("&");
                        oauthAccessTokenSecret = parsedResult[0].split("=")[1];
                        oauthAccessToken = parsedResult[1].split("=")[1];
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
                editor.putString("dropbox_auth_token", oauthAccessToken);
                editor.putString("dropbox_auth_token_secret", oauthAccessTokenSecret);
                editor.commit();
                onAuthenticated(oauthAccessToken, oauthAccessTokenSecret);
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dropbox_list_menu, menu);
        return true;
    }
    

    // The task to verify the oauth token. If the token is not valid
    // invalidate the token and restart the activity. 
    // If authenticated, run onAuthenticated.
    private class VerifyAndAuthenticTask extends AsyncTask<Void, Void, Boolean> {
        
        private ProgressDialog progressDialog;

        // Record the exception in the background task.
        private Exception backgroundTaskException = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(DropboxAccountActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        // Return true if we want to invalidate the token
		protected Boolean doInBackground(Void... params) {
            progressDialog.dismiss();
            try {
                return verifyToken(oauthAccessToken, oauthAccessTokenSecret);
            } catch (IOException e) {
                backgroundTaskException = e;
                // Do not invalidate it if we have an exception
                // This could most probably be network issue.
                return true;
            }
		}

        @Override
        protected void onPostExecute(Boolean result) {
            if (backgroundTaskException != null) {
                AMGUIUtility.displayError(DropboxAccountActivity.this, getString(R.string.error_text), getString(R.string.exception_text), backgroundTaskException);
            }
            if (result) {
                onAuthenticated(oauthAccessToken, oauthAccessTokenSecret);
            } else {
                invalidateSavedToken();
                restartActivity();
            }
        }
    }

    // Return true if the token is valid
    // false if the token is not
    private boolean verifyToken(String oauthToken, String oauthTokenSecret) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(ACCOUNT_INFO_URL);
        httpGet.setHeader("Authorization", DropboxUtils.getFileExchangeAuthHeader(oauthToken, oauthTokenSecret));
        HttpResponse response = httpClient.execute(httpGet);

        int statusCode = response.getStatusLine().getStatusCode();
        
        if (statusCode == 200){
            return true;
        } else {
            Log.w(TAG, "Call " + ACCOUNT_INFO_URL + " Status code: " + statusCode);
            return false;
        }
    }
    
    protected void invalidateSavedToken() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AMPrefKeys.DROPBOX_AUTH_TOKEN, null);
        editor.putString(AMPrefKeys.DROPBOX_AUTH_TOKEN_SECRET, null);
        editor.commit();
    }
}
