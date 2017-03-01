package com.eplan.yuraha.easyplanning.DBClasses;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.EditText;
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.ListAdapters.Goal;
import com.eplan.yuraha.easyplanning.ListAdapters.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yura on 17.02.2017.
 */

public class DBHelper {

    public static boolean addTask(SQLiteDatabase db, String text, int priority,
                                  ArrayList<String> chosenDays, List<String> checkedGoals,
                                  String remindTime, int remindTone){
try {
    long taskID = addToTasksTable(db, text, priority);
    long reminID = addToRemindingTable(db, remindTime, remindTone);

    for (String day : chosenDays) {
        long dayID = addToDateTable(db, day);
        addToRepeatingTable(db, taskID, dayID, reminID);
    }

    addToInProgressTasks(db, taskID);

    if (checkedGoals.size() > 0)
    addToTaskToGoalTable(db, taskID, checkedGoals);
}
catch (SQLiteException e)
{return false;}
        catch (Exception e)
        {return false;}


        
        return true;
    }


    public static boolean editTask(SQLiteDatabase db, String taskID, boolean isTaskEdit, String text, int priority,
                                   ArrayList<String> chosenDays, List<String> checkedGoals,
                                   String remindTime, int remindTone) {
        try {

            updateTaskTable(db, taskID, text, priority);
            String remindId = getRemindIdFromTaskId(db, taskID);
            updateRemindTable(db, remindId, remindTime, remindTone);


            if (isTaskEdit)// if user has changed repeating days (toggles) app must update they in db
                updateRepeatingTable(db, taskID, chosenDays, remindId);


                updateTaskToGoalTable(db, taskID, checkedGoals);


        }
        catch (SQLiteException e)
        {return false;}
        catch (Exception e)
        {return false;}



        return true;

    }

    private static void updateTaskTable(SQLiteDatabase db, String taskID, String text, int priority) {
        ContentValues value = new ContentValues();
        value.put("TASK_TEXT", text);
        value.put("PRIORITY", priority);
        int updCount = db.update("Tasks", value, "_id = ?",
                new String[] { taskID });


        if (updCount<1)
            throw new SQLiteException("cant update RemindTable");
    }

    private static void updateTaskToGoalTable(SQLiteDatabase db, String taskID, List<String> checkedGoals) {
        System.out.println("im updateTaskToGoalTable");
        db.beginTransaction();

        try {
           int count = db.delete("TaskToGoal", "TASK_ID" + " = ?", new String[] { taskID });
           addToTaskToGoalTable(db, Long.parseLong(taskID), checkedGoals );
            db.setTransactionSuccessful();
        }
        catch (Exception e){}
        finally {
            db.endTransaction();
        }

    }

    private static void updateRepeatingTable(SQLiteDatabase db, String taskID, ArrayList<String> chosenDays, String remindId) {

        db.beginTransaction();

        try {
            db.delete("Repeating", "TASK_ID" + " = ?", new String[] { taskID });

            for (String day : chosenDays) {
                long dayID = addToDateTable(db, day);
                addToRepeatingTable(db, Long.parseLong(taskID), dayID ,Long.parseLong(remindId));
            }
            db.setTransactionSuccessful();
        }
        catch (Exception e){}
        finally {
            db.endTransaction();
        }

    }

    private static void updateRemindTable(SQLiteDatabase db, String remindId, String remindTime, int remindTone) {
            ContentValues value = new ContentValues();
            value.put("TIME", remindTime);
            value.put("TONE", remindTone);
            int updCount = db.update("Reminding", value, "_id = ?",
                    new String[] { remindId });


        if (updCount<1)
            throw new SQLiteException("cant update RemindTable");
    }

    private static void addToTaskToGoalTable(SQLiteDatabase db, long taskID, List<String> checkedGoals) {
            ContentValues value = new ContentValues();
            for (String goal : checkedGoals)
            {
                long goalID = getGoalIdFromGoalText(db, goal);
                value.put("TASK_ID", taskID);
                value.put("GOAL_ID", goalID);
                long id = db.insert("TaskToGoal", null, value);
                System.out.println(id);
                value.clear();
            }


    }

    public static long getGoalIdFromGoalText(SQLiteDatabase db, String goalText) {
        Cursor cursor = db.query ("Goals",
                new String[] {"_id"},
                "GOAL_TEXT = ?",
                new String[] {goalText},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getLong(0);


        return -1;
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
    public static boolean isTaskNameExistInThisDay(SQLiteDatabase db, String taskName, ArrayList<String> days, String sendedTaskId) throws SQLiteException
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

            if (cursor.moveToFirst() )
            {
                // code bellow for checking tasks while its editing
                if (sendedTaskId!=null)
                {
                    if (sendedTaskId.equals(taskID+""))
                        return false;

                    else
                        return true;
                }
                return true;

            }


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

    public static ArrayList<String> getDaysForTaskId(SQLiteDatabase db, String taskId)
    {
      ArrayList<String> daysList = new ArrayList<>();

        Cursor cursor = db.query ("Repeating",
                new String[] {"DAY_ID"},
                "TASK_ID = ?",
                new String[] {taskId},
                null, null,null);

        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                try {
                    String dayId = cursor.getString(0);
                    System.out.println(dayId);
                    String day = getDayFromId(db, dayId);
                    daysList.add(day);
                }
                catch (Exception e){}
                cursor.moveToNext();
            }
        }
        return daysList;
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




    public static ArrayList<String> getAllGoalsInProgress (SQLiteDatabase db)
    {
        ArrayList<String> goals = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM InProgressGoals" , null);
        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                String goalId = cursor.getString(1);
                try {
                    Goal goal = getGoalFromId(db, goalId);
                    goals.add(goal.getGoalName());
                }
                catch (Exception e){}
                cursor.moveToNext();
            }
        }

