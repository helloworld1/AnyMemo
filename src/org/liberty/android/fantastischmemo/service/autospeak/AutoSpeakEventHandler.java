package org.liberty.android.fantastischmemo.service.autospeak;

import org.liberty.android.fantastischmemo.domain.Card;

public interface AutoSpeakEventHandler {
    void onPlayCard(Card card);
}