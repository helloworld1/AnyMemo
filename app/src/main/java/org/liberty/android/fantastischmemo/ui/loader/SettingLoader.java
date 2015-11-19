package org.liberty.android.fantastischmemo.ui.loader;

import org.liberty.android.fantastischmemo.entity.Setting;

import android.content.Context;

public class SettingLoader extends DBLoader<Setting> {
    public SettingLoader(Context context, String dbPath) {
        super(context, dbPath);
    }

    @Override
    protected Setting dbLoadInBackground() {
        return dbOpenHelper.getSettingDao().queryForId(1);
    }
}
