package org.liberty.android.fantastischmemo.modules;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.liberty.android.fantastischmemo.AMApplication;
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
import org.liberty.android.fantastischmemo.downloader.DownloaderUtils;
import org.liberty.android.fantastischmemo.entity.Option;
import org.liberty.android.fantastischmemo.entity.SchedulingAlgorithmParameters;
import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.utils.AMDateUtil;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.liberty.android.fantastischmemo.utils.AMUiUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

@Module
public class AppModules {

    private AMApplication application;

    public AppModules(AMApplication app) {
        this.application = app;
    }

    @Provides
    @PerApplication
    AMApplication providesApplication() {
        return application;
    }


    @Provides
    @PerApplication
    @ForApplication
    Context providesContext() {
        return application;
    }

    @Provides
    @PerApplication
    Scheduler providesScheduler(SchedulingAlgorithmParameters parameters) {
        return new DefaultScheduler(parameters);
    }

    @Provides
    @PerApplication
    SchedulingAlgorithmParameters providesSchedulingAlgorithmParameters(AMApplication context) {
        return new SchedulingAlgorithmParameters(context);
    }

    @Provides
    @PerApplication
    AMFileUtil providesAMFileUtil(AMApplication context, AMPrefUtil amPrefUtil) {
        return new AMFileUtil(context, amPrefUtil);
    }

    @Provides
    @PerApplication
    AMPrefUtil providesAMPrefUtil(AMApplication context) {
        return new AMPrefUtil(context);
    }

    @Provides
    @PerApplication
    Option providesOption(AMApplication context) {
        return new Option(context);
    }

    @Provides
    @PerApplication
    RecentListUtil providesRecentListUtil(AMApplication context, Option option) {
        return new RecentListUtil(context, option);
    }

    @Provides
    @PerApplication
    DatabaseUtil providesDatabaseUtil(AMApplication context) {
        return new DatabaseUtil(context);
    }

    @Provides
    @PerApplication
    FirebaseAnalytics providesFirebaseAnalytics(AMApplication context) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        return firebaseAnalytics;
    }

    @Provides
    @PerApplication
    DownloaderUtils providesDownloaderUtils(AMApplication context) {
        return new DownloaderUtils(context);
    }

    @Provides
    @PerApplication
    AMDateUtil providesAMDateUtil(AMApplication context) {
        return new AMDateUtil(context);
    }

    @Provides
    @PerApplication
    AMUiUtil providesAMUiUtil(AMApplication context) {
        return new AMUiUtil(context);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(CSVExporter.class)
    Converter providesCSVExporter() {
        return new CSVExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(CSVImporter.class)
    Converter providesCSVImporter(AMFileUtil amFileUtil) {
        return new CSVImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(Mnemosyne2CardsExporter.class)
    Converter providesMnemosyne2CardsExporter(AMFileUtil amFileUtil) {
        return new Mnemosyne2CardsExporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(Mnemosyne2CardsImporter.class)
    Converter providesMnemosyne2CardsImporter(AMFileUtil amFileUtil) {
        return new Mnemosyne2CardsImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(MnemosyneXMLExporter.class)
    Converter providesMnemosyneXMLExporter() {
        return new MnemosyneXMLExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(MnemosyneXMLImporter.class)
    Converter providesMnemosyneXMLImporter(AMFileUtil amFileUtil) {
        return new MnemosyneXMLImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(QATxtExporter.class)
    Converter providesQATxtExporter() {
        return new QATxtExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(QATxtImporter.class)
    Converter providesQATxtImporter(AMFileUtil amFileUtil) {
        return new QATxtImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(Supermemo2008XMLImporter.class)
    Converter providesSupermemo2008XMLImporter() {
        return new Supermemo2008XMLImporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(SupermemoXMLImporter.class)
    Converter providesSupermemoXMLImporter(AMFileUtil amFileUtil) {
        return new SupermemoXMLImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(TabTxtExporter.class)
    Converter providesTabTxtExporter() {
        return new TabTxtExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(TabTxtImporter.class)
    Converter providesTabTxtImporter(AMFileUtil amFileUtil) {
        return new TabTxtImporter(amFileUtil);
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(ZipExporter.class)
    Converter providesZipExporter() {
        return new ZipExporter();
    }

    @Provides
    @PerApplication
    @IntoMap
    @ClassKey(ZipImporter.class)
    Converter providesZipImporter() {
        return new ZipImporter();
    }
}
