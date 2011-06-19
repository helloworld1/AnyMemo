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
package org.liberty.android.fantastischmemo.cardscreen;

import org.liberty.android.fantastischmemo.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.List;

import android.content.SharedPreferences;
import android.content.Context;
import android.preference.PreferenceManager;
import android.database.SQLException;
import android.os.Environment;

/* Manage the database and globalwise settings */
public class SettingManager{
    public final static String TAG = "org.liberty.android.fantastischmemo.SettingManager";
    private Context mContext;
    private DatabaseHelper dbHelper = null;
    private SharedPreferences settings;


    private boolean enableThirdPartyArabic = true;
	private double questionFontSize = 23.5;
	private double answerFontSize = 23.5;
    private Alignment questionAlign = Alignment.CENTER;
    private Alignment answerAlign = Alignment.CENTER;
	private String questionLocale = "US";
	private String answerLocale = "US";
    private long htmlDisplay = CardField.QUESTION | CardField.ANSWER | CardField.NOTE;
	private String qaRatio = "50%";
    private ButtonStyle btnStyle = ButtonStyle.ANYMEMO;
    private SpeechControlMethod speechCtl = SpeechControlMethod.TAP;
    private boolean copyClipboard = true;
    private String questionTypeface = "";
    private String answerTypeface = "";
    private List<String> filters;
    private String audioLocation = "";
    private int learningQueueSize = 10;
    private boolean shufflingCards = false;
    private int linebreakConversion = 0;
    private boolean volumeKeyShortcut = false;
    private boolean fullscreenMode = false;
    private int screenHeight = 320;
    private int screenWidth = 480;
    private CardStyle cardStyle = CardStyle.SINGLE_SIDED;
    private DictApp dictApp = DictApp.COLORDICT;
    /* The colors for various elements
     * null means default color */
    protected List<Integer> colors = null;

    /* Use bitwise op */
    private long cardField1 = CardField.QUESTION;
    private long cardField2 = CardField.ANSWER;

    public SettingManager(Context context){
        mContext = context;
		settings = PreferenceManager.getDefaultSharedPreferences(context);
        loadGlobalOptions();

    }

    public SettingManager(Context context, String dbPath, String dbName) throws SQLException{
        this(context);
        dbHelper = new DatabaseHelper(context, dbPath, dbName);
        loadDBSettings();
    }

    public boolean getEnableThirdPartyArabic(){
        return enableThirdPartyArabic;
    }

    public CardStyle getCardStyle(){
        return cardStyle;
    }

    public float getQuestionFontSize(){
        return (float)questionFontSize;
    }

    public float getAnswerFontSize(){
        return (float)answerFontSize;
    }

    public Alignment getQuestionAlign(){
        return questionAlign;
    }
    public Alignment getAnswerAlign(){
        return answerAlign;
    }

    public long getHtmlDisplay(){
        return htmlDisplay;
    }

    public float getQARatio(){
		float qRatio = Float.valueOf(qaRatio.substring(0, qaRatio.length() - 1));
        return qRatio;
    }

    public ButtonStyle getButtonStyle(){
        return btnStyle;
    }

    public SpeechControlMethod getSpeechControlMethod(){
        return speechCtl;
    }

    public Locale getQuestionAudioLocale(){
        if(questionLocale.length() == 2){
            if(questionLocale.equals("US")){
                return Locale.US;
            }
            else if(questionLocale.equals("UK")){
                return Locale.UK;
            }
            else{
                return new Locale(questionLocale.toLowerCase());
            }
        }
        else{
            return null;
        }
    }

    public long getCardField1(){
        return cardField1 == 0 ? CardField.QUESTION : cardField1;
    }
    public long getCardField2(){
        return cardField2 == 0 ? CardField.ANSWER : cardField2;
    }

    public Locale getAnswerAudioLocale(){
        if(answerLocale.length() == 2){
            if(answerLocale.equals("US")){
                return Locale.US;
            }
            else if(answerLocale.equals("UK")){
                return Locale.UK;
            }
            else{
                return new Locale(answerLocale.toLowerCase());
            }
        }
        else{
            return null;
        }
    }

