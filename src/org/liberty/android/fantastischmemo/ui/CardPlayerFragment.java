/*
Copyright (C) 2013 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.ui;


import javax.inject.Inject;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.service.CardPlayerService;

import roboguice.fragment.RoboFragment;
import roboguice.util.Ln;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/*
 * Display the control bar in CardPlayerActivity. Handle the logic
 * of controlling the CardPlayerService.
 */
public class CardPlayerFragment extends RoboFragment {

    private ImageButton playButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton shuffleButton;
    private ImageButton repeatButton;

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

    // Make sure the serviceEventListener broadcast receiver
    // is registered at onResume and unregistered at onPause
    // because we do not care about the UI being updated from the
    // CardPlayerService if it is not visible to the user.
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CardPlayerService.ACTION_GO_TO_CARD);
        filter.addAction(CardPlayerService.ACTION_PLAYING_STOPPED);
        getActivity().registerReceiver(serviceEventListener, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(serviceEventListener);
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
            CardPlayerActivity activity = (CardPlayerActivity) getActivity();
            if (activity == null) {
                Ln.w("Activity is null, do not handle any click events");
                return;
            }
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
        CardPlayerActivity activity = (CardPlayerActivity) getActivity();
        activity.getCardPlayerService().startPlaying(activity.getCurrentCard());
    }

    private void stopPlaying() {
        playButton.setSelected(false);
        CardPlayerActivity activity = (CardPlayerActivity) getActivity();
        if (activity != null && activity.getCardPlayerService() != null) {
            activity.getCardPlayerService().stopPlaying();
        }
    }

    private void displaySettingsDialog() {
        stopPlaying();
        CardPlayerSettingDialogFragment fragment = new CardPlayerSettingDialogFragment();
        fragment.show(getActivity().getSupportFragmentManager(), "SettingsDialogFragment");
    }

    /*
     * This broadcast receiver receive the ACTION_GO_TO_CARD sent from
     * CardPlayerService. It will go to a specific card based on the extras
     * in received intent.
     */
    private BroadcastReceiver serviceEventListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CardPlayerService.ACTION_GO_TO_CARD)) {
                Bundle extras = intent.getExtras();
                assert extras != null : "The intent received must have card id and playing status"; 
                int currentCardId = extras.getInt(CardPlayerService.EXTRA_CURRENT_CARD_ID);

                // 1. Make sure the activity is foreground to update the card.
                // 2. Only update the card if the card is different.
                // So the background service will continue to work with this callback
                // being called.
                CardPlayerActivity activity = (CardPlayerActivity) getActivity();
                if (activity == null) {
                    Ln.w("Activity is null, do not handle any click events");
                    return;
                }
                if (activity.isActivityForeground() && currentCardId != activity.getCurrentCard().getId()) {
                    activity.gotoCardId(currentCardId);
                }
            }

            if (action.equals(CardPlayerService.ACTION_PLAYING_STOPPED)) {
                stopPlaying();
            }


        }
    };

}
