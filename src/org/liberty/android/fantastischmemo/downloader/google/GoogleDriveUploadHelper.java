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

import java.util.List;

import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;

import org.liberty.android.fantastischmemo.downloader.google.Cells;

import org.liberty.android.fantastischmemo.utils.AMUtil;

import android.content.Context;

public class GoogleDriveUploadHelper {
    private Context mContext;

    private final String authToken;

    public GoogleDriveUploadHelper(Context context, String authToken) {
        this.authToken = authToken;
        mContext = context;
    }

    public Spreadsheet createSpreadsheet(String title, String dbPath) throws Exception {

        // Find the spreadsheets to delete after the process is done
        List<Spreadsheet> spreadsheetsToDelete = SpreadsheetFactory.findSpreadsheets(title, authToken);


        // Create new spreadsheet
        SpreadsheetFactory.createSpreadsheet(title, authToken);
        List<Spreadsheet> spreadsheetList = SpreadsheetFactory.getSpreadsheets(authToken);
        Spreadsheet newSpreadsheet = spreadsheetList.get(0);

        // Create worksheets
        List<Worksheet> worksheetsToDelete = WorksheetFactory.getWorksheets(newSpreadsheet.getId(), authToken);
        Worksheet cardsWorksheet = WorksheetFactory.createWorksheet(newSpreadsheet, "cards", authToken);


        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, dbPath);
        final CardDao cardDao = helper.getCardDao();
        final CategoryDao categoryDao = helper.getCategoryDao();
        final LearningDataDao learningDataDao = helper.getLearningDataDao();
        List<Card> cardList = cardDao.callBatchTasks(new Callable<List<Card>>() {
            public List<Card> call() throws Exception {
                List<Card> cards = cardDao.queryForAll();
                for (Card c: cards) {
                    categoryDao.refresh(c.getCategory());
                    learningDataDao.refresh(c.getLearningData());
                }
                return cards;
            }
        });
        AnyMemoDBOpenHelperManager.releaseHelper(helper);
        Cells cells = new Cells();
        cells.addCell(1, 1, "question");
        cells.addCell(1, 2, "answer");
        cells.addCell(1, 3, "category");
        cells.addCell(1, 4, "note");
        for (int i = 0; i < cardList.size(); i++) {
            Card c = cardList.get(i);
            cells.addCell(i + 1, 1, c.getQuestion());
            cells.addCell(i + 1, 2, c.getAnswer());
            cells.addCell(i + 1, 3, c.getCategory().getName());
            cells.addCell(i + 1, 4, c.getNote());
        }

        // Insert rows into worksheet
        CellsFactory.uploadCells(newSpreadsheet, cardsWorksheet, cells, authToken);

        // Finally delete the unneeded worksheets ...
        for (Worksheet ws : worksheetsToDelete) {
            WorksheetFactory.deleteWorksheet(newSpreadsheet, ws, authToken);
        }
        // ... And spreadsheets with duplicated names.
        for (Spreadsheet ss : spreadsheetsToDelete) {
            SpreadsheetFactory.deleteSpreadsheet(ss, authToken);
        }
        return null;
    }

    private Spreadsheet searchSpreadsheetTitle(String title) throws Exception {
        List<Spreadsheet> sl = SpreadsheetFactory.getSpreadsheets(authToken);
        for (Spreadsheet s : sl) {
            if (title.equals(s.getTitle())) {
                return s;
            }
        }
        return null;
    }

}
