package com.eplan.yuraha.easyplanning.ListAdapters;

import java.util.ArrayList;

/**
 * Created by yuraha18 on 2/24/2017.
 */

public class Task {

    private String taskText;
    private int priority;
    private boolean isDone;
    private ArrayList<String> goals;
    private long id;

    public Task(long id, String taskText, int priority, boolean isDone, ArrayList<String> goals)
    {
        this.id = id;
        this.taskText = taskText;
        this.priority = priority;
        this.isDone = isDone;
        this.goals = goals;
    }

    public String getTaskText() {
        return taskText;
    }

    public int getPriority() {
        return priority;
    }


    public boolean isDone() {
        return isDone;
    }


    public ArrayList<String> getGoals() {
        return goals;
    }


    public long getId() {
        return id;
    }

}
