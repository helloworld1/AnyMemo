package org.liberty.android.fantastischmemo.ui;


import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.service.CardPlayerService;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerEventHandler;

import roboguice.fragment.RoboFragment;
import roboguice.util.Ln;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/*
 * Display the control bar in CardPlayerActivity
 * Handle the event from CardPlayerService
 */
public class CardPlayerFragment extends RoboFragment {

    private ImageButton playButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton shuffleButton;
    private ImageButton repeatButton;

    private CardPlayerActivity activity;

    private CardPlayerService cardPlayerService;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        bindCardPlayerService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindCardPlayerService();
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

        shuffleButton = (ImageButton) v.findViewById(R.id.card_player_shuffle_button);
        shuffleButton.setOnClickListener(buttonListener);

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

    public void bindCardPlayerService() {
        Intent intent = new Intent(activity, CardPlayerService.class);
        intent.putExtra(CardPlayerService.EXTRA_DBPATH, activity.getDbPath());
        activity.bindService(intent, cardPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindCardPlayerService() {
        activity.unbindService(cardPlayerServiceConnection);
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
                cardPlayerService.skipToPrev();
            } else if (v == nextButton) {
                cardPlayerService.skipToNext();
            } else if (v == repeatButton) {
                // TODO: implementation
            } else if (v == shuffleButton) {
                // TODO: implementation
            }
        }
    };

    private void startPlaying() {
        playButton.setSelected(true);
        cardPlayerService.startPlaying(activity.getCurrentCard(), cardPlayerEventHandler);
    }

    private void stopPlaying() {
        playButton.setSelected(false);
        cardPlayerService.stopPlaying();
        
    }

    private void displaySettingsDialog() {
        stopPlaying();
        CardPlayerSettingDialogFragment fragment = new CardPlayerSettingDialogFragment();
        fragment.show(getActivity().getSupportFragmentManager(), "SettingsDialogFragment");
    }

    /* This handler is used for callback from the CardPlayerService's startPlaying */
    private CardPlayerEventHandler cardPlayerEventHandler = new CardPlayerEventHandler() {
        @Override
        public void onPlayCard(Card card) {
            // 1. Make sure the activity is foreground to update the card.
            // 2. Only update the card if the card is different.
            // So the background service will continue to work with this callback
            // being called.
            if (activity.isActivityForeground()
                    && card.getId() != activity.getCurrentCard().getId()) {
                activity.gotoCard(card);
            }
        }
    };

    private ServiceConnection cardPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            CardPlayerService.LocalBinder localBinder = (CardPlayerService.LocalBinder) binder;

            cardPlayerService = localBinder.getService();

            Card currentPlayingCard = localBinder.getCurrentPlayingCard();

            Ln.v("Current playing card when connection to service: " + currentPlayingCard);
            // When connecting to an existing service, go to the current playing card
            if (currentPlayingCard != null) {
                activity.gotoCard(currentPlayingCard);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            cardPlayerService = null;
        }
    };

}
