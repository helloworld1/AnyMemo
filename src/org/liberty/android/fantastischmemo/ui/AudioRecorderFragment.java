package org.liberty.android.fantastischmemo.ui;

import java.io.IOException;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class AudioRecorderFragment extends DialogFragment {

    private static final String LOG_TAG = "AudioRecorder";
    private String mFileName = null;
    private Activity mActivity = null;

    private ImageButton mRecordButton = null;
    private MediaRecorder mRecorder = null;
    private ImageButton mPlayButton = null;
    private MediaPlayer mPlayer = null;
    private ImageButton mReturnButton = null;
    
    private boolean mStartPlaying = false;
    private boolean mStartRecording = false;
    
    private AudioRecorderResultListener audioRecorderResultListener;
    
    private View.OnClickListener buttonListener = new View.OnClickListener(){
        public void onClick(View v) {
            if (v == mRecordButton) {
                
                mStartRecording = !mStartRecording;
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButton.setImageResource(R.drawable.recorder_stop);
                    mPlayButton.setEnabled(false);
                    mReturnButton.setEnabled(false);
                    
                } else {
                    mRecordButton.setImageResource(R.drawable.recorder_record);
                    mPlayButton.setEnabled(true);
                    mReturnButton.setEnabled(true);
                }
            }
            if(v == mPlayButton){
                mStartPlaying = !mStartPlaying;
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    mPlayButton.setImageResource(R.drawable.recorder_pause);
                    mRecordButton.setEnabled(false);
                    mReturnButton.setEnabled(false);
                } else {
                    mPlayButton.setImageResource(R.drawable.recorder_play);
                    mRecordButton.setEnabled(true);
                    mReturnButton.setEnabled(true);
                }
            }
            
            if(v == mReturnButton){
                audioRecorderResultListener.onReceiveAudio();
                getDialog().dismiss();
            }
            
        }
    };
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity)activity;
    }
    
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setStyle(STYLE_NO_TITLE, 0);
        Bundle extras =  this.getArguments();
        mFileName = extras.getString("audioFilename");
    }
    
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.recorder, container, false);
            LinearLayout recorder_ll = (LinearLayout)v.findViewById(R.id.recorder_ll);
            Rect displayRectangle = new Rect();
            Window window = mActivity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

            recorder_ll.setMinimumWidth((int)(displayRectangle.width() * 0.9f));
            recorder_ll.setMinimumHeight((int)(displayRectangle.height() * 0.8f));

            
            
            mRecordButton = (ImageButton) v.findViewById(R.id.recorder_record_button);
            mRecordButton.setOnClickListener(buttonListener);
            mRecordButton.setBackgroundColor(Color.TRANSPARENT);
            
            mPlayButton = (ImageButton) v.findViewById(R.id.recorder_play_button);
            mPlayButton.setEnabled(false);
            mPlayButton.setOnClickListener(buttonListener);
            mPlayButton.setBackgroundColor(Color.TRANSPARENT);
            
            mReturnButton = (ImageButton) v.findViewById(R.id.recorder_return_button) ;
            mReturnButton.setEnabled(false);
            mReturnButton.setOnClickListener(buttonListener);
            mReturnButton.setBackgroundColor(Color.TRANSPARENT);
            
            return v;
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
                mPlayButton.setImageResource(R.drawable.recorder_play);
                mRecordButton.setEnabled(true);
                mReturnButton.setEnabled(true);
            }
        });
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed when trying to play recorded audio");
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
        mFileName = mFileName.replaceAll("<audio.*src=.*/>","");
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed when trying to start recording");
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
