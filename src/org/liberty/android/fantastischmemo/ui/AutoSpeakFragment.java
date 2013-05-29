package org.liberty.android.fantastischmemo.ui;


import org.apache.mycommons.lang3.StringUtils;
import org.apache.mycommons.lang3.time.DateUtils;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakEventHandler;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;

import roboguice.fragment.RoboFragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class AutoSpeakFragment extends RoboFragment {
    private static final int DEFAULT_SLEEP_TIME_IN_SEC = 1;
    private ImageButton playButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton settingsButton;
    private ImageButton exitButton;
    private PreviewEditActivity previewEditActivity;

    private SharedPreferences settings;

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.previewEditActivity = (PreviewEditActivity) activity;
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.auto_speak_layout, container, false);
        playButton = (ImageButton) v.findViewById(R.id.auto_speak_play_button);
        playButton.setOnClickListener(buttonListener);
        playButton.setSelected(false);

        previousButton = (ImageButton) v.findViewById(R.id.auto_speak_previous_button);
        previousButton.setOnClickListener(buttonListener);

        nextButton = (ImageButton) v.findViewById(R.id.auto_speak_next_button);
        nextButton.setOnClickListener(buttonListener);

        settingsButton = (ImageButton) v.findViewById(R.id.auto_speak_settings_button);
        settingsButton.setOnClickListener(buttonListener);

        exitButton = (ImageButton) v.findViewById(R.id.auto_speak_exit_button);
        exitButton.setOnClickListener(buttonListener);

        return v;
    }


    private View.OnClickListener buttonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == playButton) {
                if (playButton.isSelected()) {
                    // Button is in "Playing" state 
                    stopPlaying();
                } else {
                    // Button is in "Not playing" state 
                    startPlaying();
                }

            } else if (v == previousButton) {
                previewEditActivity.getAMTTSService().skipToPrev();
            } else if (v == nextButton) {
                previewEditActivity.getAMTTSService().skipToNext();
            } else if (v == settingsButton) {
                displaySettingsDialog();
            } else if (v == exitButton) {
                dismissFragment();
            }
        }
    };

    private void startPlaying() {
        playButton.setSelected(true);
        previewEditActivity.getAMTTSService().startPlaying(
                previewEditActivity.getCurrentCard(), autoSpeakEventHandler);
    }

    private void stopPlaying() {
        playButton.setSelected(false);
        previewEditActivity.getAMTTSService().stopPlaying();
    }

    private void displaySettingsDialog() {
        stopPlaying();
        AutoSpeakSettingDialogFragment fragment = new AutoSpeakSettingDialogFragment();
        fragment.show(getActivity().getSupportFragmentManager(), "SettingsDialogFragment");
    }

    private void dismissFragment() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .remove(this).commit();
    }

    private AutoSpeakEventHandler autoSpeakEventHandler = new AutoSpeakEventHandler() {
        @Override
        public void onPlayCard(Card card) {
            if (card.getId() != previewEditActivity.getCurrentCard().getId()) {
                previewEditActivity.gotoCard(card);
            }
        }
    };

}
