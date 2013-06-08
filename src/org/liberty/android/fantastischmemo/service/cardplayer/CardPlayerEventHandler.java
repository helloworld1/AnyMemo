package org.liberty.android.fantastischmemo.service.cardplayer;

import org.liberty.android.fantastischmemo.domain.Card;

public interface CardPlayerEventHandler {
    void onPlayCard(Card card);
}