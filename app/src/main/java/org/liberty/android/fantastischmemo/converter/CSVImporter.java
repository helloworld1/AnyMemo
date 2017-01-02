/*
Copyright (C) 2010 Haowen Ning

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
package org.liberty.android.fantastischmemo.converter;

import com.opencsv.CSVReader;

import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.entity.Category;
import org.liberty.android.fantastischmemo.entity.LearningData;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

public class CSVImporter implements Converter {

    private static final long serialVersionUID = 234745119864085982L;

    /* Null is for default separator "," */
    private Character separator = null;

    private AMFileUtil amFileUtil;

    @Inject
    public CSVImporter(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
        separator = ',';
    }

    public void setSeparator(Character separator) {
        this.separator = separator;
    }

    public void convert(String src, String dest) throws Exception {
        if (!new File(dest).exists()) {
            amFileUtil.createDbFileWithDefaultSettings(new File(dest));
        }

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(dest);

        CSVReader reader;
        if (separator == null) {
            reader = new CSVReader(new FileReader(src));
        } else {
            reader = new CSVReader(new FileReader(src), separator);
        }
        try {
            final CardDao cardDao = helper.getCardDao();


            String[] nextLine;
            final List<Card> cardList = new LinkedList<Card>();
            while((nextLine = reader.readNext()) != null) {
                if(nextLine.length < 2){
                    throw new Exception("Malformed CSV file. Please make sure the CSV's first column is question, second one is answer and the optinal third one is category");
                }
                String note = "";
                String category = "";
                if(nextLine.length >= 3){
                    category = nextLine[2];
                }
                if(nextLine.length >= 4){
                    note = nextLine[3];
                }
                Card card = new Card();
                Category cat = new Category();
                LearningData ld = new LearningData();
                cat.setName(category);

                card.setCategory(cat);
                card.setLearningData(ld);
                card.setQuestion(nextLine[0]);
                card.setAnswer(nextLine[1]);
                card.setNote(note);
                cardList.add(card);
            }

            cardDao.createCards(cardList);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
            reader.close();
        }
    }

    @Override
    public String getSrcExtension() {
        return "csv";
    }

    @Override
    public String getDestExtension() {
        return "db";
    }
}