    public boolean getQuestionUserAudio(){
		if(questionLocale.equals("1") || questionLocale.equals("User Audio")){
            return true;
		}
        else{
            return false;
        }
    }

    public boolean getAnswerUserAudio(){
        if(answerLocale.equals("1") || answerLocale.equals("User Audio")){
            return true;
		}
        else{
            return false;
        }
    }

    public boolean getCopyClipboard(){
        return copyClipboard;
    }

    public String getQuestionTypeface(){
        return questionTypeface;
    }

    public String getAnswerTypeface(){
        return answerTypeface;
    }

    public List<String> getFilters(){
        return filters;
    }

    public List<Integer> getColors(){
        return colors;
    }

    public String getAudioLocation(){
        return audioLocation;
    }

    public int getLearningQueueSize(){
        return learningQueueSize;
    }

    public boolean getShufflingCards(){
        return shufflingCards;
    }

    public boolean getVolumeKeyShortcut(){
        return volumeKeyShortcut;
    }

    public boolean getLinebreakConversion(){
        return linebreakConversion == 0 ? false : true;
    }


    public boolean getFullscreenMode(){
        return fullscreenMode;
    }

    public String getDbName(){
        if(dbHelper != null){
            return dbHelper.getDbName();
        }
        else{
            throw new IllegalStateException();
        }
    }

    public void close(){
        if(dbHelper != null){
            try{
                dbHelper.close();
            }
            catch(Exception e){
            }
        }
    }

    public String getDbPath(){
        if(dbHelper != null){
            return dbHelper.getDbPath();
        }
        else{
            throw new IllegalStateException();
        }
    }

    public int getScreenHeight(){
        return screenHeight;
    }

    public int getScreenWidth(){
        return screenWidth;
    }

    public DictApp getDictApp(){
        return dictApp;
    }
    


    private void loadGlobalOptions(){
        speechCtl = SpeechControlMethod.parse(settings.getString("speech_ctl", mContext.getResources().getStringArray(R.array.speech_ctl_list)[0]));
        btnStyle = ButtonStyle.parse(settings.getString("button_style", mContext.getResources().getStringArray(R.array.button_style_list)[0]));
        copyClipboard = settings.getBoolean("copyclipboard", true);

        volumeKeyShortcut = settings.getBoolean("enable_volume_key", false);
        fullscreenMode = settings.getBoolean("fullscreen_mode", false);
        screenHeight = settings.getInt("screen_height", 320);
        screenWidth = settings.getInt("screen_width", 480);
        enableThirdPartyArabic = settings.getBoolean("enable_third_party_arabic", true);
        dictApp = DictApp.parse(settings.getString("dict_app", mContext.getResources().getStringArray(R.array.dict_list_values)[0]));


        /* Load learning queue size from the preference */
        try{
            String size = settings.getString("learning_queue_size", "10");
            int tmpSize = Integer.parseInt(size);
            if(tmpSize > 0){
                learningQueueSize = tmpSize;
            }
            else{
                learningQueueSize = 10;
            }
        }
        catch(Exception e){
            learningQueueSize = 10;
        }
        shufflingCards = settings.getBoolean("shuffling_cards", false);
    }

