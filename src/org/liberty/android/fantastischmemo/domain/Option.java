package org.liberty.android.fantastischmemo.domain;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

public class Option {

    SharedPreferences settings;
    SharedPreferences.Editor editor;

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

}
