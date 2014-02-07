/*
Copyright (C) 2014 Haowen Ning

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
package org.liberty.android.fantastischmemo.downloader.cram;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.liberty.android.fantastischmemo.downloader.AbstractDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.DownloadItem;

import android.os.Bundle;

/**
 * List all card sets for a user.
 *
 * If the authToken is passed in, the private cards will be shown and can be downloaded.
 */
public class CramCardSetListFragment extends AbstractDownloaderFragment {

    public static final String EXTRA_AUTH_TOKEN = "authToken";

    public static final String EXTRA_SEARCH_TERM= "searchTerm";

    /**
     * The passed value must be SearchMethod.[Value].toString()
     */
    public static final String EXTRA_SEARCH_METHOD = "searchMethod";

    private String authToken = null;
    
    private String searchTerm = null;

    private SearchMethod searchMethod;

    private CramDownloadHelper cramDownloadHelper;

    @Inject
    public void setCramDownloadHelper(CramDownloadHelper cramDownloadHelper) {
        this.cramDownloadHelper = cramDownloadHelper;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle args = getArguments();
        assert args != null : "The EXTRA_SEARCH_TERM and EXTRA_SEARCH_METHOD  must be passed to SpreadsheetListFragment";

        // authTOken can be empty to indicate accessing public cards only
        this.authToken = args.getString(EXTRA_AUTH_TOKEN);

        this.searchTerm = args.getString(EXTRA_SEARCH_TERM);

        this.searchMethod = SearchMethod.valueOf(args.getString(EXTRA_SEARCH_METHOD));

        assert StringUtils.isNotEmpty(searchTerm) : "Search term should not be empty";

    }

    @Override
    protected List<DownloadItem> initialRetrieve() throws Exception {
        if (this.searchMethod == SearchMethod.ByUserName) {
            return cramDownloadHelper.getCardSetListByUserName(authToken, searchTerm);
        } else if (this.searchMethod == SearchMethod.ByTitle) {
            return cramDownloadHelper.getCardListByTitle(authToken, searchTerm);
        } else {
            throw new IllegalArgumentException("initialRetrieve does not know how to handle search method: " + this.searchMethod);
        }

    }

    @Override
    protected void openCategory(DownloadItem di) {
        // Not implemented
    }

    @Override
    protected void goBack() {
        // Not implemented
    }

    @Override
    protected String fetchDatabase(DownloadItem di) throws Exception {
        String id = di.getAddress();
        return cramDownloadHelper.downloadCardSet(authToken, id);
    }

    public enum SearchMethod {
        ByUserName,
        ByTitle
    };
}


