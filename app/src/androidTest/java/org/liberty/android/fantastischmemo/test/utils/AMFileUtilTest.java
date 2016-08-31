package org.liberty.android.fantastischmemo.test.utils;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.integrationtest.TestHelper;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.mockito.Mockito;

public class AMFileUtilTest extends AbstractExistingDBTest {

    public void setUp() throws Exception {
        super.setUp();
    }
    @SmallTest
    @Test
    public void testDeleteDbSafe() {
        AMPrefUtil mockPrefUtil = Mockito.mock(AMPrefUtil.class);
        AMFileUtil amFileUtil = new AMFileUtil(getContext(), mockPrefUtil);
        amFileUtil.deleteDbSafe(TestHelper.SAMPLE_DB_PATH);
        Mockito.verify(mockPrefUtil).removePrefKeys(TestHelper.SAMPLE_DB_PATH);
    }
}
