package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.StatDaoImpl;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stats", daoClass = StatDaoImpl.class)
public class Stat {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField
    private Integer newGrade;

    @DatabaseField
    private Integer oldGrade;

    @DatabaseField
    private Integer newInterval;

    @DatabaseField
    private Integer oldInterval;

    @DatabaseField(version = true, format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
    private Date updateDate;

    public Stat() {}

}
