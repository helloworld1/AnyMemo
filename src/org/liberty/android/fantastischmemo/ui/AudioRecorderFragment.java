package org.liberty.android.fantastischmemo.ui;

import java.io.IOException;
import org.liberty.android.fantastischmemo.R;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class AudioRecorderFragment extends DialogFragment  implements View.OnClickListener{

	private static final String LOG_TAG = "AudioRecorder";
	private String mFileName = null;

	private ImageButton mRecordButton = null;
	private MediaRecorder mRecorder = null;

	private ImageButton mPlayButton = null;
	private MediaPlayer mPlayer = null;

	private ImageButton mReturnButton = null;
	
	private boolean mStartPlaying = false;
	private boolean mStartRecording = false;
	
	
	private AudioRecorderResultListener audioRecorderResultListener;
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Bundle extras =  this.getArguments();
		mFileName = extras.getString("audioFilename");
	}
	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View v = inflater.inflate(R.layout.recorder, container, false);
	        
			mRecordButton = (ImageButton) v.findViewById(R.id.recorder_record_button);
			mRecordButton.setOnClickListener(AudioRecorderFragment.this);
			
			mPlayButton = (ImageButton) v.findViewById(R.id.recorder_play_button);
			mPlayButton.setEnabled(false);
			mPlayButton.setOnClickListener(AudioRecorderFragment.this);
			
			mReturnButton = (ImageButton) v.findViewById(R.id.recorder_return_button) ;
			mReturnButton.setEnabled(false);
			mReturnButton.setOnClickListener(AudioRecorderFragment.this);
			
	        return v;
	}
	 
	public void setAudioRecorderResultListener(AudioRecorderResultListener audioRecorderResultListener){
		this.audioRecorderResultListener = audioRecorderResultListener;
	}
	
	public void onClick(View v) {
		if (v == mRecordButton) {
			
			mStartRecording = !mStartRecording;
			Log.v("xinxin**", "mRecord and mstartRecording=" + mStartRecording);
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
			Log.v("xinxin**", "mPlayButton");
			audioRecorderResultListener.onReceiveAudio();
			getDialog().dismiss();
		}
		
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
