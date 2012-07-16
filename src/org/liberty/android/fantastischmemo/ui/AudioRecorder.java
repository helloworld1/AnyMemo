package org.liberty.android.fantastischmemo.ui;

import java.io.IOException;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AudioRecorder extends AMActivity {
	private static final String LOG_TAG = "AudioRecorder";
	private String mFileName = null;

	private Button mRecordButton = null;
	private MediaRecorder mRecorder = null;

	private Button mPlayButton = null;
	private MediaPlayer mPlayer = null;

	private Button mReturnButton = null;
	private boolean mStartPlaying = false;
	private boolean mStartRecording = false;
	

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
			Log.e(LOG_TAG, "prepare() failed in starting to play");
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
			Log.e(LOG_TAG, "prepare() failed in starting to record");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Bundle extras = getIntent().getExtras();
		Log.v("xinxin***",
				"filename to store audio :" + extras.getString("audioFilename"));
		mFileName = extras.getString("audioFilename");

		setContentView(R.layout.recorder);
		
		
		mRecordButton = (Button) findViewById(R.id.recorder_record);
		mRecordButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mStartRecording = !mStartRecording;
				onRecord(mStartRecording);
				if (mStartRecording) {
					mRecordButton.setText("Stop recording");
					mPlayButton.setEnabled(false);
				} else {
					mRecordButton.setText("Record");
					mPlayButton.setEnabled(true);
				}
			}
		});

		mPlayButton = (Button) findViewById(R.id.recorder_play);
		mPlayButton.setEnabled(false);
		mPlayButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mStartPlaying = !mStartPlaying;
				onPlay(mStartPlaying);
				if (mStartPlaying) {
					mPlayButton.setText("Stop playing");
					mRecordButton.setEnabled(false);
				} else {
					mPlayButton.setText("Play");
					mRecordButton.setEnabled(true);
				}
	
			}
		});

		mReturnButton = (Button) findViewById(R.id.recorder_return) ;
		mReturnButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
		// LinearLayout ll = new LinearLayout(this);
		// mRecordButton = new RecordButton(this);
		// ll.addView(mRecordButton,
		// new LinearLayout.LayoutParams(
		// ViewGroup.LayoutParams.WRAP_CONTENT,
		// ViewGroup.LayoutParams.WRAP_CONTENT,
		// 0));
		// mPlayButton = new PlayButton(this);
		// ll.addView(mPlayButton,
		// new LinearLayout.LayoutParams(
		// ViewGroup.LayoutParams.WRAP_CONTENT,
		// ViewGroup.LayoutParams.WRAP_CONTENT,
		// 0));
		// setContentView(ll);
	}

//	public void onPause() {
//	super.onPause();
//	if (mRecorder != null) {
//		mRecorder.release();
//		mRecorder = null;
//	}
//
//	if (mPlayer != null) {
//		mPlayer.release();
//		mPlayer = null;
//	}
//}


	// public AudioRecorder() {
	// mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
	// mFileName += "/audiorecordtest.3gp";
	// mFileName +=
	// "/anymemo/voice/5000_Collegiate_Words_SAT_Volcabulary.db/abase.3gp";
	// }

	

//	class RecordButton extends Button {
//		boolean mStartRecording = true;
//
//		OnClickListener clicker = new OnClickListener() {
//			public void onClick(View v) {
//				onRecord(mStartRecording);
//				if (mStartRecording) {
//					setText("Stop recording");
//				} else {
//					setText("Start recording");
//				}
//				mStartRecording = !mStartRecording;
//			}
//		};
//
//		public RecordButton(Context ctx) {
//			super(ctx);
//			setText("Start recording");
//			setOnClickListener(clicker);
//		}
//	}
//
//	class PlayButton extends Button {
//		boolean mStartPlaying = true;
//
//		OnClickListener clicker = new OnClickListener() {
//			public void onClick(View v) {
//				onPlay(mStartPlaying);
//				if (mStartPlaying) {
//					setText("Stop playing");
//				} else {
//					setText("Start playing");
//				}
//				mStartPlaying = !mStartPlaying;
//			}
//		};
//
//		public PlayButton(Context ctx) {
//			super(ctx);
//			setText("Start playing");
//			setOnClickListener(clicker);
//		}
//	}
	
}
