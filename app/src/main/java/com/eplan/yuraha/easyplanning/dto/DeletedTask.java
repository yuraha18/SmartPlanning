package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class DeletedTask {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private long dayId;
    private long localId;

    public DeletedTask() {
    }

    public DeletedTask(long sid, long taskId, long dayId) {
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
        return "DeletedTask{" +
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

        DeletedTask that = (DeletedTask) o;

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
