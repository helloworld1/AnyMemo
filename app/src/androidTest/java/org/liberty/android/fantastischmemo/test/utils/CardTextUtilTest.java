package org.liberty.android.fantastischmemo.test.utils;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.entity.Option;
import org.liberty.android.fantastischmemo.modules.AppComponents;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.ui.CardImageGetter;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.liberty.android.fantastischmemo.utils.CardTextUtil;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class CardTextUtilTest extends AbstractExistingDBTest {

    // The CardTextUtil is initialized in each method
    // in order to tet the desired settings after construction.
    private CardTextUtil cardTextUtil;

    private Option mockOption;

    private CardImageGetter mockCardImageGetter;

    private String[] mockImagePaths = {AMEnv.DEFAULT_IMAGE_PATH};

    private AppComponents appComponents;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockOption = Mockito.mock(Option.class);
        Mockito.when(mockOption.getEnableArabicEngine())
            .thenReturn(false);

        appComponents = Mockito.mock(AppComponents.class);
        Mockito.when(appComponents.applicationContext()).thenReturn(getContext());
        Mockito.when(appComponents.amFileUtil()).thenReturn(new AMFileUtil(getContext(), new AMPrefUtil(getContext())));
    }

    @SmallTest
    @Test
    public void testEmptyCard() {
        cardTextUtil = new CardTextUtil(appComponents, mockImagePaths);
        CharSequence result = cardTextUtil.getSpannableText("", true, false);
        assertEquals("", result.toString());
    }

    @SmallTest
    @Test
    public void testCardsWithPlainText() {
        cardTextUtil = new CardTextUtil(appComponents, mockImagePaths);
        CharSequence result = cardTextUtil.getSpannableText("Plain text", true, false);
        assertEquals("Plain text", result.toString());
    }

    @SmallTest
    @Test
    public void testCardsWithHTMLAndHTMLEnabled() {
        cardTextUtil = new CardTextUtil(appComponents, mockImagePaths);
        CharSequence result = cardTextUtil.getSpannableText("<b>HTML text</b>", true, false);
        assertEquals("HTML text", result.toString());
    }

    @SmallTest
    @Test
    public void testCardsWithHTMLAndHTMLDisabled() {
        cardTextUtil = new CardTextUtil(appComponents, mockImagePaths);
        CharSequence result = cardTextUtil.getSpannableText("<b>HTML text</b>", false, false);
        assertEquals("<b>HTML text</b>", result.toString());
    }
}
