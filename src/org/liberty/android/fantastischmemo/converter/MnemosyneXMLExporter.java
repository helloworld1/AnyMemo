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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.LearningData;

import com.google.inject.BindingAnnotation;

public class MnemosyneXMLExporter implements Converter {

    private static final long serialVersionUID = -7419489770698078017L;

    public void convert(String src, String dest) throws Exception {
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(src);
        String dbName = FilenameUtils.getName(dest);
        PrintWriter outxml = null;
        try {
            final CardDao cardDao = helper.getCardDao();
            final LearningDataDao learningDataDao = helper.getLearningDataDao();
            final CategoryDao categoryDao = helper.getCategoryDao();

            // Populate all category field in a transaction.
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
            if (cardList == null || cardList.size() == 0) {
                throw new IOException("Read empty or corrupted file");
            }



            outxml = new PrintWriter(new BufferedWriter(new FileWriter(dest)));
            if(outxml.checkError()){
                throw new IOException("Can't open: " + dest);
            }
            int count = 0;
            long timeOfStart = 0L;
            String id, u, gr, e, ac_rp, rt_rp, lps, ac_rp_l, rt_rp_l, l_rp, n_rp, question, answer, category;
            // Now write the xml to the file
            for(Card card: cardList){
                // At the first item, we write all metadata
                if(count == 0){
                    //timeOfStart = item.getDatelearnUnix();
                    // 2000-01-01 12:00
                    timeOfStart = 946728000L;
                    outxml.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    outxml.print("<mnemosyne core_version=\"1\" time_of_start=\"" + timeOfStart + "\" >\n");
                    outxml.print("<category active=\"1\">\n");
                    outxml.print("<name>" + dbName + "</name>\n");
                    outxml.print("</category>\n");
                }
                // Convert the learning data
                LearningData ld = card.getLearningData();
                id = "" + card.getOrdinal();
                gr = "" + ld.getGrade();
                e = "" + ld.getEasiness();
                ac_rp = "" + ld.getAcqReps();
                rt_rp = "" + ld.getRetReps();
                lps = "" + ld.getLapses();
                ac_rp_l = "" + ld.getAcqRepsSinceLapse();
                rt_rp_l = "" + ld.getRetRepsSinceLapse();;
                if(ac_rp.equals("0")){
                    u = "1";
                }
                else {
                    u = "0";
                }
                // Add 1 here to avoid rounding problem

                l_rp = Long.toString((ld.getLastLearnDate().getTime() / 1000 - timeOfStart) / 86400);
                n_rp = Long.toString((ld.getNextLearnDate().getTime() / 1000 - timeOfStart) / 86400 + 1);

                // Replace the illegal symbols from the question and answer
                question = card.getQuestion();
                answer = card.getAnswer();
                category = card.getCategory().getName();
                if(question == null){
                    question = "";
                }
                if(answer == null){
                    answer = "";
                }
                if(category == null){
                    category = "";
                }
                question = question.replaceAll("&", "&amp;");
                question = question.replaceAll("<", "&lt;");
                question = question.replaceAll(">", "&gt;");
                question = question.replaceAll("'", "&apos;");
                question = question.replaceAll("\"", "&quot;");
                answer = answer.replaceAll("&", "&amp;");
                answer = answer.replaceAll("<", "&lt;");
                answer = answer.replaceAll(">", "&gt;");
                answer = answer.replaceAll("'", "&apos;");
                answer = answer.replaceAll("\"", "&quot;");
                category = category.replaceAll("&", "&amp;");
                category = category.replaceAll("<", "&lt;");
                category = category.replaceAll(">", "&gt;");
                category = category.replaceAll("'", "&apos;");
                category = category.replaceAll("\"", "&quot;");


                outxml.print("<item id=\"" + id + "\" u=\"" + u +"\" gr=\"" + gr +"\" e=\"" + e + "\" ac_rp=\"" + ac_rp + "\" rt_rp=\"" + rt_rp + "\" lps=\"" + lps + "\" ac_rp_l=\"" + ac_rp_l + "\" rt_rp_l=\"" + rt_rp_l + "\" l_rp=\"" + l_rp + "\" n_rp=\"" + n_rp + "\">\n");

                if (category.equals("")) {
                    outxml.print("<cat>" + dbName + "</cat>\n");
                } else {
                    outxml.print("<cat>" + category + "</cat>\n");
                }

                outxml.print("<Q>" + question + "</Q>\n");
                outxml.print("<A>" + answer + "</A>\n");
                outxml.print("</item>\n");
                if(outxml.checkError()){
                    throw new IOException("Error writing xml on id: " + id);
                }
                count += 1;
            }
            outxml.print("</mnemosyne>");
            outxml.close();
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
            if (outxml != null) {
                outxml.close();
            }
        }
    }

    @Override
    public String getSrcExtension() {
        return "db";
    }

    @Override
    public String getDestExtension() {
        return "xml";
    }

    @BindingAnnotation
    @Target({ ElementType. FIELD, ElementType.PARAMETER, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Type {};
}
