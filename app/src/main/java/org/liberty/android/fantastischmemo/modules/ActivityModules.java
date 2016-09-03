package org.liberty.android.fantastischmemo.modules;

import android.app.Activity;
import android.content.Context;

import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.entity.Option;
import org.liberty.android.fantastischmemo.ui.loader.MultipleLoaderManager;
import org.liberty.android.fantastischmemo.utils.DictionaryUtil;
import org.liberty.android.fantastischmemo.utils.ShareUtil;

import dagger.Module;
import dagger.Provides;

@Module
@PerActivity
public class ActivityModules {
    private final BaseActivity activity;

    public ActivityModules(BaseActivity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    BaseActivity providesBaseActivity() {
        return activity;
    }

    @Provides
    @PerActivity
    Activity providesActivity() {
        return activity;
    }

    @Provides
    @PerActivity
    MultipleLoaderManager providesMultipleLoaderManager(BaseActivity activity) {
        return new MultipleLoaderManager(activity);
    }

    @Provides
    @PerActivity
    ShareUtil providesShareUtil(Activity activity) {
        return new ShareUtil(activity);
    }

    @Provides
    @PerActivity
    DictionaryUtil providesDictionaryUtil(Activity activity, Option option) {
        return new DictionaryUtil(activity, option);
    }

}
