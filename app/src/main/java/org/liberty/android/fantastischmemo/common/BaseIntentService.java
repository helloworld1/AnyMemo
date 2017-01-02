package org.liberty.android.fantastischmemo.common;

import android.app.IntentService;

import org.liberty.android.fantastischmemo.modules.AppComponents;

public abstract class BaseIntentService extends IntentService {
    public BaseIntentService(String name) {
        super(name);
    }
    public AppComponents appComponents() {
        return ((AMApplication) getApplication()).appComponents();
    }
}
