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

import org.amr.arabic.ArabicUtilities;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.gesture.GestureOverlayView;
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.util.Log;
import android.os.SystemClock;
import android.os.Environment;
import android.graphics.Typeface;
import android.text.Html.TagHandler;
import android.text.Html.ImageGetter;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;

/* Manage the database and globalwise settings */
public class SettingManager{
    public final static String TAG = "org.liberty.android.fantastischmemo.SettingManager";
    private Context mContext;
    private DatabaseHelper dbHelper = null;
    private SharedPreferences settings;


    private boolean enableThirdPartyArabic = true;
	private double questionFontSize = 23.5;
	private double answerFontSize = 23.5;
	private String questionAlign = "center";
	private String answerAlign = "center";
	private String questionLocale = "US";
	private String answerLocale = "US";
	private String htmlDisplay = "none";
	private String qaRatio = "50%";
    private String btnStyle = "";
    private String speechCtl = "";
	private boolean questionUserAudio = false;
	private boolean answerUserAudio = false;
    private boolean copyClipboard = true;
    private String questionTypeface = "";
    private String answerTypeface = "";
    private ArrayList<String> filters;
    private String audioLocation = "";
    private int learningQueueSize = 10;
    private boolean shufflingCards = false;
    private boolean volumeKeyShortcut = false;
    private boolean enableTTSExtended = false;
    private boolean fullscreenMode = false;
    /* The colors for various elements
     * null means default color */
    protected ArrayList<Integer> colors = null;

    public SettingManager(Context context){
        mContext = context;
		settings = PreferenceManager.getDefaultSharedPreferences(this);
        loadGlobalOptions();
    }

    public SettingManager(Context context, String dbPath, String dbName) throws SQLException{
        this.SettingManager(context);
        dbHelper = new DatabaseHelper(context, dbPath, dbName);
        loadDBSettings();
    }

    public boolean getEnableThirdPartyArabic(){
        return enableThirdPartyArabic;
    }

    public double getQuestionFontSize(){
        double questionFontSize;
    }

    public float getAnswerFontSize(){
        return answerFontSize;
    }

    public Alignment getQuestionAlign(){
        return Alignment.parse(questionAlign);
    }
    public Alignment getAnswerAlign(){
        return Alignment.parse(answerAlign);
    }

    public HTMLDisplayType getHtmlDisplay(){
        return HTMLDisplayType.parse(htmlDisplay);
    }

    public float getQARatio(){
		float qRatio = Float.valueOf(qaRatio.substring(0, qaRatio.length() - 1));
        return qRatio;
    }

    public ButtonStyle getButtonStyle(){
        return ButtonStyle.parse(btnStyle);
    }

    public SpeechControlMethod getSpeechControlMethod((){
        return SpeechControlMethod.parse(speechCtl);
    }

    public boolean getQuestionUserAudio(){
		questionLocale.equals("User Audio"){
            return true
		}
        else{
            return false;
        }
    }

    public boolean getAnswerUserAudio(){
        answerLocale.equals("User Audio"){
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

    public ArrayList<String> getFilters(){
        return filters;
    }

    public ArrayList<String> getColors(){
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

    public boolean getEnableTTSExtended(){
        return enableTTSExtended;
    }

    public boolean getFullscreenMode(){
        return fullscreenMode;
    }

    private void loadGlobalOptions(){
        speechCtl = settings.getString("speech_ctl", getResources().getStringArray(R.array.speech_ctl_list)[0]);
        btnStyle = settings.getString("button_style", getResources().getStringArray(R.array.button_style_list)[0]);
        copyClipboard = settings.getBoolean("copyclipboard", true);

        enableTTSExtended = settings.getBoolean("enable_tts_extended", false);
        volumeKeyShortcut = settings.getBoolean("enable_volume_key", false);
        fullscreenMode = settings.getBoolean("fullscreen_mode", false);
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
        audioLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_audio_dir);
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
				this.questionAlign = me.getValue();
			}
			if(me.getKey().equals("answer_align")){
				this.answerAlign = me.getValue();
			}
			if(me.getKey().equals("question_locale")){
				this.questionLocale = me.getValue();
			}
			if(me.getKey().equals("answer_locale")){
				this.answerLocale = me.getValue();
			}
			if(me.getKey().equals("html_display")){
				this.htmlDisplay = me.getValue();
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

		}
        filters = dbHelper.getRecentFilters();
	}



    public static enum Alignment{
        LEFT,
        RIGHT,
        CENTER;

        public static Alignment parse(String a){
            if(a.equals("left")){
                return LEFT;
            }
            else if(a.equals("right")){
                return RIGHT;
            }
            else{
                return CENTER;
            }
        }
    }

    public static enum HTMLDisplayType{
        BOTH,
        QUESTION,
        ANSWER;

        public static HTMLDisplayType parse(String a){
            if(a.equals("question")){
                return QUESTION;
            }
            else if((a.equals("answer")){
                return ANSWER;
            }
            else{
                return BOTH;
            }
        }
    }

    public static enum ButtonStyle{
        ANYMEMO,
        MNEMOSYNE,
        ANKI;

        public static parse(String a){
            if(a.equals("Mnemosyne")){
                return MNEMOSYNE;
            }
            else if((a.equals("Anki")){
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

        public static parse(String a){
            if(a.startsWith("0")){
                return MANUAL;
            }
            else if((a.startsWith("1")){
                return TAP;
            }
            else if((a.startsWith("2")){
                return AUTO;
            }
            else{
                return AUTOTAP;
            }

        }
    }


}

