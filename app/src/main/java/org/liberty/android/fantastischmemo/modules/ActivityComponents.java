package org.liberty.android.fantastischmemo.modules;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.downloader.common.DownloaderUtils;
import org.liberty.android.fantastischmemo.downloader.dropbox.DropboxOauth2AccountActivity;
import org.liberty.android.fantastischmemo.downloader.dropbox.UploadDropboxActivity;
import org.liberty.android.fantastischmemo.downloader.google.GoogleAccountActivity;
import org.liberty.android.fantastischmemo.downloader.google.SpreadsheetListScreen;
import org.liberty.android.fantastischmemo.downloader.oauth.Oauth2AccountActivity;
import org.liberty.android.fantastischmemo.downloader.oauth.Oauth2TokenUtil;
import org.liberty.android.fantastischmemo.downloader.quizlet.QuizletDownloadHelper;
import org.liberty.android.fantastischmemo.entity.Option;
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
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.liberty.android.fantastischmemo.utils.AMUiUtil;
import org.liberty.android.fantastischmemo.utils.AboutUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseOperationDialogUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;
import org.liberty.android.fantastischmemo.utils.ErrorUtil;
import org.liberty.android.fantastischmemo.utils.GooglePlayUtil;
import org.liberty.android.fantastischmemo.utils.RecentListActionModeUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;
import org.liberty.android.fantastischmemo.utils.ShareUtil;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Provider;

import dagger.BindsInstance;
import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = {ActivityModules.class})
public interface ActivityComponents {
    Activity activity();

    BaseActivity baseActivity();

    Scheduler scheduler();

    AMDateUtil amDateUtil();

    ShareUtil shareUtil();

    AMUiUtil amUiUtil();

    AboutUtil aboutUtil();

    DownloaderUtils downloaderUtils();

    DatabaseUtil databaseUtil();

    QuizletDownloadHelper quizletDownloadHelper();

    Option option();

    RecentListUtil recentListUtil();

    AMPrefUtil amPrefUtil();

    AMFileUtil amFileUtil();

    Oauth2TokenUtil oauth2TokenUtil();

    RecentListActionModeUtil recentListActionModeUtil();

    DatabaseOperationDialogUtil databaseOperationDialogUtil();

    ExecutorService executorService();

    ErrorUtil errorUtil();

    GooglePlayUtil googlePlayUtil();

    GoogleApiClient googleApiClient();

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

    void inject(Oauth2AccountActivity activity);

    void inject(DropboxOauth2AccountActivity activity);

    void inject(GoogleAccountActivity activity);

    void inject(SpreadsheetListScreen activity);

    void inject(UploadDropboxActivity activity);

    Provider<FragmentComponents.Builder> fragmentsComponentsBuilder();

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        ActivityComponents.Builder activity(BaseActivity activity);

        ActivityComponents.Builder activityModule(ActivityModules module);

        ActivityComponents build();
    }
}
