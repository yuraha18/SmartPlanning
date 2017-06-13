package com.eplan.yuraha.easyplanning.dto;

import com.google.gson.annotations.SerializedName;

public class Reminding {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private String time;
    private long localId;

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

    @Override
    public String toString() {
        return "Reminding{" +
                "sid=" + sid +
                ", taskId=" + taskId +
                ", time='" + time + '\'' +
                '}';
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reminding reminding = (Reminding) o;

        if (sid != reminding.sid) return false;
        if (taskId != reminding.taskId) return false;
        if (localId != reminding.localId) return false;
        return time != null ? time.equals(reminding.time) : reminding.time == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (int) (taskId ^ (taskId >>> 32));
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}
