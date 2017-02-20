package com.eplan.yuraha.easyplanning;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Yura on 17.02.2017.
 */

public class DBHelper {

    public static boolean addTask(SQLiteDatabase db, String text, int priority,
                                  ArrayList<String> chosenDays, String checkedGoals,
                                  String remindTime, int remindTone){

long taskID = addToTasksTable(db, text, priority);
long reminID = addToRemindingTable(db, remindTime, remindTone);

        for (String day : chosenDays)
        {
          long dayID =  addToDateTable(db, day);
          addToRepeatingTable(db, taskID, dayID, reminID);
        }
        
        return true;
    }

    protected static long addToRepeatingTable(SQLiteDatabase db, long taskID, long dayID, long remindID)
    {
        ContentValues value = new ContentValues();
        value.put("TASK_ID", taskID);
        value.put("DAY_ID", dayID);
        value.put("REMIND_ID", remindID);
        long repeatID = db.insert("Repeating", null, value);
        return repeatID;
    }

    protected static long addToRemindingTable(SQLiteDatabase db, String time, int tone)
    {
        ContentValues value = new ContentValues();
        value.put("TIME", time);
        value.put("TONE", tone);
        long remindID = db.insert("Reminding", null, value);
        return remindID;
    }

    protected static long addToTasksTable(SQLiteDatabase db, String text, int priority)
    {
        ContentValues value = new ContentValues();
        value.put("TASK_TEXT", text);
        value.put("PRIORITY", priority);
       long taskID = db.insert("Tasks", null, value);
        return taskID;
    }

    protected static long addToDateTable(SQLiteDatabase db, String date)
    {
        long dateID = getDayFromString(db, date);//this method select dayId from db, if exist
        if (dateID>0)
            return dateID;//if day exist, we run out from here and return it's id

        /* if this day doesn't exist in db, it will be added here*/
        ContentValues value = new ContentValues();
        value.put("DAY", date);
        dateID = db.insert("Days", null, value);
        return dateID;
    }

    protected static long getDayFromString(SQLiteDatabase db, String date) {

        Cursor cursor = db.query ("Days",
                new String[] {"_id"},
                "DAY = ?",
                new String[] {date},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getLong(0);


        return -1;
    }

    /* This one try to get task id by his text
    * if there are no one task with the same text we return -1*/
    protected static long getTaskIDByString(SQLiteDatabase db, String taskName)
    {
        Cursor cursor = db.query ("Tasks",
                new String[] {"_id"},
                "TASK_TEXT = ?",
                new String[] {taskName},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getLong(0);


        return -1;
    }

    /* Must be changed in future*/
    protected static boolean isTaskNameExistInThisDay(SQLiteDatabase db, String taskName, ArrayList<String> days)
    {
        long taskID = getTaskIDByString(db, taskName);
        if (taskID==-1)
            return false;

        for (String day : days) {
            long dayID = getDayFromString(db, day);
            if (dayID == -1)
                return false;

            Cursor cursor = db.query("Repeating",
                    new String[]{"_id"},
                    "TASK_ID = ? AND DAY_ID = ?",
                    new String[]{taskID + "", dayID + ""},
                    null, null, null);

            if (cursor.moveToFirst())
                return true;
        }

        return false;
    }
}
