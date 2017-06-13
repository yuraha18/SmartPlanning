package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class DTOTask {


    @SerializedName("id")
    private long sid;
    private String taskText;
    private int priority;
    private long localId;

    public DTOTask() {
    }

    public DTOTask(long sid, String taskText, int priority) {
        this.sid = sid;
        this.taskText = taskText;
        this.priority = priority;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    @Override
    public String toString() {
        return "DTOTask{" +
                "sid=" + sid +
                ", taskText='" + taskText + '\'' +
                ", priority=" + priority +
                ", localId=" + localId +
                '}';
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

        DTOTask dtoTask = (DTOTask) o;

        if (sid != dtoTask.sid) return false;
        if (priority != dtoTask.priority) return false;
        if (localId != dtoTask.localId) return false;
        return taskText != null ? taskText.equals(dtoTask.taskText) : dtoTask.taskText == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (taskText != null ? taskText.hashCode() : 0);
        result = 31 * result + priority;
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}
