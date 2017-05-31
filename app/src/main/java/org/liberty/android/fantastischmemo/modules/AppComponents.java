package org.liberty.android.fantastischmemo.modules;

import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.liberty.android.fantastischmemo.common.AMApplication;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.downloader.common.DownloaderUtils;
import org.liberty.android.fantastischmemo.downloader.dropbox.DropboxApiHelper;
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
import java.util.concurrent.ExecutorService;

import dagger.Component;
import okhttp3.OkHttpClient;

@PerApplication
@Component(modules = AppModules.class)
public interface AppComponents {
    AMApplication application();
    Scheduler scheduler();
    SharedPreferences sharedPreferences();
    AMFileUtil amFileUtil();
    AMPrefUtil amPrefUtil();
    AMDateUtil amDateUtil();
    AMUiUtil amUiUtil();
    OkHttpClient okHttpClient();
    RecentListUtil recenetListUtil();
    SchedulingAlgorithmParameters schedulingAlgorithmParameters();
    DownloaderUtils downloaderUtils();
    DatabaseUtil databaseUtil();
    DropboxApiHelper dropboxApiHelper();
    QuizletDownloadHelper quizletDownloadHelper();
    EventBus eventBus();
    Map<Class<?>, Converter> converterMap();
    Option option();
    ExecutorService executorService();

    @ForApplication Context applicationContext();

    void inject(StudyActivity.LearnQueueManagerLoader loader);

    void inject(QuizActivity.QuizQueueManagerLoader loader);

    void inject(AnyMemoService service);

    void inject(CardPlayerService service);

    void inject(ConvertIntentService service);

    void inject(WidgetRemoteViewsFactory factory);

    void inject(GoogleDriveDownloadHelper helper);
}
