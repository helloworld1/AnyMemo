package org.liberty.android.fantastischmemo.test.converter;

import java.io.File;

import org.liberty.android.fantastischmemo.converter.AbstractConverter;
import org.liberty.android.fantastischmemo.converter.TabTxtExporter;

public class TabTxtExporterTest extends AbstractConverterTest {

    @Override
    protected AbstractConverter getConverter() {
        return new TabTxtExporter(getContext());
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
