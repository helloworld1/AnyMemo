package org.liberty.android.fantastischmemo.test.utils;

import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.mockito.Mockito;

import android.test.suitebuilder.annotation.SmallTest;

public class AMFileUtilTest extends AbstractExistingDBTest {

    AMFileUtil amFileUtil;

    public void setUp() throws Exception {
        super.setUp();
        amFileUtil = new AMFileUtil(getContext());
    }
    @SmallTest
    public void testDeleteDbSafe() {
        AMPrefUtil mockPrefUtil = Mockito.mock(AMPrefUtil.class);
        amFileUtil.setAmPrefUtil(mockPrefUtil);
        amFileUtil.deleteDbSafe(TestHelper.SAMPLE_DB_PATH);
        Mockito.verify(mockPrefUtil).removePrefKeys(TestHelper.SAMPLE_DB_PATH);
    }
}
