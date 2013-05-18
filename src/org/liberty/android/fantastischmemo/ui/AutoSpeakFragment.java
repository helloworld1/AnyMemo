package org.liberty.android.fantastischmemo.ui;


import org.apache.mycommons.lang3.StringUtils;
import org.apache.mycommons.lang3.time.DateUtils;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class AutoSpeakFragment extends Fragment {
    private static final String TAG = "AutoSpeakFragment";
    private static final int DEFAULT_SLEEP_TIME_IN_SEC = 1;
    private ImageButton playButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton settingsButton;
    private ImageButton exitButton;
    private PreviewEditActivity previewEditActivity;
    
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    
    private boolean isPlayButtonSelected = false;
    private Handler handler;
    
    // We need to check this since activity may finish early while TTS thread is still trying to call gotoNext().
    private volatile boolean isActivityFinished = false;
  
    private AnyMemoTTS.OnTextToSpeechCompletedListener mQuestionListener = new AnyMemoTTS.OnTextToSpeechCompletedListener() {
        
        @Override
        public void onTextToSpeechCompleted(final String text) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if(!isActivityFinished && isPlayButtonSelected) {
                            // This logic ensures that if we change card when speaking, we want to start from the question 
                            // for the new card. 
                            if (!StringUtils.equals(text, previewEditActivity.getCurrentCard().getQuestion())) {
                                previewEditActivity.speakQuestion(mQuestionListener);
                                return;
                            }
                            previewEditActivity.speakAnswer(mAnswerListener);
                        }
                    }
                };
                
                handler.postDelayed(r, 
                        DateUtils.MILLIS_PER_SECOND * settings.getInt(AMPrefKeys.AUTO_SPEAK_QA_SLEEP_INTERVAL_KEY, DEFAULT_SLEEP_TIME_IN_SEC));
        }
    };
    
    private AnyMemoTTS.OnTextToSpeechCompletedListener mAnswerListener = new AnyMemoTTS.OnTextToSpeechCompletedListener() {
        
        @Override
        public void onTextToSpeechCompleted(final String text) {
            
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    // This logic ensures that if we change card when speaking, we want to start from the question 
                    // for the new card. 
                    if(!isActivityFinished && isPlayButtonSelected) {
                        if (!StringUtils.equals(text, previewEditActivity.getCurrentCard().getAnswer())) {
                            previewEditActivity.speakQuestion(mQuestionListener);
                            return;
                        }
                        Log.i(TAG, "going to the next card");
                        previewEditActivity.gotoNext();
                        previewEditActivity.speakQuestion(mQuestionListener);
                    }

                }
            };
            handler.postDelayed(r, 
                    DateUtils.MILLIS_PER_SECOND * settings.getInt(AMPrefKeys.AUTO_SPEAK_CARD_SLEEP_INTERVAL_KEY, DEFAULT_SLEEP_TIME_IN_SEC));
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        isActivityFinished = true;
        if (isPlayButtonSelected) {
            isPlayButtonSelected = false;
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        isActivityFinished = false;
        playButton.setSelected(false);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.previewEditActivity = (PreviewEditActivity)activity;
        this.handler = new Handler();
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        editor = settings.edit();
    }
    
    @Override
    public void onDestroy() {
        this.isActivityFinished = true;
        super.onDestroy();
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(v == playButton) {
                isPlayButtonSelected = !isPlayButtonSelected;
                Log.i(TAG, "Play button clicked, isPlaying " + isPlayButtonSelected);
                
                if(isPlayButtonSelected) {
                    Log.i(TAG, "start speaking");
                    isActivityFinished = false;
                    previewEditActivity.speakQuestion(mQuestionListener);
                    playButton.setSelected(true);
                } else {
                    isActivityFinished = true;
                    playButton.setSelected(false);
                }
                
            } else if(v == previousButton) {
                previewEditActivity.gotoPrev();
            } else if(v == nextButton) {
                previewEditActivity.gotoNext();
            } else if(v == settingsButton) {
                displaySettingsDialog();
            } else if(v == exitButton) {
                dismissFragment();
            }
        }
    };

    private void displaySettingsDialog() {
        isPlayButtonSelected = !isPlayButtonSelected;
        playButton.setSelected(false);
        AutoSpeakSettingDialogFragment a = new AutoSpeakSettingDialogFragment();
        a.show(getActivity().getSupportFragmentManager(), TAG);
    }
    
    private void dismissFragment() {
        isActivityFinished = true;
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.auto_speak_layout, container, false);
        playButton = (ImageButton) v.findViewById(R.id.auto_speak_play_button);
        playButton.setOnClickListener(buttonListener);
        
        previousButton = (ImageButton) v.findViewById(R.id.auto_speak_previous_button);
        previousButton.setOnClickListener(buttonListener);
        previousButton.setBackgroundColor(Color.TRANSPARENT);
        
        nextButton = (ImageButton) v.findViewById(R.id.auto_speak_next_button);
        nextButton.setOnClickListener(buttonListener);
        nextButton.setBackgroundColor(Color.TRANSPARENT);
        
        settingsButton = (ImageButton) v.findViewById(R.id.auto_speak_settings_button);
        settingsButton.setOnClickListener(buttonListener);
        settingsButton.setBackgroundColor(Color.TRANSPARENT);
        
        exitButton = (ImageButton) v.findViewById(R.id.auto_speak_exit_button);
        exitButton.setOnClickListener(buttonListener);
        exitButton.setBackgroundColor(Color.TRANSPARENT);
        
        return v;
    }

}
