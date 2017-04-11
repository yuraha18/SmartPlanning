package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class InProgressTask {

    @SerializedName("id")
    private long sid;
    private long taskId;

    public InProgressTask() {
    }

    public long getSid() {
        return sid;
    }

    public InProgressTask(long sid, long taskId) {
        this.sid = sid;
        this.taskId = taskId;
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
}

