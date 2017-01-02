package org.liberty.android.fantastischmemo.ui.loader;

import android.content.Context;

import org.liberty.android.fantastischmemo.entity.Setting;

public class SettingLoader extends DBLoader<Setting> {
    public SettingLoader(Context context, String dbPath) {
        super(context, dbPath);
    }

    @Override
    protected Setting dbLoadInBackground() {
        return dbOpenHelper.getSettingDao().queryForId(1);
    }
}
