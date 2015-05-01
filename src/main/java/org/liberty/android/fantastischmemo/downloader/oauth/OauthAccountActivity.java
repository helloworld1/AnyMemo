/*
Copyright (C) 2013 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.downloader.oauth;

import java.io.IOException;

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
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.common.base.Joiner;

public abstract class OauthAccountActivity extends AMActivity {

    SharedPreferences settings;

    SharedPreferences.Editor editor;

    /* Authenticated! Now get the auth token */
    // For oauth1, the accessTokens are accessToken and accessKey
    // For oauth2 the accessTokens are just acces token.
    protected abstract void onAuthenticated(final String[] authTokens);

    // Verify the accessTokens. Return true if it is valid.
    // For oauth1, the accessTokens are accessToken and accessKey
    // For oauth2 the accessTokens are just acces token.
    protected abstract boolean verifyAccessToken(final String[] accessTokens) throws IOException;

    // Get the access tokens string from the request strings
    // For oauth 1, the request has requestToken and requestSecret
    // and returns the accessToken and accessKey
    // For oauth 2, the request is access code
    // and  returns the access token.
    protected abstract String[] getAccessTokens(final String[] requests) throws IOException;

    // Get the fragment that request the Oauth through a web page.
    protected abstract OauthAccessCodeRetrievalFragment getOauthRequestFragment();

    // The preference key to save / retrieve the access token. The preference name is based
    // on the prefix and the package of the class. So the same package use the same keys.
    private final String oauthAccessTokenPrefKey = AMPrefKeys.OAUTH_ACCESS_TOKEN_KEY_PREFIX + getClass().getPackage().getName();


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();

        String savedTokens = settings.getString(oauthAccessTokenPrefKey, null);

        // Request new one if nothing saved.
        if (savedTokens == null) {
            showGetTokenDialog();
        } else {
            String[] tokens = savedTokens.split(",");

            ValidateAccessTokenAndRunCallbackTask task = new ValidateAccessTokenAndRunCallbackTask();
            task.execute(tokens);
        }
    }

    protected void invalidateSavedToken() {
        editor.putString(oauthAccessTokenPrefKey, null);
        editor.commit();
    }

    private OauthAccessCodeRetrievalFragment.AuthCodeReceiveListener authCodeReceiveListener =
        new OauthAccessCodeRetrievalFragment.AuthCodeReceiveListener() {
			public void onAuthCodeReceived(String... codes) {
                GetAccessTokenTask task = new GetAccessTokenTask();
                task.execute(codes);
			}
			public void onAuthCodeError(String error) {
                showAuthErrorDialog(error);
			}
            public void onCancelled() {
                finish();
            }
        };

    private class ValidateAccessTokenAndRunCallbackTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog;

    	private Exception backgroundTaskException = null;

        private String[] tokens = null;

		@Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(OauthAccountActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_connect_net));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public Boolean doInBackground(String... accessTokens) {
            tokens = accessTokens;

        	try {
                return verifyAccessToken(accessTokens);
        	} catch (Exception e) {
        		backgroundTaskException = e;
                return false;
            }
        }


        @Override
        public void onPostExecute(Boolean isTokenValid){
            progressDialog.dismiss();

            if (backgroundTaskException != null) {
        		AMGUIUtility.displayError(OauthAccountActivity.this, getString(R.string.error_text), getString(R.string.exception_text), backgroundTaskException);
            }
            if (isTokenValid) {
                Log.i(TAG, "Token is valid");
                onAuthenticated(tokens);
            } else {
                invalidateSavedToken();
                showGetTokenDialog();
            }
        }
    }


    private class GetAccessTokenTask extends AsyncTask<String, Void, String[]> {

        private ProgressDialog progressDialog;

    	private Exception backgroundTaskException = null;

		@Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(OauthAccountActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_auth_text));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public String[] doInBackground(String... requests) {
            try {
                return getAccessTokens(requests);
            } catch (Exception e) {
                backgroundTaskException = e;
                return null;
            }
        }

        @Override
        public void onPostExecute(String[] accessTokens){
            progressDialog.dismiss();

        	if (backgroundTaskException != null) {
        		AMGUIUtility.displayError(OauthAccountActivity.this, getString(R.string.error_text), getString(R.string.exception_text), backgroundTaskException);
                return;
        	}

            editor.putString(oauthAccessTokenPrefKey, Joiner.on(",").join(accessTokens));
            editor.commit();

            if (accessTokens == null) {
                showAuthErrorDialog(null);

            } else {
                onAuthenticated(accessTokens);
            }
        }
    }

    private void showAuthErrorDialog(String error) {
        String errorMessage = getString(R.string.auth_error_text);
        if (error != null) {
            errorMessage += " " + error;
        }
        new AlertDialog.Builder(OauthAccountActivity.this)
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

    // show the diaglog ti get the token
    private void showGetTokenDialog() {
        OauthAccessCodeRetrievalFragment df = getOauthRequestFragment();
        df.setAuthCodeReceiveListener(authCodeReceiveListener);
        df.show(((FragmentActivity)this).getSupportFragmentManager(), "OauthAccessCodeRetrievalFragment");
    }

}
