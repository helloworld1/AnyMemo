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

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

public abstract class OauthAccountActivity extends AMActivity {

    SharedPreferences settings;

    SharedPreferences.Editor editor;

    /* Authenticated! Now get the auth token */
    protected abstract void onAuthenticated(final String authToken);

    protected abstract boolean verifyAccessToken(final String accessToken);

    // Get the access token string from the request strings
    protected abstract String getAccessToken(final String... requests);

    // Get the fragment that request the Oauth through a web page.
    protected abstract OauthAccessCodeRetrievalFragment getOauthRequestFragment();

    // The preference key to save / retrieve the access token
    private final String oauthAccessTokenPrefKey = AMPrefKeys.OAUTH_ACCESS_TOKEN_KEY_PREFIX + getClass().getName();


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();

        String savedGoogleAccessToken = settings.getString(oauthAccessTokenPrefKey, null);

        // Request new one if nothing saved.
        if (savedGoogleAccessToken == null) {
            showGetTokenDialog();
        } else {
            ValidateAccessTokenAndRunCallbackTask task = new ValidateAccessTokenAndRunCallbackTask();
            task.execute(savedGoogleAccessToken);
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

        private String token = null;

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
            return verifyAccessToken(accessTokens[0]);
        }

        
        @Override
        public void onPostExecute(Boolean isTokenValid){
            if (isTokenValid) {
                onAuthenticated(token);
            } else {
                invalidateSavedToken();
                showGetTokenDialog();
            }
            progressDialog.dismiss();
        }
    }


    private class GetAccessTokenTask extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;

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
        public String doInBackground(String... requests) {
            return getAccessToken(requests);
        }

        
        @Override
        public void onPostExecute(String accessToken){
            progressDialog.dismiss();
            editor.putString(AMPrefKeys.GOOGLE_AUTH_TOKEN, accessToken);
            editor.commit();
            if (accessToken == null) {
                showAuthErrorDialog(null);
                
            } else {
                onAuthenticated(accessToken);
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
