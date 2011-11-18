package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.CardDaoImpl;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cards", daoClass = CardDaoImpl.class)
public class Card {
    @DatabaseField(generatedId = true)
    private Integer id;

    /* The actual card ordinal in a deck */
    @DatabaseField(index = true)
    private Integer ordinal;

    @DatabaseField(defaultValue = "", width = 8192)
    private String question;

    @DatabaseField(defaultValue = "", width = 8192)
    private String answer;

    @DatabaseField(defaultValue = "", width = 8192)
    private String note;

    @DatabaseField(foreign = true, index = true)
    private Category category;

    @DatabaseField(foreign = true)
    private LearningData learningData;

    @DatabaseField
    private Integer cardType;

    @DatabaseField
    private Date creationDate;

    @DatabaseField(version = true)
    private Date updateDate;

    public Card() {}


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public Category getCategory() {
        return category;
    }

    public Integer getCardType() {
        return cardType;
    }

    public void setCardType(Integer cardType) {
        this.cardType = cardType;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }


	public LearningData getLearningData() {
		return learningData;
	}


	public void setLearningData(LearningData learningData) {
		this.learningData = learningData;
	}

    @Override
    public boolean equals(Object c) {
        Card card2 = (Card)c;
        return this.getId().equals(card2.getId());
    }
}
