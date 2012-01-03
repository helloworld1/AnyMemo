package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.SettingDaoImpl;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "settings", daoClass = SettingDaoImpl.class)
public class Setting {
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
    private Integer qaRatio = 50;

    @DatabaseField(defaultValue = "US")
    private String questionAudio = "US";

    @DatabaseField(defaultValue = "US")
    private String answerAudio = "US";
    
    @DatabaseField(defaultValue = "0xFFBEBEBE")
    private Integer questionTextColor = 0xFFBEBEBE;

    @DatabaseField(defaultValue = "0xFFBEBEBE")
    private Integer answerTextColor = 0xFFBEBEBE;

    @DatabaseField(defaultValue = "0xFF000000")
    private Integer questionBackgroundColor = 0xFF000000;

    @DatabaseField(defaultValue = "0xFF000000")
    private Integer answerBackgroundColor = 0xFF000000;

    @DatabaseField(defaultValue = "0xFF909090")
    private Integer separatorColor = 0xFF909090;

    /* 1 = question, 2 = answer, 4 = note */
    @DatabaseField(defaultValue = "7")
    private Long displayInHTML = 7L;

    @DatabaseField(defaultValue = "false")
    private Boolean htmlLineBreakConversion = false;

    /* 1 = question, 2 = answer, 4 = note */
    @DatabaseField(defaultValue = "1")
    private Long questionField = CardField.QUESTION;

    /* 1 = question, 2 = answer, 4 = note */
    @DatabaseField(defaultValue = "2")
    private Long answerField = CardField.ANSWER;

    /* Empty = no font*/
    @DatabaseField(defaultValue = "")
    private String questionFont = "";

    /* Empty = no font*/
    @DatabaseField(defaultValue = "")
    private String answerFont = "";

    /* Empty = no font*/
    @DatabaseField(defaultValue = "FORA")
    private DictApp dictApp = DictApp.FORA;

    @DatabaseField(defaultValue = "NONE")
    private ShuffleType shuffleType = ShuffleType.NONE;

    @DatabaseField(defaultValue = "TAP")
    private SpeakingType speakingType = SpeakingType.TAP;

    @DatabaseField(defaultValue = "")
    private String questionAudioLocation;

    @DatabaseField(defaultValue = "")
    private String answerAudioLocation;

    @DatabaseField
    private Date creationDate = new Date();

    @DatabaseField(version = true)
    private Date updateDate = new Date();

    public Setting() {}

    public static enum Align {
        LEFT,
        RIGHT,
        CENTER
    }

    public static enum CardStyle {
        SINGLE_SIDED,
        DOUBLE_SIDED
    }

    /* Use bitfield opertion on this class */
    public static class CardField {
        public static final long QUESTION = 1;
        public static final long ANSWER = 2;
        public static final long NOTE = 4;
        public static final long CATEGORY = 8;
    }

    public static enum DictApp {
        COLORDICT,
        FORA
    }

    public static enum ShuffleType {
        NONE,
        LOCAL
    }

    public static enum SpeakingType {
        MANUAL,
        TAP,
        AUTO,
        AUTOTAP
        
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

	public String getAnswerAudio() {
		return answerAudio;
	}

	public void setAnswerAudio(String answerAudio) {
		this.answerAudio = answerAudio;
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

	public Long getDisplayInHTML() {
		return displayInHTML;
	}

	public void setDisplayInHTML(Long displayInHTML) {
		this.displayInHTML = displayInHTML;
	}

	public Boolean getHtmlLineBreakConversion() {
		return htmlLineBreakConversion;
	}

	public void setHtmlLineBreakConversion(Boolean htmlLineBreakConversion) {
		this.htmlLineBreakConversion = htmlLineBreakConversion;
	}

	public Long getQuestionField() {
		return questionField;
	}

	public void setQuestionField(Long questionField) {
		this.questionField = questionField;
	}

	public Long getAnswerField() {
		return answerField;
	}

	public void setAnswerField(Long answerField) {
		this.answerField = answerField;
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

	public DictApp getDictApp() {
		return dictApp;
	}

	public void setDictApp(DictApp dictApp) {
		this.dictApp = dictApp;
	}

	public ShuffleType getShuffleType() {
		return shuffleType;
	}

	public void setShuffleType(ShuffleType shuffleType) {
		this.shuffleType = shuffleType;
	}

	public SpeakingType getSpeakingType() {
		return speakingType;
	}

	public void setSpeakingType(SpeakingType speakingType) {
		this.speakingType = speakingType;
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
}
