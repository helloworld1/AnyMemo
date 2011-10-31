package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.CardDao;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cards", daoClass = CardDao.class)
public class Card {
    @DatabaseField(id = true)
    private Integer id;

    @DatabaseField(defaultValue = "", width = 8192)
    private String question;

    @DatabaseField(defaultValue = "", width = 8192)
    private String answer;

    @DatabaseField(defaultValue = "", width = 8192)
    private String note;

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

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }






}
