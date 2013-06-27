package org.liberty.android.fantastischmemo.test.utils;

import java.util.EnumSet;
import java.util.List;

import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.CardImageGetter;
import org.liberty.android.fantastischmemo.ui.CardImageGetterFactory;
import org.liberty.android.fantastischmemo.utils.CardTextUtil;
import org.mockito.Mockito;

import android.test.suitebuilder.annotation.SmallTest;
import android.text.Spannable;

public class CardTextUtilTest extends AbstractExistingDBTest {

    // The CardTextUtil is initialized in each method
    // in order to tet the desired settings after construction.
    private CardTextUtil cardTextUtil;

    private Option mockOption;

    private CardImageGetterFactory mockCardImageGetterFactory;

    private CardImageGetter mockCardImageGetter;

    private SettingDao settingDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockOption = Mockito.mock(Option.class);
        Mockito.when(mockOption.getEnableArabicEngine())
            .thenReturn(false);

        mockCardImageGetterFactory = Mockito.mock(CardImageGetterFactory.class);

        mockCardImageGetter = Mockito.mock(CardImageGetter.class);

        Mockito.when(mockCardImageGetterFactory.create(TestHelper.SAMPLE_DB_PATH))
            .thenReturn(mockCardImageGetter);

        settingDao = helper.getSettingDao();
    }

    @SmallTest
    public void testEmptyCard() {
        Card card = new Card();
        card.setCategory(new Category());

        cardTextUtil = new CardTextUtil(getContext(),
                mockOption,
                mockCardImageGetterFactory,
                TestHelper.SAMPLE_DB_PATH);
        List<Spannable> fields = cardTextUtil.getFieldsToDisplay(card);
        assertEquals("", fields.get(0).toString());
        assertEquals("", fields.get(1).toString());
    }

    @SmallTest
    public void testCardsWithPlainText() {
        Card card = new Card();
        card.setQuestion("test question");
        card.setAnswer("test answer");
        card.setCategory(new Category());

        cardTextUtil = new CardTextUtil(getContext(),
                mockOption,
                mockCardImageGetterFactory,
                TestHelper.SAMPLE_DB_PATH);
        List<Spannable> fields = cardTextUtil.getFieldsToDisplay(card);
        assertEquals("test question", fields.get(0).toString());
        assertEquals("test answer", fields.get(1).toString());
    }

    @SmallTest
    public void testCardsWithHTMLAndHTMLEnabled() {
        Card card = new Card();
        card.setQuestion("<b>test question</b>");
        card.setAnswer("<i>test answer</i>");
        card.setCategory(new Category());

        cardTextUtil = new CardTextUtil(getContext(),
                mockOption,
                mockCardImageGetterFactory,
                TestHelper.SAMPLE_DB_PATH);
        List<Spannable> fields = cardTextUtil.getFieldsToDisplay(card);
        assertEquals("test question", fields.get(0).toString());
        assertEquals("test answer", fields.get(1).toString());
    }

    @SmallTest
    public void testCardsWithHTMLAndHTMLDisabled() {
        Card card = new Card();
        card.setQuestion("<b>test question</b>");
        card.setAnswer("<i>test answer</i>");

        // Disable HTML
        Setting setting = settingDao.queryForId(1);
        setting.setDisplayInHTMLEnum(EnumSet.noneOf(Setting.CardField.class));
        settingDao.update(setting);

        card.setCategory(new Category());

        cardTextUtil = new CardTextUtil(getContext(),
                mockOption,
                mockCardImageGetterFactory,
                TestHelper.SAMPLE_DB_PATH);
        List<Spannable> fields = cardTextUtil.getFieldsToDisplay(card);
        assertEquals("<b>test question</b>", fields.get(0).toString());
        assertEquals("<i>test answer</i>", fields.get(1).toString());
    }

    @SmallTest
    public void testCardsWithFieldCustomization() {
        Card card = new Card();
        card.setQuestion("test question");
        card.setAnswer("test answer");

        // Disable HTML
        Setting setting = settingDao.queryForId(1);
        // Question field has both question and answer text
        setting.setQuestionFieldEnum(EnumSet.of(Setting.CardField.QUESTION, Setting.CardField.ANSWER));

        // Answer field has both question text
        setting.setAnswerFieldEnum(EnumSet.of(Setting.CardField.QUESTION));
        settingDao.update(setting);

        card.setCategory(new Category());
        cardTextUtil = new CardTextUtil(getContext(),
                mockOption,
                mockCardImageGetterFactory,
                TestHelper.SAMPLE_DB_PATH);
        List<Spannable> fields = cardTextUtil.getFieldsToDisplay(card);
        assertTrue(fields.get(0).toString().contains("test question"));
        assertTrue(fields.get(0).toString().contains("test answer"));
        assertEquals("test question", fields.get(1).toString());
    }
}
