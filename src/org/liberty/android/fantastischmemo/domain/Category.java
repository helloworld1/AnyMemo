package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.CategoryDaoImpl;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

import android.os.Parcel;
import android.os.Parcelable;

@DatabaseTable(tableName = "categories", daoClass = CategoryDaoImpl.class)
public class Category implements Parcelable {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(defaultValue = "", width = 8192)
    private String name;

    @DatabaseField(version = true)
    private Date updateDate;

    public Category() {}

    public Category(Parcel in) {
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

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeInt(id);
        out.writeString(name);
        out.writeSerializable(updateDate);
    }

     public static final Parcelable.Creator<Category> CREATOR
             = new Parcelable.Creator<Category>() {
         public Category createFromParcel(Parcel in) {
             Category c = new Category();
             c.setId(in.readInt());
             c.setName(in.readString());
             c.setUpdateDate((Date)in.readSerializable());
             return c;
         }

         public Category[] newArray(int size) {
             return new Category[size];
         }
     };

     @Override
     public int describeContents() {
         return 0;
     }
}
