package org.liberty.android.fantastischmemo.test.service.cardplayer;

import android.os.Handler;
import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerContext;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerEventHandler;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerMessage;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerState;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.utils.CardTTSUtil;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CardPlayerStateTest extends AbstractExistingDBTest {

    private final int TEST_CARD_ID = 5;

    private final int TEST_FIRST_CARD_ID = 1;

    private final int TEST_LAST_CARD_ID = 28;

    private CardPlayerContext cardPlayerContext;


    private CardPlayerEventHandler mockEventHandler;

    private CardTTSUtil mockCardTTSUtil;

    private Handler mockAmTTSServiceHandler;

    private final int delayBeteenQAInSec = 3;

    private final int delayBeteenCardsInSec = 5;


    @Override
    public void setUp() throws Exception {
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
            delayBeteenCardsInSec,
            false,
            true);
        cardPlayerContext.setCurrentCard(helper.getCardDao().queryForId(TEST_CARD_ID));
    }

    @SmallTest
    @Test
    public void testStoppedStateReceiveStartPlayingShouldGoToPlayQuestion() {
        verifyStateTransition(CardPlayerState.STOPPED,
                CardPlayerMessage.START_PLAYING, CardPlayerState.PLAYING_QUESTION);
    }

    @SmallTest
    @Test
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
    @Test
    public void testPlayingQuestionReceivedStopPlayingShouldStop() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.STOP_PLAYING, CardPlayerState.STOPPED);
    }

    @SmallTest
    @Test
    public void testPlayingQuestionReceivedGoToNextShouldPlayNextQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID + 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    @Test
    public void testPlayingQuestionReceivedGoToPrevShouldPlayPrevQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_PREV, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID - 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    @Test
    public void testPlayingQuestionReceivedPlayQuesetionCompletedShouldPlayAnswer() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.PLAYING_QUESTION_COMPLETED, CardPlayerState.PLAYING_ANSWER);
        verify(mockCardTTSUtil, times(1)).speakCardAnswer(any(Card.class), any(AnyMemoTTS.OnTextToSpeechCompletedListener.class));
    }

    @SmallTest
    @Test
    public void testPlayingAnswerReceivedStopPlayingShouldStop() {
        verifyStateTransition(CardPlayerState.PLAYING_ANSWER,
                CardPlayerMessage.STOP_PLAYING, CardPlayerState.STOPPED);
    }

    @SmallTest
    @Test
    public void testPlayingAnswerReceivedGoToNextShouldPlayNextQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_ANSWER,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID + 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    @Test
    public void testPlayingAnswerReceivedGoToPrevShouldPlayPrevQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_PREV, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID - 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    @Test
    public void testPlayingAnswerReceivedPlayAnswerCompletedShouldPlayNextQuestion() {
        verifyStateTransition(CardPlayerState.PLAYING_ANSWER,
                CardPlayerMessage.PLAYING_ANSWER_COMPLETED, CardPlayerState.PLAYING_QUESTION);
        verify(mockCardTTSUtil, times(1)).speakCardQuestion(any(Card.class), any(AnyMemoTTS.OnTextToSpeechCompletedListener.class));
        // Also verify the event callback to the fragment.
        verify(mockEventHandler, times(1)).onPlayCard(any(Card.class));
        assertEquals(TEST_CARD_ID + 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    // With repeat option disabled
    @SmallTest
    @Test
    public void testPlayNextQuestionNoRepeatWithCardInMiddle() {
        cardPlayerContext = new CardPlayerContext(
            mockEventHandler,
            mockCardTTSUtil,
            mockAmTTSServiceHandler,
            helper,
            delayBeteenQAInSec,
            delayBeteenCardsInSec,
            false,
            // No repeat
            false);
        cardPlayerContext.setCurrentCard(helper.getCardDao().queryForId(TEST_CARD_ID));

        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID + 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    @Test
    public void testPlayNextQuestionNoRepeatWithCardAtEndShouldStop() {
        cardPlayerContext = new CardPlayerContext(
            mockEventHandler,
            mockCardTTSUtil,
            mockAmTTSServiceHandler,
            helper,
            delayBeteenQAInSec,
            delayBeteenCardsInSec,
            false,
            // No repeat
            false);
        cardPlayerContext.setCurrentCard(helper.getCardDao().queryForId(TEST_LAST_CARD_ID));

        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.STOPPED);
    }

    @SmallTest
    @Test
    public void testPlayPrevQuestionNoRepeatWithCardInMiddle() {
        cardPlayerContext = new CardPlayerContext(
            mockEventHandler,
            mockCardTTSUtil,
            mockAmTTSServiceHandler,
            helper,
            delayBeteenQAInSec,
            delayBeteenCardsInSec,
            false,
            // No repeat
            false);
        cardPlayerContext.setCurrentCard(helper.getCardDao().queryForId(TEST_CARD_ID));

        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_PREV, CardPlayerState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID - 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    @Test
    public void testPlayPrevQuestionNoRepeatWithFirstCardShouldStop() {
        cardPlayerContext = new CardPlayerContext(
            mockEventHandler,
            mockCardTTSUtil,
            mockAmTTSServiceHandler,
            helper,
            delayBeteenQAInSec,
            delayBeteenCardsInSec,
            false,
            // No repeat
            false);
        cardPlayerContext.setCurrentCard(helper.getCardDao().queryForId(TEST_FIRST_CARD_ID));

        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_PREV, CardPlayerState.STOPPED);
    }

    // Shuffle test
    @SmallTest
    @Test
    public void testPlayRandomPrevCard() {
        cardPlayerContext = new CardPlayerContext(
            mockEventHandler,
            mockCardTTSUtil,
            mockAmTTSServiceHandler,
            helper,
            delayBeteenQAInSec,
            delayBeteenCardsInSec,
            // Shuffle enabled
            true,
            true);
        cardPlayerContext.setCurrentCard(helper.getCardDao().queryForId(TEST_CARD_ID));

        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_PREV, CardPlayerState.PLAYING_QUESTION);
    }

    @SmallTest
    @Test
    public void testPlayRandomNextCard() {
        cardPlayerContext = new CardPlayerContext(
            mockEventHandler,
            mockCardTTSUtil,
            mockAmTTSServiceHandler,
            helper,
            delayBeteenQAInSec,
            delayBeteenCardsInSec,
            // Shuffle enabled
            true,
            true);
        cardPlayerContext.setCurrentCard(helper.getCardDao().queryForId(TEST_CARD_ID));

        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.PLAYING_QUESTION);
    }

    // Shuffle and repeat work together. Shuffle should take precedence
    // so it alway repeat
    @SmallTest
    @Test
    public void testPlayRandomNextCardWithRepeat() {
        cardPlayerContext = new CardPlayerContext(
            mockEventHandler,
            mockCardTTSUtil,
            mockAmTTSServiceHandler,
            helper,
            delayBeteenQAInSec,
            delayBeteenCardsInSec,
            // Shuffle enabled
            true,
            // No repeat
            false);

        // Use last card to test that repeat has no effect
        cardPlayerContext.setCurrentCard(helper.getCardDao().queryForId(TEST_LAST_CARD_ID));

        verifyStateTransition(CardPlayerState.PLAYING_QUESTION,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.PLAYING_QUESTION);
    }

    @SmallTest
    @Test
    public void testStoppedReceivedGoToNextShouldGotoNextQuestion() {
        verifyStateTransition(CardPlayerState.STOPPED,
                CardPlayerMessage.GO_TO_NEXT, CardPlayerState.STOPPED);
        assertEquals(TEST_CARD_ID + 1, (int)cardPlayerContext.getCurrentCard().getId());
    }

    @SmallTest
    @Test
    public void testStoppedReceivedGoToPrevShouldGotoPrevQuestion() {
        verifyStateTransition(CardPlayerState.STOPPED,
                CardPlayerMessage.GO_TO_PREV, CardPlayerState.STOPPED);
        assertEquals(TEST_CARD_ID - 1, (int)cardPlayerContext.getCurrentCard().getId());
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

