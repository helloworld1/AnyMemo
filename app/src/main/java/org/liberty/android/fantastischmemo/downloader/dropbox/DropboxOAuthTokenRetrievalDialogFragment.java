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

import java.io.IOException;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;

public final class DropboxOAuthTokenRetrievalDialogFragment extends OauthAccessCodeRetrievalFragment {

    private static final String REQUEST_TOKEN_URL = "https://api.dropbox.com/1/oauth/request_token";
    private static final String AUTHORIZE_TOKEN_URL =
            String.format("https://www.dropbox.com/oauth2/authorize?client_id=%s&response_type=token&redirect_uri=%s",
                    AMEnv.DROPBOX_CONSUMER_KEY,
                    AMEnv.DROPBOX_REDIRECT_URI);

    @Override
    protected void requestToken() throws IOException {}

    @Override
    protected String getLoginUrl() {
        return AUTHORIZE_TOKEN_URL;
    }

    @Override
    protected boolean processCallbackUrl(String url) {
        if (url.startsWith(AMEnv.DROPBOX_REDIRECT_URI)) {
            String[] splitUrl = url.split("[=&]");
            getAuthCodeReceiveListener().onAuthCodeReceived(splitUrl[0]);
            return true;
        }
        return false;
    }
}
