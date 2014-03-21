package org.liberty.android.fantastischmemo.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.EnumSet;

import org.liberty.android.fantastischmemo.dao.SettingDaoImpl;
import org.liberty.android.fantastischmemo.utils.AMStringUtils;

import com.google.common.base.Strings;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "settings", daoClass = SettingDaoImpl.class)
public class Setting implements Serializable {
    private static final long serialVersionUID = 9204749417247448509L;

    public static final String AUDIO_USER_DEFINED = "User Audio";

    public static final Integer DEFAULT_QUESTION_TEXT_COLOR = 0xFFBEBEBE;
    public static final Integer DEFAULT_ANSWER_TEXT_COLOR = 0xFFBEBEBE;
    public static final Integer DEFAULT_QUESTION_BACKGROUND_COLOR = 0xFF000000;
    public static final Integer DEFAULT_ANSWER_BACKGROUND_COLOR = 0xFF000000;
    public static final Integer DEFAULT_SEPARATOR_COLOR = 0xFF909090;
    public static final Integer DEFAULT_QA_RATIO = 50;
    @DatabaseField(generatedId = true)
    private Integer id = 1;

    @DatabaseField(defaultValue = "default", width = 8192)
    private String name = "default";

    @DatabaseField(defaultValue = "default description.", width = 8192)
    private String description = "default description.";

    @DatabaseField(defaultValue = "24")
    private Integer questionFontSize = 24;

    @DatabaseField(defaultValue = "24")
    private Integer answerFontSize = 24;

    @DatabaseField(defaultValue = "CENTER")
    private Align questionTextAlign = Align.CENTER;

    @DatabaseField(defaultValue = "CENTER")
    private Align answerTextAlign = Align.CENTER;

    @DatabaseField(defaultValue = "SINGLE_SIDED")
    private CardStyle cardStyle = CardStyle.SINGLE_SIDED;

    @DatabaseField(defaultValue = "50")
    private Integer qaRatio = DEFAULT_QA_RATIO;

    @DatabaseField(defaultValue = "US")
    private String questionAudio = "US";

    @DatabaseField(defaultValue = "US")
    private String answerAudio = "US";

    // 0xFFBEBEBE
    @DatabaseField(defaultValue = "-4276546")
    private Integer questionTextColor = DEFAULT_QUESTION_TEXT_COLOR;

    @DatabaseField(defaultValue = "-4276546")
    private Integer answerTextColor = DEFAULT_ANSWER_TEXT_COLOR;

    //0xFF0000000
    @DatabaseField(defaultValue = "-16777216")
    private Integer questionBackgroundColor = DEFAULT_QUESTION_BACKGROUND_COLOR;

    @DatabaseField(defaultValue = "-16777216")
    private Integer answerBackgroundColor = DEFAULT_ANSWER_BACKGROUND_COLOR;

    @DatabaseField(defaultValue = "-7303024")
    private Integer separatorColor = DEFAULT_SEPARATOR_COLOR;

    /* 1 = question, 2 = answer, 4 = note */
    @DatabaseField(defaultValue = "QUESTION,ANSWER,NOTE")
    private String displayInHTML = "QUESTION,ANSWER,NOTE";

    @DatabaseField(defaultValue = "false")
    private Boolean htmlLineBreakConversion = false;

    /* 1 = question, 2 = answer, 4 = note */
    @DatabaseField(defaultValue = "QUESTION")
    private String questionField = CardField.QUESTION.toString();

    /* 1 = question, 2 = answer, 4 = note */
    @DatabaseField(defaultValue = "ANSWER")
    private String answerField = CardField.ANSWER.toString();

    /* Empty = no font*/
    @DatabaseField(defaultValue = "")
    private String questionFont = "";

    /* Empty = no font*/
    @DatabaseField(defaultValue = "")
    private String answerFont = "";

    @DatabaseField(defaultValue = "")
    private String questionAudioLocation = "";

    @DatabaseField(defaultValue = "")
    private String answerAudioLocation = "";

