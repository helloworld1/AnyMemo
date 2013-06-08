package org.liberty.android.fantastischmemo.service.cardplayer;


public interface CardPlayerStateTransition {
    void transition(CardPlayerContext context, CardPlayerMessage message);
}