package org.color;

import org.liberty.android.fantastischmemo.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressWarnings("all")
public class ColorDialog extends AlertDialog implements OnSeekBarChangeListener, OnClickListener {

    public interface OnClickListener {
        public void onClick(View view, int color);
    }

	private SeekBar mHue;
	private SeekBar mSaturation;
	private SeekBar mValue;
	private ColorDialog.OnClickListener mListener;
	private int mColor;
	private View mView;
	private GradientDrawable mPreviewDrawable;

	public ColorDialog(Context context, View view, int color, OnClickListener listener) {
		super(context);
		mView = view;
		mListener = listener;

		Resources res = context.getResources();
		setTitle(res.getText(R.string.color_dialog_title));
		setButton(BUTTON_POSITIVE, res.getText(android.R.string.yes), this);
		setButton(BUTTON_NEGATIVE, res.getText(android.R.string.cancel), this);
		View root = LayoutInflater.from(context).inflate(R.layout.color_picker, null);
		setView(root);

		View preview = root.findViewById(R.id.preview);
		mPreviewDrawable = new GradientDrawable();
		// 2 pix more than color_picker_frame's radius
		mPreviewDrawable.setCornerRadius(7);
		Drawable[] layers = {
			mPreviewDrawable,
			res.getDrawable(R.drawable.color_picker_frame),
		};
		preview.setBackgroundDrawable(new LayerDrawable(layers));

		mHue = (SeekBar) root.findViewById(R.id.hue);
		mSaturation = (SeekBar) root.findViewById(R.id.saturation);
		mValue = (SeekBar) root.findViewById(R.id.value);

		mColor = color;
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		int h = (int) (hsv[0] * mHue.getMax() / 360);
		int s = (int) (hsv[1] * mSaturation.getMax());
		int v = (int) (hsv[2] * mValue.getMax());
		setupSeekBar(mHue, R.string.color_h, h, res);
		setupSeekBar(mSaturation, R.string.color_s, s, res);
		setupSeekBar(mValue, R.string.color_v, v, res);

		updatePreview(color);
	}

	private void setupSeekBar(SeekBar seekBar, int id, int value, Resources res) {
		seekBar.setProgressDrawable(new TextSeekBarDrawable(res, id, value < seekBar.getMax() / 2));
		seekBar.setProgress(value);
		seekBar.setOnSeekBarChangeListener(this);
	}

	private void update() {
		float[] hsv = {
			360 * mHue.getProgress() / (float) mHue.getMax(),
			mSaturation.getProgress() / (float) mSaturation.getMax(),
			mValue.getProgress() / (float) mValue.getMax(),
		};
		mColor = Color.HSVToColor(hsv);
		updatePreview(mColor);
	}

	private void updatePreview(int color) {
		mPreviewDrawable.setColor(color);
		mPreviewDrawable.invalidateSelf();
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		update();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			mListener.onClick(mView, mColor);
		}
		dismiss();
	}

	static final int[] STATE_FOCUSED = {android.R.attr.state_focused};
	static final int[] STATE_PRESSED = {android.R.attr.state_pressed};

	class TextSeekBarDrawable extends Drawable implements Runnable {

		private static final String TAG = "TextSeekBarDrawable";
		private static final long DELAY = 25;
		private String mText;
		private Drawable mProgress;
		private Paint mPaint;
		private Paint mOutlinePaint;
		private float mTextWidth;
		private boolean mActive;
		private float mTextX;
		private float mDelta;

		public TextSeekBarDrawable(Resources res, int id, boolean labelOnRight) {
			mText = res.getString(id);
			mProgress = res.getDrawable(android.R.drawable.progress_horizontal);
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setTypeface(Typeface.DEFAULT_BOLD);
			mPaint.setTextSize(16);
			mPaint.setColor(0xff000000);
			mOutlinePaint = new Paint(mPaint);
			mOutlinePaint.setStyle(Style.STROKE);
			mOutlinePaint.setStrokeWidth(3);
			mOutlinePaint.setColor(0xbbffc300);
			mOutlinePaint.setMaskFilter(new BlurMaskFilter(1, Blur.NORMAL));
			mTextWidth = mOutlinePaint.measureText(mText);
			mTextX = labelOnRight? 1 : 0;
		}

		@Override
		protected void onBoundsChange(Rect bounds) {
			mProgress.setBounds(bounds);
		}

		@Override
		protected boolean onStateChange(int[] state) {
			mActive = StateSet.stateSetMatches(STATE_FOCUSED, state) | StateSet.stateSetMatches(STATE_PRESSED, state);
			invalidateSelf();
			return false;
		}

		@Override
		public boolean isStateful() {
			return true;
		}

		@Override
		protected boolean onLevelChange(int level) {
//			Log.d(TAG, "onLevelChange " + level);
			if (level < 4000 && mDelta <= 0) {
				mDelta = 0.05f;
//				Log.d(TAG, "onLevelChange scheduleSelf ++");
				scheduleSelf(this, SystemClock.uptimeMillis() + DELAY);
			} else
			if (level > 6000 && mDelta >= 0) {
//				Log.d(TAG, "onLevelChange scheduleSelf --");
				mDelta = -0.05f;
				scheduleSelf(this, SystemClock.uptimeMillis() + DELAY);
			}
			return mProgress.setLevel(level);
		}

		@Override
		public void draw(Canvas canvas) {
			mProgress.draw(canvas);
			Rect bounds = getBounds();

			float x = 6 + mTextX * (bounds.width() - mTextWidth - 6 - 6);
			float y = (bounds.height() + mPaint.getTextSize()) / 2;
			mOutlinePaint.setAlpha(mActive? 255 : 255 / 2);
			mPaint.setAlpha(mActive? 255 : 255 / 2);
			canvas.drawText(mText, x, y, mOutlinePaint);
			canvas.drawText(mText, x, y, mPaint);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
		}

		public void run() {
			mTextX += mDelta;
			if (mTextX >= 1) {
				mTextX = 1;
				mDelta = 0;
			} else
			if (mTextX <= 0) {
				mTextX = 0;
				mDelta = 0;
			} else {
				scheduleSelf(this, SystemClock.uptimeMillis() + DELAY);
			}
			invalidateSelf();
//			Log.d(TAG, "run " + mTextX + " " + SystemClock.uptimeMillis());
		}
	}
}
