package org.liberty.android.fantastischmemo.test.converter;

import java.io.File;

import org.liberty.android.fantastischmemo.converter.AbstractConverter;
import org.liberty.android.fantastischmemo.converter.ZipImporter;

public class ZipImporterTest extends AbstractConverterTest {

    @Override
    protected AbstractConverter getConverter() {
        return new ZipImporter();
    }

    @Override
    protected String getFileNamePrefix() {
        return "zip-test";
    }

    @Override
    protected void verify(String destFilePath) throws Exception {
        assertTrue(new File(destFilePath).length() > 0);
    }

}
