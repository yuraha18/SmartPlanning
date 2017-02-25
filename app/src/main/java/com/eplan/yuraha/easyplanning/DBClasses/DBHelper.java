package com.eplan.yuraha.easyplanning.DBClasses;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.EditText;
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.ListAdapters.Goal;

import java.util.ArrayList;

/**
 * Created by Yura on 17.02.2017.
 */

public class DBHelper {

    public static boolean addTask(SQLiteDatabase db, String text, int priority,
                                  ArrayList<String> chosenDays, String checkedGoals,
                                  String remindTime, int remindTone){
try {
    long taskID = addToTasksTable(db, text, priority);
    long reminID = addToRemindingTable(db, remindTime, remindTone);

    for (String day : chosenDays) {
        long dayID = addToDateTable(db, day);
        addToRepeatingTable(db, taskID, dayID, reminID);
    }

    addToInProgressTasks(db, taskID);
}
catch (SQLiteException e)
{return false;}
        catch (Exception e)
        {return false;}


        
        return true;
    }

    private static long addToInProgressTasks(SQLiteDatabase db, long taskID) throws SQLiteException {
        ContentValues value = new ContentValues();
        value.put("TASK_ID", taskID);
        long id = db.insert("InProgressTasks", null, value);

        if (id < 0)
            throw new SQLiteException("id=-1 in addToInProgressTasks");

        return id;

    }

    public static long addToRepeatingTable(SQLiteDatabase db, long taskID, long dayID, long remindID) throws SQLiteException
    {
        ContentValues value = new ContentValues();
        value.put("TASK_ID", taskID);
        value.put("DAY_ID", dayID);
        value.put("REMIND_ID", remindID);
        long repeatID = db.insert("Repeating", null, value);


        if (repeatID < 0)
            throw new SQLiteException("id=-1 in addToRepeatingTable");
        return repeatID;
    }

    public static long addToRemindingTable(SQLiteDatabase db, String time, int tone) throws SQLiteException
    {
        ContentValues value = new ContentValues();
        value.put("TIME", time);
        value.put("TONE", tone);
        long remindID = db.insert("Reminding", null, value);

        if (remindID < 0)
            throw new SQLiteException("id=-1 in addToRemindingTable");
        return remindID;
    }

    public static long addToTasksTable(SQLiteDatabase db, String text, int priority) throws SQLiteException
    {
        ContentValues value = new ContentValues();
        value.put("TASK_TEXT", text);
        value.put("PRIORITY", priority);
       long taskID = db.insert("Tasks", null, value);

        if (taskID < 0)
            throw new SQLiteException("id=-1 in addToTasksTable");

        return taskID;
    }

    public static long addToDateTable(SQLiteDatabase db, String date) throws SQLiteException
    {
        long dateID = getDayFromString(db, date);//this method select dayId from db, if exist
        if (dateID>0)
            return dateID;//if day exist, we run out from here and return it's id

        /* if this day doesn't exist in db, it will be added here*/
        ContentValues value = new ContentValues();
        value.put("DAY", date);
        dateID = db.insert("Days", null, value);

        if (dateID < 0)
            throw new SQLiteException("id=-1 in addToDateTable");
        return dateID;
    }

