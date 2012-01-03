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
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.domain.Card;

import android.view.View;

public interface FlashcardDisplay{

    public View getView();

    public View getQuestionView();
    
    public View getAnswerView();

    public void updateView(Card card);

	public void updateView(Card card, boolean showAnswer);

    public void setQuestionLayoutClickListener(View.OnClickListener l);

    public void setAnswerLayoutClickListener(View.OnClickListener l);

    public void setQuestionTextClickListener(View.OnClickListener l);

    public void setAnswerTextClickListener(View.OnClickListener l);

    public void setQuestionLayoutLongClickListener(View.OnLongClickListener l);

    public void setAnswerLayoutLongClickListener(View.OnLongClickListener l);

    public void setScreenOnTouchListener(View.OnTouchListener l);

    public boolean isAnswerShown();

}
