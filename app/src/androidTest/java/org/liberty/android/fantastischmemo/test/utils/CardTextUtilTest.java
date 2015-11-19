package org.liberty.android.fantastischmemo.test.utils;

import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.ui.CardImageGetter;
import org.liberty.android.fantastischmemo.ui.CardImageGetterFactory;
import org.liberty.android.fantastischmemo.utils.CardTextUtil;
import org.mockito.Mockito;

import android.test.suitebuilder.annotation.SmallTest;

public class CardTextUtilTest extends AbstractExistingDBTest {

    // The CardTextUtil is initialized in each method
    // in order to tet the desired settings after construction.
    private CardTextUtil cardTextUtil;

    private Option mockOption;

    private CardImageGetterFactory mockCardImageGetterFactory;

    private CardImageGetter mockCardImageGetter;

    private String[] mockImagePaths = {AMEnv.DEFAULT_IMAGE_PATH};

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockOption = Mockito.mock(Option.class);
        Mockito.when(mockOption.getEnableArabicEngine())
            .thenReturn(false);

        mockCardImageGetterFactory = Mockito.mock(CardImageGetterFactory.class);

        mockCardImageGetter = Mockito.mock(CardImageGetter.class);

        Mockito.when(mockCardImageGetterFactory.create(mockImagePaths))
            .thenReturn(mockCardImageGetter);

    }

    @SmallTest
    public void testEmptyCard() {
        cardTextUtil = new CardTextUtil(getContext(),
                mockOption,
                mockCardImageGetterFactory,
                mockImagePaths);
        CharSequence result = cardTextUtil.getSpannableText("", true, false);
        assertEquals("", result.toString());
    }

    @SmallTest
    public void testCardsWithPlainText() {
        cardTextUtil = new CardTextUtil(getContext(),
                mockOption,
                mockCardImageGetterFactory,
                mockImagePaths);
        CharSequence result = cardTextUtil.getSpannableText("Plain text", true, false);
        assertEquals("Plain text", result.toString());
    }

    @SmallTest
    public void testCardsWithHTMLAndHTMLEnabled() {
        cardTextUtil = new CardTextUtil(getContext(),
                mockOption,
                mockCardImageGetterFactory,
                mockImagePaths);
        CharSequence result = cardTextUtil.getSpannableText("<b>HTML text</b>", true, false);
        assertEquals("HTML text", result.toString());
    }

    @SmallTest
    public void testCardsWithHTMLAndHTMLDisabled() {
        cardTextUtil = new CardTextUtil(getContext(),
                mockOption,
                mockCardImageGetterFactory,
                mockImagePaths);
        CharSequence result = cardTextUtil.getSpannableText("<b>HTML text</b>", false, false);
        assertEquals("<b>HTML text</b>", result.toString());
    }
}