    @DatabaseField(format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
    private Date creationDate = new Date();

    @DatabaseField(version = true, format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
    private Date updateDate = new Date();

    public Setting() {}

    public static enum Align {
        LEFT,
        CENTER,
        RIGHT,
        CENTER_LEFT,
        CENTER_RIGHT,
    }

    public static enum CardStyle {
        SINGLE_SIDED,
        DOUBLE_SIDED
    }

    public static enum CardField {
        QUESTION,
        ANSWER,
        NOTE
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuestionFontSize() {
        return questionFontSize;
    }

    public void setQuestionFontSize(Integer questionFontSize) {
        this.questionFontSize = questionFontSize;
    }

    public Integer getAnswerFontSize() {
        return answerFontSize;
    }

    public void setAnswerFontSize(Integer answerFontSize) {
        this.answerFontSize = answerFontSize;
    }

    public Align getQuestionTextAlign() {
        return questionTextAlign;
    }

    public void setQuestionTextAlign(Align questionTextAlign) {
        this.questionTextAlign = questionTextAlign;
    }

    public Align getAnswerTextAlign() {
        return answerTextAlign;
    }

    public void setAnswerTextAlign(Align answerTextAlign) {
        this.answerTextAlign = answerTextAlign;
    }

    public CardStyle getCardStyle() {
        return cardStyle;
    }

    public void setCardStyle(CardStyle cardStyle) {
        this.cardStyle = cardStyle;
    }

    public Integer getQaRatio() {
        return qaRatio;
    }

    public void setQaRatio(Integer qaRatio) {
        this.qaRatio = qaRatio;
    }

    public String getQuestionAudio() {
        return questionAudio;
    }

    public void setQuestionAudio(String questionAudio) {
        this.questionAudio = questionAudio;
    }

    public boolean isQuestionAudioEnabled(){
        return !Strings.isNullOrEmpty(getQuestionAudio());
    }

    public String getAnswerAudio() {
        return answerAudio;
    }

    public void setAnswerAudio(String answerAudio) {
        this.answerAudio = answerAudio;
    }

    public boolean isAnswerAudioEnabled(){
        return !Strings.isNullOrEmpty(getAnswerAudio());
    }

    public Integer getQuestionTextColor() {
        return questionTextColor;
    }

    public void setQuestionTextColor(Integer questionTextColor) {
        this.questionTextColor = questionTextColor;
    }

    public Integer getAnswerTextColor() {
        return answerTextColor;
    }

    public void setAnswerTextColor(Integer answerTextColor) {
        this.answerTextColor = answerTextColor;
    }

    public Integer getQuestionBackgroundColor() {
        return questionBackgroundColor;
    }

    public void setQuestionBackgroundColor(Integer questionBackgroundColor) {
        this.questionBackgroundColor = questionBackgroundColor;
    }

    public Integer getAnswerBackgroundColor() {
        return answerBackgroundColor;
    }

    public void setAnswerBackgroundColor(Integer answerBackgroundColor) {
        this.answerBackgroundColor = answerBackgroundColor;
    }

    public Integer getSeparatorColor() {
        return separatorColor;
    }

    public void setSeparatorColor(Integer separatorColor) {
        this.separatorColor = separatorColor;
    }

    public String getDisplayInHTML() {
        return displayInHTML;
    }

    public void setDisplayInHTML(String displayInHTML) {
        this.displayInHTML = displayInHTML;
    }

    public EnumSet<CardField> getDisplayInHTMLEnum() {
        return AMStringUtils.getEnumSetFromString(CardField.class, displayInHTML);
    }

    public void setDisplayInHTMLEnum(EnumSet<CardField> displayInHTMLEnum) {
        displayInHTML = AMStringUtils.getStringFromEnumSet(displayInHTMLEnum);
    }

    public Boolean getHtmlLineBreakConversion() {
        return htmlLineBreakConversion;
    }

    public void setHtmlLineBreakConversion(Boolean htmlLineBreakConversion) {
        this.htmlLineBreakConversion = htmlLineBreakConversion;
    }

    public String getQuestionField() {
        return questionField;
    }

    public void setQuestionField(String questionField) {
        this.questionField = questionField;
    }

    public EnumSet<CardField> getQuestionFieldEnum() {
        return AMStringUtils.getEnumSetFromString(CardField.class, questionField);
    }

    public void setQuestionFieldEnum(EnumSet<CardField> questionFieldEnum) {
        questionField = AMStringUtils.getStringFromEnumSet(questionFieldEnum);
    }

    public String getAnswerField() {
        return answerField;
    }

    public void setAnswerField(String answerField) {
        this.answerField = answerField;
    }

    public void setAnswerFieldEnum(EnumSet<CardField> answerFieldEnum) {
        answerField = AMStringUtils.getStringFromEnumSet(answerFieldEnum);
    }

    public EnumSet<CardField> getAnswerFieldEnum() {
        return AMStringUtils.getEnumSetFromString(CardField.class, answerField);
    }

    public void setAnswerEnum(EnumSet<CardField> answerFieldEnum) {
        answerField = AMStringUtils.getStringFromEnumSet(answerFieldEnum);
    }

    public String getQuestionFont() {
        return questionFont;
    }

    public void setQuestionFont(String questionFont) {
        this.questionFont = questionFont;
    }

    public String getAnswerFont() {
        return answerFont;
    }

    public void setAnswerFont(String answerFont) {
        this.answerFont = answerFont;
    }


    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getQuestionAudioLocation() {
        return questionAudioLocation;
    }

    public void setQuestionAudioLocation(String questionAudioLocation) {
        this.questionAudioLocation = questionAudioLocation;
    }

    public String getAnswerAudioLocation() {
        return answerAudioLocation;
    }

    public void setAnswerAudioLocation(String answerAudioLocation) {
        this.answerAudioLocation = answerAudioLocation;
    }

    public boolean isDefaultColor() {
        return (questionTextColor.equals(DEFAULT_QUESTION_TEXT_COLOR)) &&
               (answerTextColor.equals(DEFAULT_ANSWER_TEXT_COLOR)) &&
               (questionBackgroundColor.equals(DEFAULT_QUESTION_BACKGROUND_COLOR)) &&
               (answerBackgroundColor.equals(DEFAULT_ANSWER_BACKGROUND_COLOR)) &&
               (separatorColor.equals(DEFAULT_SEPARATOR_COLOR));
    }
}
