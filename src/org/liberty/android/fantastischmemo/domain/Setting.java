package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.SettingDaoImpl;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "settings", daoClass = SettingDaoImpl.class)
public class Setting {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(defaultValue = "default", width = 8192)
    private String name;

    @DatabaseField(defaultValue = "default description.", width = 8192)
    private String description;

    @DatabaseField(defaultValue = "24")
    private Integer questionFontSize;

    @DatabaseField(defaultValue = "24")
    private Integer answerFontSize;

    /* 1 = left, 2 = center, 3 = right */
    @DatabaseField(defaultValue = "2")
    private Integer questionTextAlign;

    /* 1 = single sided, 2 = double sided. */
    @DatabaseField(defaultValue = "1")
    private Integer cardStyle;

    @DatabaseField(defaultValue = "50")
    private Integer qaRatio;


    @DatabaseField(defaultValue = "US")
    private String questionAudio;

    @DatabaseField(defaultValue = "US")
    private String answerAudio; 
    
    @DatabaseField(defaultValue = "0xFFBEBEBE")
    private String questionTextColor;

    @DatabaseField(defaultValue = "0xFFBEBEBE")
    private String answerTextColor;

    @DatabaseField(defaultValue = "0xFF000000")
    private String questionBackgroundColor;

    @DatabaseField(defaultValue = "0xFF000000")
    private String answerBackgroundColor;

    @DatabaseField(defaultValue = "0xFF909090")
    private String separatorColor;


    /* 1 = question, 2 = answer, 3 = both */
    @DatabaseField(defaultValue = "3")
    private Integer displayInHTML;

    @DatabaseField(defaultValue = "false")
    private Boolean htmlLineBreakConversion;

    /* 1 = question, 2 = answer, 4 = note */
    @DatabaseField(defaultValue = "1")
    private Integer questionField;

    /* 1 = question, 2 = answer, 4 = note */
    @DatabaseField(defaultValue = "2")
    private Integer answerField;

    /* Empty = no font*/
    @DatabaseField(defaultValue = "")
    private String questionFont;

    /* Empty = no font*/
    @DatabaseField(defaultValue = "")
    private String answerFont;

    @DatabaseField
    private Date creationDate;

    @DatabaseField(version = true)
    private Date updateDate;

    public Setting() {}

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

	public Integer getQuestionTextAlign() {
		return questionTextAlign;
	}

	public void setQuestionTextAlign(Integer questionTextAlign) {
		this.questionTextAlign = questionTextAlign;
	}

	public Integer getCardStyle() {
		return cardStyle;
	}

	public void setCardStyle(Integer cardStyle) {
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

	public String getQuestionTextColor() {
		return questionTextColor;
	}

	public void setQuestionTextColor(String questionTextColor) {
		this.questionTextColor = questionTextColor;
	}

	public String getAnswerTextColor() {
		return answerTextColor;
	}

	public void setAnswerTextColor(String answerTextColor) {
		this.answerTextColor = answerTextColor;
	}

	public String getQuestionBackgroundColor() {
		return questionBackgroundColor;
	}

	public void setQuestionBackgroundColor(String questionBackgroundColor) {
		this.questionBackgroundColor = questionBackgroundColor;
	}

	public String getAnswerBackgroundColor() {
		return answerBackgroundColor;
	}

	public void setAnswerBackgroundColor(String answerBackgroundColor) {
		this.answerBackgroundColor = answerBackgroundColor;
	}

	public String getSeparatorColor() {
		return separatorColor;
	}

	public void setSeparatorColor(String separatorColor) {
		this.separatorColor = separatorColor;
	}

	public Integer getDisplayInHTML() {
		return displayInHTML;
	}

	public void setDisplayInHTML(Integer displayInHTML) {
		this.displayInHTML = displayInHTML;
	}

	public Boolean getHtmlLineBreakConversion() {
		return htmlLineBreakConversion;
	}

	public void setHtmlLineBreakConversion(Boolean htmlLineBreakConversion) {
		this.htmlLineBreakConversion = htmlLineBreakConversion;
	}

	public Integer getQuestionField() {
		return questionField;
	}

	public void setQuestionField(Integer questionField) {
		this.questionField = questionField;
	}

	public Integer getAnswerField() {
		return answerField;
	}

	public void setAnswerField(Integer answerField) {
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
    
}
