package org.liberty.android.fantastischmemo.test.converter;

import java.io.File;
import java.lang.reflect.Method;

import org.liberty.android.fantastischmemo.converter.AbstractConverter;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public abstract class AbstractConverterTest extends AndroidTestCase {

    private AbstractConverter converter;

    private String srcFilePath;

    private String destFilePath;

    private Context testContext;

    protected abstract AbstractConverter getConverter();

    protected abstract String getFileNamePrefix();

    protected abstract void verify(String destFilePath) throws Exception;

    @Override
    public void setUp() throws Exception {
        // Reflect out the test context
        Method getTestContext = ServiceTestCase.class.getMethod("getTestContext");
        testContext = (Context) getTestContext.invoke(this);

        converter = getConverter();

        String srcFileName = getFileNamePrefix() + "." + converter.getSrcExtension();

        srcFilePath = "/sdcard/" + getFileNamePrefix() + "." + converter.getSrcExtension();
        destFilePath = "/sdcard/" + getFileNamePrefix() + "." + converter.getDestExtension();

        AMFileUtil amFileUtil = new AMFileUtil(testContext);

        amFileUtil.copyFileFromAsset(srcFileName, new File(srcFilePath));

    }

    @SmallTest
    public void testConvert() throws Exception {
        converter.convert(srcFilePath, destFilePath);
        verify(destFilePath);
    }

    @Override
    public void tearDown() {
        (new File(srcFilePath)).delete();
        (new File(destFilePath)).delete();
    }

}
