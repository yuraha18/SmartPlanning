package com.eplan.yuraha.easyplanning.dto;

import com.google.gson.annotations.SerializedName;

public class Day {

    @SerializedName("id")
    private long sid;
    private String text;

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
}
