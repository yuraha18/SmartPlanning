package com.eplan.yuraha.easyplanning;

public class DayStatistic
{
    private int countDone;
    private int countInProgress;

    public DayStatistic(int countDone, int countInProgress) {
        this.countDone = countDone;
        this.countInProgress = countInProgress;
    }

    public int getCountDone() {
        return countDone;
    }

    public int getCountInProgress() {
        return countInProgress;
    }
}
