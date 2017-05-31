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

import android.os.Bundle;

import org.liberty.android.fantastischmemo.downloader.common.AbstractDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.common.DownloadItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SpreadsheetListFragment extends AbstractDownloaderFragment {

    public static final String EXTRA_AUTH_TOKEN = "authToken";

    private String authToken = null;

    private GoogleDriveDownloadHelper downloadHelper;

    public SpreadsheetListFragment() { }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle args = getArguments();
        this.authToken = args.getString(EXTRA_AUTH_TOKEN);
        downloadHelper = new GoogleDriveDownloadHelper(appComponents(), authToken);
    }

    @Override
    protected List<DownloadItem> initialRetrieve() {
        try {
            List<Spreadsheet> spreadsheetList;
            spreadsheetList = downloadHelper.getListSpreadsheets();

            List<DownloadItem> downloadItemList = new ArrayList<>(50);
            for (Spreadsheet spreadsheet : spreadsheetList) {
                downloadItemList.add(convertSpreadsheetToDownloadItem(spreadsheet));
            }
            return downloadItemList;

        } catch (final Exception e) {
            // Catch the exception here to give user change to log out to avoid invalid token exception
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activityComponents().errorUtil().showNonFatalError("Error retrieving initial list. Please log out and try again.", e);
                    }
                });
            }
            return Collections.emptyList();
        }

    }

    @Override
    protected List<DownloadItem> openCategory(DownloadItem di) {
        return Collections.emptyList();
    }

    @Override
    protected List<DownloadItem> goBack() {
        return Collections.emptyList();
    }

    @Override
    protected String fetchDatabase(DownloadItem di) throws Exception {
        return downloadHelper.downloadSpreadsheetToDB(convertDownloadItemToSpreadsheet(di));
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

    @Override
    protected List<DownloadItem> loadMore() throws Exception {
        return null;
    }

    @Override
    protected boolean hasMore() {
        return false;
    }

}
