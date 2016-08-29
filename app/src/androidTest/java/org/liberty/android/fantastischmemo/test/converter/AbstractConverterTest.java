package org.liberty.android.fantastischmemo.test.converter;

import android.support.test.filters.SmallTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.test.BaseTest;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;

import java.io.File;

public abstract class AbstractConverterTest extends BaseTest {

    private Converter converter;

    private String srcFilePath;

    private String destFilePath;

    protected abstract Converter getConverter();

    protected abstract String getFileNamePrefix();

    protected abstract void verify(String destFilePath) throws Exception;

    protected AMFileUtil amFileUtil;

    @Before
    public void setUp() throws Exception {
        // Set up necessary dependencies first
        amFileUtil = new AMFileUtil(getTargetContext(), new AMPrefUtil(getTargetContext()));

        converter = getConverter();

        String srcFileName = getFileNamePrefix() + "." + converter.getSrcExtension();

        srcFilePath = "/sdcard/" + getFileNamePrefix() + "." + converter.getSrcExtension();
        destFilePath = "/sdcard/" + getFileNamePrefix() + "." + converter.getDestExtension();

        // This amFileUtil is used on the test package so it can copy the 
        // asset file from the test package.
        AMFileUtil amFileUtilForTest = new AMFileUtil(getContext(), new AMPrefUtil(getContext()));
        amFileUtilForTest.copyFileFromAsset(srcFileName, new File(srcFilePath));
    }

    @SmallTest
    @Test
    public void testConvert() throws Exception {
        converter.convert(srcFilePath, destFilePath);
        verify(destFilePath);
    }

    @After
    public void tearDown() {
        if (srcFilePath != null) {
            new File(srcFilePath).delete();
        }
        if (destFilePath != null) {
            new File(destFilePath).delete();
        }
    }
}
