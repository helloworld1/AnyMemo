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

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.liberty.android.fantastischmemo.AMEnv;

import org.liberty.android.fantastischmemo.downloader.AbstractDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.DownloadItem;

import org.liberty.android.fantastischmemo.utils.AMUtil;

class SpreadsheetListFragment extends AbstractDownloaderFragment {
    private String authToken = null;

    public SpreadsheetListFragment(String authToken) {
        this.authToken = authToken;
    }

	@Override
	protected List<DownloadItem> initialRetrieve() throws Exception {
        URL url = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);

        List<Spreadsheet> spreadsheetList = SpreadsheetFactory.getSpreadsheetsFromRequest(conn);
        List<DownloadItem> downloadItemList = new ArrayList<DownloadItem>(spreadsheetList.size());
        for (Spreadsheet spreadsheet : spreadsheetList) {
            DownloadItem di = new DownloadItem();
            di.setTitle(spreadsheet.getTitle());
            di.setAddress(spreadsheet.getId());
            di.setType(DownloadItem.TYPE_DATABASE);
            downloadItemList.add(di);
        }
        return downloadItemList;
	}

	@Override
	protected void openCategory(DownloadItem di) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void goBack() {
        // Do nothing
	}

	@Override
	protected void fetchDatabase(DownloadItem di) throws Exception {
        String worksheetAddress = "https://spreadsheets.google.com/feeds/worksheets/" + di.getAddress()+ "/private/full";
        System.out.println("worksheet address: " + worksheetAddress);
        URL url = new URL(worksheetAddress);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);
        List<Worksheet> worksheets = WorksheetFactory.getWorksheetsFromRequest(conn);

        for (Worksheet w : worksheets) {
            System.out.println("Worksheet: " + w);

            URL url1 = new URL("https://spreadsheets.google.com/feeds/cells/" + di.getAddress() + "/" + w.getId() + "/private/full");
            HttpsURLConnection conn1 = (HttpsURLConnection) url1.openConnection();
            conn1.addRequestProperty("Authorization", "GoogleLogin auth=" + authToken);
            Cells cells = CellsFactory.getCellsFromRequest(conn1);
            System.out.println(cells.toString());
            if (!"learningdata".equalsIgnoreCase(w.getTitle())) {

                CellsDBConverter converter = new CellsDBConverter(getActivity());
                String saveDBPath= AMEnv.DEFAULT_ROOT_PATH + "/" + di.getTitle() + ".db";
                AMUtil.deleteFileWithBackup(saveDBPath);
                converter.convertCellsToDb(cells, null, saveDBPath);
            }
        }


        //String s = new String(IOUtils.toByteArray(conn.getInputStream()));
        //System.out.println(s);

		
	}

    private String getIdFromUrl(String url) {
        String[] splitedAddress = url.split("/");
        return splitedAddress[splitedAddress.length - 1];
    }
}
