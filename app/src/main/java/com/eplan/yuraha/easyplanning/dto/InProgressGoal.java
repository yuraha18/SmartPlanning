package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class InProgressGoal {

    @SerializedName("id")
    private long sid;
    private long goalId;
    private long localId;


    public long getSid() {
        return sid;
    }

    public InProgressGoal(long sid, long goalId) {
        this.sid = sid;
        this.goalId = goalId;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @Override
    public String toString() {
        return "InProgressGoal{" +
                "sid=" + sid +
                ", goalId=" + goalId +
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InProgressGoal that = (InProgressGoal) o;

        if (sid != that.sid) return false;
        if (goalId != that.goalId) return false;
        return localId == that.localId;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (int) (goalId ^ (goalId >>> 32));
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}
