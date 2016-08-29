package org.liberty.android.fantastischmemo.modules;

import android.content.Context;

import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.downloader.DownloaderUtils;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.ui.AnyMemo;
import org.liberty.android.fantastischmemo.ui.CardEditor;
import org.liberty.android.fantastischmemo.ui.CardListActivity;
import org.liberty.android.fantastischmemo.ui.DatabaseMerger;
import org.liberty.android.fantastischmemo.ui.PreviewEditActivity;
import org.liberty.android.fantastischmemo.ui.QACardActivity;
import org.liberty.android.fantastischmemo.ui.QuizActivity;
import org.liberty.android.fantastischmemo.ui.SettingsScreen;
import org.liberty.android.fantastischmemo.ui.ShareScreen;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.utils.AMDateUtil;
import org.liberty.android.fantastischmemo.utils.AMUiUtil;
import org.liberty.android.fantastischmemo.utils.ShareUtil;

import java.util.Map;

import dagger.Component;

@PerActivity
@Component(dependencies = AppComponents.class, modules = {ActivityModules.class})
public interface ActivityComponents {
    BaseActivity activity();

    Scheduler scheduler();

    AMDateUtil amDateUtil();

    ShareUtil shareUtil();

    AMUiUtil amUiUtil();

    DownloaderUtils downloaderUtils();

    Map<Class<?>, Converter> converterMap();

    @ForApplication Context applicationContext();

    void inject(AnyMemo activity);

    void inject(QACardActivity activity);

    void inject(StudyActivity activity);

    void inject(PreviewEditActivity activity);

    void inject(CardEditor activity);

    void inject(QuizActivity activity);

    void inject(CardListActivity activity);

    void inject(DatabaseMerger activity);

    void inject(SettingsScreen activity);

    void inject(ShareScreen activity);
}
