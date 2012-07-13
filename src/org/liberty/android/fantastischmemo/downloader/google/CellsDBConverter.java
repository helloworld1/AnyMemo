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

import java.io.IOException;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;

import android.content.Context;

public class CellsDBConverter {

    private Context mContext;

    public CellsDBConverter(Context context) {
        mContext = context; 
    }

    /* cardCells contains the question, answer, category and note
     * category and note is optionally.
     * learningDataCells contains all necessary learning data. 
     * If learningDataCells, new learning data is used.
     * dbPath is the place to store converted database
     */
    public void convertCellsToDb(Cells cardCells, Cells learningDataCells, String dbPath) throws IOException {

        int numberOfRows = cardCells.getRowCounts();
        
        // We ignore the header row
        List<Card> cardList = new ArrayList<Card>(numberOfRows - 1);
        for (int i = 1; i < numberOfRows; i++) {
            List<String> row = cardCells.getRow(i);
            Card card = new Card();
            Category category = new Category();
            LearningData learningData = new LearningData();

            if (row.size() < 2) {
                throw new IOException("Each row in spreadsheet should have at least 2 column.");
            }

            card.setQuestion(row.get(0));
            card.setAnswer(row.get(1));

            if (row.size() >= 3) {
                category.setName(row.get(2));
            }

            if (row.size() >= 4) {
                card.setNote(row.get(3));
            }

            card.setCategory(category);
            card.setLearningData(learningData);
            cardList.add(card);
        }

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, dbPath); 
        try {
            CardDao cardDao = helper.getCardDao();
            cardDao.createCards(cardList);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

    }
}
