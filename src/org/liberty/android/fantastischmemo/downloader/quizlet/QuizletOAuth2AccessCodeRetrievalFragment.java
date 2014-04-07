package org.liberty.android.fantastischmemo.downloader.quizlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;

import roboguice.util.Ln;

public final class QuizletOAuth2AccessCodeRetrievalFragment extends
        OauthAccessCodeRetrievalFragment {

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
            Ln.i("Oauth request uri is " + uri);
            return uri;
        } catch (UnsupportedEncodingException e) {
            // This is unlikely to happen
            Ln.e(e, "The URL encodeing UTF-8 is not supported ");
            return null;
        }
    }

    @Override
    protected boolean processCallbackUrl(String url) {
        Ln.i("Callback url is " + url);

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
