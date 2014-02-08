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
package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.liberty.android.fantastischmemo.downloader.AbstractDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.DownloadItem;

import android.os.Bundle;

public class DownloadDBFileListFragment extends AbstractDownloaderFragment {

    private DropboxDownloadHelperFactory downloadHelperFactory;

    private DropboxDownloadHelper downloadHelper;

    public static final String EXTRA_AUTH_TOKEN = "authToken";

    public static final String EXTRA_AUTH_TOKEN_SECRET = "autoTokenSecret";

    @Inject
    public void setDownloadHelperFactory(
            DropboxDownloadHelperFactory downloadHelperFactory) {
        this.downloadHelperFactory = downloadHelperFactory;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle args = getArguments();
        assert args != null : "The DownloadDBFileListFragment must have authToken and autoTokenSecret";
        String authToken = args.getString(EXTRA_AUTH_TOKEN);
        String authTokenSecret = args.getString(EXTRA_AUTH_TOKEN_SECRET);
        downloadHelper = downloadHelperFactory.create(authToken, authTokenSecret);
    }

    @Override
    protected List<DownloadItem> initialRetrieve() throws ClientProtocolException,
              IOException, JSONException {
        return downloadHelper.fetchDBFileList();
    }

    @Override
    protected void openCategory(DownloadItem di) {
        // Do nothing
    }

    @Override
    protected void goBack() {
        // Do nothing
    }

    @Override
    protected String fetchDatabase(DownloadItem di) throws Exception {
        return downloadHelper.downloadDBFromDropbox(di);
    }

    @Override
    protected List<DownloadItem> loadMore() throws Exception {
        return null;
    }

    @Override
    protected boolean hasMore() {
        return false;
    }

}
