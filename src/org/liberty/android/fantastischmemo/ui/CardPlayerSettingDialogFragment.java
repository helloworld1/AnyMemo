/*
Copyright (C) 2013 Haowen Ning, Xiaoyu Shi

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


import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CardPlayerSettingDialogFragment extends DialogFragment{
    private static final int DEFAULT_QA_SLEEP_TIME_IN_SEC = 1;
    private static final int DEFAULT_CARD_SLEEP_TIME_IN_SEC = 1;


    private SeekBar seekBarQA;
    private TextView textViewQA;
    private SeekBar seekBarCard;
    private TextView textViewCard;
    private int qaSleepTime;
    private int cardSleepTime;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Activity mActivity;

    public static CardPlayerSettingDialogFragment newInstance(int title) {
        CardPlayerSettingDialogFragment frag = new CardPlayerSettingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("title", title);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.card_player_settings, container, false);

        textViewQA = (TextView) v.findViewById(R.id.card_player_qa_sleep_interval_text);
        seekBarQA = (SeekBar) v.findViewById(R.id.card_player_qa_sleep_interval_seekbar);
        qaSleepTime = settings.getInt(AMPrefKeys.CARD_PLAYER_QA_SLEEP_INTERVAL_KEY, DEFAULT_QA_SLEEP_TIME_IN_SEC);
        textViewQA.setText(String.format(getString(R.string.card_player_qa_sleep_interval_text), qaSleepTime));

        seekBarQA.setProgress(qaSleepTime);
        seekBarQA.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                textViewQA.setText(String.format(getString(R.string.card_player_qa_sleep_interval_text), progress));
            }
        });


        seekBarCard = (SeekBar) v.findViewById(R.id.card_player_card_sleep_interval_seekbar);
        cardSleepTime = settings.getInt(AMPrefKeys.CARD_PLAYER_CARD_SLEEP_INTERVAL_KEY, DEFAULT_CARD_SLEEP_TIME_IN_SEC);
        seekBarCard.setProgress(cardSleepTime);
        textViewCard = (TextView) v.findViewById(R.id.card_player_card_sleep_interval_text);
        textViewCard.setText(String.format(getString(R.string.card_player_card_sleep_interval_text), cardSleepTime));

        seekBarCard.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                textViewCard.setText(String.format(getString(R.string.card_player_card_sleep_interval_text), progress));
            }
        });

        getDialog().setTitle(R.string.settings_menu_text);

        return v;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        int qaInterval = seekBarQA.getProgress();
        int cardInterval = seekBarCard.getProgress();

        editor.putInt(AMPrefKeys.CARD_PLAYER_QA_SLEEP_INTERVAL_KEY, qaInterval);
        editor.putInt(AMPrefKeys.CARD_PLAYER_CARD_SLEEP_INTERVAL_KEY, cardInterval) ;
        editor.commit();
        super.onDismiss(dialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
        settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        editor = settings.edit();
    }

}
