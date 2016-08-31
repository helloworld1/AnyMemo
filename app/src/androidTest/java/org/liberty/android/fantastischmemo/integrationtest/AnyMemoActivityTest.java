package org.liberty.android.fantastischmemo.integrationtest;

import android.content.ComponentName;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.AnyMemo;
import org.liberty.android.fantastischmemo.ui.StudyActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

public class AnyMemoActivityTest {
    TestHelper testHelper;
    @Rule
    public ActivityTestRule<AnyMemo> mActivityRule = new IntentsTestRule<>(AnyMemo.class);

    @Before
    public void setUp() {
        testHelper = new TestHelper(InstrumentationRegistry.getInstrumentation());
        testHelper.clearPreferences();
        testHelper.markNotFirstTime();
    }

    @Test
    @LargeTest
    public void testOpenActivityWithSampleDB() {
        onView(allOf(withText(TestHelper.SAMPLE_DB_NAME), withId(R.id.recent_item_filename)))
                .check(matches(isDisplayed()));
    }

    @Test
    @LargeTest
    public void testOpenStudyActivity() {
        onView(allOf(withText(TestHelper.SAMPLE_DB_NAME), withId(R.id.recent_item_filename)))
                .perform(click());
        intended(hasComponent(new ComponentName(InstrumentationRegistry.getTargetContext(), StudyActivity.class)));
        onView(allOf(withText(TestHelper.SAMPLE_DB_NAME), withId(R.id.card_text_view)))
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    @LargeTest
    public void testOpenStudyActivityFromAction() {
        onView(allOf(withText(TestHelper.SAMPLE_DB_NAME), withId(R.id.recent_item_filename)))
                .perform(longClick());
        onView(withText(R.string.study_text)).perform(click());
        intended(hasComponent(new ComponentName(InstrumentationRegistry.getTargetContext(), StudyActivity.class)));
        onView(allOf(withText(TestHelper.SAMPLE_DB_NAME), withId(R.id.card_text_view)))
                .check(matches(isDisplayed()));
        pressBack();
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(2000);
    }
}
