package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class Notification {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private long dayId;
    private long localId;

    public Notification() {
    }

    public long getSid() {
        return sid;
    }

    public Notification(long sid, long taskId, long dayId) {
        this.sid = sid;
        this.taskId = taskId;
        this.dayId = dayId;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "sid=" + sid +
                ", taskId=" + taskId +
                ", dayId=" + dayId +
                ", localId=" + localId +
                '}';
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Notification that = (Notification) o;

        if (sid != that.sid) return false;
        if (taskId != that.taskId) return false;
        if (dayId != that.dayId) return false;
        return localId == that.localId;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (int) (taskId ^ (taskId >>> 32));
        result = 31 * result + (int) (dayId ^ (dayId >>> 32));
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}
