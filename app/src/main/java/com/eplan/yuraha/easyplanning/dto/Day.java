package com.eplan.yuraha.easyplanning.dto;

import com.google.gson.annotations.SerializedName;

public class Day {

    @SerializedName("id")
    private long sid;
    private String text;
    private long localId;

    public Day() {
    }

    public Day(long sid, String text) {
        this.sid = sid;
        this.text = text;
    }

    @Override
    public String toString() {
        return "Day{" +
                "sid=" + sid +
                ", text='" + text + '\'' +
                ", localId=" + localId +
                '}';
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

        Day day = (Day) o;

        if (sid != day.sid) return false;
        if (localId != day.localId) return false;
        return text != null ? text.equals(day.text) : day.text == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (sid ^ (sid >>> 32));
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (int) (localId ^ (localId >>> 32));
        return result;
    }
}
