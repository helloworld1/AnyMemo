package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.CardDaoImpl;

import com.google.common.base.Objects;
import com.j256.ormlite.field.DataType;
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
    private String question = "";

    @DatabaseField(defaultValue = "", width = 8192)
    private String answer = "";

    @DatabaseField(defaultValue = "", width = 8192)
    private String note = "";

    /* Category = 1 should be uncategorized */
    @DatabaseField(foreign = true, index = true)
    private Category category;

    @DatabaseField(foreign = true)
    private LearningData learningData;

    @DatabaseField(defaultValue = "0")
    private Integer cardType = 0;

    @DatabaseField(format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
    private Date creationDate;

    @DatabaseField(version = true, format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
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
        if (question == null) {
            return "";
        }

        return question;
    }

    public void setQuestion(String question) {
        if (question == null) {
            this.question = "";
        } else{
            this.question = question;
        }
    }

    public String getAnswer() {
        if (answer == null ) {
            return "";
        }
        return answer;
    }

    public void setAnswer(String answer) {
        if (answer == null) {
            this.answer = "";
        } else {
            this.answer = answer;
        }
    }

    public String getNote() {
        if (note == null) {
            return "";
        } else {
            return note;
        }
    }

    public void setNote(String note) {
        if (note == null) {
            this.note = "";
        } else {
            this.note = note;
        }
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
    public String toString() {
        return Objects.toStringHelper(this)
            .add("id", id)
            .add("question", question)
            .add("answer", answer)
            .toString();
    }

    @Override
    public boolean equals(Object c) {
        Card card2 = (Card)c;
        return this.getId().equals(card2.getId());
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
