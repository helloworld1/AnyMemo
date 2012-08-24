/*
Copyright (C) 2011 Haowen Ning

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

import java.io.File;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;

import android.app.Activity;

import android.graphics.Typeface;

import android.os.Bundle;

import android.support.v4.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * This class display a side of the card and export necessary callbacks.
 */
public class CardFragment extends Fragment {

    public static final String EXTRA_CARD_TEXT = "cardText";

    private static final String TAG = CardFragment.class.getSimpleName();

    private AMActivity mActivity;

    private CharSequence mCardText;

    private LinearLayout rootView;

    private TextView cardTextView;

    private String fontFile = null;

    private View.OnClickListener cardOnClickListener = null;

    private View.OnLongClickListener cardOnLongClickListener = null;
    
    private View.OnClickListener textOnClickListener = null;

    private View.OnLongClickListener textOnLongClickListener = null;

    private int fontSize = 24;

    private int textColor = 0xFFBEBEBE;

    private int backgroundColor = 0xFF000000;

    private int textAlignment = Gravity.LEFT;


    // The argumetn set in the factory will stored in the private variable here.
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mCardText = getArguments().getCharSequence(EXTRA_CARD_TEXT);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.card_layout, container, false);
        cardTextView = (TextView) v.findViewById(R.id.card_text_view);
        rootView = (LinearLayout) v.findViewById(R.id.root);

        cardTextView.setText(mCardText);

        if (cardOnClickListener != null) {
            rootView.setOnClickListener(cardOnClickListener);
        }

        if (cardOnLongClickListener != null) {
            rootView.setOnLongClickListener(cardOnLongClickListener);
        }

        if (textOnClickListener != null) {
            cardTextView.setOnClickListener(textOnClickListener);
        }

        if (textOnLongClickListener != null) {
            cardTextView.setOnLongClickListener(textOnLongClickListener);
        }

        cardTextView.setTextColor(textColor);

        rootView.setBackgroundColor(backgroundColor);

        if (fontFile != null && new File(fontFile).exists()) {
            Typeface qt = Typeface.createFromFile(fontFile);
            cardTextView.setTypeface(qt);
        }

        cardTextView.setTextSize(fontSize);

        // It is tricky to set up the alignment of the text.
        if (textAlignment == Gravity.CENTER) {
            cardTextView.setGravity(Gravity.CENTER);
            rootView.setGravity(Gravity.CENTER);
        } else if (textAlignment == Gravity.RIGHT) {
            cardTextView.setGravity(Gravity.RIGHT);
            rootView.setGravity(Gravity.NO_GRAVITY);
        } else {
            cardTextView.setGravity(Gravity.LEFT);
            rootView.setGravity(Gravity.NO_GRAVITY);
        }

        return v;
    }


    public static class Builder {
        CardFragment fragment;

        public Builder(CharSequence text) {
            fragment = new CardFragment();
            Bundle b = new Bundle(1);
            b.putCharSequence(EXTRA_CARD_TEXT, text);
            fragment.setArguments(b);
        }

        /* Set the click listener on the card */
        public Builder setCardOnClickListener(View.OnClickListener l) {
            fragment.cardOnClickListener = l;
            return this;
        }

        /* Set the click listener on the card text */
        public Builder setCardOnLongClickListener(View.OnLongClickListener l) {
            fragment.cardOnLongClickListener = l;
            return this;
        }

        /* Set the click listener on the card text */
        public Builder setTextOnClickListener(View.OnClickListener l) {
            fragment.textOnClickListener = l;
            return this;
        }

        /* Set the long click listener on the card text */
        public Builder setTextOnLongClickListener(View.OnLongClickListener l) {
            fragment.textOnLongClickListener = l;
            return this;
        }

        /* Set the card text's color */
        public Builder setTextColor(int color) {
            fragment.textColor = color;
            return this;
        }

        /* Set the font from a font file */
        public Builder setTypefaceFromFile(String fontFile) {
            fragment.fontFile = fontFile;
            return this;
        }
        /* Set the card background color */
        public Builder setBackgroundColor(int color){
            fragment.backgroundColor = color;
            return this;
        }

        public Builder setTextFontSize(int size) {
            fragment.fontSize = size;
            return this;
        }

        /*
         * Set up the alignment of the text in the card.
         * The parameter gravity is from Gravity.*
         */
        public Builder setTextAlignment(int gravity) {
            fragment.textAlignment = gravity;
            return this;
        }

        public CardFragment build() {
            return fragment;
        }
    }

}
