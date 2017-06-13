package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class MonthRepeating {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private int dayOfMonth;
    private long localId;

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

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @Override
    public String toString() {
        return "MonthRepeating{" +
                "sid=" + sid +
                ", taskId=" + taskId +
                ", dayOfMonth=" + dayOfMonth +
                ", localId=" + localId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonthRepeating that = (MonthRepeating) o;

        if (sid != that.sid) return false;
        if (taskId != that.taskId) return false;
        if (dayOfMonth != that.dayOfMonth) return false;
        return localId == that.localId;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (int) (taskId ^ (taskId >>> 32));
        result = 31 * result + dayOfMonth;
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}
