package org.liberty.android.fantastischmemo.ui;


import org.liberty.android.fantastischmemo.R;

import android.graphics.Color;
import android.os.Bundle;
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
    
    private boolean isPlaying = false;

    private AutoSpeakEventHandler autoSpeakEventHandler = 
            new AutoSpeakEventHandler() {
                
                @Override
                public void onNextButtonClick() {
                }

                @Override
                public void onPreviousButtonClick() {
                }

                @Override
                public void onPlayButtonClick() {
                }

                @Override
                public void onPauseButtonClick() {
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
                
            }
        }
    };

    private void displaySettingsDialog() {
        
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
    
    public interface AutoSpeakEventHandler {
        void onNextButtonClick();
        void onPreviousButtonClick();
        void onPlayButtonClick();
        void onPauseButtonClick();
    }
}
