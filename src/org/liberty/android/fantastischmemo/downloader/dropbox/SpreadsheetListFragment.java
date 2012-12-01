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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.liberty.android.fantastischmemo.downloader.AbstractDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.DownloadItem;
import org.liberty.android.fantastischmemo.downloader.DownloadItem.ItemType;
import org.liberty.android.fantastischmemo.downloader.google.Spreadsheet;

class SpreadsheetListFragment extends AbstractDownloaderFragment {
    private final String authToken;
    private final String authTokenSecret;

    public SpreadsheetListFragment(String authToken, String authTokenSecret) {
        this.authToken = authToken;
        this.authTokenSecret = authTokenSecret;
    }

	@Override
	protected List<DownloadItem> initialRetrieve() throws Exception {
		DropboxDownloadHelper downloadHelper = new DropboxDownloadHelper(getActivity(), authToken, authTokenSecret);
        List<DownloadItem> spreadsheetList = downloadHelper.getListSpreadsheets();
        return spreadsheetList;
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
        DropboxDownloadHelper downloadHelper = new DropboxDownloadHelper(getActivity(), authToken, authTokenSecret);
        return downloadHelper.downloadSpreadsheetToDB(di);
	}

    private DownloadItem convertSpreadsheetToDownloadItem(Spreadsheet spreadsheet) {
        DownloadItem di = new DownloadItem();
        di.setTitle(spreadsheet.getTitle());
        di.setType(DownloadItem.ItemType.Spreadsheet);
        di.setAddress(spreadsheet.getId());
        return di;
    }

    private Spreadsheet convertDownloadItemToSpreadsheet(DownloadItem di) {
        Spreadsheet sp = new Spreadsheet();
        sp.setTitle(di.getTitle());
        sp.setId(di.getAddress());
        sp.setUpdateDate(new Date());
        return sp;
    }

}
