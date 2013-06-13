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
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;

public final class DropboxOAuthTokenRetrievalDialogFragment extends OauthAccessCodeRetrievalFragment {

    private static final String REQUEST_TOKEN_URL = "https://api.dropbox.com/1/oauth/request_token";
    private static final String AUTHORIZE_TOKEN_URL = "https://www.dropbox.com/1/oauth/authorize";

    private String oauthToken = null;

    private String oauthTokenSecret = null;

    @Override
    protected void requestToken() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(REQUEST_TOKEN_URL);
        httpPost.setHeader("Authorization", DropboxUtils.buildOAuthRequestHeader());
        HttpResponse response = httpClient.execute(httpPost);

        if( response.getStatusLine().getStatusCode() == 200){
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
            String result = reader.readLine();

            if (result.length() != 0){
                String[] parsedResult = result.split("&");
                oauthTokenSecret = parsedResult[0].split("=")[1];
                oauthToken = parsedResult[1].split("=")[1];
            }
            reader.close();
        } else {
            throw new IOException("HTTP code for fetching Request token: " + response.getStatusLine().getStatusCode());
        }
    }

    @Override
    protected String getLoginUrl() {
        return AUTHORIZE_TOKEN_URL + "?oauth_token=" + oauthToken + "&oauth_callback="+AMEnv.DROPBOX_REDIRECT_URI;
    }

    @Override
    protected boolean processCallbackUrl(String url) {
        if (url.startsWith(AMEnv.DROPBOX_REDIRECT_URI)) {
            getAuthCodeReceiveListener().onAuthCodeReceived(oauthToken, oauthTokenSecret);
            return true;
        }
        return false;
    }

}
