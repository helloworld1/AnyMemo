package org.liberty.android.fantastischmemo.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.liberty.android.fantastischmemo.common.AMApplication;
import org.liberty.android.fantastischmemo.converter.CSVExporter;
import org.liberty.android.fantastischmemo.converter.CSVImporter;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.converter.Mnemosyne2CardsExporter;
import org.liberty.android.fantastischmemo.converter.Mnemosyne2CardsImporter;
import org.liberty.android.fantastischmemo.converter.MnemosyneXMLExporter;
import org.liberty.android.fantastischmemo.converter.MnemosyneXMLImporter;
import org.liberty.android.fantastischmemo.converter.QATxtExporter;
import org.liberty.android.fantastischmemo.converter.QATxtImporter;
import org.liberty.android.fantastischmemo.converter.Supermemo2008XMLImporter;
import org.liberty.android.fantastischmemo.converter.SupermemoXMLImporter;
import org.liberty.android.fantastischmemo.converter.TabTxtExporter;
import org.liberty.android.fantastischmemo.converter.TabTxtImporter;
import org.liberty.android.fantastischmemo.converter.ZipExporter;
import org.liberty.android.fantastischmemo.converter.ZipImporter;
import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import okhttp3.OkHttpClient;

@Module(subcomponents = ActivityComponents.class)
public abstract class AppModules {

    @Binds
    @PerApplication
    @ForApplication
    abstract Context provideContext(AMApplication application);

    @Binds
    @PerApplication
    abstract Scheduler providesScheduler(DefaultScheduler scheduler);

    @Provides
    @PerApplication
    static ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(4);
    }

    @Provides
    @PerApplication
    static SharedPreferences providesSharedPreferences(AMApplication application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @PerApplication
    static OkHttpClient providesOkHttpClient() {
        return new OkHttpClient();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(CSVExporter.class)
    static Converter providesCSVExporter() {
        return new CSVExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(CSVImporter.class)
    static Converter providesCSVImporter(AMFileUtil amFileUtil) {
        return new CSVImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(Mnemosyne2CardsExporter.class)
    static Converter providesMnemosyne2CardsExporter(AMFileUtil amFileUtil) {
        return new Mnemosyne2CardsExporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(Mnemosyne2CardsImporter.class)
    static Converter providesMnemosyne2CardsImporter(AMFileUtil amFileUtil) {
        return new Mnemosyne2CardsImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(MnemosyneXMLExporter.class)
    static Converter providesMnemosyneXMLExporter() {
        return new MnemosyneXMLExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(MnemosyneXMLImporter.class)
    static Converter providesMnemosyneXMLImporter(AMFileUtil amFileUtil) {
        return new MnemosyneXMLImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(QATxtExporter.class)
    static Converter providesQATxtExporter() {
        return new QATxtExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(QATxtImporter.class)
    static Converter providesQATxtImporter(AMFileUtil amFileUtil) {
        return new QATxtImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(Supermemo2008XMLImporter.class)
    static Converter providesSupermemo2008XMLImporter() {
        return new Supermemo2008XMLImporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(SupermemoXMLImporter.class)
    static Converter providesSupermemoXMLImporter(AMFileUtil amFileUtil) {
        return new SupermemoXMLImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(TabTxtExporter.class)
    static Converter providesTabTxtExporter() {
        return new TabTxtExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(TabTxtImporter.class)
    static Converter providesTabTxtImporter(AMFileUtil amFileUtil) {
        return new TabTxtImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(ZipExporter.class)
    static Converter providesZipExporter() {
        return new ZipExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(ZipImporter.class)
    static Converter providesZipImporter() {
        return new ZipImporter();
    }
}
