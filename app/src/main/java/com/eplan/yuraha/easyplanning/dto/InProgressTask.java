package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class InProgressTask {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private long localId;

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

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @Override
    public String toString() {
        return "InProgressTask{" +
                "sid=" + sid +
                ", taskId=" + taskId +
                ", localId=" + localId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InProgressTask that = (InProgressTask) o;

        if (sid != that.sid) return false;
        if (taskId != that.taskId) return false;
        return localId == that.localId;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (int) (taskId ^ (taskId >>> 32));
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}

