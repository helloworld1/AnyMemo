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
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AutoSpeakSettingDialogFragment extends DialogFragment{
    private SeekBar seekBarQA;
    private TextView textViewQA;
    private SeekBar seekBarCard;
    private TextView textViewCard;
    private int qaSleepTime;
    private int cardSleepTime;
    
    private Button button;
    private static final String TAG = "AutoSpeakFragment";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Activity mActivity;
    
    private static final int MAX_QA_SLEEP_TIME_IN_SEC = 10;
    private static final int MAX_CARD_SLEEP_TIME_IN_SEC = 10;
    
    public static AutoSpeakSettingDialogFragment newInstance(int title) {
        AutoSpeakSettingDialogFragment frag = new AutoSpeakSettingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("title", title);
        frag.setArguments(bundle);
        return frag;
    }
    
    /*
    public Dialog onCreateDialog(Bundle bundle) {
        //int title = getArguments().getInt("title");
        
        return new AlertDialog.Builder(getActivity()).create();
    }
    */
    
    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.auto_speak_settings, container, false);
        
        textViewQA = (TextView) v.findViewById(R.id.auto_speak_qa_sleep_interval_text);
        seekBarQA = (SeekBar) v.findViewById(R.id.auto_speak_qa_sleep_interval_seekbar);
        qaSleepTime = settings.getInt(AMPrefKeys.AUTO_SPEAK_QA_SLEEP_INTERVAL_KEY, 1);
        textViewQA.setText(String.format(getString(R.string.auto_speak_qa_sleep_interval_text), qaSleepTime));
        
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
                textViewQA.setText(String.format(getString(R.string.auto_speak_qa_sleep_interval_text), progress));
            }
        });
        
        
        seekBarCard = (SeekBar) v.findViewById(R.id.auto_speak_card_sleep_interval_seekbar);
        cardSleepTime = settings.getInt(AMPrefKeys.AUTO_SPEAK_CARD_SLEEP_INTERVAL_KEY, 1);
        seekBarCard.setProgress(cardSleepTime);
        textViewCard = (TextView) v.findViewById(R.id.auto_speak_card_sleep_interval_text);
        textViewCard.setText(String.format(getString(R.string.auto_speak_card_sleep_interval_text), cardSleepTime));
        
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
                textViewCard.setText(String.format(getString(R.string.auto_speak_card_sleep_interval_text), progress));
            }
        });
        
        getDialog().setTitle(R.string.auto_speak_setting_dialog);
        
        return v;
    }
    
    @Override
    public void onDismiss(DialogInterface dialog) {
   //     Log.i(TAG, "Previous value is " + settings.getInt(AMPrefKeys.AUTO_SPEAK_SLEEP_TIME_KEY, 5));
        int qa = seekBarQA.getProgress();
        int card = seekBarCard.getProgress();
        
        editor.putInt(AMPrefKeys.AUTO_SPEAK_QA_SLEEP_INTERVAL_KEY, qa);
        editor.putInt(AMPrefKeys.AUTO_SPEAK_CARD_SLEEP_INTERVAL_KEY, card) ;
  //      Log.i(TAG, editText.getText().toString());
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
