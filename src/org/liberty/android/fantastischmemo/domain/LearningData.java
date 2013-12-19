package org.liberty.android.fantastischmemo.domain;

import java.util.Date;

import org.liberty.android.fantastischmemo.dao.LearningDataDaoImpl;
import org.liberty.android.fantastischmemo.utils.AMDateUtil;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "learning_data", daoClass = LearningDataDaoImpl.class)
public class LearningData {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(defaultValue = "2010-01-01 00:00:00.000000", format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
    private Date lastLearnDate = new Date(1262304000000L);

    @DatabaseField(defaultValue = "2010-01-01 00:00:00.000000", format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
    private Date nextLearnDate = new Date(1262304000000L);

    @DatabaseField
    private Integer grade = 3;

    @DatabaseField(defaultValue = "2.5")
    private Float easiness = 0.0f;

    @DatabaseField
    private Integer acqReps = 0;

    @DatabaseField
    private Integer retReps = 0;

    @DatabaseField
    private Integer lapses = 0;

    @DatabaseField
    private Integer acqRepsSinceLapse = 0;

    @DatabaseField
    private Integer retRepsSinceLapse = 0;

    @DatabaseField(version = true, format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
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

    public void cloneFromLearningData(LearningData ld) {
        setAcqReps(ld.getAcqReps());
        setAcqRepsSinceLapse(ld.getAcqRepsSinceLapse());
        setEasiness(ld.getEasiness());
        setGrade(ld.getGrade());
        setLapses(ld.getLapses());
        setLastLearnDate(ld.getLastLearnDate());
        setNextLearnDate(ld.getNextLearnDate());
        setRetReps(ld.getRetReps());
        setRetRepsSinceLapse(ld.getRetRepsSinceLapse());
    }

    public double getInterval() {
        return AMDateUtil.diffDate(getLastLearnDate(), getNextLearnDate());
    }

	@Override
	public String toString() {
		return "LearningData [id=" + id + ", lastLearnDate=" + lastLearnDate
				+ ", nextLearnDate=" + nextLearnDate + ", grade=" + grade
				+ ", easiness=" + easiness + ", acqReps=" + acqReps
				+ ", retReps=" + retReps + ", lapses=" + lapses
				+ ", acqRepsSinceLapse=" + acqRepsSinceLapse
				+ ", retRepsSinceLapse=" + retRepsSinceLapse + ", updateDate="
				+ updateDate + "]";
	}
}
