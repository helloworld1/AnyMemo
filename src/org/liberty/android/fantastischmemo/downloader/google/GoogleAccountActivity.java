/*
Copyright (C) 2012 Haowen Ning

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
package org.liberty.android.fantastischmemo.downloader.google;

import java.io.OutputStreamWriter;

import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.apache.mycommons.io.IOUtils;

import org.json.JSONObject;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.R;

import android.accounts.AccountManager;

import android.app.ProgressDialog;

import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.FragmentActivity;

import android.util.Log;

public abstract class GoogleAccountActivity extends AMActivity {
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    AccountManager accountManager;

    /* Authenticated! Now get the auth token */
    protected abstract void onAuthenticated(final String authToken);

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        String savedGoogleAccessToken = settings.getString("google_auth_token", null);
        // Request new one if nothing saved.
        if (savedGoogleAccessToken == null) {
            showGetTokenDialog();
        } else {
            ValidateAccessTokenAndRunCallbackTask task = new ValidateAccessTokenAndRunCallbackTask();
            task.execute(savedGoogleAccessToken);
        }
    }

    private void showGetTokenDialog() {
        GoogleOAuth2AccessCodeRetrievalFragment df = new GoogleOAuth2AccessCodeRetrievalFragment();
        df.setAuthCodeReceiveListener(authCodeReceiveListener);
        df.show(((FragmentActivity)this).getSupportFragmentManager(), "GoogleAutoFragment");
    }

    private GoogleOAuth2AccessCodeRetrievalFragment.AuthCodeReceiveListener authCodeReceiveListener = 
        new GoogleOAuth2AccessCodeRetrievalFragment.AuthCodeReceiveListener() {
			public void onAuthCodeReceived(String code) {
                GetAccessTokenTask task = new GetAccessTokenTask();
                task.execute(code);
			}
			public void onAuthCodeError(String error) {
                System.out.println("authCodeReceiveListener Error: " + error);
			}
        };

    private class ValidateAccessTokenAndRunCallbackTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog;

        private String token = null;

		@Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(GoogleAccountActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_save));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public Boolean doInBackground(String... accessTokens) {
            token = accessTokens[0];
            try {
                URL url1 = new URL("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + token);
                HttpsURLConnection conn = (HttpsURLConnection) url1.openConnection();

                String s = new String(IOUtils.toByteArray(conn.getInputStream()));
                System.out.println(s);
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.has("error")) {
                    String error = jsonObject.getString("error");
                    Log.e(TAG, "Token validation error: " + error);
                    return false;
                }

                String audience = jsonObject.getString("audience");
                return AMEnv.GOOGLE_CLIENT_ID.equals(audience);
            } catch (Exception e) {
                Log.e(TAG, "Error redeeming access token", e);
            }
            return false;
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
            progressDialog = new ProgressDialog(GoogleAccountActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_save));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public String doInBackground(String... accessCodes) {
            String code = accessCodes[0];
            try {
                URL url1 = new URL("https://accounts.google.com/o/oauth2/token");
                HttpsURLConnection conn = (HttpsURLConnection) url1.openConnection();
                conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                String payload = String.format("code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=%s",
                        URLEncoder.encode(code, "UTF-8"),
                        URLEncoder.encode(AMEnv.GOOGLE_CLIENT_ID, "UTF-8"),
                        URLEncoder.encode(AMEnv.GOOGLE_CLIENT_SECRET, "UTF-8"),
                        URLEncoder.encode(AMEnv.GOOGLE_REDIRECT_URI, "UTF-8"),
                        URLEncoder.encode("authorization_code", "UTF-8"));
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(payload);
                out.close();

                String s = new String(IOUtils.toByteArray(conn.getInputStream()));
                JSONObject jsonObject = new JSONObject(s);
                String accessToken = jsonObject.getString("access_token");
                //String refreshToken= jsonObject.getString("refresh_token");
                return accessToken;
            } catch (Exception e) {
                Log.e(TAG, "Error redeeming access token", e);
            }
            return null;
        }

        
        @Override
        public void onPostExecute(String accessToken){
            editor.putString("google_auth_token", accessToken);
            editor.commit();
            if (accessToken == null) {
                // TODO: Display something beautiful.
                
            } else {
                onAuthenticated(accessToken);
            }
            progressDialog.dismiss();
        }
    }

    private void invalidateSavedToken() {
        editor.putString("google_auth_token", null);
        editor.commit();
    }
}
