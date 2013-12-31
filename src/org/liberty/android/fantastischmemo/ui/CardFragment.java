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
import java.io.Serializable;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.utils.CardTextUtil;
import org.liberty.android.fantastischmemo.utils.CardTextUtilFactory;

import roboguice.fragment.RoboFragment;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * This class display a side of the card and export necessary callbacks.
 */
public class CardFragment extends RoboFragment {

    public static final String EXTRA_CARD_TEXT = "cardText";

    private String mCardText;

    private LinearLayout rootView;

    private TextView cardTextView;

    private String fontFile = null;

    private OnClickListener cardOnClickListener = null;

    private OnLongClickListener cardOnLongClickListener = null;

    private OnClickListener textOnClickListener = null;

    private OnLongClickListener textOnLongClickListener = null;

    private int fontSize = 24;

    private Integer textColor = null;

    private Integer backgroundColor = null;

    private Setting.Align textAlignment = Setting.Align.CENTER;

    private boolean displayInHtml = true;

    private boolean htmlLinebreakConversion = false;

    private CardTextUtilFactory cardTextUtilFactory;

    private CardTextUtil cardTextUtil;

    private String[] imageSearchPaths = {AMEnv.DEFAULT_IMAGE_PATH};

    @Inject
    public void setCardTextUtilFactory(CardTextUtilFactory cardTextUtilFactory) {
        this.cardTextUtilFactory = cardTextUtilFactory;
    }

