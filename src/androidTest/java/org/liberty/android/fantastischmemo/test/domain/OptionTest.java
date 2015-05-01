package org.liberty.android.fantastischmemo.test.domain;

import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.test.AbstractPreferencesTest;

import android.test.suitebuilder.annotation.SmallTest;

public class OptionTest extends AbstractPreferencesTest {

    @SmallTest
    public void testGetDictApp() {
        Option option = new Option(getContext());
        editor.putString(AMPrefKeys.DICT_APP_KEY, "COLORDICT");
        editor.commit();
        assertEquals(Option.DictApp.COLORDICT, option.getDictApp());

        editor.putString(AMPrefKeys.DICT_APP_KEY, "FORA");
        editor.commit();
        assertEquals(Option.DictApp.FORA, option.getDictApp());

        editor.putString(AMPrefKeys.DICT_APP_KEY, "BLUEDICT");
        editor.commit();
        assertEquals(Option.DictApp.BLUEDICT, option.getDictApp());
    }

    @SmallTest
    public void testGetButtonStyle() {
        Option option = new Option(getContext());
        editor.putString(AMPrefKeys.BUTTON_STYLE_KEY, "ANYMEMO");
        editor.commit();
        assertEquals(Option.ButtonStyle.ANYMEMO, option.getButtonStyle());

        editor.putString(AMPrefKeys.BUTTON_STYLE_KEY, "MNEMOSYNE");
        editor.commit();
        assertEquals(Option.ButtonStyle.MNEMOSYNE, option.getButtonStyle());

        editor.putString(AMPrefKeys.BUTTON_STYLE_KEY, "ANKI");
        editor.commit();
        assertEquals(Option.ButtonStyle.ANKI, option.getButtonStyle());

    }

    @SmallTest
    public void testGetShuffleType() {
        Option option = new Option(getContext());
        editor.putBoolean(AMPrefKeys.SHUFFLING_CARDS_KEY, true);
        editor.commit();
        assertEquals(Option.ShuffleType.LOCAL, option.getShuffleType());

        editor.putBoolean(AMPrefKeys.SHUFFLING_CARDS_KEY, false);
        editor.commit();
        assertEquals(Option.ShuffleType.NONE, option.getShuffleType());

    }

    @SmallTest
    public void testGetSpeakingType() {
        Option option = new Option(getContext());
        editor.putString(AMPrefKeys.SPEECH_CONTROL_KEY, "TAP");
        editor.commit();
        assertEquals(Option.SpeakingType.TAP, option.getSpeakingType());

        editor.putString(AMPrefKeys.SPEECH_CONTROL_KEY, "AUTO");
        editor.commit();
        assertEquals(Option.SpeakingType.AUTO, option.getSpeakingType());

        editor.putString(AMPrefKeys.SPEECH_CONTROL_KEY, "MANUAL");
        editor.commit();
        assertEquals(Option.SpeakingType.MANUAL, option.getSpeakingType());

        editor.putString(AMPrefKeys.SPEECH_CONTROL_KEY, "AUTOTAP");
        editor.commit();
        assertEquals(Option.SpeakingType.AUTOTAP, option.getSpeakingType());

    }

}
