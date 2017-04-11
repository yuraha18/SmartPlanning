package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class MonthRepeating {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private int dayOfMonth;

    public MonthRepeating() {
    }

    public long getSid() {
        return sid;
    }

    public MonthRepeating(long sid, long taskId, int dayOfMonth) {
        this.sid = sid;
        this.taskId = taskId;
        this.dayOfMonth = dayOfMonth;
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

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }
}
