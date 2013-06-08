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
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.aspect.CheckNullArgs;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CardPlayerActivity extends QACardActivity {
    public static final String EXTRA_START_CARD_ID = "start_card_id";

    public static final String EXTRA_PLAYING_STATUS = "playing_status";

    private static final int MAGIC_FRAME_LAYOUT_ID = 338125929;

    private CardDao cardDao;

    private SettingDao settingDao;

    /* Settings */
    private Option option;
    private Setting setting;

    private int startCardId = 1;

    private long totalCardCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        startCardId = extras.getInt(EXTRA_START_CARD_ID, -1);

        showAutoSpeakFragment();

    }

    @Override
    public void onInit() throws Exception {
        cardDao = getDbOpenHelper().getCardDao();
        settingDao = getDbOpenHelper().getSettingDao();

        option = getOption();
        setting = settingDao.queryForId(1);

        /* Run the learnQueue init in a separate thread */
        if (startCardId != -1) {
            setCurrentCard(cardDao.queryForId(startCardId));
        } else {
            setCurrentCard(cardDao.queryFirstOrdinal());
        }

        totalCardCount = cardDao.countOf();
    }

    @Override
    public void onPostInit() {
        if (getCurrentCard() == null) {
            showNoItemDialog();
            return;
        }
        displayCard(true);
        setSmallTitle(getTitle());
        setTitle(getDbName());
    }

    @Override
    public void onPostDisplayCard() {
        getCardTTSUtil().stopSpeak();
    }

    private void updateTitle(){
        if (getCurrentCard() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.total_text) + ": " + totalCardCount + " ");
            sb.append(getString(R.string.id_text) + ": " + getCurrentCard().getId() + " ");
            sb.append(getString(R.string.ordinal_text_short) + ": " + getCurrentCard().getOrdinal() + " ");
            sb.append(getCurrentCard().getCategory().getName());
            setSmallTitle(sb.toString());
        }
    }

    @Override
    protected void onClickAnswerText() {
        onClickAnswerView();
    }

    @Override
    protected void onClickQuestionView() {
        if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED) {
            displayCard(true);
            // TODO: fragment color
            //buttonsLayout.setBackgroundColor(setting.getAnswerBackgroundColor());
        }
    }

    private void showNoItemDialog(){
        new AlertDialog.Builder(this)
            .setTitle(this.getString(R.string.memo_no_item_title))
            .setMessage(this.getString(R.string.memo_no_item_message))
            .setNeutralButton(getString(R.string.back_menu_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    /* Finish the current activity and go back to the last activity.
                     * It should be the open screen. */
                    finish();
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener(){
                public void onCancel(DialogInterface dialog){
                    finish();
                }
            })
            .create()
            .show();
    }

    @CheckNullArgs
    protected void gotoCard(Card card) {
        Card currentCard = getCurrentCard();
        if (currentCard.getOrdinal() > card.getOrdinal()) {
            // This is previoius card
            setAnimation(R.anim.slide_right_in, R.anim.slide_right_out);
        } else {
            setAnimation(R.anim.slide_left_in, R.anim.slide_left_out);
        }
        setCurrentCard(card);

        updateCardFrontSide();
        updateTitle();

        // Set animation back
        setAnimation(R.anim.slide_left_in, R.anim.slide_left_out);
    }

    /*
     * Show the front side of the current card
     * This method is called instead directly update the flashcard
     * so both single and double sided card will work.
     */
    private void updateCardFrontSide(){
        if(getCurrentCard() != null){
            if(setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED){
                /* Double sided card, show front */
                displayCard(false);
                // buttonsLayout.setBackgroundColor(setting.getQuestionBackgroundColor());
            } else {
                /* Single sided, show both answer and questjion. */
                displayCard(true);
                // buttonsLayout.setBackgroundColor(setting.getAnswerBackgroundColor());
            }
        }
    }

    /*
     * This method will add append the frame layout to the layout
     * and use it as a fragment.
     */
    private void showAutoSpeakFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        LinearLayout root = (LinearLayout)findViewById(R.id.root);
        FrameLayout cardPlayerView = new FrameLayout(this);

        CardPlayerFragment f = new CardPlayerFragment();

        cardPlayerView.setId(MAGIC_FRAME_LAYOUT_ID);
        root.addView(cardPlayerView);
        ft.replace(cardPlayerView.getId(), f);
        ft.commit();
    }
}

