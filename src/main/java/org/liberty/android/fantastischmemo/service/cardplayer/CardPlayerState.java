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
package org.liberty.android.fantastischmemo.service.cardplayer;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;

import roboguice.util.Ln;

import android.text.format.DateUtils;

import com.google.common.base.Objects;

/*
 * State object representing the state machine of card player 
 * The normal flow is STOPPED -> PLAYING_QUESTION -> PLAYING_ANSWER -> PLAYING_QUESTION ...
 * If START_PLAYING is received, the context will transit to PLAYING_QUESTION
 * If STOP_PLAYING is received, any state of context will transit to STOPPED
 */
public enum CardPlayerState implements CardPlayerStateTransition {
    STOPPED {
        public void transition(CardPlayerContext context, CardPlayerMessage message) {
            switch(message) {
            case START_PLAYING:
                context.setState(PLAYING_QUESTION);
                playQuestion(context);
                break;
            case GO_TO_NEXT:
                // In STOPPED state, GO_TO_NEXT / GO_TO_PREV message is still handled
                // because it need to change the current card in the context and callback
                // the handler so the UI will change card without actually speaking.
                Card nextCard = findNextCard(context);
                if (nextCard == null) {
                    break;
                }
                context.setCurrentCard(nextCard);
                context.getEventHandler().onPlayCard(context.getCurrentCard());
                break;
            case GO_TO_PREV:
                Card prevCard = findPrevCard(context);
                if (prevCard == null) {
                    break;
                }
                context.setCurrentCard(prevCard);
                context.getEventHandler().onPlayCard(context.getCurrentCard());
                break;
            default:
                // Once it is in STOPPED state, no call other than START_PLAYING can go through.
                break;

            }
        }
    },
    PLAYING_QUESTION {
        public void transition(CardPlayerContext context, CardPlayerMessage message) {
            switch(message) {
            case GO_TO_NEXT:
                context.getCardTTSUtil().stopSpeak();
                Card nextCard = findNextCard(context);

                // Always check null card, if it happens it means no card can be played so
                // it will send STOP_PLAYING message.
                if (nextCard == null) {
                    context.getState().transition(context, CardPlayerMessage.STOP_PLAYING);
                    break;
                }
                context.setCurrentCard(nextCard);

                context.setState(PLAYING_QUESTION);
                playQuestion(context);

                break;
            case GO_TO_PREV:
                context.getCardTTSUtil().stopSpeak();
                Card prevCard = findPrevCard(context);
                if (prevCard == null) {
                    context.getState().transition(context, CardPlayerMessage.STOP_PLAYING);
                    break;
                }
                context.setCurrentCard(prevCard);
                context.setState(PLAYING_QUESTION);
                playQuestion(context);
                break;
            case PLAYING_ANSWER_COMPLETED:
                Ln.w("Wrong state, the question is playing but receive message that answer completed!");
                assert false : "Wrong state";
                break;
            case PLAYING_QUESTION_COMPLETED:
                context.setState(PLAYING_ANSWER);
                playAnswer(context);
                break;
            case STOP_PLAYING:
                stopPlaying(context);
                break;
            default:
                break;

            }
        }
    },
    PLAYING_ANSWER {
        public void transition(CardPlayerContext context, CardPlayerMessage message) {
            switch(message) {
            case GO_TO_NEXT:
                context.getCardTTSUtil().stopSpeak();
                Card nextCard = findNextCard(context);
                if (nextCard == null) {
                    context.getState().transition(context, CardPlayerMessage.STOP_PLAYING);
                    break;
                }
                context.setCurrentCard(nextCard);
                context.setState(PLAYING_QUESTION);
                playQuestion(context);
                break;
            case GO_TO_PREV:
                context.getCardTTSUtil().stopSpeak();
                Card prevCard = findPrevCard(context);
                if (prevCard == null) {
                    context.getState().transition(context, CardPlayerMessage.STOP_PLAYING);
                    break;
                }
                context.setCurrentCard(prevCard);
                context.setState(PLAYING_QUESTION);
                playQuestion(context);
                break;
            case PLAYING_ANSWER_COMPLETED:
                nextCard = findNextCard(context);
                if (nextCard == null) { 
                    stopPlaying(context);
                    break;
                }
                context.setCurrentCard(nextCard);
                context.setState(PLAYING_QUESTION);
                playQuestion(context);
                break;
            case PLAYING_QUESTION_COMPLETED:
                Ln.w("Wrong state, the answer is playing but receive message that question completed!");
                assert false : "Wrong state";
                break;
            case STOP_PLAYING:
                stopPlaying(context);
                break;
            default:
                break;

            }
        }
    };

