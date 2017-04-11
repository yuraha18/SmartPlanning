package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class InProgressGoal {

    @SerializedName("id")
    private long sid;
    private long goalId;


    public long getSid() {
        return sid;
    }

    public InProgressGoal(long sid, long goalId) {
        this.sid = sid;
        this.goalId = goalId;
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
}
