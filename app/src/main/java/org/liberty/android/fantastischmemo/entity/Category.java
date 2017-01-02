package org.liberty.android.fantastischmemo.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.liberty.android.fantastischmemo.dao.CategoryDaoImpl;

import java.util.Date;

@DatabaseTable(tableName = "categories", daoClass = CategoryDaoImpl.class)
public class Category implements VersionableDomainObject {
    @DatabaseField(generatedId = true)
    private Integer id = 1;

    @DatabaseField(defaultValue = "", width = 8192)
    private String name = "";

    @DatabaseField(version = true, format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
    private Date updateDate;

    /**
     * TODO: Implement it.
     */
    private Date creationDate;

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

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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
