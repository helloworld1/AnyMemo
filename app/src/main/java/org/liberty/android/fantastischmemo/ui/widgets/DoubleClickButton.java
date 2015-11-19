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

package org.liberty.android.fantastischmemo.ui.widgets;

import java.util.Date;

import org.liberty.android.fantastischmemo.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * A button that is only effective after double click,
 * and the first click will display a toast text.
 */
public class DoubleClickButton extends Button {

    /**
     * The onClickListener set by the "setOnClickListener" method.
     */
    private View.OnClickListener onClickListener = null;

    private static final int DEFAULT_EFFECTIVE_DURATION_MILLIS = 5000;

    private Context context;

    private String textOnFirstClick = "";

    private int effectiveDurationMillis = DEFAULT_EFFECTIVE_DURATION_MILLIS;

    private long lastClickedTime = -1;

    public DoubleClickButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initAttributes(attrs);
    }

    public DoubleClickButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initAttributes(attrs);
    }

    public DoubleClickButton(Context context) {
        super(context);
        this.context = context;
    }

    private void initAttributes(AttributeSet attrs) {
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.DoubleClickButton);
        textOnFirstClick = arr.getText(R.styleable.DoubleClickButton_text_on_first_click).toString();
        effectiveDurationMillis = arr.getInt(R.styleable.DoubleClickButton_effective_duration_millis, DEFAULT_EFFECTIVE_DURATION_MILLIS);
        arr.recycle();
    }

    @Override
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;

        // Always use the customOnClickListener to execute onClick.
        super.setOnClickListener(customOnClickListener);
    }

    private View.OnClickListener customOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (onClickListener != null) {
                long currentTime = new Date().getTime();

                if (currentTime - lastClickedTime > effectiveDurationMillis) {
                    lastClickedTime = currentTime;
                    Toast.makeText(context, textOnFirstClick, Toast.LENGTH_SHORT).show();
                } else {
                    onClickListener.onClick(v);

                    // We need to rest the lastClickTime so the third click will be
                    // treated as the first click
                    lastClickedTime = 0;
                }
            }
        }
    };
}
