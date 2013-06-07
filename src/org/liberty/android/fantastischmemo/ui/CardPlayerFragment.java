package org.liberty.android.fantastischmemo.ui;


import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.service.CardPlayerService;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerEventHandler;

import roboguice.fragment.RoboFragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class CardPlayerFragment extends RoboFragment {

    private ImageButton playButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton settingsButton;
    private ImageButton exitButton;

    private PreviewEditActivity previewEditActivity;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.card_player_layout, container, false);
        playButton = (ImageButton) v.findViewById(R.id.card_player_play_button);
        playButton.setOnClickListener(buttonListener);
        playButton.setSelected(false);

        previousButton = (ImageButton) v.findViewById(R.id.card_player_previous_button);
        previousButton.setOnClickListener(buttonListener);

        nextButton = (ImageButton) v.findViewById(R.id.card_player_next_button);
        nextButton.setOnClickListener(buttonListener);

        settingsButton = (ImageButton) v.findViewById(R.id.card_player_settings_button);
        settingsButton.setOnClickListener(buttonListener);

        exitButton = (ImageButton) v.findViewById(R.id.card_player_exit_button);
        exitButton.setOnClickListener(buttonListener);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Make sure stop playing if the fragment is killed.
        stopPlaying();
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == playButton) {
                if (playButton.isSelected()) {
                    // Button is currently in "Playing" state 
                    stopPlaying();
                } else {
                    // Button is currently in "Not playing" state 
                    startPlaying();
                }
            } else if (v == previousButton) {
                //previewEditActivity.getAMTTSService().skipToPrev();
            } else if (v == nextButton) {
                //previewEditActivity.getAMTTSService().skipToNext();
            } else if (v == settingsButton) {
                //displaySettingsDialog();
            } else if (v == exitButton) {
                dismissFragment();
            }
        }
    };

    private void startPlaying() {
        playButton.setSelected(true);
        // previewEditActivity.getAMTTSService().startPlaying(
        //         previewEditActivity.getCurrentCard(), cardPlayerEventHandler);
    }

    private void stopPlaying() {
        playButton.setSelected(false);
        //previewEditActivity.getAMTTSService().stopPlaying();
    }

    private void displaySettingsDialog() {
        stopPlaying();
        CardPlayerSettingDialogFragment fragment = new CardPlayerSettingDialogFragment();
        fragment.show(getActivity().getSupportFragmentManager(), "SettingsDialogFragment");
    }

    private void dismissFragment() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .remove(this).commit();
    }

    private CardPlayerEventHandler cardPlayerEventHandler = new CardPlayerEventHandler() {
        @Override
        public void onPlayCard(Card card) {
            // 1. Make sure the activity is foreground to update the card.
            // 2. Only update the card if the card is different.
            if (previewEditActivity.isActivityForeground()
                    && card.getId() != previewEditActivity.getCurrentCard().getId()) {
                previewEditActivity.gotoCard(card);
            }
        }
    };

    private CardPlayerService cardPlayerService;

    private ServiceConnection autoSpeakServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            cardPlayerService = ((CardPlayerService.LocalBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            cardPlayerService = null;
        }
    };

}
