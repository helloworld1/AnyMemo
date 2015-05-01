package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.FilterDaoImpl;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "filters", daoClass = FilterDaoImpl.class)
public class Filter {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(defaultValue = "", width = 8192)
    private String name;

    @DatabaseField(defaultValue = "", width = 8192)
    private String expression;

    @DatabaseField(defaultValue = "0")
    private Boolean isActive;

    @DatabaseField(version = true)
    private Date updateDate;

    public Filter() {}

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

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