	private void loadDBSettings(){
        /* Set a default audio location */
        audioLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + mContext.getString(R.string.default_audio_dir);
		/* Here is the global settings from the preferences */
		
		HashMap<String, String> hm = dbHelper.getSettings();
		Set<Map.Entry<String, String>> set = hm.entrySet();
		Iterator<Map.Entry<String, String> > i = set.iterator();
		while(i.hasNext()){
			Map.Entry<String, String> me = i.next();
			if((me.getKey()).equals("question_font_size")){
				this.questionFontSize = new Double(me.getValue());
			}
			if(me.getKey().equals("answer_font_size")){
				this.answerFontSize = new Double(me.getValue());
			}
			if(me.getKey().equals("question_align")){
				this.questionAlign = Alignment.parse(me.getValue());
			}
			if(me.getKey().equals("answer_align")){
				this.answerAlign = Alignment.parse(me.getValue());
			}
			if(me.getKey().equals("question_locale")){
				this.questionLocale = me.getValue();
			}
			if(me.getKey().equals("answer_locale")){
				this.answerLocale = me.getValue();
			}
			if(me.getKey().equals("html_display")){
                if(AMUtil.isInteger(me.getValue())){
                    htmlDisplay = Long.parseLong(me.getValue());
                }
			}
			if(me.getKey().equals("linebreak_conversion")){
                if(AMUtil.isInteger(me.getValue())){
                    linebreakConversion = Integer.parseInt(me.getValue());
                }
			}
			if(me.getKey().equals("ratio")){
				this.qaRatio = me.getValue();
			}
			if(me.getKey().equals("question_typeface")){
                this.questionTypeface = me.getValue();
            }
			if(me.getKey().equals("answer_typeface")){
                this.answerTypeface = me.getValue();
            }
            if(me.getKey().equals("colors")){
                String colorString = me.getValue();
                if(colorString.equals("")){
                    colors = null;
                }
                else{
                    colors = new ArrayList<Integer>();
                    // Log.v(TAG, "Color String: " + colorString);
                    String[] ca = colorString.split(" ");
                    for(int j = 0; j < ca.length; j++){
                        colors.add(j, Integer.parseInt(ca[j]));
                    }
                }
            }
            if(me.getKey().toString().equals("audio_location")){
                String loc = me.getValue().toString();
                if(!loc.equals("")){
                    audioLocation = loc;
                }
            }
            if(me.getKey().toString().equals("card_style")){
                cardStyle =  CardStyle.parse(me.getValue().toString());
            }

            if(me.getKey().toString().equals("card_field_1")){
                String s =  me.getValue().toString();
                long v = Long.parseLong(s);
                cardField1 = v;
            }
            if(me.getKey().toString().equals("card_field_2")){
                String s =  me.getValue().toString();
                long v = Long.parseLong(s);
                cardField2 = v;
            }

		}
        filters = dbHelper.getRecentFilters();
	}


    public static enum Alignment{
        LEFT,
        RIGHT,
        CENTER;

        public static Alignment parse(String a){
            if(a.equals("0") || a.equals("left")){
                return LEFT;
            }
            else if(a.equals("2") || a.equals("right")){
                return RIGHT;
            }
            else{
                return CENTER;
            }
        }
    }

    public static enum ButtonStyle{
        ANYMEMO,
        MNEMOSYNE,
        ANKI;

        public static ButtonStyle parse(String a){
            if(a.equals("MNEMOSYNE")){
                return MNEMOSYNE;
            }
            else if(a.equals("ANKI")){
                return ANKI;
            }
            else{
                return ANYMEMO;
            }

        }
    }

    public static enum SpeechControlMethod{
        MANUAL,
        TAP,
        AUTO,
        AUTOTAP;

        public static SpeechControlMethod parse(String a){
            if(a.equals("MANUAL")){
                return MANUAL;
            }
            else if(a.equals("AUTO")){
                return AUTO;
            }
            else if(a.equals("AUTOTAP")){
                return AUTOTAP;
            }
            else{
                return TAP;
            }

        }
    }

    public static enum CardStyle{
        SINGLE_SIDED,
        DOUBLE_SIDED;
        public static CardStyle parse(String a){
            if(a.equals("0") || a.equals("single_sided")){
                return SINGLE_SIDED;
            }
            else if(a.equals("1") || a.equals("double_sided")){
                return DOUBLE_SIDED;
            }
            else{
                return SINGLE_SIDED;
            }
        }
    }

    public static enum DictApp{
        COLORDICT,
        FORA;
        public static DictApp parse(String a){
            System.out.println("Parse string: "+ a);
            if(a.equals("FORA")){
                return FORA;
            }
            else{
                return COLORDICT;
            }
        }
    }


    /* Use bitfield opertion on this class */
    public static class CardField{
        public static final long QUESTION = 1;
        public static final long ANSWER = 2;
        public static final long CATEGORY = 4;
        public static final long NOTE = 8;
    }

}

