package org.liberty.android.fantastischmemo.ui;

import java.io.IOException;

import org.liberty.android.fantastischmemo.R;

import roboguice.fragment.RoboDialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ToggleButton;

public class AudioRecorderFragment extends RoboDialogFragment {
    public static final String EXTRA_AUDIO_FILENAME = "audioFilename";

    private static final String TAG = "AudioRecorderFragment";

    private Activity mActivity;

    private String mFileName = null;

    private ToggleButton mRecordButton = null;
    private ToggleButton mPlayButton = null;
    private Button mSaveButton = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    private boolean mStartPlaying = false;
    private boolean mStartRecording = false;

    private AudioRecorderResultListener audioRecorderResultListener;

    private View.OnClickListener buttonListener = new View.OnClickListener(){
        public void onClick(View v) {
            if (v == mRecordButton) {

                mStartRecording = !mStartRecording;
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mPlayButton.setChecked(true);
                    mPlayButton.setEnabled(false);
                    mSaveButton.setEnabled(false);

                } else {
                    mPlayButton.setChecked(false);
                    mPlayButton.setEnabled(true);
                    mSaveButton.setEnabled(true);
                }
            }
            if(v == mPlayButton){
                mStartPlaying = !mStartPlaying;
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    mPlayButton.setChecked(true);
                    mRecordButton.setEnabled(false);
                    mSaveButton.setEnabled(false);
                } else {
                    mPlayButton.setChecked(false);
                    mRecordButton.setEnabled(true);
                    mSaveButton.setEnabled(true);
                }
            }

            if(v == mSaveButton){
                audioRecorderResultListener.onReceiveAudio();
                dismiss();
            }

        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras =  this.getArguments();
        mFileName = extras.getString(EXTRA_AUDIO_FILENAME);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.recorder, null);

        mRecordButton= (ToggleButton) v.findViewById(R.id.record_button);
        mRecordButton.setOnClickListener(buttonListener);

        mPlayButton = (ToggleButton) v.findViewById(R.id.play_button);
        mPlayButton.setEnabled(false);
        mPlayButton.setOnClickListener(buttonListener);

        mSaveButton = (Button) v.findViewById(R.id.save_button);
        mSaveButton.setEnabled(false);
        mSaveButton.setOnClickListener(buttonListener);

        // Hide the keyboard
        mActivity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.record_your_memo_text)
            .setView(v)
            .create();
    }

    public void setAudioRecorderResultListener(AudioRecorderResultListener audioRecorderResultListener){
        this.audioRecorderResultListener = audioRecorderResultListener;
    }


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mStartPlaying = !mStartPlaying;
                stopPlaying();

                mPlayButton.setChecked(false);

                mRecordButton.setEnabled(true);
                mSaveButton.setEnabled(true);
            }
        });
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed when trying to play recorded audio");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed when trying to start recording");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
    }

    public static interface AudioRecorderResultListener {
        void onReceiveAudio();
    }
}
