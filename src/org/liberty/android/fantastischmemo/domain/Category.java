package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.CategoryDaoImpl;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "categories", daoClass = CategoryDaoImpl.class)
public class Category {
    @DatabaseField(generatedId = true)
    private Integer id = 1;

    @DatabaseField(defaultValue = "", width = 8192)
    private String name = "";

    @DatabaseField(version = true)
    private Date updateDate;

    public Category() {}

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

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public boolean equals(Object o) {
        Category cc = (Category)o;
        if (cc == null) {
            return false;
        }
        return this.getName().equals(cc.getName());
    }

}
