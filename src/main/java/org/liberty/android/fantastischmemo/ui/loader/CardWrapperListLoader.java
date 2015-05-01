/*
Copyright (C) 2013 Haowen Ning

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
package org.liberty.android.fantastischmemo.ui.loader;

import java.util.ArrayList;
import java.util.List;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.ui.CardListActivity.CardWrapper;

import android.content.Context;

public class CardWrapperListLoader extends DBLoader<List<CardWrapper>> {

    boolean initialAnswerVisible;

    public CardWrapperListLoader(Context context, String dbPath, boolean initialAnswerVisible) {
        super(context, dbPath);
        this.initialAnswerVisible = initialAnswerVisible;
    }

    @Override
    protected List<CardWrapper> dbLoadInBackground() {
        CardDao cardDao = dbOpenHelper.getCardDao();
        List<CardWrapper> cardWrappers = new ArrayList<CardWrapper>((int)cardDao.countOf());

        for (Card card : cardDao.getAllCards(null)) {
            cardWrappers.add(new CardWrapper(card, initialAnswerVisible));
        }

        return cardWrappers;
    }
}

