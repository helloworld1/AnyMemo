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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;

import com.google.inject.BindingAnnotation;

public class QATxtExporter implements Converter {

    private static final long serialVersionUID = -3390568891037351618L;

    public void convert(String src, String dest) throws Exception{
        new File(dest).delete();
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(src);
        try {
            final CardDao cardDao = helper.getCardDao();
            PrintWriter outtxt = new PrintWriter(new BufferedWriter(new FileWriter(dest)));
            if(outtxt.checkError()){
                throw new IOException("Can't open: " + dest + " for writting");
            }
            List<Card> cardList = cardDao.queryForAll();
            if(cardList == null || cardList.size() == 0){
                throw new IOException("Can't retrieve items for database: " + src);
            }
            for(Card card: cardList){
                outtxt.print("Q: " + card.getQuestion() + "\n");
                outtxt.print("A: " + card.getAnswer() + "\n\n");
            }
            outtxt.close();
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    @Override
    public String getSrcExtension() {
        return "db";
    }

    @Override
    public String getDestExtension() {
        return "txt";
    }

    @BindingAnnotation
    @Target({ ElementType. FIELD, ElementType.PARAMETER, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Type {};
}




