package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.downloader.dropbox.DropboxOAuth1AccessCodeRetrievalFragment;
import org.liberty.android.fantastischmemo.downloader.google.UploadGoogleDriveScreen;

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

	private String OAUTH_ACCESS_TOKEN;
	private String OAUTH_ACCESS_TOKEN_SECRET;
	private final String ACCESS_TOKEN_URL = "https://api.dropbox.com/1/oauth/access_token";
	private final String DROPBOX_AUTH_TOKEN="dropbox_auth_token";
	private final String DROPBOX_AUTH_TOKEN_SECRET="dropbox_auth_token_secret";

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		editor = settings.edit();

		OAUTH_ACCESS_TOKEN = settings.getString(DROPBOX_AUTH_TOKEN, null);
		OAUTH_ACCESS_TOKEN_SECRET = settings.getString(DROPBOX_AUTH_TOKEN_SECRET, null);

		// Request new one if nothing saved.
		 if (OAUTH_ACCESS_TOKEN == null || OAUTH_ACCESS_TOKEN_SECRET == null) {
			 showGetTokenDialog();
		 } 
		 else {
		     onAuthenticated(OAUTH_ACCESS_TOKEN, OAUTH_ACCESS_TOKEN_SECRET);
		 }
		 
		 setContentView(R.layout.spreadsheet_list_screen);
	}

	private void showGetTokenDialog() {
		DropboxOAuth1AccessCodeRetrievalFragment df = new DropboxOAuth1AccessCodeRetrievalFragment();
		df.setAuthCodeReceiveListener(authCodeReceiveListener);
		df.show(getSupportFragmentManager(), TAG);
	}

	private DropboxOAuth1AccessCodeRetrievalFragment.AuthCodeReceiveListener authCodeReceiveListener = new DropboxOAuth1AccessCodeRetrievalFragment.AuthCodeReceiveListener() {
		public void onAuthCodeReceived(String uid) {
			GetAccessTokenTask task = new GetAccessTokenTask();
			task.execute(uid);
		}

		public void onAuthCodeError(String error) {
			 showAuthErrorDialog(error);
		}

		public void onCancelled() {
			finish();
		}
	};

	private class GetAccessTokenTask extends AsyncTask<String, Void, Boolean> {
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
		public Boolean doInBackground(String... accessCodes) {
			BufferedReader reader = null;
			try {

				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(ACCESS_TOKEN_URL);
				httpPost.setHeader("Authorization", DropboxUtils.buildOAuthAccessHeader());
				
				HttpResponse response = null;
				response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
				
				InputStream instream = entity.getContent();
				reader = new BufferedReader(new InputStreamReader(instream));
				String result = reader.readLine();
				String[] parsedResult = result.split("&");
				OAUTH_ACCESS_TOKEN_SECRET = parsedResult[0].split("=")[1];
				OAUTH_ACCESS_TOKEN = parsedResult[1].split("=")[1];

				return true;
			} catch (Exception e) {
				Log.e(TAG, "Error redeeming access token", e);
			}
			return false;
		}

		@Override
		public void onPostExecute(Boolean tokenObtained) {
			progressDialog.dismiss();
			if (tokenObtained) {
			    editor.putString("dropbox_auth_token", OAUTH_ACCESS_TOKEN);
			    editor.putString("dropbox_auth_token_secret", OAUTH_ACCESS_TOKEN_SECRET);
			    editor.commit();
		        onAuthenticated(OAUTH_ACCESS_TOKEN, OAUTH_ACCESS_TOKEN_SECRET);
			} else {
			    showAuthErrorDialog("Access token being null");
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
        Fragment newFragment = new SpreadsheetListFragment(authToken, authTokenSecret);
        ft.add(R.id.spreadsheet_list, newFragment);
        ft.commit();
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.spreadsheet_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:{
                startActivityForResult(new Intent(this, UploadDropboxScreen.class), 1);
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
    
    
    private void invalidateSavedToken() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(DROPBOX_AUTH_TOKEN, null);
        editor.putString(DROPBOX_AUTH_TOKEN_SECRET, null);
        editor.commit();
    }
}
