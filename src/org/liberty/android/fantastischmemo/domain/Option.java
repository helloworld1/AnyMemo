package org.liberty.android.fantastischmemo.domain;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

public class Option {

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    public SharedPreferences getSettings() {
    	return this.settings;
    }
    
    public SharedPreferences.Editor getEditor() {
    	return this.editor;
    }
    
    public Option (Context context) {
    	settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
    }

    public boolean getEnableArabicEngine() {
        return settings.getBoolean("enable_third_party_arabic", true);
    }

    public void setEnableArabicEngine(boolean enable) {
        editor.putBoolean("enable_third_party_arabic", enable);
        editor.commit();
    }

    public ButtonStyle getButtonStyle() {
        return ButtonStyle.parse(settings.getString("button_style", "ANYMEMO"));
    }

    public void setButtonStyle(ButtonStyle style) {
        editor.putString("button_style", style.toString());
        editor.commit();
    }

    public boolean getVolumeKeyShortcut() {
        return settings.getBoolean("enable_volume_key", false);
    }

    public void setVolumeKeyShortcut(boolean enable) {
        editor.putBoolean("enable_volume_key", enable);
        editor.commit();
    }

    public boolean getCopyClipboard() {
        return settings.getBoolean("copyclipboard", true);
    }

    public void setCopyClipboard(boolean enable) {
        editor.putBoolean("copyclipboard", enable);
        editor.commit();
    }

	public DictApp getDictApp() {
        return DictApp.parse(settings.getString("dict_app", "FORA"));
	}

	public ShuffleType getShuffleType() {
        if (settings.getBoolean("shuffling_cards", false)) {
            return ShuffleType.LOCAL;
        } else {
            return ShuffleType.NONE;
        }
	}

	public SpeakingType getSpeakingType() {
        return SpeakingType.parse(settings.getString("speech_ctl", "TAP"));
	}

    public int getSavedId(String prefix, String key, int defaultValue) {
        return settings.getInt(prefix + key, defaultValue);
    }

    public void setSavedId(String prefix, String key, int value) {
        editor.putInt(prefix + key, value);
        editor.commit();
    }
    
    public void setEditorString(String key, String value) {
    	editor.putString(key, value);
    	editor.commit();
    }
    
    public int getQueueSize() {
        String size = settings.getString("learning_queue_size", "10");
        int tmpSize = Integer.parseInt(size);
        if(tmpSize > 0){
            return tmpSize;
        } else {
            return 10;
        }
    }

    public String getSettingString(String key, String value) {
    	return settings.getString(key, value);
    }
    
    public static enum ButtonStyle {
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

    public static enum DictApp {
        COLORDICT,
        FORA;
        public static DictApp parse(String a){
            if(a.equals("COLORDICT")){
                return COLORDICT;
            } else{
                return FORA;
            }
        }
    }

    public static enum ShuffleType {
        NONE,
        LOCAL
    }

    public static enum SpeakingType {
        MANUAL,
        TAP,
        AUTO,
        AUTOTAP;

        public static SpeakingType parse(String a){
            if(a.equals("MANUAL")){
                return MANUAL;
            } else if(a.equals("AUTO")){
                return AUTO;
            } else if(a.equals("AUTOTAP")){
                return AUTOTAP;
            } else{
                return TAP;
            }
        }
        
    }

}
