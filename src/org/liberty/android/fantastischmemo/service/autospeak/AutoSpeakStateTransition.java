package org.liberty.android.fantastischmemo.service.autospeak;


public interface AutoSpeakStateTransition {
    void transition(AutoSpeakContext context, AutoSpeakMessage message);
}