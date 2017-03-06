package com.eplan.yuraha.easyplanning.DBClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Yura on 16.02.2017.
 */

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
        db.execSQL("CREATE TABLE DoneTasks ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "DAY_ID INTEGER);");

        /* Save id's all notDoneTasks*/
        db.execSQL("CREATE TABLE InProgressTasks ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER);");

        /* Information about repeating some tasks in future*/
        db.execSQL("CREATE TABLE Repeating ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "DAY_ID INTEGER, "
                + "REMIND_ID INTEGER);");

         /* Table with reminding information. Contains time for remind and it's one*/
        db.execSQL("CREATE TABLE Reminding ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TIME TEXT, "
                + "TONE INTEGER);");


         /* Table with goals info*/
        db.execSQL("CREATE TABLE Goals("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "GOAL_TEXT TEXT, "
                + "NOTICE TEXT, "
                + "DEADLINE INTEGER);");

           /* Save id's all doneGoals*/
        db.execSQL("CREATE TABLE DoneGoals ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "GOAL_ID INTEGER, "
                + "DAY_ID INTEGER);");

        /* Save id's all goals in progress*/
        db.execSQL("CREATE TABLE InProgressGoals ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "GOAL_ID INTEGER);");

          /* This one connecting tasks with goals*/
        db.execSQL("CREATE TABLE TaskToGoal("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "GOAL_ID INTEGER);");

         /* All days have their own id used in other tables */
        db.execSQL("CREATE TABLE Days("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "DAY TEXT);");

        /* If user delete repeated task from some day, it check in here */
        db.execSQL("CREATE TABLE DeletedTasks("
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
        db.insert("Days", null, value);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
