package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class DTOGoal {

    @SerializedName("id")
    private long sid;
    private String goalText;
    private String notice;
    private long deadline;

    public DTOGoal() {
    }

    public long getSid() {
        return sid;
    }

    public DTOGoal(long sid, String goalText, String notice, long deadline) {
        this.sid = sid;
        this.goalText = goalText;
        this.notice = notice;
        this.deadline = deadline;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public String getGoalText() {
        return goalText;
    }

    public void setGoalText(String goalText) {
        this.goalText = goalText;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }
}

