package org.liberty.android.fantastischmemo.test.service.autospeak;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.service.AMTTSService;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakContext;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakEventHandler;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakMessage;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakState;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;

import android.os.Handler;
import android.test.suitebuilder.annotation.SmallTest;

public class AutoSpeakStateTest extends AbstractExistingDBTest {

    private final int TEST_CARD_ID = 5;

    private AutoSpeakContext autoSpeakContext;


    private AutoSpeakEventHandler mockEventHandler;

    private AMTTSService mockAmTTSService;
    private Handler mockAmTTSServiceHandler;

    private final int delayBeteenQAInSec = 3;

    private final int delayBeteenCardsInSec = 5;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockEventHandler = mock(AutoSpeakEventHandler.class); 
        mockAmTTSService = mock(AMTTSService.class);
        mockAmTTSServiceHandler = mock(Handler.class);
        autoSpeakContext = new AutoSpeakContext(
            mockEventHandler,
            mockAmTTSService,
            mockAmTTSServiceHandler,
            helper,
            delayBeteenQAInSec,
            delayBeteenCardsInSec);
        autoSpeakContext.setCurrentCard(helper.getCardDao().queryForId(TEST_CARD_ID));
    }

    @SmallTest
    public void testStoppedStateReceiveStartPlayingShouldGoToPlayQuestion() {
        verifyStateTransition(AutoSpeakState.STOPPED,
                AutoSpeakMessage.START_PLAYING, AutoSpeakState.PLAYING_QUESTION);
    }

    @SmallTest
    public void testStoppedStateReceiveOtherMessagesShouldDoNothing() {
        verifyStateTransition(AutoSpeakState.STOPPED,
                AutoSpeakMessage.GO_TO_NEXT, AutoSpeakState.STOPPED);
        verifyStateTransition(AutoSpeakState.STOPPED,
                AutoSpeakMessage.GO_TO_PREV, AutoSpeakState.STOPPED);
        verifyStateTransition(AutoSpeakState.STOPPED,
                AutoSpeakMessage.STOP_PLAYING, AutoSpeakState.STOPPED);
        verifyStateTransition(AutoSpeakState.STOPPED,
                AutoSpeakMessage.PLAYING_ANSWER_COMPLETED, AutoSpeakState.STOPPED);
        verifyStateTransition(AutoSpeakState.STOPPED,
                AutoSpeakMessage.PLAYING_QUESTION_COMPLETED, AutoSpeakState.STOPPED);
    }

    @SmallTest
    public void testPlayingQuestionReceivedStopPlayingShouldStop() {
        verifyStateTransition(AutoSpeakState.PLAYING_QUESTION,
                AutoSpeakMessage.STOP_PLAYING, AutoSpeakState.STOPPED);
    }

    @SmallTest
    public void testPlayingQuestionReceivedGoToNextShouldPlayNextQuestion() {
        verifyStateTransition(AutoSpeakState.PLAYING_QUESTION,
                AutoSpeakMessage.GO_TO_NEXT, AutoSpeakState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID + 1, (int)autoSpeakContext.getCurrentCard().getId());
    }

    @SmallTest
    public void testPlayingQuestionReceivedGoToPrevShouldPlayPrevQuestion() {
        verifyStateTransition(AutoSpeakState.PLAYING_QUESTION,
                AutoSpeakMessage.GO_TO_PREV, AutoSpeakState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID - 1, (int)autoSpeakContext.getCurrentCard().getId());
    }

    @SmallTest
    public void testPlayingQuestionReceivedPlayQuesetionCompletedShouldPlayAnswer() {
        verifyStateTransition(AutoSpeakState.PLAYING_QUESTION,
                AutoSpeakMessage.PLAYING_QUESTION_COMPLETED, AutoSpeakState.PLAYING_ANSWER);
        verify(mockAmTTSService, times(1)).speakCardAnswer(any(Card.class), any(AnyMemoTTS.OnTextToSpeechCompletedListener.class));
    }

    @SmallTest
    public void testPlayingAnswerReceivedStopPlayingShouldStop() {
        verifyStateTransition(AutoSpeakState.PLAYING_ANSWER,
                AutoSpeakMessage.STOP_PLAYING, AutoSpeakState.STOPPED);
    }

    @SmallTest
    public void testPlayingAnswerReceivedGoToNextShouldPlayNextQuestion() {
        verifyStateTransition(AutoSpeakState.PLAYING_ANSWER,
                AutoSpeakMessage.GO_TO_NEXT, AutoSpeakState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID + 1, (int)autoSpeakContext.getCurrentCard().getId());
    }

    @SmallTest
    public void testPlayingAnswerReceivedGoToPrevShouldPlayPrevQuestion() {
        verifyStateTransition(AutoSpeakState.PLAYING_QUESTION,
                AutoSpeakMessage.GO_TO_PREV, AutoSpeakState.PLAYING_QUESTION);
        assertEquals(TEST_CARD_ID - 1, (int)autoSpeakContext.getCurrentCard().getId());
    }

    @SmallTest
    public void testPlayingAnswerReceivedPlayAnswerCompletedShouldPlayNextQuestion() {
        verifyStateTransition(AutoSpeakState.PLAYING_ANSWER,
                AutoSpeakMessage.PLAYING_ANSWER_COMPLETED, AutoSpeakState.PLAYING_QUESTION);
        verify(mockAmTTSService, times(1)).speakCardQuestion(any(Card.class), any(AnyMemoTTS.OnTextToSpeechCompletedListener.class));
        // Also verify the event callback to the fragment.
        verify(mockEventHandler, times(1)).onPlayCard(any(Card.class));
        assertEquals(TEST_CARD_ID + 1, (int)autoSpeakContext.getCurrentCard().getId());
    }

    /* 
     * Helper method that verify the state transition from startState
     * to endStateToVerify if message is received
     */
    private void verifyStateTransition(AutoSpeakState startState,
            AutoSpeakMessage message,
            AutoSpeakState endStateToVerify) {
        autoSpeakContext.setState(startState);
        autoSpeakContext.getState().transition(autoSpeakContext, message);
        assertEquals(endStateToVerify, autoSpeakContext.getState());
    }
}

