package com.eplan.yuraha.easyplanning;

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
                + "TASK_ID INTEGER);");

        /* Save id's all notDoneTasks*/
        db.execSQL("CREATE TABLE NotDoneTasks ("
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
                + "DEADLINE INTEGER, "
                + "IS_DONE INTEGER, "
                + "WEIGHT INTEGER);");

          /* This one connecting tasks with goals*/
        db.execSQL("CREATE TABLE TaskToGoal("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TASK_ID INTEGER, "
                + "GOAL_ID INTEGER);");

         /* All days have their own id used in other tables */
        db.execSQL("CREATE TABLE Days("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "DAY TEXT);");

        addDayToDB(db, "Sunday");
        addDayToDB(db, "Monday");
        addDayToDB(db, "Tuesday");
        addDayToDB(db, "Wednesday");
        addDayToDB(db, "Thursday");
        addDayToDB(db, "Friday");
        addDayToDB(db, "Saturday");



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