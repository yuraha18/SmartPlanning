package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class TaskLifecycle {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private long dayFromId;
    private long dayToId;
    private long localId;

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

    @Override
    public String toString() {
        return "TaskLifecycle{" +
                "sid=" + sid +
                ", taskId=" + taskId +
                ", dayFromId=" + dayFromId +
                ", dayToId=" + dayToId +
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

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskLifecycle that = (TaskLifecycle) o;

        if (sid != that.sid) return false;
        if (taskId != that.taskId) return false;
        if (dayFromId != that.dayFromId) return false;
        if (dayToId != that.dayToId) return false;
        return localId == that.localId;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (int) (taskId ^ (taskId >>> 32));
        result = 31 * result + (int) (dayFromId ^ (dayFromId >>> 32));
        result = 31 * result + (int) (dayToId ^ (dayToId >>> 32));
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}