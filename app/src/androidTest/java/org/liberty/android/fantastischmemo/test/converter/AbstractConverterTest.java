package org.liberty.android.fantastischmemo.test.converter;

import java.io.File;
import java.lang.reflect.Method;

import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public abstract class AbstractConverterTest extends AndroidTestCase {

    private Converter converter;

    private String srcFilePath;

    private String destFilePath;

    private Context testContext;

    protected abstract Converter getConverter();

    protected abstract String getFileNamePrefix();

    protected abstract void verify(String destFilePath) throws Exception;

    protected AMFileUtil amFileUtil;

    @Override
    public void setUp() throws Exception {

        // Set up necessary dependencies first
        amFileUtil = new AMFileUtil(getContext());
        amFileUtil.setAmPrefUtil(new AMPrefUtil(getContext()));

        // Reflect out the test context
        Method getTestContext = ServiceTestCase.class.getMethod("getTestContext");
        testContext = (Context) getTestContext.invoke(this);

        converter = getConverter();

        String srcFileName = getFileNamePrefix() + "." + converter.getSrcExtension();

        srcFilePath = "/sdcard/" + getFileNamePrefix() + "." + converter.getSrcExtension();
        destFilePath = "/sdcard/" + getFileNamePrefix() + "." + converter.getDestExtension();

        // This amFileUtil is used on the test package so it can copy the 
        // asset file from the test package.
        AMFileUtil amFileUtilForTest = new AMFileUtil(testContext);
        amFileUtilForTest.setAmPrefUtil(new AMPrefUtil(getContext()));
        amFileUtilForTest.copyFileFromAsset(srcFileName, new File(srcFilePath));
    }

    @SmallTest
    public void testConvert() throws Exception {
        converter.convert(srcFilePath, destFilePath);
        verify(destFilePath);
    }

    @Override
    public void tearDown() {
        if (srcFilePath != null) {
            new File(srcFilePath).delete();
        }
        if (destFilePath != null) {
            new File(destFilePath).delete();
        }
    }

}
