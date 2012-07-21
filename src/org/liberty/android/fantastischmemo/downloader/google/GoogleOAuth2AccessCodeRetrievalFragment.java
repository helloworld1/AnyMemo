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

import java.net.URLEncoder;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.webkit.WebView;
import android.webkit.WebViewClient;

class GoogleOAuth2AccessCodeRetrievalFragment extends DialogFragment {
    private Activity mActivity;
    private AuthCodeReceiveListener authCodeReceiveListener = null;

    private final static String TAG = "GoogleAuthFragment";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final WebView webview = new WebView(mActivity);
        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient() {
            private boolean authenticated = false;
            @Override
            public void onPageFinished(WebView view, String url)  {
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
        try {
            String uri = String.format("https://accounts.google.com/o/oauth2/auth?client_id=%s&response_type=%s&redirect_uri=%s&scope=%s",
                    URLEncoder.encode(AMEnv.GOOGLE_CLIENT_ID, "UTF-8"),
                    URLEncoder.encode("code", "UTF-8"),
                    URLEncoder.encode(AMEnv.GOOGLE_REDIRECT_URI, "UTF-8"),
                    URLEncoder.encode(AMEnv.GDRIVE_SCOPE, "UTF-8"));
            webview.loadUrl(uri);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new AlertDialog.Builder(mActivity)
            .setView(webview)
            .create();
    }

    public void setAuthCodeReceiveListener(AuthCodeReceiveListener listener) {
        authCodeReceiveListener = listener;
    }

    public static interface AuthCodeReceiveListener {
        void onAuthCodeReceived(String code);
        void onAuthCodeError(String error);
    }

    private String getAuthCodeFromUrl(String url) {
        if (!url.startsWith(AMEnv.GOOGLE_REDIRECT_URI)) {
            return null;
        }
        int index = url.indexOf("code=");
        if (index == -1) {
            return null;
        }
        // Move index through "code="
        index += 5;
        return url.substring(index);
    }

    private String getErrorFromUrl(String url) {
        if (!url.startsWith(AMEnv.GOOGLE_REDIRECT_URI)) {
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