    public static long getDayFromString(SQLiteDatabase db, String date) throws SQLiteException {

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
    public static long getTaskIDByString(SQLiteDatabase db, String taskName) throws SQLiteException
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
    public static boolean isTaskNameExistInThisDay(SQLiteDatabase db, String taskName, ArrayList<String> days) throws SQLiteException
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

    public static boolean isGoalExist(SQLiteDatabase db, String goalName) throws SQLiteException
    {
        Cursor cursor = db.query("Goals",
                new String[]{"_id"},
                "GOAL_TEXT = ?",
                new String[]{goalName},
                null, null, null);

        if (cursor.moveToFirst())
            return true;

        return false;
    }

    /* Override method isGoalExist for editing goals */
    public static boolean isGoalExist(SQLiteDatabase db, String goalName, String goalId) throws SQLiteException
    {
        Cursor cursor = db.query("Goals",
                new String[]{"_id"},
                "GOAL_TEXT = ?",
                new String[]{goalName},
                null, null, null);

        if (cursor.moveToFirst())
        {
            String id = cursor.getString(0);
            System.out.println(id + " " + goalId);
           if (!id.equalsIgnoreCase(goalId))
               return true;

            else
               return false;

        }


        return false;
    }

    public static long addGoalToDB(SQLiteDatabase db, String text, String note, String deadline)
    {
        long goalId =0;
        try {
            long dayId = addToDateTable(db, deadline);
            ContentValues value = new ContentValues();
            value.put("GOAL_TEXT", text);
            value.put("NOTICE", note);
            value.put("DEADLINE", dayId);
            goalId = db.insert("Goals", null, value);

            addToInProgressGoals(db, goalId);
            if (goalId < 0)
                throw new SQLiteException("id=-1 in addGoalToDB");
        }
        catch (SQLiteException e)
        {return -1;}
        catch (Exception e)
        {return -1;}



        return goalId;
    }

    private static void addToInProgressGoals(SQLiteDatabase db, long goalId) throws SQLiteException{
        ContentValues value = new ContentValues();
        value.put("GOAL_ID", goalId);
        long id = db.insert("InProgressGoals", null, value);

        if (id < 0)
            throw new SQLiteException("id=-1 in addToInProgressGoals");
    }

    public static String getDayFromId(SQLiteDatabase db, String dayId) throws SQLiteException
    {

        Cursor cursor = db.query ("Days",
                new String[] {"DAY"},
                "_id = ?",
                new String[] {dayId},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getString(0);


        throw new SQLiteException();
    }

    public static void moveGoalToDone(SQLiteDatabase db,  String goalId, String todaysDate) throws SQLiteException
  {
      long id =0;
      long todaysDayId = addToDateTable(db, todaysDate);
      ContentValues value = new ContentValues();
      value.put("GOAL_ID", goalId);
      value.put("DAY_ID", todaysDayId);
      db.beginTransaction();

      try {
           id = db.insert("DoneGoals", null, value);
          db.delete("InProgressGoals", "GOAL_ID" + " = ?", new String[] { goalId });
          db.setTransactionSuccessful();
      }
      catch (Exception e){}
      finally {
          db.endTransaction();
      }

  }

    public static ArrayList<Goal> getDoneGoalsList(SQLiteDatabase db)
    {
        ArrayList<Goal> goals = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM DoneGoals" , null);
        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                String goalId = cursor.getString(1);
                try {
                    Goal goal = getGoalFromId(db, goalId);
                    goals.add(0,goal);
                }
                catch (Exception e){}
                cursor.moveToNext();
            }
        }

        return goals;
    }

    public static ArrayList<Goal> getInProgressGoalsList(SQLiteDatabase db)
    {
        ArrayList<Goal> goals = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM InProgressGoals" , null);
        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                String goalId = cursor.getString(1);
                try {
                    Goal goal = getGoalFromId(db, goalId);
                    goals.add(0,goal);
                }
                catch (Exception e){}
                cursor.moveToNext();
            }
        }
        return goals;
    }

    public static Goal getGoalFromId(SQLiteDatabase db, String goalId) throws SQLiteException {
        Cursor cursor = db.query ("Goals",
                new String[] {"GOAL_TEXT", "NOTICE", "DEADLINE"},
                "_id = ?",
                new String[] {goalId},
                null, null,null);

        Goal goal;
        if (cursor.moveToFirst())
        {
            String goalName = cursor.getString(0);
            String goalNote = cursor.getString(1);
            String deadline = cursor.getString(2);
            goal = new Goal(goalName, goalNote, deadline, goalId);
        }

        else
            throw new SQLiteException("goalId not found");

        return goal;
    }

    public static boolean deleteGoal(SQLiteDatabase db, String goalId)
    {
        try {
            int result =  db.delete("Goals","_id=? ",new String[]{goalId});
            if (result >0)
                return true;

            else
                return false;
        }
        catch (Exception e){return false;}

    }

    public static boolean updateGoal(SQLiteDatabase db, String text, String note, String goalId)
    {
        try {
            ContentValues value = new ContentValues();
            value.put("GOAL_TEXT", text);
            value.put("NOTICE", note);
            int updCount = db.update("Goals", value, "_id = ?",
                    new String[] { goalId });

            if (updCount < 0)
               return false;
        }
        catch (SQLiteException e)
        {return false;}
        catch (Exception e)
        {return false;}



        return true;
    }

   public static String getDayFromDoneGoalByGoalId(SQLiteDatabase db, String goalId)
   {
       Cursor cursor = db.query ("DoneGoals",
           new String[] {"DAY_ID"},
           "GOAL_ID = ?",
           new String[] {goalId},
           null, null,null);

       long dateId;
       if (cursor.moveToFirst())
       {
           dateId = cursor.getLong(0);


          return getDayFromId(db, dateId+"");
       }
       else
           throw new SQLiteException();








   }

}



