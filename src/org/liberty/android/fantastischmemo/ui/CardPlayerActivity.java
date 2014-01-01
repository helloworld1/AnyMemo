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
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.service.CardPlayerService;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;

public class CardPlayerActivity extends QACardActivity {
    public static final String EXTRA_START_CARD_ID = "start_card_id";

    public static final String EXTRA_PLAYING_STATUS = "playing_status";

    public static final String EXTRA_RESULT_CARD_ID = "resultCardId";

    private CardDao cardDao;

    private SettingDao settingDao;

    private CardPlayerService cardPlayerService;

    /* Settings */
    private Setting setting;

    private int startCardId = 1;

    private long totalCardCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        startCardId = extras.getInt(EXTRA_START_CARD_ID, -1);

        if (savedInstanceState != null) {
            startCardId = savedInstanceState.getInt(EXTRA_START_CARD_ID, -1);
        }
        startInit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Card currentCard = getCurrentCard();
        if (currentCard != null) {
            outState.putInt(EXTRA_START_CARD_ID, currentCard.getId());
        }
    }

    @Override
    public int getContentView() {
        return R.layout.qa_card_layout_card_player;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindCardPlayerService();
    }

    @Override
    public void onPostInit() {
        super.onPostInit();
        cardDao = getDbOpenHelper().getCardDao();
        settingDao = getDbOpenHelper().getSettingDao();

        setting = settingDao.queryForId(1);

        /* Run the learnQueue init in a separate thread */
        if (startCardId != -1) {
            setCurrentCard(cardDao.queryForId(startCardId));
        } else {
            setCurrentCard(cardDao.queryFirstOrdinal());
        }

        totalCardCount = cardDao.countOf();

        bindCardPlayerService();
        if (getCurrentCard() == null) {
            showNoItemDialog();
            return;
        }
        setupControlButtons();
        updateCardFrontSide();
        setSmallTitle(getTitle());
        updateTitle();
    }

    private void setupControlButtons() {
        CardPlayerFragment cardPlayerFragment = new CardPlayerFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.buttons_root, cardPlayerFragment);
        ft.commit();
    }

    @Override
    public void onPostDisplayCard() {
        getCardTTSUtil().stopSpeak();
    }

    public CardPlayerService getCardPlayerService() {
        return cardPlayerService;
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
        setCurrentCard(card);

        updateCardFrontSide();
        updateTitle();
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_RESULT_CARD_ID, getCurrentCard().getId());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // Query out the card id and display that card
    protected void gotoCardId(final int cardId) {
        Card card = cardDao.queryForId(cardId);
        gotoCard(card);
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
            } else {
                /* Single sided, show both answer and questjion. */
                displayCard(true);
            }
        }
    }

    private void bindCardPlayerService() {
        Intent intent = new Intent(this, CardPlayerService.class);
        intent.putExtra(CardPlayerService.EXTRA_DBPATH, getDbPath());
        intent.putExtra(CardPlayerService.EXTRA_CURRENT_CARD_ID, getCurrentCard().getId());
        bindService(intent, cardPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindCardPlayerService() {
        unbindService(cardPlayerServiceConnection);
    }

    private ServiceConnection cardPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            CardPlayerService.LocalBinder localBinder = (CardPlayerService.LocalBinder) binder;

            cardPlayerService = localBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            cardPlayerService = null;
        }
    };

}

