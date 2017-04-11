package com.eplan.yuraha.easyplanning;

import java.util.HashMap;

/**
 * Created by yuraha18 on 3/5/2017.
 */

public final class Constants {
    public static final String PREF_TURN_ON_NOTIFICATIONS = "pref_turn_on_notifications";
    public static final String PREF_RINGTONE = "pref_tone";
    public static final String PREF_VIBRATION = "pref_vibration";
    public static final String PREF_REMIND_ME = "pref_remind_about_planning";
    public static final String PREF_SET_REMIND_TIME = "pref_set_time";
    public static final String PREF_LANGUAGE= "pref_language";
    public static final String PREF_THEME = "pref_theme";

    public static final String DB_TABLE_ID = "_id";

    /* Constants for Preference TABLE IN DB*/
    public static final String DB_PREF_TABLE = "Preference";
    public static final String DB_PREF_LANGUAGE = "LANGUAGE";
    public static final String DB_PREF_THEME = "THEME";
    public static final String DB_PREF_IS_SET_NOTIFICATIONS = "IS_SET_NOTIFICATION";
    public static final String DB_NOTIF_RINGTONE = "RINGTONE";
    public static final String DB_NOTIF_VIBRATION = "VIBRATION";
    public static final String DB_NOTIF_IS_SET_REMINDTIME = "IS_SET_REMIND_TIME";
    public static final String DB_NOTIF_REMIND_TIME = "REMIND_TIME";

    public static final String DATEFORMAT = "dd-MM-yyyy";


    public static final String LAST_SYNCH_TIME = "lastSynchTime";

    public static  HashMap<String, Integer> dbTables ;

    public static final String PROPERTIES_PATH = ".properties";
    public static final String LAST_UPDATE_TIME = "lastUpdateTime";
    public static final String USER_ID = "user_id" ;

    public static final String TASK_TABLE = "Tasks";
    public static final String DONE_TASKS_TABLE = "DoneTask";
    public static final String IN_PROGRESS_TASKS_TABLE = "InProgressTask";
    public static final String REPEATING_TABLE = "Repeating";
    public static final String MONTH_REPEATING_TABLE = "MonthRepeating";
    public static final String REMINDING_TABLE = "Reminding";
    public static final String GOALS_TABLE = "DTOGoal";
    public static final String DONE_GOALS_TABLE = "DoneGoal";
    public static final String IN_PROGRESS_GOALS_TABLE = "InProgressGoal";
    public static final String TASK_TO_GOAL_TABLE = "TaskToGoal";
    public static final String DAYS_TABLE = "Day";
    public static final String DELETED_TASKS_TABLE = "DeletedTask";
    public static final String TASK_LIFECYCLE_TABLE = "TaskLifecycle";
    public static final String NOTIFICATIONS_TABLE = "Notification";

    static  {
        dbTables = new HashMap<>();
        dbTables.put(TASK_TABLE, 1);
        dbTables.put(REMINDING_TABLE, 2);
        dbTables.put(DAYS_TABLE, 3);
        dbTables.put(GOALS_TABLE, 4);
        dbTables.put(DONE_TASKS_TABLE, 5);
        dbTables.put(IN_PROGRESS_TASKS_TABLE, 6);
        dbTables.put(REPEATING_TABLE, 7);
        dbTables.put(MONTH_REPEATING_TABLE, 8);
        dbTables.put(IN_PROGRESS_GOALS_TABLE, 9);
        dbTables.put(TASK_TO_GOAL_TABLE, 10);
        dbTables.put(DONE_GOALS_TABLE, 11);
        dbTables.put(DELETED_TASKS_TABLE, 12);
        dbTables.put(TASK_LIFECYCLE_TABLE, 13);
        dbTables.put(NOTIFICATIONS_TABLE, 14);
    }

    public static final int TASK_TABLE_ID = dbTables.get(Constants.TASK_TABLE);
    public static final int DAYS_TABLE_ID = dbTables.get(Constants.DAYS_TABLE);
    public static final int GOALS_TABLE_ID =  dbTables.get(Constants.GOALS_TABLE);


}
