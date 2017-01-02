package org.liberty.android.fantastischmemo.common;

import android.app.Service;

import org.liberty.android.fantastischmemo.modules.AppComponents;

public abstract class BaseService extends Service {
    public AppComponents appComponents() {
        return ((AMApplication) getApplication()).appComponents();
    }
}
