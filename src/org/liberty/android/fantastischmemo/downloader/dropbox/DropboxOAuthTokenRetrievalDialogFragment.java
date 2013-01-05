/*
Copyright (C) 2012 xinxin wang

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

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.R;

import android.app.Activity;

import android.content.DialogInterface;

import android.graphics.Rect;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
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
        final WebView webview = (WebView)v.findViewById(R.id.login_page);
        final View loadingText = v.findViewById(R.id.auth_page_load_text);
        final View progressDialog = v.findViewById(R.id.auth_page_load_progress);
        final LinearLayout ll = (LinearLayout)v.findViewById(R.id.ll);
        
        // We have to set up the dialog's webview size manually or the webview will be zero size.
        // This should be a bug of Android.
        Rect displayRectangle = new Rect();
        Window window = mActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        ll.setMinimumWidth((int)(displayRectangle.width() * 0.9f));
        ll.setMinimumHeight((int)(displayRectangle.height() * 0.8f));

        
        
        WebSettings webviewSettings = webview.getSettings();
        webviewSettings.setJavaScriptEnabled(true);
        webviewSettings.setLoadWithOverviewMode(true);
        webviewSettings.setUseWideViewPort(true);
        webviewSettings.setBuiltInZoomControls(true);
        
        webview.setWebViewClient(new WebViewClient() {
            private boolean authenticated = false;
            @Override
            public void onPageFinished(WebView view, String url)  {
                loadingText.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                webview.setVisibility(View.VISIBLE);
                if (authenticated == true) {
                    return;
                }
                String code = getAuthCodeFromUrl(url);
                String error = getErrorFromUrl(url);
                if (error != null) {
                    authCodeReceiveListener.onAuthCodeError(error);
                    authenticated = true;
                    dismiss();
                }
                if (code != null) {
                    authenticated = true;
                    authCodeReceiveListener.onAuthCodeReceived(code);
                    dismiss();
                }
            }
        });
  	
        //load webview to show the authorize page
        new RequestTokenTask().execute(webview);
        
        return v;
    }

    
    private class RequestTokenTask extends AsyncTask<WebView, Void, Void> {
        @Override
		protected Void doInBackground(WebView... v) {
	        DropboxUtils.retrieveOAuthRequestToken();
	        v[0].loadUrl(DropboxUtils.getAuthorizationPageUrl());
			return null;
		}
    }
    
    public void setAuthCodeReceiveListener(AuthCodeReceiveListener listener) {
        authCodeReceiveListener = listener;
    }

    public static interface AuthCodeReceiveListener {
        void onAuthCodeReceived(String code);
        void onAuthCodeError(String error);
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


}
