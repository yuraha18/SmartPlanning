package com.eplan.yuraha.easyplanning.dto;

import com.google.gson.annotations.SerializedName;

public class Repeating {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private long dayId;
    private long localId;

    public Repeating() {
    }

    public Repeating(long sid, long taskId, long dayId) {
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
        return "Repeating{" +
                "sid=" + sid +
                ", taskId=" + taskId +
                ", dayId=" + dayId +
                ", localId=" + localId +
                '}';
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Repeating repeating = (Repeating) o;

        if (sid != repeating.sid) return false;
        if (taskId != repeating.taskId) return false;
        if (dayId != repeating.dayId) return false;
        return localId == repeating.localId;

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