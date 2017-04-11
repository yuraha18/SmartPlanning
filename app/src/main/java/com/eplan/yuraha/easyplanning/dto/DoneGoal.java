package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class DoneGoal {

    @SerializedName("id")
    private long sid;
    private long goalId;
    private long dayId;

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
}