        return goals;
    }

    public static ArrayList<Task> getAllTasksFromDay(SQLiteDatabase db, String day, String dayOfWeek)
    {
      ArrayList<Task> tasksList = new ArrayList<>();

        long dayID = getDayFromString(db, day);
        long dayOfWeekId = getDayFromString(db, dayOfWeek);

        ArrayList<Long> list1 =  getTasksListFromRepeatingTable(db, dayID);
        ArrayList<Long> list2 =  getTasksListFromRepeatingTable(db, dayOfWeekId);

        ArrayList<Long> tasksIdList = new ArrayList<>();
        tasksIdList.addAll(list1);
        tasksIdList.addAll(list2);

        for (Long taskId : tasksIdList)
        {
            String taskText = getTaskTextFromId(db, taskId+"");
            int priority = getTaskPriorityFromID(db, taskId);
            boolean isDone = isTaskDone(db, taskId);
            ArrayList<String> goals = getTaskGoals(db, taskId+"");
            Task task = new Task(taskId, taskText, priority, isDone, goals);

            // if task is done put it to the end of list (and listView). If now done - to the top
            if (isDone)
                tasksList.add(task);
            else
                tasksList.add(0, task);

        }

        return tasksList;
    }


    public static String getRemindTimeForTaskId(SQLiteDatabase db, String taskId)
    {
        String remindId = getRemindIdFromTaskId(db, taskId);

        Cursor cursor = db.query ("Reminding",
                new String[] {"TIME"},
                "_id = ?",
                new String[] {remindId},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getString(0);


        else
            return "-1";
    }

    public static int getRemindToneForTaskId(SQLiteDatabase db, String taskId)
    {
        String remindId = getRemindIdFromTaskId(db, taskId);

        Cursor cursor = db.query ("Reminding",
                new String[] {"TONE"},
                "_id = ?",
                new String[] {remindId},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getInt(0);


        else
            return 0;
    }

    public static String getRemindIdFromTaskId(SQLiteDatabase db, String taskId)
    {
        Cursor cursor = db.query ("Repeating",
                new String[] {"REMIND_ID"},
                "TASK_ID = ?",
                new String[] {taskId},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getString(0);


        else
            return "-1";
    }

    public static ArrayList<String> getTaskGoals(SQLiteDatabase db, String taskId) {

        ArrayList<String> goalList = new ArrayList<>();

        Cursor cursor = db.query ("TaskToGoal",
                new String[] {"GOAL_ID"},
                "TASK_ID = ?",
                new String[] {taskId},
                null, null,null);

        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                try {
                    long goalId = cursor.getLong(0);
                    String goalText = getGoalTextFromId(db, goalId);
                    goalList.add(goalText);

                }
                catch (Exception e){}
                cursor.moveToNext();
            }
        }
        return goalList;
    }

    public static String getGoalTextFromId(SQLiteDatabase db, long goalId) {
        Cursor cursor = db.query ("Goals",
                new String[] {"GOAL_TEXT"},
                "_id = ?",
                new String[] {goalId+""},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getString(0);


        throw new SQLiteException();
    }

    public static int getPriorityFromTaskId(SQLiteDatabase db, String taskId)
    {

        Cursor cursor = db.query ("Tasks",
                new String[] {"PRIORITY"},
                "_id = ?",
                new String[] {taskId},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getInt(0);

        else
            return 1;

    }


    public static boolean isTaskDone(SQLiteDatabase db, Long taskId) {
        Cursor cursor = db.query ("DoneTasks",
                new String[] {"_id"},
                "TASK_ID = ?",
                new String[] {taskId+""},
                null, null,null);

        if (cursor.moveToFirst())
            return true;

        return false;
    }

    public static int getTaskPriorityFromID(SQLiteDatabase db, Long taskId) {
        Cursor cursor = db.query ("Tasks",
                new String[] {"PRIORITY"},
                "_id = ?",
                new String[] {taskId+""},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getInt(0);


        throw new SQLiteException();
    }

    public static String getTaskTextFromId(SQLiteDatabase db, String taskId) {
        Cursor cursor = db.query ("Tasks",
                new String[] {"TASK_TEXT"},
                "_id = ?",
                new String[] {taskId+""},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getString(0);


        throw new SQLiteException();
    }

    public static ArrayList<Long> getTasksListFromRepeatingTable(SQLiteDatabase db, long dayID) {
        ArrayList<Long> idList = new ArrayList<>();

        Cursor cursor = db.query ("Repeating",
                new String[] {"TASK_ID"},
                "DAY_ID = ?",
                new String[] {dayID+""},
                null, null,null);

        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                try {
                   long taskId = cursor.getLong(0);
                    idList.add(taskId);
                }
                catch (Exception e){}
                cursor.moveToNext();
            }
        }
        return idList;
    }

    public static boolean deleteTask(SQLiteDatabase db, long taskId)
    {
        try {
            int result =  db.delete("Tasks","_id=? ",new String[]{taskId+""});
            db.delete("Repeating","TASK_ID=? ",new String[]{taskId+""});
            db.delete("InProgressTasks","TASK_ID=? ",new String[]{taskId+""});
            if (result >0)
                return true;

            else
                return false;
        }
        catch (Exception e){return false;}

    }


}



