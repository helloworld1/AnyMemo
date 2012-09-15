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
package org.liberty.android.fantastischmemo.ui;

import org.apache.mycommons.lang3.StringUtils;

import org.liberty.android.fantastischmemo.R;

import android.content.Context;

import android.text.Html;

import android.view.LayoutInflater;

import android.widget.Button;
import android.view.View;

import android.widget.LinearLayout;

public class GradeButtons {

    private LinearLayout buttonView;
    
    private Button[] gradeButtons = new Button[6];

    // The default button titles from the string
    private CharSequence[] defaultGradeButtonTitles = new CharSequence[6];

    private OnGradeButtonClickListener onGradeButtonClickListener = 
        new OnGradeButtonClickListener() {
            public void onGradeButtonClick(int grade) {
                // Dummy implementation
            }
        };

    private OnGradeButtonLongClickListener onGradeButtonLongClickListener = 
        new OnGradeButtonLongClickListener() {
            public void onGradeButtonLongClick(int grade) {
                // Dummy implementation
            }
        };


    public GradeButtons(Context context, int gradeButtonResource) {
        LayoutInflater factory = LayoutInflater.from(context);
        buttonView = (LinearLayout) factory.inflate(gradeButtonResource, null);

        gradeButtons[0] = (Button)buttonView.findViewById(R.id.grade_button_0);
        gradeButtons[1] = (Button)buttonView.findViewById(R.id.grade_button_1);
        gradeButtons[2] = (Button)buttonView.findViewById(R.id.grade_button_2);
        gradeButtons[3] = (Button)buttonView.findViewById(R.id.grade_button_3);
        gradeButtons[4] = (Button)buttonView.findViewById(R.id.grade_button_4);
        gradeButtons[5] = (Button)buttonView.findViewById(R.id.grade_button_5);
        for (int i = 0; i < 6; i++) {
            setButtonOnClickListener(gradeButtons[i], i);
            setButtonOnLongClickListener(gradeButtons[i], i);
            defaultGradeButtonTitles[i] = gradeButtons[i].getText();
            gradeButtons[i].setText(Html.fromHtml("<b>" + gradeButtons[i].getText() + "</b>"));
        }

    }

    public LinearLayout getView() {
        return buttonView;
    }

    public void setBackgroundColor(int color){
       buttonView.setBackgroundColor(color);
    }

    public void setOnGradeButtonClickListener(OnGradeButtonClickListener listener) {
        onGradeButtonClickListener = listener;
    }

    public void setOnGradeButtonLongClickListener(OnGradeButtonLongClickListener listener) {
        onGradeButtonLongClickListener = listener;
    }

    public void setGradeButtonBackground(int grade, int drawableResourceId) {
        gradeButtons[grade].setBackgroundResource(drawableResourceId);
    }

    public void setButtonText(int grade, CharSequence title, CharSequence description) {
        if (StringUtils.isNotEmpty(description)) {
            gradeButtons[grade].setText(Html.fromHtml("<b>" + title + "</b>" +  "<br />" + "<small>" + description + "</small>"));
        } else {
            gradeButtons[grade].setText(Html.fromHtml("<b>" + title + "</b>"));
        }
    }

    public void setButtonDescription(int grade, CharSequence description) {
        if (StringUtils.isNotEmpty(description)) {
            gradeButtons[grade].setText(Html.fromHtml("<b>" + defaultGradeButtonTitles[grade] + "</b>" +  "<br />" + "<small>" + description + "</small>"));
        }
    }

    public void hide() {
        buttonView.setVisibility(View.INVISIBLE);
    }

    public void show() {
        buttonView.setVisibility(View.VISIBLE);
    }

    public static interface OnGradeButtonClickListener {
        void onGradeButtonClick(int grade);
    }

    public static interface OnGradeButtonLongClickListener {
        void onGradeButtonLongClick(int grade);
    }

    private void setButtonOnClickListener(final Button button, final int grade) {
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onGradeButtonClickListener.onGradeButtonClick(grade);
            }
        });
    }

    private void setButtonOnLongClickListener(final Button button, final int grade) {
        button.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                onGradeButtonLongClickListener.onGradeButtonLongClick(grade);
                return true;
            }
        });
    }
}
