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

import android.accounts.Account;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.common.base.Strings;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.downloader.oauth.Oauth2AccountActivity;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccountActivity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GoogleAccountActivity extends BaseActivity {

    private static final String TAG = GoogleAccountActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 100;

    private static final int RC_AUTHORIZATION= 101;

    private GoogleApiClient googleApiClient;

    private final Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
// basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

// Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.e(TAG, "Connection failure: " + connectionResult.getErrorMessage());
                        finish();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        //   GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result == null) {
                Log.e(TAG, "Null google sign in result");
                return;
            }
            if (!result.isSuccess()) {
                Log.e(TAG, "Google sign in is not successful");
                return;
            }

            GoogleSignInAccount acct = result.getSignInAccount();

            if (acct == null) {
                Log.e(TAG, "Getting null Google sign in account");
                return;
            }

            // Get account information
            final String email = acct.getEmail();
            Log.i(TAG, "Google Authenticated. email: " + email);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String token = GoogleAuthUtil.getToken(GoogleAccountActivity.this,
                                email, AMEnv.GDRIVE_SCOPE);
                        Log.i(TAG, "Google authenticated, token: " + token);
                        onAuthenticated(token);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (UserRecoverableAuthException e) {
                        startActivityForResult(e.getIntent(), RC_SIGN_IN);
                    } catch (GoogleAuthException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    private void onAuthenticated(@NonNull String token) {
        finish();
        Intent intent = new Intent(this, SpreadsheetListScreen.class);
        intent.putExtra(SpreadsheetListScreen.EXTRA_AUTH_TOKEN, token);
        startActivity(intent);
    }
}

