/*
Copyright (C) 2010 Haowen Ning

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
package org.liberty.android.fantastischmemo.downloader;

import org.liberty.android.fantastischmemo.*;

import java.util.ArrayList;
import java.util.Stack;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.util.Comparator;
import java.net.URLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Serializable;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.liberty.android.fantastischmemo.downloader.FELauncher;
import org.liberty.android.fantastischmemo.downloader.FELauncher;

import android.content.SharedPreferences;


import android.os.Bundle;
import android.content.Context;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.net.Uri;
import android.webkit.WebView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import oauth.signpost.*;
import oauth.signpost.commonshttp.*;
import oauth.signpost.exception.*;
import oauth.signpost.signature.*;

public class FEOauth extends AMActivity{
    private static final String TAG = "org.liberty.android.fantastischmemo.FEOauth";
    public static final String CONSUMER_KEY= "anymemo_android";
    public static final String CONSUMER_SECRET = "nju5M3ezHk";
    private static final String REQUEST_URL= "https://secure.flashcardexchange.com/oauth_request_token";
    private static final String AUTH_URL= "https://secure.flashcardexchange.com/oauth_login";
    private static final String ACCESS_TOKEN_URL = "https://secure.flashcardexchange.com/oauth_access_token";
    private static final String CALLBACK_URL = "anymemo-fe://fe";
    private static OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
    private static OAuthProvider provider = new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_TOKEN_URL, AUTH_URL); 
    private WebView webview;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        webview = new WebView(FEOauth.this);
        setContentView(webview);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        AMGUIUtility.doProgressTask(this, R.string.loading_please_wait, R.string.access_authorization_text, new AMGUIUtility.ProgressTask(){
            private String authUrl;
            public void doHeavyTask() throws Exception{
                authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
            }
            public void doUITask(){
                Log.v(TAG, "Request token: " + consumer.getToken());
                Log.v(TAG, "Token secret: " + consumer.getTokenSecret());
                webview.loadUrl(authUrl);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //Uri uri = this.getIntent().getData();

    }

    @Override
    protected void onNewIntent (Intent intent) {
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
            Log.d("Oauth", uri.toString());
            String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
            try {

                provider.retrieveAccessToken(consumer, verifier);
                String token = consumer.getToken();
                String tokenSecret= consumer.getTokenSecret();

                Log.d("Oauth Verifier ", verifier);
                Log.d("Oauth Token ", token);
                Log.d("Oauth Token Secret ", tokenSecret);

                editor.putString("saved_oauth_token", token);
                editor.putString("saved_oauth_token_secret", tokenSecret);
                editor.commit();

                /* Ask user to input the username */
                final EditText et = new EditText(this);
                new AlertDialog.Builder(this)
                    .setTitle(R.string.fe_private_login)
                    .setMessage(R.string.fe_private_login_message)
                    .setView(et)
                    .setPositiveButton(R.string.search_text, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            String searchText = et.getText().toString();
                            editor.putString("saved_username", searchText);
                            editor.commit();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel_text, null)
                    .create()
                    .show();


            } catch (OAuthMessageSignerException e) {
                e.printStackTrace();
            } catch (OAuthNotAuthorizedException e) {
                e.printStackTrace();
            } catch (OAuthExpectationFailedException e) {
                e.printStackTrace();
            } catch (OAuthCommunicationException e) {
                e.printStackTrace();
            }
        } else{

            //try{
            //    receiveKey = false;
            //    String authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);

            //    System.out.println("Request token: " + consumer.getToken());
            //    System.out.println("Token secret: " + consumer.getTokenSecret());


            //    //Intent intent = new Intent(Intent.ACTION_VIEW);
            //    //intent.setData(Uri.parse(authUrl));
            //    //startActivity(intent);
            //    webview.loadUrl(authUrl);
            //}
            //catch(Exception e){
            //    e.printStackTrace();
            //}
        }
    }


}
