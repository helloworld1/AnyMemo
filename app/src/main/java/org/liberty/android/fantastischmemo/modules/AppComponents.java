package org.liberty.android.fantastischmemo.modules;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.liberty.android.fantastischmemo.AMApplication;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.downloader.DownloaderUtils;
import org.liberty.android.fantastischmemo.downloader.google.GoogleDriveDownloadHelper;
import org.liberty.android.fantastischmemo.downloader.quizlet.QuizletDownloadHelper;
import org.liberty.android.fantastischmemo.entity.Option;
import org.liberty.android.fantastischmemo.entity.SchedulingAlgorithmParameters;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.service.AnyMemoService;
import org.liberty.android.fantastischmemo.service.CardPlayerService;
import org.liberty.android.fantastischmemo.service.ConvertIntentService;
import org.liberty.android.fantastischmemo.ui.QuizActivity;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.utils.AMDateUtil;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.liberty.android.fantastischmemo.utils.AMUiUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;
import org.liberty.android.fantastischmemo.widget.WidgetRemoteViewsFactory;

import java.util.Map;

import dagger.Component;

@PerApplication
@Component(modules = AppModules.class)
public interface AppComponents {
    AMApplication application();
    Scheduler scheduler();
    FirebaseAnalytics firebaseAnalytics();
    AMFileUtil amFileUtil();
    AMPrefUtil amPrefUtil();
    AMDateUtil amDateUtil();
    AMUiUtil amUiUtil();
    RecentListUtil recenetListUtil();
    SchedulingAlgorithmParameters schedulingAlgorithmParameters();
    DownloaderUtils downloaderUtils();
    DatabaseUtil databaseUtil();
    QuizletDownloadHelper quizletDownloadHelper();
    Map<Class<?>, Converter> converterMap();
    Option option();

    @ForApplication Context applicationContext();

    void inject(StudyActivity.LearnQueueManagerLoader loader);

    void inject(QuizActivity.QuizQueueManagerLoader loader);

    void inject(AnyMemoService service);

    void inject(CardPlayerService service);

    void inject(ConvertIntentService service);

    void inject(WidgetRemoteViewsFactory factory);

    void inject(GoogleDriveDownloadHelper helper);
}
