package org.liberty.android.fantastischmemo.test.converter;

import java.io.File;

import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.converter.Mnemosyne2CardsExporter;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

public class Mnemosyne2CardsExportTest extends AbstractConverterTest {

    @Override
    protected Converter getConverter() {
        Mnemosyne2CardsExporter exporter = new Mnemosyne2CardsExporter();
        exporter.setAmFileUtil(new AMFileUtil(getContext()));
        return exporter;
    }

    @Override
    protected String getFileNamePrefix() {
        return "french-body-parts";
    }

    @Override
    protected void verify(String destFilePath) throws Exception {
        assertTrue(new File(destFilePath).length() > 0);
    }

}
