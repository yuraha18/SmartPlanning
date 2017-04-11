package com.eplan.yuraha.easyplanning.dto;

import com.google.gson.annotations.SerializedName;

public class DoneTask {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private long dayId;

    public DoneTask() {
    }

    public DoneTask(long sid, long taskId, long dayId) {
        this.sid = sid;
        this.taskId = taskId;
        this.dayId = dayId;
    }

    public long getSid() {
        return sid;
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

    public long getDayId() {
        return dayId;
    }

    public void setDayId(long dayId) {
        this.dayId = dayId;
    }
}
