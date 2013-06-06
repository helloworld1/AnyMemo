package org.liberty.android.fantastischmemo.service.autospeak;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.utils.CardTTSUtil;

import android.os.Handler;

public class AutoSpeakContext {
    private volatile AutoSpeakState state = AutoSpeakState.STOPPED;
    private volatile Card currentCard;
    
    private final AutoSpeakEventHandler eventHandler;
    private final CardTTSUtil cardTTSUtil;
    private final Handler amTTSServiceHandler;
    private final AnyMemoDBOpenHelper dbOpenHelper;

    private final int delayBeteenQAInSec;
    private final int delayBeteenCardsInSec;

    public AutoSpeakContext(
            AutoSpeakEventHandler eventHandler,
            CardTTSUtil cardTTSUtil,
            Handler amTTSServiceHandler,
            AnyMemoDBOpenHelper dbOpenHelper,
            int delayBeteenQAInSec,
            int delayBeteenCardsInSec) {
        this.eventHandler = eventHandler;
        this.cardTTSUtil = cardTTSUtil;
        this.amTTSServiceHandler = amTTSServiceHandler;
        this.dbOpenHelper = dbOpenHelper;
        this.delayBeteenQAInSec = delayBeteenQAInSec;
        this.delayBeteenCardsInSec = delayBeteenCardsInSec;
    }

    public AutoSpeakState getState() {
        return state;
    }

    public void setState(AutoSpeakState state) {
        this.state = state;
    }

    public Card getCurrentCard() {
        return currentCard;
    }

    public void setCurrentCard(Card currentCard) {
        this.currentCard = currentCard;
    }

    public AutoSpeakEventHandler getEventHandler() {
        return eventHandler;
    }

    public CardTTSUtil getCardTTSUtil() {
        return cardTTSUtil;
    }

    public Handler getAmTTSServiceHandler() {
        return amTTSServiceHandler;
    }

    public AnyMemoDBOpenHelper getDbOpenHelper() {
        return dbOpenHelper;
    }

    public int getDelayBeteenQAInSec() {
        return delayBeteenQAInSec;
    }

    public int getDelayBeteenCardsInSec() {
        return delayBeteenCardsInSec;
    }
}
