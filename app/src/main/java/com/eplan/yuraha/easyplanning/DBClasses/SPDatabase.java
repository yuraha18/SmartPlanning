package com.eplan.yuraha.easyplanning.DBClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eplan.yuraha.easyplanning.API.DBSynchronizer;
import com.eplan.yuraha.easyplanning.AddTaskFragment;
import com.eplan.yuraha.easyplanning.Constants;
import com.eplan.yuraha.easyplanning.TaskListFragment;


public class SPDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SPdatabase.db";

    public SPDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /* Table contains main info about goals*/
        db.execSQL("CREATE TABLE Tasks ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_TEXT TEXT, "
                + "PRIORITY INTEGER);");

        /* Save id's all doneTasks*/
        db.execSQL("CREATE TABLE DoneTask ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "DAY_ID INTEGER);");

        /* Save id's all notDoneTasks*/
        db.execSQL("CREATE TABLE InProgressTask ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER);");

        /* Information about repeating some tasks in future*/
        db.execSQL("CREATE TABLE Repeating ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "DAY_ID INTEGER);");

        /* Information about month repeating  */
        db.execSQL("CREATE TABLE MonthRepeating ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "DAY_OF_MONTH INTEGER);");

         /* Table with reminding information. Contains time for remind and it's one*/
        db.execSQL("CREATE TABLE Reminding ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "TIME TEXT);");

         /* Table with goals info*/
        db.execSQL("CREATE TABLE Goal("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "GOAL_TEXT TEXT, "
                + "NOTICE TEXT, "
                + "DEADLINE INTEGER);");

           /* Save id's all doneGoals*/
        db.execSQL("CREATE TABLE DoneGoal ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "GOAL_ID INTEGER, "
                + "DAY_ID INTEGER);");

        /* Save id's all goals in progress*/
        db.execSQL("CREATE TABLE InProgressGoal ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "GOAL_ID INTEGER);");

          /* This one connecting tasks with goals*/
        db.execSQL("CREATE TABLE TaskToGoal("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "GOAL_ID INTEGER);");

         /* All days have their own id used in other tables */
        db.execSQL("CREATE TABLE Day("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "DAY TEXT);");


        /* If user delete repeated task from some day, it check in here */
        db.execSQL("CREATE TABLE DeletedTask("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "DAY_ID INTEGER);");

        /* Tasks Lifecycle: from day when it was created to deleted day */
        db.execSQL("CREATE TABLE TaskLifecycle("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "DAY_FROM_ID INTEGER, "
                + "DAY_TO_ID INTEGER);");

        /* Tables with Preferences. Will be using with Preferences*/
        db.execSQL("CREATE TABLE Preference("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "LANGUAGE INTEGER, "
                + "THEME INTEGER, "
                + "IS_SET_NOTIFICATION INTEGER, "
                + "RINGTONE TEXT, "
                + "VIBRATION INTEGER, "
                + "IS_SET_REMIND_TIME INTEGER, "
                + "REMIND_TIME TEXT);");

        /* Table with statistic information */
        db.execSQL("CREATE TABLE Statistic("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "DAY_ID INTEGER, "
                + "COUNT_DONE INTEGER, "
                + "COUNT_IN_PROGRESS INTEGER);");

         /* Table with Notification ids. This id is taskId (which is unique)*/
        db.execSQL("CREATE TABLE Notification("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "DAY_ID INTEGER);");

        /* In this table are information about whole rows not sync with server */
        db.execSQL("CREATE TABLE SyncData("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "ROW_ID INTEGER, "
                + "TABLE_ID INTEGER);");

         /*  */
        db.execSQL("CREATE TABLE Adapter("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TABLE_ID INTEGER,"
                + "LOCAL_ID INTEGER, "
                + "SID INTEGER);");




        DBHelper.addToDateTable(db, TaskListFragment.getTodaysDay(), false);//save day creating app
        addDayToDB(db, "Sunday");
        addDayToDB(db, "Monday");
        addDayToDB(db, "Tuesday");
        addDayToDB(db, "Wednesday");
        addDayToDB(db, "Thursday");
        addDayToDB(db, "Friday");
        addDayToDB(db, "Saturday");

        setPreferencesDefaultValues(db);



    }

    private void setPreferencesDefaultValues(SQLiteDatabase db) {
        ContentValues value = new ContentValues();
        value.put("RINGTONE", "content://settings/system/notification_sound");
        value.put("VIBRATION", 1);
        value.put("IS_SET_REMIND_TIME", 1);
        value.put("REMIND_TIME", "22:00");
        value.put("LANGUAGE", 2);
        value.put("THEME", 1);
        value.put("IS_SET_NOTIFICATION", 1);
        db.insert("Preference", null, value);
    }


    private void addDayToDB(SQLiteDatabase db, String day) {
        ContentValues value = new ContentValues();
        value.put("DAY", day);
       long id = db.insert("Day", null, value);
        DBSynchronizer.addToSyncTable(db, id, Constants.DAYS_TABLE_ID);

    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
