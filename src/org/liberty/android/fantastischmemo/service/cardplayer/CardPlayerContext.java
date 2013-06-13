package org.liberty.android.fantastischmemo.service.cardplayer;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.utils.CardTTSUtil;

import android.os.Handler;

public class CardPlayerContext {
    private volatile CardPlayerState state = CardPlayerState.STOPPED;
    private volatile Card currentCard;
    
    private final CardPlayerEventHandler eventHandler;
    private final CardTTSUtil cardTTSUtil;
    private final Handler amTTSServiceHandler;
    private final AnyMemoDBOpenHelper dbOpenHelper;

    private final int delayBeteenQAInSec;
    private final int delayBeteenCardsInSec;

    private final boolean shuffle;

    private final boolean repeat;

    public CardPlayerContext(CardPlayerEventHandler eventHandler,
            CardTTSUtil cardTTSUtil,
            Handler amTTSServiceHandler,
            AnyMemoDBOpenHelper dbOpenHelper,
            int delayBeteenQAInSec,
            int delayBeteenCardsInSec,
            boolean shuffle,
            boolean repeat) {
        this.eventHandler = eventHandler;
        this.cardTTSUtil = cardTTSUtil;
        this.amTTSServiceHandler = amTTSServiceHandler;
        this.dbOpenHelper = dbOpenHelper;
        this.delayBeteenQAInSec = delayBeteenQAInSec;
        this.delayBeteenCardsInSec = delayBeteenCardsInSec;
        this.shuffle = shuffle;
        this.repeat = repeat;
    }

    public CardPlayerState getState() {
        return state;
    }

    public void setState(CardPlayerState state) {
        this.state = state;
    }

    public Card getCurrentCard() {
        return currentCard;
    }

    public void setCurrentCard(Card currentCard) {
        this.currentCard = currentCard;
    }

    public CardPlayerEventHandler getEventHandler() {
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

    public boolean getShuffle() {
        return shuffle;
    }

    public boolean getRepeat() {
        return repeat;
    }
}
