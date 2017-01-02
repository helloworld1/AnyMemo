package org.liberty.android.fantastischmemo.modules;


import org.liberty.android.fantastischmemo.downloader.anymemo.AnyMemoDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.common.AbstractDownloaderFragment;
import org.liberty.android.fantastischmemo.downloader.quizlet.CardsetsListFragment;
import org.liberty.android.fantastischmemo.ui.CardPlayerFragment;
import org.liberty.android.fantastischmemo.ui.ConverterFragment;
import org.liberty.android.fantastischmemo.ui.FileBrowserFragment;
import org.liberty.android.fantastischmemo.ui.GestureSelectionDialogFragment;
import org.liberty.android.fantastischmemo.ui.GradeButtonsFragment;
import org.liberty.android.fantastischmemo.ui.MiscTabFragment;
import org.liberty.android.fantastischmemo.ui.OpenActionsFragment;
import org.liberty.android.fantastischmemo.ui.QuizLauncherDialogFragment;
import org.liberty.android.fantastischmemo.ui.RecentListFragment;

import dagger.Component;

@PerFragment
@Component(dependencies = ActivityComponents.class, modules = FragmentModules.class)
public interface FragmentComponents {

    void inject(RecentListFragment fragment);

    void inject(FileBrowserFragment fragment);

    void inject(GradeButtonsFragment fragment);

    void inject(OpenActionsFragment fragment);

    void inject(QuizLauncherDialogFragment fragment);

    void inject(GestureSelectionDialogFragment fragment);

    void inject(AbstractDownloaderFragment fragment);

    void inject(CardPlayerFragment fragment);

    void inject(ConverterFragment fragment);

    void inject(AnyMemoDownloaderFragment fragment);

    void inject(CardsetsListFragment fragment);

    void inject(MiscTabFragment fragment);

}
