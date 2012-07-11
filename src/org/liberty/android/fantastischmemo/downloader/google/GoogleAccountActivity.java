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

import java.io.IOException;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;

import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Bundle;

import android.preference.PreferenceManager;

import android.widget.Toast;

public abstract class GoogleAccountActivity extends AMActivity {
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    AccountManager accountManager;
    private final static int REQUEST_AUTHENTICATE = 1;

    /* Authenticated! Now get the auth token */
    protected abstract void onAuthenticated(final String authToken);

    // Return the auth token type
    protected abstract String getAuthTokenType();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        accountManager = AccountManager.get(this);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        chooseAccount(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_AUTHENTICATE:
                if (resultCode == RESULT_OK) {
                    chooseAccount(null);
                } else {
                    showAccountChooseDialog();
                }
                break;
        }
    }

    private void showAccountChooseDialog() {
        final AccountManager manager = AccountManager.get(this);
        final Account[] accounts = manager.getAccountsByType("com.google");
        final int size = accounts.length;
        String[] names = new String[size];

        for (int i = 0; i < size; i++) {
            names[i] = accounts[i].name;
        }
        new AlertDialog.Builder(this)
            .setTitle(R.string.choose_account_text)
            .setItems(names, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    editor.putString("saved_account_name", accounts[which].name);
                    editor.commit();
                    GetAuthTokenTask task = new GetAuthTokenTask();
                    task.execute(accounts[which]);
                }
            })
        .create()
        .show();
    }

    private void chooseAccount(String oldAuthToken) {

        String accountName = settings.getString("saved_account_name", null);

        if (accountName == null) {
            showAccountChooseDialog();
        } else {
            AccountManager manager = AccountManager.get(this);
            Account[] accounts = manager.getAccountsByType("com.google");
            int size = accounts.length;
            for (int i = 0; i < size; i++) {
                Account account = accounts[i];
                if (accountName.equals(account.name)) {

                    Toast.makeText(this,getString(R.string.login_account_text) + ": " + account.name , Toast.LENGTH_SHORT).show();
                    if (oldAuthToken != null) {
                        manager.invalidateAuthToken("com.google", oldAuthToken);

                    }
                    GetAuthTokenTask task = new GetAuthTokenTask();
                    task.execute(account);
                    return;
                }
            }
        }
    }

    private class GetAuthTokenTask extends AsyncTask<Account, Void, Void> {

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
        public Void doInBackground(Account... accounts) {
            accountManager.getAuthToken(
                    accounts[0],
                    getAuthTokenType(),          // Auth scope
                    null,                        // Authenticator-specific options
                    GoogleAccountActivity.this,  // The activity
                    new OnTokenAcquired(),       // Callback called when a token is successfully acquired
                    null);    // Callback called if an error occurs
            return null;
        }

        
        @Override
        public void onPostExecute(Void nothing){
            progressDialog.dismiss();
        }
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                // Get the result of the operation from the AccountManagerFuture.
                Bundle bundle = result.getResult();

                // The token is a named value in the bundle. The name of the value
                // is stored in the constant AccountManager.KEY_AUTHTOKEN.
                String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                onAuthenticated(token);

                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, 0);
                    return;
                }
            } catch (OperationCanceledException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (AuthenticatorException e ) {
                throw new RuntimeException(e);
            }

        }
    }



}
