package com.eplan.yuraha.easyplanning.dto;


import com.google.gson.annotations.SerializedName;

public class DTOGoal {

    @SerializedName("id")
    private long sid;
    private String goalText;
    private String notice;
    private long deadline;
    private long localId;

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

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @Override
    public String toString() {
        return "DTOGoal{" +
                "sid=" + sid +
                ", goalText='" + goalText + '\'' +
                ", notice='" + notice + '\'' +
                ", deadline=" + deadline +
                ", localId=" + localId +
                '}';
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DTOGoal dtoGoal = (DTOGoal) o;

        if (sid != dtoGoal.sid) return false;
        if (deadline != dtoGoal.deadline) return false;
        if (localId != dtoGoal.localId) return false;
        if (goalText != null ? !goalText.equals(dtoGoal.goalText) : dtoGoal.goalText != null)
            return false;
        return notice != null ? notice.equals(dtoGoal.notice) : dtoGoal.notice == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (goalText != null ? goalText.hashCode() : 0);
        result = 31 * result + (notice != null ? notice.hashCode() : 0);
        result = 31 * result + (int) (deadline ^ (deadline >>> 32));
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
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

