package com.eplan.yuraha.easyplanning.ListAdapters;

/**
 * Created by yuraha18 on 2/24/2017.
 */

public class Goal {

    private String goalName;
    private String goalNote;
    private String deadline;
    private String id;

    public Goal(String goalName, String goalNote, String deadline, String id)
    {
        this.goalName = goalName;
        this.goalNote = goalNote;
        this.deadline = deadline;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoalName() {
        return goalName;
    }

    public String getGoalNote() {
        return goalNote;
    }

    public String getDeadline() {
        return deadline;
    }
}
