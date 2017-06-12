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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.common.base.Strings;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.common.BaseActivity;

import java.io.IOException;

public class GoogleAccountActivity extends BaseActivity {

    private static final String TAG = GoogleAccountActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 100;

    private static final int RC_AUTH_TOKEN = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth2_account_activity);

        if (!activityComponents().googlePlayUtil().checkPlayServices(RC_SIGN_IN)) {
            return;
        }

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(activityComponents().googleApiClient());
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        //   GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result == null) {
                activityComponents().errorUtil().showFatalError(getString(R.string.google_sign_in_empty_error_text), null);
                return;
            }
            if (!result.isSuccess()) {
                activityComponents().errorUtil().showFatalError(getString(R.string.google_sign_in_not_successful_error_text), null);
                return;
            }

            GoogleSignInAccount acct = result.getSignInAccount();

            if (acct == null) {
                activityComponents().errorUtil().showFatalError(getString(R.string.google_sign_in_account_empty_result), null);
                return;
            }

            // Get account information
            final String email = acct.getEmail();

            appComponents().executorService().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String token = GoogleAuthUtil.getToken(GoogleAccountActivity.this,
                                email, AMEnv.GDRIVE_SCOPE);
                        onAuthenticated(token);
                    } catch (IOException e) {
                        activityComponents().errorUtil().showFatalError("IO Error", e);
                    } catch (UserRecoverableAuthException e) {
                        startActivityForResult(e.getIntent(), RC_AUTH_TOKEN);
                    } catch (GoogleAuthException e) {
                        activityComponents().errorUtil().showFatalError("GoogleAuthException", e);
                    }
                }
            });
        } else if (requestCode == RC_AUTH_TOKEN) {
            Bundle extra = data.getExtras();
            if (extra == null) {
                activityComponents().errorUtil().showFatalError("RC_AUTH_TOKEN does not have extra", null);
                return;
            }
            String token = extra.getString("authtoken");

            if (Strings.isNullOrEmpty(token)) {
                activityComponents().errorUtil().showFatalError("RC_AUTH_TOKEN does not have token", null);
                return;
            }

            onAuthenticated(token);
        }
    }

    private void onAuthenticated(@NonNull String token) {
        finish();
        Intent intent = new Intent(this, SpreadsheetListScreen.class);
        intent.putExtra(SpreadsheetListScreen.EXTRA_AUTH_TOKEN, token);
        startActivity(intent);
    }
}

