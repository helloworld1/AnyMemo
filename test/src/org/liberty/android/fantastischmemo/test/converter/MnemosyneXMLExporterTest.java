package org.liberty.android.fantastischmemo.test.converter;

import java.io.File;

import org.liberty.android.fantastischmemo.converter.AbstractConverter;
import org.liberty.android.fantastischmemo.converter.MnemosyneXMLExporter;

public class MnemosyneXMLExporterTest extends AbstractConverterTest {

    @Override
    protected AbstractConverter getConverter() {
        return new MnemosyneXMLExporter(getContext());
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
