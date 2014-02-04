package org.liberty.android.fantastischmemo.domain;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AMPrefKeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Option {

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    @Inject
    public Option (Context context) {
    	settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
    }

    public boolean getEnableArabicEngine() {
        return settings.getBoolean(AMPrefKeys.ENABLE_THIRD_PARTY_ARABIC_KEY, true);
    }

    public void setEnableArabicEngine(boolean enable) {
        editor.putBoolean(AMPrefKeys.ENABLE_THIRD_PARTY_ARABIC_KEY, enable);
        editor.commit();
    }

    public ButtonStyle getButtonStyle() {
        return ButtonStyle.parse(settings.getString(AMPrefKeys.BUTTON_STYLE_KEY, "ANYMEMO"));
    }

    public void setButtonStyle(ButtonStyle style) {
        editor.putString(AMPrefKeys.BUTTON_STYLE_KEY, style.toString());
        editor.commit();
    }

    public boolean getVolumeKeyShortcut() {
        return settings.getBoolean(AMPrefKeys.ENABLE_VOLUME_KEY_KEY, false);
    }

    public void setVolumeKeyShortcut(boolean enable) {
        editor.putBoolean(AMPrefKeys.ENABLE_VOLUME_KEY_KEY, enable);
        editor.commit();
    }

    public CopyToClipboard getCopyClipboard() {
        return CopyToClipboard.valueOf(settings.getString(AMPrefKeys.COPY_CLIPBOARD_KEY, "QUESTION"));
    }

    public void setCopyClipboard(CopyToClipboard copyToClipboard) {
        editor.putString(AMPrefKeys.COPY_CLIPBOARD_KEY, copyToClipboard.toString());
        editor.commit();
    }

	public DictApp getDictApp() {
        return DictApp.parse(settings.getString(AMPrefKeys.DICT_APP_KEY, "COLORDICT"));
	}

	public ShuffleType getShuffleType() {
        if (settings.getBoolean(AMPrefKeys.SHUFFLING_CARDS_KEY, false)) {
            return ShuffleType.LOCAL;
        } else {
            return ShuffleType.NONE;
        }
	}

	public SpeakingType getSpeakingType() {
        return SpeakingType.parse(settings.getString(AMPrefKeys.SPEECH_CONTROL_KEY, "TAP"));
	}

    public int getRecentCount() {
    	return settings.getInt(AMPrefKeys.RECENT_COUNT_KEY, 7);
    }

    public int getQueueSize() {
        String size = settings.getString(AMPrefKeys.LEARN_QUEUE_SIZE_KEY, "10");
        int tmpSize = Integer.parseInt(size);
        if(tmpSize > 0){
            return tmpSize;
        } else {
            return 10;
        }
    }

    public boolean getEnableAnimation() {
        return settings.getBoolean(AMPrefKeys.ENABLE_ANIMATION_KEY, true);
    }

    public boolean getGestureEnabled() {
        return settings.getBoolean(AMPrefKeys.CARD_GESTURE_ENABLED, false);
    }

    public void setGestureEnabled(boolean enable) {
        editor.putBoolean(AMPrefKeys.CARD_GESTURE_ENABLED, enable);
        editor.commit();
    }

    public int getCardPlayerIntervalBetweenQA() {
        return settings.getInt(AMPrefKeys.CARD_PLAYER_QA_SLEEP_INTERVAL_KEY, 1);
    }

    public int getCardPlayerIntervalBetweenCards() {
        return settings.getInt(AMPrefKeys.CARD_PLAYER_CARD_SLEEP_INTERVAL_KEY, 1);
    }


    public void setCardPlayerShuffleEnabled(boolean enabled) {
        editor.putBoolean(AMPrefKeys.CARD_PLAYER_SHUFFLE_ENABLED_KEY, enabled);
        editor.commit();
    }

    public boolean getCardPlayerShuffleEnabled() {
        return settings.getBoolean(AMPrefKeys.CARD_PLAYER_SHUFFLE_ENABLED_KEY, false);
    }

    public void setCardPlayerRepeatEnabled(boolean enabled) {
        editor.putBoolean(AMPrefKeys.CARD_PLAYER_REPEAT_ENABLED_KEY, enabled);
        editor.commit();
    }
    public boolean getCardPlayerRepeatEnabled() {
        return settings.getBoolean(AMPrefKeys.CARD_PLAYER_REPEAT_ENABLED_KEY, true);
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
        FORA,
        BLUEDICT;

        public static DictApp parse(String a){
            if(a.equals("FORA")){
                return FORA;
            } else if (a.equals("BLUEDICT")) {
                return BLUEDICT;
            } else {
                return COLORDICT;
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

    public static enum CopyToClipboard {
        DISABLED,
        QUESTION,
        ANSWER,
        BOTH;
        public static CopyToClipboard parse(String a){
            if(a.equals("DISABLED")){
                return DISABLED;
            } else if(a.equals("ANSWER")){
                return ANSWER;
            } else if(a.equals("BOTH")){
                return BOTH;
            } else{
                return QUESTION;
            }
        }
    }

}
