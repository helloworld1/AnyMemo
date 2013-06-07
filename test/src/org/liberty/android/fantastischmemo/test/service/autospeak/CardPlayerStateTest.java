package org.liberty.android.fantastischmemo.test.service.autospeak;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerContext;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerEventHandler;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerMessage;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerState;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.utils.CardTTSUtil;

import android.os.Handler;
import android.test.suitebuilder.annotation.SmallTest;

public class CardPlayerStateTest extends AbstractExistingDBTest {

    private final int TEST_CARD_ID = 5;

    private CardPlayerContext cardPlayerContext;


    private CardPlayerEventHandler mockEventHandler;

    private CardTTSUtil mockCardTTSUtil;

    private Handler mockAmTTSServiceHandler;

    private final int delayBeteenQAInSec = 3;

    private final int delayBeteenCardsInSec = 5;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockEventHandler = mock(CardPlayerEventHandler.class); 
        mockCardTTSUtil = mock(CardTTSUtil.class);
        mockAmTTSServiceHandler = mock(Handler.class);
        cardPlayerContext = new CardPlayerContext(
            mockEventHandler,
            mockCardTTSUtil,
            mockAmTTSServiceHandler,
            helper,
            delayBeteenQAInSec,
            delayBeteenCardsInSec);
        cardPlayerContext.setCurrentCard(helper.getCardDao().queryForId(TEST_CARD_ID));
    }

    @SmallTest
    public void testStoppedStateReceiveStartPlayingShouldGoToPlayQuestion() {
        verifyStateTransition(CardPlayerState.STOPPED,
                CardPlayerMessage.START_PLAYING, CardPlayerState.PLAYING_QUESTION);
    }

    @SmallTest
    public void testStoppedStateReceiveOtherMessagesShouldDoNothing() {
        verifyStateTransition(CardPlayerState.STOPPED,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.STOPPED);
        verifyStateTransition(CardPlayerState.STOPPED,
                CardPlayerMessage.GO_TO_PREV, CardPlayerState.STOPPED);
        verifyStateTransition(CardPlayerState.STOPPED,
                CardPlayerMessage.STOP_PLAYING, CardPlayerState.STOPPED);
        verifyStateTransition(CardPlayerState.STOPPED,
                CardPlayerMessage.PLAYING_ANSWER_COMPLETED, CardPlayerState.STOPPED);
        verifyStateTransition(CardPlayerState.STOPPED,
                CardPlayerMessage.PLAYING_QUESTION_COMPLETED, CardPlayerState.STOPPED);
    }

    @SmallTest
    public void testPlayingQuestionReceivedStopPlayingShouldStop() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.STOP_PLAYING, CardPlayerState.STOPPED);
    }

    @SmallTest
    public void testPlayingQuestionReceivedGoToNextShouldPlayNextQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID + 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    public void testPlayingQuestionReceivedGoToPrevShouldPlayPrevQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_PREV, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID - 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    public void testPlayingQuestionReceivedPlayQuesetionCompletedShouldPlayAnswer() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.PLAYING_QUESTION_COMPLETED, CardPlayerState.PLAYING_ANSWER);
        verify(mockCardTTSUtil, times(1)).speakCardAnswer(any(Card.class), any(AnyMemoTTS.OnTextToSpeechCompletedListener.class));
    }

    @SmallTest
    public void testPlayingAnswerReceivedStopPlayingShouldStop() {
        verifyStateTransition(CardPlayerState.PLAYING_ANSWER,
                CardPlayerMessage.STOP_PLAYING, CardPlayerState.STOPPED);
    }

    @SmallTest
    public void testPlayingAnswerReceivedGoToNextShouldPlayNextQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_ANSWER,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID + 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    public void testPlayingAnswerReceivedGoToPrevShouldPlayPrevQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_PREV, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID - 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    public void testPlayingAnswerReceivedPlayAnswerCompletedShouldPlayNextQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_ANSWER,
                CardPlayerMessage.PLAYING_ANSWER_COMPLETED, CardPlayerState.PLAYING_QUESTION);
        verify(mockCardTTSUtil, times(1)).speakCardQuestion(any(Card.class), any(AnyMemoTTS.OnTextToSpeechCompletedListener.class));
        // Also verify the event callback to the fragment.
        verify(mockEventHandler, times(1)).onPlayCard(any(Card.class));
        assertEquals(TEST_CARD_ID + 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    /* 
     * Helper method that verify the state transition from startState
     * to endStateToVerify if message is received
     */
    private void verifyStateTransition(CardPlayerState startState,
            CardPlayerMessage message,
            CardPlayerState endStateToVerify) {
        cardPlayerContext.setState(startState);
        cardPlayerContext.getState().transition(cardPlayerContext, message);
        assertEquals(endStateToVerify, cardPlayerContext.getState());
    }
}