    // The argument set in the factory will stored in the private variable here.
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mCardText = getArguments().getString(EXTRA_CARD_TEXT);
        cardTextUtil = cardTextUtilFactory.create(imageSearchPaths);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.card_layout, container, false);
        cardTextView = (TextView) v.findViewById(R.id.card_text_view);
        rootView = (LinearLayout) v.findViewById(R.id.root);

        cardTextView.setText(cardTextUtil.getSpannableText(mCardText, displayInHtml, htmlLinebreakConversion));

        // Uncomment the line below for the text field to handle links.
        // The line is commented out because it is not well tested.
        // cardTextView.setMovementMethod(LinkMovementMethod.getInstance());

        if (cardOnClickListener != null) {
            rootView.setOnClickListener(cardOnClickListener);
        }

        if (cardOnLongClickListener != null) {
            rootView.setOnLongClickListener(cardOnLongClickListener);
        }

        if (textOnClickListener != null) {
            cardTextView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Make sure the link (set for text view autoLink="web") is not recognized as a click
                    if (((TextView) v).getSelectionStart() == -1 && ((TextView) v).getSelectionEnd() == -1) {
                        textOnClickListener.onClick(v);
                    }
                }
            });
        }

        if (textOnLongClickListener != null) {
            cardTextView.setOnLongClickListener(textOnLongClickListener);
        }

        if (textColor != null) {
            cardTextView.setTextColor(textColor);
        }

        if (backgroundColor != null) {
            rootView.setBackgroundColor(backgroundColor);
        }

        if (fontFile != null && new File(fontFile).exists()) {
            Typeface qt = Typeface.createFromFile(fontFile);
            cardTextView.setTypeface(qt);
        }

        cardTextView.setTextSize(fontSize);

        // It is tricky to set up the alignment of the text.

        switch (textAlignment) {
            case CENTER:
                cardTextView.setGravity(Gravity.CENTER);
                rootView.setGravity(Gravity.CENTER);
                break;

            case RIGHT:
                cardTextView.setGravity(Gravity.RIGHT);
                rootView.setGravity(Gravity.NO_GRAVITY);
                break;

            case LEFT:
                cardTextView.setGravity(Gravity.LEFT);
                rootView.setGravity(Gravity.NO_GRAVITY);
                break;

            case CENTER_LEFT:
                cardTextView.setGravity(Gravity.LEFT);
                rootView.setGravity(Gravity.CENTER);
                break;

            case CENTER_RIGHT:
                cardTextView.setGravity(Gravity.RIGHT);
                rootView.setGravity(Gravity.CENTER);
                break;

            default:
                cardTextView.setGravity(Gravity.CENTER);
                rootView.setGravity(Gravity.CENTER);
        }

        return v;
    }

    public static interface OnClickListener extends View.OnClickListener {
        // No definitions, inherrited void onClick(View v)
    }

    public static interface OnLongClickListener extends View.OnLongClickListener{
        // No definitions, inherrited void onClick(View v)
    }


    public static class Builder implements Serializable {

        private static final long serialVersionUID = -3698059438530591747L;

        private String text = null;

        private transient OnClickListener cardOnClickListener = null;

        private transient OnLongClickListener cardOnLongClickListener = null;

        private transient OnClickListener textOnClickListener = null;

        private transient OnLongClickListener textOnLongClickListener = null;

        private Integer textColor = null;

        private Integer backgroundColor = null;

        private Integer fontSize = null;

        private String fontFile = null;

        private Setting.Align textAlignment = Setting.Align.CENTER;

        private boolean displayInHtml = true;

        private boolean htmlLinebreakConversion = false;

        private String[] imageSearchPaths = {AMEnv.DEFAULT_IMAGE_PATH};

        public Builder(String text) {
            this.text = text;
        }

        /* Set the click listener on the card */
        public Builder setCardOnClickListener(OnClickListener l) {
            this.cardOnClickListener = l;
            return this;
        }

        /* Set the click listener on the card text */
        public Builder setCardOnLongClickListener(OnLongClickListener l) {
            this.cardOnLongClickListener = l;
            return this;
        }

        /* Set the click listener on the card text */
        public Builder setTextOnClickListener(OnClickListener l) {
            this.textOnClickListener = l;
            return this;
        }

        /* Set the long click listener on the card text */
        public Builder setTextOnLongClickListener(OnLongClickListener l) {
            this.textOnLongClickListener = l;
            return this;
        }

        /* Set the card text's color */
        public Builder setTextColor(int color) {
            this.textColor = color;
            return this;
        }

        /* Set the font from a font file */
        public Builder setTypefaceFromFile(String fontFile) {
            this.fontFile = fontFile;
            return this;
        }
        /* Set the card background color */
        public Builder setBackgroundColor(int color){
            this.backgroundColor = color;
            return this;
        }

        public Builder setTextFontSize(int size) {
            this.fontSize = size;
            return this;
        }

        public Builder setDisplayInHtml(boolean displayInHtml) {
            this.displayInHtml = displayInHtml;
            return this;
        }

        public Builder setHtmlLinebreakConversion(boolean htmlLinebreakConversion) {
            this.htmlLinebreakConversion = htmlLinebreakConversion;
            return this;
        }

        public Builder setImageSearchPaths(String[] imageSearchPaths) {
            this.imageSearchPaths = imageSearchPaths;
            return this;
        }

        /*
         * Set up the alignment of the text in the card.
         * The parameter gravity is from Gravity.*
         */
        public Builder setTextAlignment(Setting.Align align) {
            this.textAlignment = align;
            return this;
        }

        public CardFragment build() {
            CardFragment fragment = new CardFragment();
            Bundle b = new Bundle(1);
            b.putString(EXTRA_CARD_TEXT, text);

            fragment.setArguments(b);

            if (fontFile != null) {
                fragment.fontFile = fontFile;
            }

            if (cardOnClickListener != null) {
                fragment.cardOnClickListener = cardOnClickListener;
            }

            if (cardOnLongClickListener != null) {
                fragment.cardOnLongClickListener = cardOnLongClickListener;
            }

            if (textOnClickListener != null) {
                fragment.textOnClickListener = textOnClickListener;
            }

            if (textOnLongClickListener != null) {
                fragment.textOnLongClickListener = textOnLongClickListener;
            }

            if (fontSize != null) {
                fragment.fontSize = fontSize;
            }

            if (textColor != null) {
                fragment.textColor = textColor;
            }

            if (backgroundColor != null) {
                fragment.backgroundColor = backgroundColor;
            }

            if (textAlignment != null) {
                fragment.textAlignment = textAlignment;
            }

            fragment.htmlLinebreakConversion = htmlLinebreakConversion;

            fragment.displayInHtml = displayInHtml;

            fragment.imageSearchPaths = imageSearchPaths;

            return fragment;
        }
    }
}
