package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class TaskToGoal {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private long goalId;

    public TaskToGoal() {
    }

    public long getSid() {
        return sid;
    }

    public TaskToGoal(long sid, long taskId, long goalId) {
        this.sid = sid;
        this.taskId = taskId;
        this.goalId = goalId;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getGoalId() {
        return goalId;
    }

    public void setGoalId(long goalId) {
        this.goalId = goalId;
    }
}
