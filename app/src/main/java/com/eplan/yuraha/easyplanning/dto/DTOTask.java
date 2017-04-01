package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class DTOTask {


    @SerializedName("id")
    private long sid;
    private String taskText;
    private int priority;

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
                '}';
    }
}
