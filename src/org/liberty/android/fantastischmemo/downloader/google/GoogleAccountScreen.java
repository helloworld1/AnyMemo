package org.liberty.android.fantastischmemo.downloader.google;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.mycommons.io.IOUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import com.google.api.client.googleapis.services.GoogleKeyInitializer;

import com.google.api.client.http.HttpTransport;

import com.google.api.client.http.apache.ApacheHttpTransport;

import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;

import com.google.api.client.json.JsonFactory;

import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.drive.Drive;

import com.google.api.services.drive.Drive.Files;

import com.google.api.services.drive.DriveRequest;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

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

import android.util.Log;
import android.widget.Toast;

public class GoogleAccountScreen extends Activity {
    /** Called when the activity is first created. */
    private static final int DIALOG_ACCOUNTS = 0;
    private static final int REQUEST_AUTHENTICATE = 0;
    private static final String TAG = "GoogleAccountScreen";
    protected static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";
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
        final String apiKey = "AIzaSyBgSh_sLf2FUvKgc6ZjoYXLvf6viYWFZSo";
        try {
        URL url = new URL("https://www.googleapis.com/drive/v2/files?key=" + apiKey);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("client_id", "45533559525-77qfkgj6skoo222jqu2r18p2gcgreouc.apps.googleusercontent.com");
        conn.addRequestProperty("client_secret", "oD_rScGrwBO0iO3lArIktDoL");
        conn.addRequestProperty("Authorization", "OAuth " + authToken);

        BufferedReader in = new BufferedReader(new InputStreamReader(
                                    conn.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
        }
        in.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //final HttpTransport transport = new ApacheHttpTransport(ApacheHttpTransport.newDefaultHttpClient());

        //final JsonFactory jsonFactory = new GsonFactory();
        //GoogleCredential credential = new GoogleCredential();
        //credential.setAccessToken(authToken);

        //Drive service = new Drive.Builder(transport, jsonFactory, credential)
        //    .setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {

        //        @Override
        //        public void initialize(JsonHttpRequest request) throws IOException {
        //            DriveRequest driveRequest = (DriveRequest) request;
        //            driveRequest.setPrettyPrint(true);
        //            driveRequest.setKey(apiKey);
        //            driveRequest.setOauthToken(authToken);
        //        }
        //    })

        //    .build();

        //List<File> files = retrieveAllFiles(service);
        //Log.i("CHEOK", "size is " + files.size());
        //for (File file : files) {
        //    Log.i(TAG, "title = " + file.getTitle());
        //}

        //((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
        //authenticated();
    }

private static List<File> retrieveAllFiles(Drive service) {
        List<File> result = new ArrayList<File>();
        Files.List request = null;
        try {
            request = service.files().list();
        } catch (IOException e) {
            Log.e(TAG, "", e);
            return result;
        }

        do {
            try {
                FileList files = request.execute();
                for (File f : files.getItems()) {

                result.addAll(files.getItems());
                request.setPageToken(files.getNextPageToken());
                }
            } catch (IOException e) {
                Log.e(TAG, "", e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0);

        Log.i("CHEOK", "yup!");
        return result;
    } 

}
