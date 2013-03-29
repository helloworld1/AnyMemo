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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;

import android.util.Log;

public final class GoogleOAuth2AccessCodeRetrievalFragment extends OauthAccessCodeRetrievalFragment {

    private final static String TAG = "GoogleAuthFragment";

    @Override
    protected void requestToken() throws IOException {
        // Do nothing.
    }

    @Override
    protected String getLoginUrl() {
        try {
            String uri = String.format("https://accounts.google.com/o/oauth2/auth?client_id=%s&response_type=%s&redirect_uri=%s&scope=%s",
                    URLEncoder.encode(AMEnv.GOOGLE_CLIENT_ID, "UTF-8"),
                    URLEncoder.encode("code", "UTF-8"),
                    URLEncoder.encode(AMEnv.GOOGLE_REDIRECT_URI, "UTF-8"),
                    URLEncoder.encode(AMEnv.GDRIVE_SCOPE, "UTF-8"));
            return uri;
        } catch (UnsupportedEncodingException e) {
            // This is unlikely to happen
            Log.e(TAG, "The URL encodeing UTF-8 is not supported " + e);
            return null;
        }
    }

    @Override
    protected boolean processCallbackUrl(String url) {
        if (!url.startsWith(AMEnv.GOOGLE_REDIRECT_URI)) {
            return false;
        }

        int index = url.indexOf("code=");
        // If there is access token
        if (index != -1) {
            // Move index through "code="
            index += 5;
            String accessToken = url.substring(index);
            getAuthCodeReceiveListener().onAuthCodeReceived(accessToken);
            return true;
        }

        index = url.indexOf("error=");
        if (index != -1) {
            // Move index through "error="
            index += 6;
            String errorString = url.substring(index);
            getAuthCodeReceiveListener().onAuthCodeError(errorString);
            return true;
        }
        return false;
    }
}
