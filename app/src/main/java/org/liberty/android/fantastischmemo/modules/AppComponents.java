package org.liberty.android.fantastischmemo.modules;

import org.liberty.android.fantastischmemo.common.AMApplication;
import org.liberty.android.fantastischmemo.downloader.google.GoogleDriveDownloadHelper;
import org.liberty.android.fantastischmemo.service.AnyMemoService;
import org.liberty.android.fantastischmemo.service.CardPlayerService;
import org.liberty.android.fantastischmemo.service.ConvertIntentService;
import org.liberty.android.fantastischmemo.ui.QuizActivity;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.widget.WidgetRemoteViewsFactory;

import dagger.BindsInstance;
import dagger.Component;

@PerApplication
@Component(modules = AppModules.class)
public interface AppComponents {

    void inject(AMApplication application);

    void inject(StudyActivity.LearnQueueManagerLoader loader);

    void inject(QuizActivity.QuizQueueManagerLoader loader);

    void inject(AnyMemoService service);

    void inject(CardPlayerService service);

    void inject(ConvertIntentService service);

    void inject(WidgetRemoteViewsFactory factory);

    void inject(GoogleDriveDownloadHelper helper);


    @Component.Builder
    interface Builder {
        @BindsInstance
        AppComponents.Builder application(AMApplication application);

        AppComponents build();
    }
}
