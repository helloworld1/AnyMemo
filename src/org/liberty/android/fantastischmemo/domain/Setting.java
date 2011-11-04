package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.SettingDaoImpl;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "settings", daoClass = SettingDaoImpl.class)
public class Setting {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(defaultValue = "", width = 8192)
    private String name;

    @DatabaseField(defaultValue = "", width = 8192)
    private String description;

    @DatabaseField
    private Date creationDate;

    @DatabaseField(version = true)
    private Date updateDate;

    public Setting() {}

    public Integer getId() {
        return id;
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

    public void setId(Integer id) {
        this.id = id;
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
