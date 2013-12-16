/*
Copyright (C) 2013 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo;

import org.liberty.android.fantastischmemo.aspect.LogInvocation;
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
import org.liberty.android.fantastischmemo.downloader.dropbox.DropboxDownloadHelperFactory;
import org.liberty.android.fantastischmemo.downloader.dropbox.DropboxUploadHelperFactory;
import org.liberty.android.fantastischmemo.downloader.google.GoogleDriveDownloadHelperFactory;
import org.liberty.android.fantastischmemo.downloader.google.GoogleDriveUploadHelperFactory;
import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.ui.CardImageGetterFactory;
import org.liberty.android.fantastischmemo.utils.CardTTSUtilFactory;
import org.liberty.android.fantastischmemo.utils.CardTextUtilFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/* Define the moduled used in Guice dependency injection. */
public class AMModules extends AbstractModule {

    @Override
    @LogInvocation
    protected void configure() {
        install(new FactoryModuleBuilder()
                .build(GoogleDriveDownloadHelperFactory.class));

        install(new FactoryModuleBuilder()
                .build(GoogleDriveUploadHelperFactory.class));

        install(new FactoryModuleBuilder()
                .build(DropboxDownloadHelperFactory.class));

        install(new FactoryModuleBuilder()
                .build(DropboxUploadHelperFactory.class));

        install(new FactoryModuleBuilder()
                .build(CardTTSUtilFactory.class));

        install(new FactoryModuleBuilder()
                .build(CardImageGetterFactory.class));

        install(new FactoryModuleBuilder()
                .build(CardTextUtilFactory.class));

        bind(Scheduler.class).to(DefaultScheduler.class);

        bind(Converter.class).annotatedWith(Mnemosyne2CardsExporter.Type.class).to(Mnemosyne2CardsExporter.class);

        bind(Converter.class).annotatedWith(Mnemosyne2CardsImporter.Type.class).to(Mnemosyne2CardsImporter.class);

        bind(Converter.class).annotatedWith(Mnemosyne2CardsImporter.Type.class).to(Mnemosyne2CardsImporter.class);

        bind(Converter.class).annotatedWith(MnemosyneXMLExporter.Type.class).to(MnemosyneXMLExporter.class);

        bind(Converter.class).annotatedWith(MnemosyneXMLImporter.Type.class).to(MnemosyneXMLImporter.class);

        bind(Converter.class).annotatedWith(QATxtExporter.Type.class).to(QATxtExporter.class);

        bind(Converter.class).annotatedWith(QATxtImporter.Type.class).to(QATxtImporter.class);

        bind(Converter.class).annotatedWith(CSVExporter.Type.class).to(CSVExporter.class);

        bind(Converter.class).annotatedWith(CSVImporter.Type.class).to(CSVImporter.class);

        bind(Converter.class).annotatedWith(TabTxtExporter.Type.class).to(TabTxtExporter.class);

        bind(Converter.class).annotatedWith(TabTxtImporter.Type.class).to(TabTxtImporter.class);

        bind(Converter.class).annotatedWith(Supermemo2008XMLImporter.Type.class).to(Supermemo2008XMLImporter.class);

        bind(Converter.class).annotatedWith(SupermemoXMLImporter.Type.class).to(SupermemoXMLImporter.class);

        bind(Converter.class).annotatedWith(ZipExporter.Type.class).to(ZipExporter.class);

        bind(Converter.class).annotatedWith(ZipImporter.Type.class).to(ZipImporter.class);
    }
}

