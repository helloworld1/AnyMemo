/*
Copyright (C) 2012 Haowen Ning, Xinxin Wang

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

import org.liberty.android.fantastischmemo.AMEnv;


public class DropboxUtils {

    public static String buildOAuthAccessHeader(String oauthRequestToken, String oauthRequestTokenSecret){
        String headerValue =
            "OAuth oauth_version=\""+ AMEnv.DROPBOX_OAUTH_VERSION +"\", "
            + "oauth_signature_method=\""+ AMEnv.DROPBOX_OAUTH_SIGNATURE_METHOD+"\", "
            + "oauth_token=\"" + oauthRequestToken + "\", "
            + "oauth_consumer_key=\""+ AMEnv.DROPBOX_CONSUMER_KEY +"\", "
            + "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET+ "&" + oauthRequestTokenSecret + "\"";

        return headerValue;
    }

    public static String buildOAuthRequestHeader(){
        String requestHeader =
            "OAuth oauth_version=\""+AMEnv.DROPBOX_OAUTH_VERSION+"\", " +
            "oauth_signature_method=\""+ AMEnv.DROPBOX_OAUTH_SIGNATURE_METHOD +"\", " +
            "oauth_consumer_key=\""+ AMEnv.DROPBOX_CONSUMER_KEY +"\", "+
            "oauth_signature=\""+ AMEnv.DROPBOX_CONSUMER_SECRET + "&\"";
        return requestHeader;

    }

    public static String getFileExchangeAuthHeader(String authToken, String authTokenSecret){
        return "OAuth oauth_version=\""+ AMEnv.DROPBOX_OAUTH_VERSION +"\", "
            + "oauth_signature_method=\""+ AMEnv.DROPBOX_OAUTH_SIGNATURE_METHOD +"\", "
            + "oauth_consumer_key=\"" + AMEnv.DROPBOX_CONSUMER_KEY + "\", "
            + "oauth_token=\"" + authToken + "\", "
            + "oauth_signature=\"" + AMEnv.DROPBOX_CONSUMER_SECRET + "&"
            + authTokenSecret + "\"";
    }

}
