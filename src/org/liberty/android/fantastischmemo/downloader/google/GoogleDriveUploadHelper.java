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

import java.util.List;

import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;

import org.liberty.android.fantastischmemo.downloader.google.Cells;

import android.content.Context;

public class GoogleDriveUploadHelper {
    private Context mContext;

    private final String authToken;

    public GoogleDriveUploadHelper(Context context, String authToken) {
        this.authToken = authToken;
        mContext = context;
    }

    public Spreadsheet createSpreadsheet(String title, String dbPath) throws Exception {


        // First read card because if it failed we don't even bother uploading.
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, dbPath);
        List<Card> cardList = null;
        try {
            final CardDao cardDao = helper.getCardDao();
            final CategoryDao categoryDao = helper.getCategoryDao();
            final LearningDataDao learningDataDao = helper.getLearningDataDao();
            cardList = cardDao.callBatchTasks(new Callable<List<Card>>() {
                public List<Card> call() throws Exception {
                    List<Card> cards = cardDao.queryForAll();
                    for (Card c: cards) {
                        categoryDao.refresh(c.getCategory());
                        learningDataDao.refresh(c.getLearningData());
                    }
                    return cards;
                }
            });
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

        // Find the spreadsheets to delete after the process is done
        List<Document> spreadsheetsToDelete = DocumentFactory.findDocuments(title, authToken);



        // Create the AnyMemo folder if needed
        Folder folder = FolderFactory.createOrReturnFolder("AnyMemo", authToken);

        // Create new spreadsheet
        Document newSpreadsheetDocument = DocumentFactory.createSpreadsheet(title, authToken);
        List<Spreadsheet> spreadsheetList = SpreadsheetFactory.getSpreadsheets(authToken);
        Spreadsheet newSpreadsheet = spreadsheetList.get(0);

        // Create worksheets
        List<Worksheet> worksheetsToDelete = WorksheetFactory.getWorksheets(newSpreadsheet, authToken);

        // setting up the worksheet size is critical.
        Worksheet cardsWorksheet = WorksheetFactory.createWorksheet(newSpreadsheet, "cards", cardList.size() + 1, 4, authToken);

        Cells cells = new Cells();

        // Add the header for cards first
        cells.addCell(1, 1, "question");
        cells.addCell(1, 2, "answer");
        cells.addCell(1, 3, "category");
        cells.addCell(1, 4, "note");
        for (int i = 0; i < cardList.size(); i++) {
            Card c = cardList.get(i);
            // THe first row is the header.
            cells.addCell(i + 2, 1, c.getQuestion());
            cells.addCell(i + 2, 2, c.getAnswer());
            cells.addCell(i + 2, 3, c.getCategory().getName());
            cells.addCell(i + 2, 4, c.getNote());
        }

        // Insert rows into worksheet
        CellsFactory.uploadCells(newSpreadsheet, cardsWorksheet, cells, authToken);


        // Put new spreadsheet into the folder
        FolderFactory.addDocumentToFolder(newSpreadsheetDocument, folder, authToken);

        // Finally delete the unneeded worksheets ...
        for (Worksheet ws : worksheetsToDelete) {
            WorksheetFactory.deleteWorksheet(newSpreadsheet, ws, authToken);
        }
        // ... And spreadsheets with duplicated names.
        for (Document ss : spreadsheetsToDelete) {
            DocumentFactory.deleteDocument(ss, authToken);
        }
        return null;
    }
}
