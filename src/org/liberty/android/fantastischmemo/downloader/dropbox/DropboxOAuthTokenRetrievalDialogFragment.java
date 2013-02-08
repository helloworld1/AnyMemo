/*
Copyright (C) 2012 Xinxin Wang, Haowen Ning

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
package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class DropboxOAuthTokenRetrievalDialogFragment extends DialogFragment {
    private Activity mActivity;
    private AuthCodeReceiveListener authCodeReceiveListener = null;

    private final static String TAG = "DropboxAuthFragment";

    private static final String REQUEST_TOKEN_URL = "https://api.dropbox.com/1/oauth/request_token";
    private static final String AUTHORIZE_TOKEN_URL = "https://www.dropbox.com/1/oauth/authorize";
 
    private String oauthRequestTokenSecret = null;
    private String oauthRequestToken = null;

    private WebView webview;

    private View loadingText;

    private View progressDialog;

    private LinearLayout rootView;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        authCodeReceiveListener.onCancelled();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View v = inflater.inflate(R.layout.oauth_login_layout, container, false);

        webview = (WebView)v.findViewById(R.id.login_page);

        loadingText = v.findViewById(R.id.auth_page_load_text);

        progressDialog = v.findViewById(R.id.auth_page_load_progress);

        rootView = (LinearLayout)v.findViewById(R.id.ll);
        
        // We have to set up the dialog's webview size manually or the webview will be zero size.
        // This should be a bug of Android.
        Rect displayRectangle = new Rect();
        Window window = mActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        rootView.setMinimumWidth((int)(displayRectangle.width() * 0.9f));
        rootView.setMinimumHeight((int)(displayRectangle.height() * 0.8f));
        
        //load webview to show the authorize page
        new RequestTokenTask().execute();
        
        return v;
    }

        
    public void setAuthCodeReceiveListener(AuthCodeReceiveListener listener) {
        authCodeReceiveListener = listener;
    }

    public static interface AuthCodeReceiveListener {
        void onRequestTokenSecretReceived(String requestToken, String requestTokenSecret);
        void onRequestTokenSecretError(String error);
        void onCancelled();
    }

    private String getAuthCodeFromUrl(String url) {
        if (!url.startsWith(AMEnv.DROPBOX_REDIRECT_URI)) {
            return null;
        }
        int startIndex = url.indexOf("uid=");
        if (startIndex == -1) {
            return null;
        }
        // Move index through "code="
        startIndex += 4;
        
        int endIndex = url.indexOf("&", startIndex);
        String uid;
        if(endIndex > startIndex){
        	uid = url.substring(startIndex, endIndex);
        } else {
        	uid = url.substring(startIndex);
        }
        
        return uid;
        
    }

    private String getErrorFromUrl(String url) {
        if (!url.startsWith(AMEnv.DROPBOX_REDIRECT_URI)) {
            return null;
        }
        int index = url.indexOf("error=");
        if (index == -1) {
            return null;
        }
        // Move index through "error="
        index += 6;
        return url.substring(index);
    }

    
    private void retrieveOAuthRequestToken() throws IOException{
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(REQUEST_TOKEN_URL);
        httpPost.setHeader("Authorization", DropboxUtils.buildOAuthRequestHeader());
        HttpResponse response = httpClient.execute(httpPost);
        
        if( response.getStatusLine().getStatusCode() == 200){
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
            String result = reader.readLine();
            
            if(result.length() != 0){
                String[] parsedResult = result.split("&");
                oauthRequestTokenSecret=parsedResult[0].split("=")[1];
                oauthRequestToken=parsedResult[1].split("=")[1];
            } 
            reader.close();
        } else {
            throw new IOException("HTTP code for fetching Request token: " + response.getStatusLine().getStatusCode());
        }
    }

    
    private class RequestTokenTask extends AsyncTask<Void, Void, Exception> {

        @Override
        protected void onPreExecute() {
            WebSettings webviewSettings = webview.getSettings();
            webviewSettings.setJavaScriptEnabled(true);
            
            webview.setWebViewClient(new WebViewClient() {
                private boolean authenticated = false;
                @Override
                public void onPageFinished(WebView view, String url)  {
                    // Disable the progress and show the loaded webpage.
                    webview.setVisibility(View.VISIBLE);
                    progressDialog.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);

                    if (authenticated == true) {
                        return;
                    }
                    String code = getAuthCodeFromUrl(url);
                    String error = getErrorFromUrl(url);
                    if (error != null) {
                        authCodeReceiveListener.onRequestTokenSecretError(error);
                        authenticated = true;
                        dismiss();
                        cancel(true);
                    }
                    if (code != null) {
                        authenticated = true;
                        authCodeReceiveListener.onRequestTokenSecretReceived(oauthRequestToken, oauthRequestTokenSecret);
                        dismiss();
                        cancel(true);
                    }
                }
            });

            // This is workaround to show input on some android version.
            webview.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_UP:
                            if (!v.hasFocus()) {
                                v.requestFocus();
                            }
                            break;
                    }
                    return false;
                }
            });
            webview.requestFocus(View.FOCUS_DOWN);
        }
        @Override
		protected Exception doInBackground(Void... params) {
            try {
                retrieveOAuthRequestToken();
            } catch (IOException e) {
                return e;
            }
            return null;
		}

        protected void onPostExecute(Exception e) {
            if (e == null){
                webview.loadUrl(AUTHORIZE_TOKEN_URL + "?oauth_token="+ oauthRequestToken+"&oauth_callback="+AMEnv.DROPBOX_REDIRECT_URI);
            } else {
                AMGUIUtility.displayError(mActivity, getString(R.string.error_text), getString(R.string.error_text), e);
            }
        }
    }
}
