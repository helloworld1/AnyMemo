package org.liberty.android.fantastischmemo.modules;

import android.app.Activity;
import androidx.annotation.NonNull;

import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.entity.Option;
import org.liberty.android.fantastischmemo.ui.loader.MultipleLoaderManager;
import org.liberty.android.fantastischmemo.utils.DictionaryUtil;
import org.liberty.android.fantastischmemo.utils.ErrorUtil;
import org.liberty.android.fantastischmemo.utils.ShareUtil;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module(subcomponents = FragmentComponents.class)
public abstract class ActivityModules {
    @Binds
    @PerActivity
    abstract Activity providesActivity(BaseActivity activity);

    @Provides
    @PerActivity
    static MultipleLoaderManager providesMultipleLoaderManager(BaseActivity activity) {
        return new MultipleLoaderManager(activity);
    }

    @Provides
    @PerActivity
    static ShareUtil providesShareUtil(Activity activity) {
        return new ShareUtil(activity);
    }

    @Provides
    @PerActivity
    static DictionaryUtil providesDictionaryUtil(Activity activity, Option option) {
        return new DictionaryUtil(activity, option);
    }
}
