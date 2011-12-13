package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.LearningDataDaoImpl;

import org.liberty.android.fantastischmemo.domain.LearningData;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

import android.os.Parcel;
import android.os.Parcelable;

@DatabaseTable(tableName = "learning_data", daoClass = LearningDataDaoImpl.class)
public class LearningData implements Parcelable {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(defaultValue = "2010-01-01 00:00:00.000000")
    private Date lastLearnDate;

    @DatabaseField
    private Date nextLearnDate;

    @DatabaseField
    private Integer grade;

    @DatabaseField(defaultValue = "2.5")
    private Float easiness;

    @DatabaseField
    private Integer acqReps;

    @DatabaseField
    private Integer retReps;

    @DatabaseField
    private Integer lapses;

    @DatabaseField
    private Integer acqRepsSinceLapse;

    @DatabaseField
    private Integer retRepsSinceLapse;

    @DatabaseField(version = true)
    private Date updateDate;

    public LearningData() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getLastLearnDate() {
		return lastLearnDate;
	}

	public void setLastLearnDate(Date lastLearnDate) {
		this.lastLearnDate = lastLearnDate;
	}

	public Date getNextLearnDate() {
		return nextLearnDate;
	}

	public void setNextLearnDate(Date nextLearnDate) {
		this.nextLearnDate = nextLearnDate;
	}

	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	public Float getEasiness() {
		return easiness;
	}

	public void setEasiness(Float easiness) {
		this.easiness = easiness;
	}

	public Integer getAcqReps() {
		return acqReps;
	}

	public void setAcqReps(Integer acqReps) {
		this.acqReps = acqReps;
	}

	public Integer getRetReps() {
		return retReps;
	}

	public void setRetReps(Integer retReps) {
		this.retReps = retReps;
	}

	public Integer getLapses() {
		return lapses;
	}

	public void setLapses(Integer lapses) {
		this.lapses = lapses;
	}

	public Integer getAcqRepsSinceLapse() {
		return acqRepsSinceLapse;
	}

	public void setAcqRepsSinceLapse(Integer acqRepsSinceLapse) {
		this.acqRepsSinceLapse = acqRepsSinceLapse;
	}

	public Integer getRetRepsSinceLapse() {
		return retRepsSinceLapse;
	}

	public void setRetRepsSinceLapse(Integer retRepsSinceLapse) {
		this.retRepsSinceLapse = retRepsSinceLapse;
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
        out.writeSerializable(lastLearnDate);
        out.writeSerializable(nextLearnDate);
        out.writeInt(grade);
        out.writeFloat(easiness);
        out.writeInt(acqReps);
        out.writeInt(retReps);
        out.writeInt(lapses);
        out.writeInt(acqRepsSinceLapse);
        out.writeInt(retRepsSinceLapse);
        out.writeSerializable(updateDate);
    }

     public static final Parcelable.Creator<LearningData> CREATOR
             = new Parcelable.Creator<LearningData>() {
         public LearningData createFromParcel(Parcel in) {
             LearningData ld = new LearningData();
             ld.setId(in.readInt());
             ld.setLastLearnDate((Date)in.readSerializable());
             ld.setNextLearnDate((Date)in.readSerializable());
             ld.setGrade(in.readInt());
             ld.setEasiness(in.readFloat());
             ld.setAcqReps(in.readInt());
             ld.setRetReps(in.readInt());
             ld.setLapses(in.readInt());
             ld.setAcqRepsSinceLapse(in.readInt());
             ld.setRetRepsSinceLapse(in.readInt());
             ld.setUpdateDate((Date)in.readSerializable());
             return ld;

         }

         public LearningData[] newArray(int size) {
             return new LearningData[size];
         }
     };

     @Override
     public int describeContents() {
         return 0;
     }


}
