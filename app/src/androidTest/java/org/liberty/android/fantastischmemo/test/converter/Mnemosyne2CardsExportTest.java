package org.liberty.android.fantastischmemo.test.converter;

import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.converter.Mnemosyne2CardsExporter;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class Mnemosyne2CardsExportTest extends AbstractConverterTest {

    @Override
    protected Converter getConverter() {
        Mnemosyne2CardsExporter exporter = new Mnemosyne2CardsExporter(new AMFileUtil(getContext(), new AMPrefUtil(getContext())));
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