    private static void playQuestion(final CardPlayerContext context) {
        Ln.v("Playing Question: " + context.getCurrentCard().getId());

        // Callback to the handler first since the actual TTS call would take some time.
        // We usually need UI to update first.
        context.getEventHandler().onPlayCard(context.getCurrentCard());

        context.getCardTTSUtil().speakCardQuestion(context.getCurrentCard(),
            new AnyMemoTTS.OnTextToSpeechCompletedListener() {
                public void onTextToSpeechCompleted(final String text) {
                    // Use UI thread's handler to post call instead of sleeping.
                    // Note the TTS is running on its own thread.
                    context.getAmTTSServiceHandler().postDelayed(new Runnable() {
                        public void run() {
                            // Make sure the card is still current.
                            // If the card changed, it is most likely the fast foward / backward
                            // function is needed.
                            if (Objects.equal(context.getCurrentCard().getQuestion(), text)) {
                                Ln.v("Playing question completed for id " + context.getCurrentCard().getId());
                                context.getState().transition(context, CardPlayerMessage.PLAYING_QUESTION_COMPLETED);
                            }
                        }
                    }, context.getDelayBeteenQAInSec() * 1000);

                }
        });
    }

    private static void playAnswer(final CardPlayerContext context) {
        Ln.v("Playing Answer: " + context.getCurrentCard().getId());
        context.getEventHandler().onPlayCard(context.getCurrentCard());
        context.getCardTTSUtil().speakCardAnswer(context.getCurrentCard(),
            new AnyMemoTTS.OnTextToSpeechCompletedListener() {
                public void onTextToSpeechCompleted(final String text) {
                    context.getAmTTSServiceHandler().postDelayed(new Runnable() {
                        public void run() {
                            if (Objects.equal(context.getCurrentCard().getAnswer(), text)) {
                                Ln.v("Playing answer completed for id " + context.getCurrentCard().getId());
                                context.getState().transition(context, CardPlayerMessage.PLAYING_ANSWER_COMPLETED);
                            }
                        }
                    }, context.getDelayBeteenCardsInSec() * 1000);

                }
        });
    }

    /* This method will return null if there is no card to play */
    private static Card findNextCard(final CardPlayerContext context) {
        Card card;
        if (context.getShuffle()) {
            card = context.getDbOpenHelper().getCardDao().getRandomCards(null, 1).get(0);
        } else {
            // Get the next ordinal card
            card = context.getDbOpenHelper().getCardDao().queryNextCard(context.getCurrentCard());

            // Do not repeat from the beginning if the repeat is set
            if (!context.getRepeat() && card.getOrdinal() <= context.getCurrentCard().getOrdinal()) {
                return null;
            }
        }

        assert card != null : "Next card should not be null";

        context.getDbOpenHelper().getLearningDataDao().refresh(card.getLearningData());
        context.getDbOpenHelper().getCategoryDao().refresh(card.getCategory());
        return card;
    }

    /* This method will return null if there is no card to play */
    private static Card findPrevCard(final CardPlayerContext context) {
        Card card = null;
        if (context.getShuffle()) {
            card = context.getDbOpenHelper().getCardDao().getRandomCards(null, 1).get(0);
        } else {
            // Get the next ordinal card
            card = context.getDbOpenHelper().getCardDao().queryPrevCard(context.getCurrentCard());

            // Do not repeat from the beginning if the repeat is set
            if (!context.getRepeat() && card.getOrdinal() >= context.getCurrentCard().getOrdinal()) {
                return null;
            }
        }

        assert card != null : "Prev card should not be null";

        context.getDbOpenHelper().getLearningDataDao().refresh(card.getLearningData());
        context.getDbOpenHelper().getCategoryDao().refresh(card.getCategory());
        return card;
    }

    private static void stopPlaying(final CardPlayerContext context) {
        context.setState(STOPPED);
        context.getEventHandler().onStopPlaying();
    }
}
