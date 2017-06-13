package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class TaskToGoal {

    @SerializedName("id")
    private long sid;
    private long taskId;
    private long goalId;
    private long localId;

    public TaskToGoal() {
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @Override
    public String toString() {
        return "TaskToGoal{" +
                "sid=" + sid +
                ", taskId=" + taskId +
                ", goalId=" + goalId +
                ", localId=" + localId +
                '}';
    }

    public long getSid() {
        return sid;
    }

    public TaskToGoal(long sid, long taskId, long goalId) {
        this.sid = sid;
        this.taskId = taskId;
        this.goalId = goalId;
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

    public long getGoalId() {
        return goalId;
    }

    public void setGoalId(long goalId) {
        this.goalId = goalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskToGoal that = (TaskToGoal) o;

        if (sid != that.sid) return false;
        if (taskId != that.taskId) return false;
        if (goalId != that.goalId) return false;
        return localId == that.localId;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (int) (taskId ^ (taskId >>> 32));
        result = 31 * result + (int) (goalId ^ (goalId >>> 32));
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}
