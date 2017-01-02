package org.liberty.android.fantastischmemo.test.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.filters.SmallTest;
import android.view.Display;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Test;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.modules.AppComponents;
import org.liberty.android.fantastischmemo.test.BaseTest;
import org.liberty.android.fantastischmemo.ui.CardImageGetter;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class CardImageGetterTest extends BaseTest {
    private int screenWidth;
	
    private CardImageGetter cardImageGetter;

    private String[] imageSearchPaths = {AMEnv.DEFAULT_IMAGE_PATH};

    @Before
    public void setUp() {
        Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        screenWidth = display.getWidth();
        AppComponents appComponents = Mockito.mock(AppComponents.class);
        Mockito.when(appComponents.applicationContext()).thenReturn(getContext());
        Mockito.when(appComponents.amFileUtil()).thenReturn(new AMFileUtil(getContext(), new AMPrefUtil(getContext())));
        cardImageGetter = new CardImageGetter(appComponents, imageSearchPaths);
    }

    @SmallTest
    @Test
    public void testSmallIconBitmapShouldNotResize() {
        int bitmapWidth = (int) (0.1 * screenWidth);
        int[] colorArray = new int[bitmapWidth * bitmapWidth];
        Bitmap b = Bitmap.createBitmap(colorArray, bitmapWidth, bitmapWidth, Bitmap.Config.ARGB_8888);
        Bitmap test = cardImageGetter.scaleBitmap(b);
        assertEquals(bitmapWidth, test.getWidth());
    }

    @SmallTest
    @Test
    public void testNormalImageBitmapShouldEnlarge() {	
        int bitmapWidth = (int) (0.4 * screenWidth);
        int[] colorArray = new int[bitmapWidth * bitmapWidth];
        Bitmap b = Bitmap.createBitmap(colorArray, bitmapWidth, bitmapWidth, Bitmap.Config.ARGB_8888);
        Bitmap test = cardImageGetter.scaleBitmap(b);
        assertEquals((int)(0.6 * screenWidth), test.getWidth());
    }
	
    @SmallTest
    @Test
    public void testNormalImageBitmapShouldNotResize() {
        int bitmapWidth = (int) (0.8 * screenWidth);
        int[] colorArray = new int[bitmapWidth * bitmapWidth];
        Bitmap b = Bitmap.createBitmap(colorArray, bitmapWidth, bitmapWidth, Bitmap.Config.ARGB_8888);
        Bitmap test = cardImageGetter.scaleBitmap(b);
        assertEquals(bitmapWidth, test.getWidth());
    }

    @SmallTest
    @Test
    public void testLargeImageBitmapShouldShrink() {
        int bitmapWidth = (int) (1.5 * screenWidth);
        int[] colorArray = new int[bitmapWidth * bitmapWidth];
        Bitmap b = Bitmap.createBitmap(colorArray, bitmapWidth, bitmapWidth, Bitmap.Config.ARGB_8888);
        Bitmap test = cardImageGetter.scaleBitmap(b);
        assertEquals(screenWidth, test.getWidth());
    }
}
