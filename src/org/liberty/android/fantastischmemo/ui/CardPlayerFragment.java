package org.liberty.android.fantastischmemo.ui;


import javax.inject.Inject;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Option;

import roboguice.fragment.RoboFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/*
 * Display the control bar in CardPlayerActivity
 */
public class CardPlayerFragment extends RoboFragment {

    private ImageButton playButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton shuffleButton;
    private ImageButton repeatButton;

    private CardPlayerActivity activity;

    private Option option;

    @Inject
    public void setOption(Option option) {
        this.option = option;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (CardPlayerActivity) activity;
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

        repeatButton = (ImageButton) v.findViewById(R.id.card_player_repeat_button);
        repeatButton.setOnClickListener(buttonListener);
        repeatButton.setSelected(option.getCardPlayerRepeatEnabled());

        shuffleButton = (ImageButton) v.findViewById(R.id.card_player_shuffle_button);
        shuffleButton.setOnClickListener(buttonListener);
        shuffleButton.setSelected(option.getCardPlayerShuffleEnabled());

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Make sure stop playing if the fragment is killed.
        // TODO: Is this still needed if we want background service
        // to continue playing?
        stopPlaying();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.card_player_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.card_player_settings:
            displaySettingsDialog();
            return true;

        default:
            return false;
        }
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
                activity.getCardPlayerService().skipToPrev();
            } else if (v == nextButton) {
                activity.getCardPlayerService().skipToNext();
            } else if (v == repeatButton) {
                stopPlaying();
                if (repeatButton.isSelected()) {
                    repeatButton.setSelected(false);
                    option.setCardPlayerRepeatEnabled(false);
                } else {
                    repeatButton.setSelected(true);
                    option.setCardPlayerRepeatEnabled(true);
                }
                // Need to reset the service context to pick up
                // the new config
                activity.getCardPlayerService().reset();
            } else if (v == shuffleButton) {
                stopPlaying();
                if (shuffleButton.isSelected()) {
                    shuffleButton.setSelected(false);
                    option.setCardPlayerShuffleEnabled(false);
                } else {
                    shuffleButton.setSelected(true);
                    option.setCardPlayerShuffleEnabled(true);
                }
                activity.getCardPlayerService().reset();
            }
        }
    };

    private void startPlaying() {
        playButton.setSelected(true);
        activity.getCardPlayerService().startPlaying(activity.getCurrentCard());
    }

    private void stopPlaying() {
        playButton.setSelected(false);
        activity.getCardPlayerService().stopPlaying();
    }

    private void displaySettingsDialog() {
        stopPlaying();
        CardPlayerSettingDialogFragment fragment = new CardPlayerSettingDialogFragment();
        fragment.show(getActivity().getSupportFragmentManager(), "SettingsDialogFragment");
    }

}
