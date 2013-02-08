/*
Copyright (C) 2013 Haowen Ning

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
package org.liberty.android.fantastischmemo.downloader.oauth;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public abstract class OauthAccessCodeRetrievalFragment extends DialogFragment {
    private Activity mActivity;
    private AuthCodeReceiveListener authCodeReceiveListener = null;

    private final static String TAG = "OauthAccessCodeRetrievalFragment";

    // Return the URL to request token
    protected abstract String getTokenRequesetUrl();

    protected abstract String[] getAuthCodeFromUrl(String url);

    protected abstract String getErrorFromUrl(String url);

    
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

        webview.getSettings().setJavaScriptEnabled(true);

        webview.loadUrl(getTokenRequesetUrl());

        // This is workaround to show input on some android version.
        webview.requestFocus(View.FOCUS_DOWN);
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
                String[] codes = getAuthCodeFromUrl(url);
                String error = getErrorFromUrl(url);
                if (error != null) {
                    authCodeReceiveListener.onAuthCodeError(error);
                    authenticated = true;
                    dismiss();
                }
                if (codes != null) {
                    authenticated = true;
                    authCodeReceiveListener.onAuthCodeReceived(codes);
                    dismiss();
                }
            }
        });
        return v;
    }

    public void setAuthCodeReceiveListener(AuthCodeReceiveListener listener) {
        authCodeReceiveListener = listener;
    }

    public static interface AuthCodeReceiveListener {
        // the auth code received are different for oauth1 an oauth2
        // so this mehtod just has a list of possible codes
        void onAuthCodeReceived(String... codes);

        void onAuthCodeError(String error);

        void onCancelled();
    }
}
