/*
Copyright (C) 2010 Haowen Ning

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

import java.util.EnumSet;

import org.amr.arabic.ArabicUtilities;

import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.utils.AMUtil;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.File;
import java.net.URL;

import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.util.Log;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Matrix;

import android.text.Html.TagHandler;
import android.text.Html.ImageGetter;

public class SingleSidedCardDisplay implements FlashcardDisplay, TagHandler, ImageGetter{
    private final static String TAG = "org.liberty.android.fantastischmemo.ui.SingleSidedCardDisplay";
    private Context mContext;
    private View flashcardView;
    LinearLayout questionLayout;
    LinearLayout answerLayout;
    LinearLayout separator;
    TextView questionView;
    TextView answerView;
    Setting setting;
    Option option;
    boolean answerShown = true;
    String dbPath;
    int screenWidth;
    int screenHeight;

    public SingleSidedCardDisplay(Context context){
        throw new UnsupportedOperationException();
    }

    public SingleSidedCardDisplay(Context context, String dbPath, Setting setting, Option option) {
        mContext = context;
        this.setting = setting;
        this.option = option;
        this.dbPath = dbPath;
        LayoutInflater factory = LayoutInflater.from(mContext);
        flashcardView= factory.inflate(R.layout.flashcard_screen, null);
        questionLayout = (LinearLayout)flashcardView.findViewById(R.id.layout_question);
        answerLayout = (LinearLayout)flashcardView.findViewById(R.id.layout_answer);
        questionView = (TextView)flashcardView.findViewById(R.id.question);
        answerView = (TextView)flashcardView.findViewById(R.id.answer);
        separator = (LinearLayout)flashcardView.findViewById(R.id.horizontalLine);
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

    }


    public View getView(){
        return flashcardView;
    }

    public View getQuestionView(){
        return questionView;
    }
    
    public View getAnswerView(){
        return answerView;
    }

    /* Update view with answer shown */
    public void updateView(Card card){
        updateView(card, true);
    }

	public void updateView(Card card, boolean showAnswer) {
        if(card == null){
            return;
        }
        answerShown = showAnswer;
        float qaRatio = setting.getQaRatio();
        setQARatio(qaRatio);
        setScreenColor();
        displayQA(card);
        if(showAnswer == false){
            answerView.setText(R.string.memo_show_answer);
        }
	}

    public void setQuestionLayoutClickListener(View.OnClickListener l){
        questionLayout.setOnClickListener(l);
    }

    public void setAnswerLayoutClickListener(View.OnClickListener l){
        answerLayout.setOnClickListener(l);
    }

    public void setQuestionTextClickListener(View.OnClickListener l){
        questionView.setOnClickListener(l);
    }

    public void setAnswerTextClickListener(View.OnClickListener l){
        answerView.setOnClickListener(l);
    }

    public void setQuestionLayoutLongClickListener(View.OnLongClickListener l){
        questionLayout.setOnLongClickListener(l);
        questionView.setOnLongClickListener(l);
    }

    public void setAnswerLayoutLongClickListener(View.OnLongClickListener l){
        answerLayout.setOnLongClickListener(l);
        answerView.setOnLongClickListener(l);
    }

    public void setScreenOnTouchListener(View.OnTouchListener l){
        flashcardView.setOnTouchListener(l);
        questionView.setOnTouchListener(l);
        answerView.setOnTouchListener(l);
    }

    public boolean isAnswerShown(){
        return answerShown;
    }



	protected void displayQA(Card card) {
		/* Display question and answer according to item */
        
        String questionTypeface = setting.getQuestionFont();
        String answerTypeface = setting.getAnswerFont();
        boolean enableThirdPartyArabic = option.getEnableArabicEngine();
        Setting.Align questionAlign = setting.getQuestionTextAlign();
        Setting.Align answerAlign = setting.getAnswerTextAlign();
        EnumSet<Setting.CardField> htmlDisplay = setting.getDisplayInHTMLEnum();

        /* Set the typeface of question an d answer */
        if(questionTypeface != null && !questionTypeface.equals("")){
            try{
                Typeface qt = Typeface.createFromFile(questionTypeface);
                questionView.setTypeface(qt);
            }
            catch(Exception e){
                Log.e(TAG, "Typeface error when setting question font", e);
            }

        }
        if(answerTypeface != null && !answerTypeface.equals("")){
            try{
                Typeface at = Typeface.createFromFile(answerTypeface);
                answerView.setTypeface(at);
            }
            catch(Exception e){
                Log.e(TAG, "Typeface error when setting answer font", e);
            }

        }

        String itemQuestion = card.getQuestion();
        String itemAnswer = card.getAnswer();
        String itemCategory = card.getCategory().getName();
        String itemNote = card.getNote();

        if(enableThirdPartyArabic){
            itemQuestion = ArabicUtilities.reshape(itemQuestion);
            itemAnswer = ArabicUtilities.reshape(itemAnswer);
            itemCategory = ArabicUtilities.reshape(itemCategory);
            itemNote = ArabicUtilities.reshape(itemNote);
        }

        // For question field (field1)
        SpannableStringBuilder sq = new SpannableStringBuilder();

        // For answer field  (field2)
        SpannableStringBuilder sa = new SpannableStringBuilder();
        /* Show the field that is enabled in settings */
        EnumSet<Setting.CardField> field1 = setting.getQuestionFieldEnum();
        EnumSet<Setting.CardField> field2 = setting.getAnswerFieldEnum();
        Log.v(TAG, "Field1: " + field1);
        Log.v(TAG, "Field2: " + field2);
        Log.v(TAG, "html: " + htmlDisplay);

        /* Iterate all fields */
        for (Setting.CardField cf : Setting.CardField.values()) {
            String str = "";
            if (cf == Setting.CardField.QUESTION) {
                str = card.getQuestion();
            } else if (cf == Setting.CardField.ANSWER) {
                str = card.getAnswer();
            } else if (cf == Setting.CardField.NOTE) {
                str = card.getNote();
            } else {
                throw new AssertionError("This is a bug! New CardField enum has been added but the display field haven't been nupdated");
            }
            SpannableStringBuilder buffer = new SpannableStringBuilder();

            /* Automatic check HTML */
            if(AMUtil.isHTML(str) && (htmlDisplay.contains(cf))) {
                if(sq.length() != 0){
                    buffer.append(Html.fromHtml("<br /><br />", this, this));
                }
                if(setting.getHtmlLineBreakConversion() == true) {
                    String s = str.replace("\n", "<br />");
                    buffer.append(Html.fromHtml(s, this, this));
                } else{
                    buffer.append(Html.fromHtml(str, this, this));
                }
            } else{
                if(sq.length() != 0){
                    buffer.append("\n\n");
                }
                buffer.append(str);
            }
            if (field1.contains(cf)) {
                sq.append(buffer);
            }
            if (field2.contains(cf)) {
                sa.append(buffer);
            }

        }
        questionView.setText(sq);
        answerView.setText(sa);

        /* Here is tricky to set up the alignment of the text */
		if(questionAlign == Setting.Align.CENTER){
			questionView.setGravity(Gravity.CENTER);
			questionLayout.setGravity(Gravity.CENTER);
		}
		else if(questionAlign == Setting.Align.RIGHT){
			questionView.setGravity(Gravity.RIGHT);
			questionLayout.setGravity(Gravity.NO_GRAVITY);
		}
		else{
			questionView.setGravity(Gravity.LEFT);
			questionLayout.setGravity(Gravity.NO_GRAVITY);
		}
		if(answerAlign == Setting.Align.CENTER){
			answerView.setGravity(Gravity.CENTER);
			answerLayout.setGravity(Gravity.CENTER);
		} 
        else if(answerAlign == Setting.Align.RIGHT){
			answerView.setGravity(Gravity.RIGHT);
			answerLayout.setGravity(Gravity.NO_GRAVITY);
			
		}
		else{
			answerView.setGravity(Gravity.LEFT);
			answerLayout.setGravity(Gravity.NO_GRAVITY);
		}
		questionView.setTextSize(setting.getQuestionFontSize());
		answerView.setTextSize(setting.getAnswerFontSize());
	}


    
    private void setScreenColor(){
        /* Set both text and the background color */
            questionView.setTextColor(setting.getQuestionTextColor());
            answerView.setTextColor(setting.getAnswerTextColor());
            questionLayout.setBackgroundColor(setting.getQuestionBackgroundColor());
            answerLayout.setBackgroundColor(setting.getAnswerBackgroundColor());
            separator.setBackgroundColor(setting.getSeparatorColor());
    }

    void setQARatio(float qRatio){

        if(qRatio > 99.0f){
            answerLayout.setVisibility(View.GONE);
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
        }
        else if(qRatio < 1.0f){
            questionLayout.setVisibility(View.GONE);
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
        }
        else{

            float aRatio = 100.0f - qRatio;
            qRatio /= 50.0;
            aRatio /= 50.0;
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, qRatio));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, aRatio));
        }
    }

    @Override
    public Drawable getDrawable(String source){
        Log.v(TAG, "Source: " + source);
        String dbName = AMUtil.getFilenameFromPath(dbPath);
        try{
            String[] paths = {
                /* Relative path */
                "" + dbName + "/" + source,
                /* Try the image in /sdcard/anymemo/images/dbname/myimg.png */
                AMEnv.DEFAULT_IMAGE_PATH + dbName + "/" + source,
                /* Try the image in /sdcard/anymemo/images/myimg.png */
                AMEnv.DEFAULT_IMAGE_PATH + source};
            Bitmap orngBitmap = null;
            for(String path : paths){
                Log.v(TAG, "Try path: " + path);
                if(new File(path).exists()){
                    orngBitmap = BitmapFactory.decodeFile(path);
                    break;
                }
            }
            /* Try the image from internet */
            if(orngBitmap == null){
                InputStream is = (InputStream)new URL(source).getContent();
                orngBitmap = BitmapFactory.decodeStream(is);
            }

            int width = orngBitmap.getWidth();
            int height = orngBitmap.getHeight();
            int scaledWidth = width;
            int scaledHeight = height;
            float scaleFactor = ((float)screenWidth) / width;
            Matrix matrix = new Matrix();
            if(scaleFactor < 1.0f){
                matrix.postScale(scaleFactor, scaleFactor);
                scaledWidth = (int)(width * scaleFactor);
                scaledHeight = (int)(height * scaleFactor);
            }
            Bitmap resizedBitmap = Bitmap.createBitmap(orngBitmap, 0, 0, width, height, matrix, true);
            BitmapDrawable d = new BitmapDrawable(resizedBitmap);
            //d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            d.setBounds(0, 0, scaledWidth, scaledHeight);
            return d; 
        }
        catch(Exception e){
            Log.e(TAG, "getDrawable() Image handling error", e);
        }

        /* Fallback, display default image */
        Drawable d = mContext.getResources().getDrawable(R.drawable.picture);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        return d;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader){
        return;
    }
}
