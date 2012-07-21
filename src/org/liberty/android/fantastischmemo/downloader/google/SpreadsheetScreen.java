package org.liberty.android.fantastischmemo.downloader.google;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.widget.Toast;

public class SpreadsheetScreen extends Activity {
    /** Called when the activity is first created. */
    private static final int DIALOG_ACCOUNTS = 0;
    private static final int REQUEST_AUTHENTICATE = 0;
    private static final String TAG = "SpreadsheetScreen";
    protected static final String AUTH_TOKEN_TYPE = "wise";
    private String authToken;
    Context context;    


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;          
        gotAccount(false);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ACCOUNTS:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select a Google account");
                final AccountManager manager = AccountManager.get(this);
                final Account[] accounts = manager.getAccountsByType("com.google");
                final int size = accounts.length;
                String[] names = new String[size];
                for (int i = 0; i < size; i++) {
                    names[i] = accounts[i].name;
                }
                builder.setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        gotAccount(manager, accounts[which]);
                    }
                });
                return builder.create();
        }
        return null;
    }

    private void gotAccount(boolean tokenExpired) {
        SharedPreferences settings = getSharedPreferences("test", 0);
        String accountName = settings.getString("accountName", null);
        if (accountName != null) {
            AccountManager manager = AccountManager.get(this);
            Account[] accounts = manager.getAccountsByType("com.google");
            int size = accounts.length;
            for (int i = 0; i < size; i++) {
                Account account = accounts[i];
                if (accountName.equals(account.name)) {

                    Toast.makeText(this,"OLD accunt name"+account.name , Toast.LENGTH_SHORT).show();
                    if (tokenExpired) {
                        Toast.makeText(this,"Token EXpired", Toast.LENGTH_SHORT).show();
                        manager.invalidateAuthToken("com.google", this.authToken);

                    }
                    gotAccount(manager, account);
                    return;
                }
            }
        }
        showDialog(DIALOG_ACCOUNTS);
    }

    private void gotAccount(final AccountManager manager, final Account account) {
        SharedPreferences settings = getSharedPreferences("test", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("accountName", account.name);
        editor.commit();            

        new Thread() {
            @Override
            public void run() {
                try {
                    final Bundle bundle =
                        manager.getAuthToken(account, AUTH_TOKEN_TYPE, true, null, null)
                        .getResult();

                    runOnUiThread(new Runnable() {

                        public void run() {
                            try {
                                if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                                    Intent intent =
                        bundle.getParcelable(AccountManager.KEY_INTENT);
                    int flags = intent.getFlags();
                    flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
                    intent.setFlags(flags);
                    startActivityForResult(intent, REQUEST_AUTHENTICATE);
                                } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                                    authenticatedClientLogin(
                                        bundle.getString(AccountManager.KEY_AUTHTOKEN));
                                }
                            } catch (Exception e) {
                                // handleException(e);
                                throw new RuntimeException(e);
                                //Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show(); 
                            }
                        }
                    });
                } catch (Exception e) {
                    //handleException(e);
                    //Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show(); 
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_AUTHENTICATE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this,"Result OK!!" , Toast.LENGTH_SHORT).show();
                    gotAccount(false);
                } else {
                    Toast.makeText(this,"Result False!!" , Toast.LENGTH_SHORT).show();
                    showDialog(DIALOG_ACCOUNTS);
                }
                break;
        }
            }

    private void authenticatedClientLogin(final String authToken) {
        this.authToken = authToken;
        Toast.makeText(this,"Token "+authToken, Toast.LENGTH_LONG).show();
        try {
        URL url = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);

        BufferedReader in = new BufferedReader(new InputStreamReader(
                                    conn.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
        }
        in.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
