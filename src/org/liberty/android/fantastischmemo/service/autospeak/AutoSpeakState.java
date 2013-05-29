package org.liberty.android.fantastischmemo.service.autospeak;

import org.apache.mycommons.lang3.time.DateUtils;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;

import roboguice.util.Ln;

// State object representing the state of auto speak
public enum AutoSpeakState implements AutoSpeakStateTransition {
    STOPPED {
        public void transition(AutoSpeakContext context, AutoSpeakMessage message) {
            switch(message) {
            case START_PLAYING:
                context.setState(PLAYING_QUESTION);
                playQuestion(context);
                break;
            default:
                break;

            }
        }
    },
    PLAYING_QUESTION {
        public void transition(AutoSpeakContext context, AutoSpeakMessage message) {
            switch(message) {
            case GO_TO_NEXT:
                break;
            case GO_TO_PREV:
                break;
            case PLAYING_ANSWER_COMPLETED:
                Ln.w("Wrong state, the question is playing but receive message that answer completed!");
                break;
            case PLAYING_QUESTION_COMPLETED:
                context.setState(PLAYING_ANSWER);
                playAnswer(context);
                break;
            case STOP_PLAYING:
                context.setState(STOPPED);
                break;
            default:
                break;

            }
        }
    },
    PLAYING_ANSWER {
        public void transition(AutoSpeakContext context, AutoSpeakMessage message) {
            switch(message) {
            case GO_TO_NEXT:
                break;
            case GO_TO_PREV:
                break;
            case PLAYING_ANSWER_COMPLETED:
                context.setCurrentCard(findNextCard(context));
                context.setState(PLAYING_QUESTION);
                playQuestion(context);
                break;
            case PLAYING_QUESTION_COMPLETED:
                Ln.w("Wrong state, the answer is playing but receive message that question completed!");
                break;
            case STOP_PLAYING:
                context.setState(STOPPED);
                break;
            default:
                break;

            }
        }
    };

    private static void playQuestion(final AutoSpeakContext context) {
        Ln.v("Playing Question: " + context.getCurrentCard().getId());
        context.getEventHandler().onPlayCard(context.getCurrentCard());
        context.getAmTTSService().speakCardQuestion(context.getCurrentCard(),
            new AnyMemoTTS.OnTextToSpeechCompletedListener() {
                public void onTextToSpeechCompleted(String text) {
                    context.getAmTTSServiceHandler().postDelayed(new Runnable() {
                        public void run() {
                            Ln.v("Playing question completed for id " + context.getCurrentCard().getId());
                            context.getState().transition(context, AutoSpeakMessage.PLAYING_QUESTION_COMPLETED);
                        }
                    }, context.getDelayBeteenQAInSec() * DateUtils.MILLIS_PER_SECOND);

                }
        });
    }

    private static void playAnswer(final AutoSpeakContext context) {
        Ln.v("Playing Answer: " + context.getCurrentCard().getId());
        context.getEventHandler().onPlayCard(context.getCurrentCard());
        context.getAmTTSService().speakCardAnswer(context.getCurrentCard(),
            new AnyMemoTTS.OnTextToSpeechCompletedListener() {
                public void onTextToSpeechCompleted(String text) {
                    context.getAmTTSServiceHandler().postDelayed(new Runnable() {
                        public void run() {
                            Ln.v("Playing answer completed for id " + context.getCurrentCard().getId());
                            context.getState().transition(context, AutoSpeakMessage.PLAYING_ANSWER_COMPLETED);
                        }
                    }, context.getDelayBeteenQAInSec() * DateUtils.MILLIS_PER_SECOND);

                }
        });
    }

    private static Card findNextCard(final AutoSpeakContext context) {
        return context.getDbOpenHelper().getCardDao().queryNextCard(context.getCurrentCard());

    }

    private static Card findPrevCard(final AutoSpeakContext context) {
        return context.getDbOpenHelper().getCardDao().queryPrevCard(context.getCurrentCard());
    }
}
