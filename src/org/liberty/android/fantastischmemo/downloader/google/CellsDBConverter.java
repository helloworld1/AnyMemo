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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;

import android.content.Context;

import android.util.Log;

public class CellsDBConverter {

    private Context mContext;

    private final static SimpleDateFormat ISO8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private final static String TAG = "CellsDBConverter";

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
        int numberOfLearningDataRows = 0;
        if (learningDataCells != null) {
            numberOfLearningDataRows = learningDataCells.getRowCounts();
        }

        // We ignore the header row
        List<Card> cardList = new ArrayList<Card>(numberOfRows + 1);
        for (int i = 1; i < numberOfRows; i++) {
            List<String> row = cardCells.getRow(i);
            Card card = new Card();
            Category category = new Category();


            if (row.size() == 0) {
                Log.w(TAG, "Each row in spreadsheet should have at least 2 column: question and answer. Row number: " + i);
            }

            if (row.size() >= 1) {
                card.setQuestion(row.get(0));
            }
            if (row.size() >= 2) {
                card.setAnswer(row.get(1));
            }

            if (row.size() >= 3) {
                category.setName(row.get(2));
            }

            if (row.size() >= 4) {
                card.setNote(row.get(3));
            }

            // This can't be null because numberOfLearningDataRows is 0
            // if learningDataCells is 0.
            LearningData learningData;
            if (i < numberOfLearningDataRows) {
                learningData = getLearningDataFromRow(learningDataCells.getRow(i));

            } else {
                learningData = new LearningData();
            }


            card.setCategory(category);
            card.setLearningData(learningData);
            cardList.add(card);
        }

        if (cardList.size() == 0) {
            throw new IOException("Wrong spreadsheet format. The spreadsheet should contain at least 1 worksheet with at least 2 columns of questions and answers.");
        }

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, dbPath);
        try {
            CardDao cardDao = helper.getCardDao();
            cardDao.createCards(cardList);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

    }

    private LearningData getLearningDataFromRow(List<String> row) {
        LearningData learningData = new LearningData();
        // Make sure it is valid learning data
        if (row.size() == 9) {
            learningData.setAcqReps(Integer.parseInt(row.get(0)));
            learningData.setAcqRepsSinceLapse(Integer.parseInt(row.get(1)));
            learningData.setEasiness(Float.parseFloat(row.get(2)));
            learningData.setGrade(Integer.parseInt(row.get(3)));
            learningData.setLapses(Integer.parseInt(row.get(4)));
            try {
                learningData.setLastLearnDate(ISO8601_FORMATTER.parse(row.get(5)));
            } catch (ParseException e) {
                Log.w(TAG, "Parset date error", e);
                // 2010-01-01 00:00:00
                learningData.setLastLearnDate(new Date(1262304000000L));
            }
            try {
                learningData.setNextLearnDate(ISO8601_FORMATTER.parse(row.get(6)));
            } catch (ParseException e) {
                Log.w(TAG, "Parset date error", e);
                // 2010-01-01 00:00:00
                learningData.setNextLearnDate(new Date(1262304000000L));
            }
            learningData.setRetReps(Integer.parseInt(row.get(7)));
            learningData.setRetRepsSinceLapse(Integer.parseInt(row.get(8)));
        }
        return learningData;

    }
}
