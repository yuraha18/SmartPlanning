package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class DoneGoal {

    @SerializedName("id")
    private long sid;
    private long goalId;
    private long dayId;
    private long localId;

    public DoneGoal() {
    }

    public long getSid() {
        return sid;
    }

    public DoneGoal(long sid, long goalId, long dayId) {
        this.sid = sid;
        this.goalId = goalId;
        this.dayId = dayId;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @Override
    public String toString() {
        return "DoneGoal{" +
                "sid=" + sid +
                ", goalId=" + goalId +
                ", dayId=" + dayId +
                ", localId=" + localId +
                '}';
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public long getGoalId() {
        return goalId;
    }

    public void setGoalId(long goalId) {
        this.goalId = goalId;
    }

    public long getDayId() {
        return dayId;
    }

    public void setDayId(long dayId) {
        this.dayId = dayId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoneGoal doneGoal = (DoneGoal) o;

        if (sid != doneGoal.sid) return false;
        if (goalId != doneGoal.goalId) return false;
        if (dayId != doneGoal.dayId) return false;
        return localId == doneGoal.localId;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (int) (goalId ^ (goalId >>> 32));
        result = 31 * result + (int) (dayId ^ (dayId >>> 32));
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}
