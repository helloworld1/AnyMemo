package org.liberty.android.fantastischmemo;

import org.liberty.android.fantastischmemo.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AboutScreen extends Activity{
	private WebView webview;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_screen);
		webview = (WebView) findViewById(R.id.about_webview);
		webview.setWebViewClient(new AboutScreenClient());
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadUrl("file:///android_asset/about.html");
	}
	
	private class AboutScreenClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
	        webview.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
			
}
	