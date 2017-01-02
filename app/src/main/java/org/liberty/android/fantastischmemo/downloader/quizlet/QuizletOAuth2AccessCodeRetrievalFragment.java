package org.liberty.android.fantastischmemo.downloader.quizlet;

import android.util.Log;

import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class QuizletOAuth2AccessCodeRetrievalFragment extends OauthAccessCodeRetrievalFragment {
    private static final String TAG = QuizletOAuth2AccessCodeRetrievalFragment.class.getSimpleName();

    public QuizletOAuth2AccessCodeRetrievalFragment() { }

    @Override
    protected void requestToken() throws IOException {
        // Do nothing.
    }

    @Override
    protected String getLoginUrl() {
        try {
            String uri = String
                .format("https://quizlet.com/authorize/?response_type=%s&client_id=%s&scope=%s&state=%s&redirect_uri=%s",
                    URLEncoder.encode("code", "UTF-8"), URLEncoder
                    .encode(AMEnv.QUIZLET_CLIENT_ID, "UTF-8"),
                    URLEncoder.encode("read write_set", "UTF-8"),
                    URLEncoder.encode("login", "UTF-8"),
                    URLEncoder.encode(AMEnv.QUIZLET_REDIRECT_URI,
                        "UTF-8"));
            Log.i(TAG, "Oauth request uri is " + uri);
            return uri;
        } catch (UnsupportedEncodingException e) {
            // This is unlikely to happen
            Log.e(TAG, "The URL encodeing UTF-8 is not supported ", e);
            return null;
        }
    }

    @Override
    protected boolean processCallbackUrl(String url) {
        Log.i(TAG, "Callback url is " + url);

        if (!url.startsWith(AMEnv.QUIZLET_REDIRECT_URI)) {
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
