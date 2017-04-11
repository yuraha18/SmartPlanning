package com.eplan.yuraha.easyplanning.dto;

import com.google.gson.annotations.SerializedName;

public class Reminding {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private String time;

    public long getSid() {
        return sid;
    }

    public Reminding() {
    }

    public Reminding(long sid, long taskId, String time) {
        this.sid = sid;
        this.taskId = taskId;
        this.time = time;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
