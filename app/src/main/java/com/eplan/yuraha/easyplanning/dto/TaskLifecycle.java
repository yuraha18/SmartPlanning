package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class TaskLifecycle {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private long dayFromId;
    private long dayToId;

    public TaskLifecycle() {
    }

    public long getSid() {
        return sid;
    }

    public TaskLifecycle(long sid, long taskId, long dayFromId, long dayToId) {
        this.sid = sid;
        this.taskId = taskId;
        this.dayFromId = dayFromId;
        this.dayToId = dayToId;
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

    public long getDayFromId() {
        return dayFromId;
    }

    public void setDayFromId(long dayFromId) {
        this.dayFromId = dayFromId;
    }

    public long getDayToId() {
        return dayToId;
    }

    public void setDayToId(long dayToId) {
        this.dayToId = dayToId;
    }

    @Override
    public String toString() {
        return "TaskLifecycle{" +
                "sid=" + sid +
                ", taskId=" + taskId +
                ", dayFromId=" + dayFromId +
                ", dayToId=" + dayToId +
                '}';
    }
}