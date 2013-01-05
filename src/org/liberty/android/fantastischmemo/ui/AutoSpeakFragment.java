package org.liberty.android.fantastischmemo.ui;


import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class AutoSpeakFragment extends Fragment {
    
    private static final String TAG = "AutoSpeakFragment";
    private ImageButton playButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton settingsButton;
    private ImageButton exitButton;
    private PreviewEditActivity previewEditActivity;
    
    private boolean isPlaying = false;
    private Handler handler;
    
    // We need to check this since activity may finish early while TTS thread is still trying to call gotoNext().
    private volatile boolean isActivityFinished = false;

    public AutoSpeakEventHandler getAutoSpeakEventHandler() {
        return this.autoSpeakEventHandler;
    } 
  
    private AnyMemoTTS.OnTextToSpeechCompletedListener mQuestionListener = new AnyMemoTTS.OnTextToSpeechCompletedListener() {
        
        @Override
        public void onTextToSpeechCompleted(final String text) {
            Log.i(TAG, "mAnswerListener is " + mAnswerListener);
            
                //TODO: This won't block UI thread but blocked TTS thread.
            // Should use postDelay in handler 
                /* 
                try {
                    Thread.sleep(10000);
                } catch(InterruptedException e) {
                    Log.i(TAG, "ffffffff" + e.getMessage());
                }
                */
                
                
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "ppppppppppppppppppppp: " + text);
                        if(!isActivityFinished) {
                            previewEditActivity.speakAnswer(mAnswerListener);
                        }
                    }
                };
                
                    handler.postDelayed(r, 5000);
               // previewEditActivity.runOnUiThread(r);
                
            Log.i(TAG, "in preview edit activity");
        }
    };
    
    private AnyMemoTTS.OnTextToSpeechCompletedListener mAnswerListener = new AnyMemoTTS.OnTextToSpeechCompletedListener() {
        
        @Override
        public void onTextToSpeechCompleted(final String text) {

            // Need to run gotoNext() in UI thread not TTS thread since it changes view. 
            previewEditActivity.runOnUiThread(new Runnable() {
                
                public void run() {
                    Log.i(TAG, "jiiiiwowoowjiowjeifjweoifgjeos: " + text);
                    
                    if(!isActivityFinished) {
                        previewEditActivity.gotoNext();
                        previewEditActivity.speakQuestion(mQuestionListener);
                    }
                }
            });
            
            Log.i(TAG, "lalalala");
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.previewEditActivity = (PreviewEditActivity)activity;
        this.handler = new Handler();
    }
    
    @Override
    public void onDestroy() {
        this.isActivityFinished = true;
        super.onDestroy();
    }
    
    private AutoSpeakEventHandler autoSpeakEventHandler = 
            new AutoSpeakEventHandler() {
                
                @Override
                public void onNextButtonClick() {
                    previewEditActivity.gotoNext();
                }

                @Override
                public void onPreviousButtonClick() {
                    previewEditActivity.gotoPrev();
                }

                @Override
                public void onPlayButtonClick() {
                    isActivityFinished = false;
                    previewEditActivity.speakQuestion(mQuestionListener);
                }

                @Override
                public void onPauseButtonClick() {
                    isActivityFinished = true;
                }
            };

    private View.OnClickListener buttonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(v == playButton) {
                isPlaying = !isPlaying;
                Log.i(TAG, "Play button clicked, isPlaying " + isPlaying);

                if(isPlaying) {
                    Log.i(TAG, "start speaking");
                    autoSpeakEventHandler.onPlayButtonClick();
                    playButton.setSelected(true);
                } else {
                    autoSpeakEventHandler.onPauseButtonClick();
                    playButton.setSelected(false);
                }
                
            } else if(v == previousButton) {
                autoSpeakEventHandler.onPreviousButtonClick();
            } else if(v == nextButton) {
                autoSpeakEventHandler.onNextButtonClick();
            } else if(v == settingsButton) {
                displaySettingsDialog();
            } else if(v == exitButton) {
                dismissFragment();
            }
        }
    };

    private void displaySettingsDialog() {
        
    }
    
    private void dismissFragment() {
        autoSpeakEventHandler.onPauseButtonClick();
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
    
    public void setAutoSpeakEventHander(AutoSpeakEventHandler autoSpeakEventHander) {
        this.autoSpeakEventHandler = autoSpeakEventHander;
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
