package com.eplan.yuraha.easyplanning.DBClasses;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;


import com.eplan.yuraha.easyplanning.AddTaskFragment;
import com.eplan.yuraha.easyplanning.Constants;
import com.eplan.yuraha.easyplanning.DayStatistic;
import com.eplan.yuraha.easyplanning.ListAdapters.Goal;
import com.eplan.yuraha.easyplanning.ListAdapters.Task;
import com.eplan.yuraha.easyplanning.StatisticActivity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

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

    //create new row in TaskLifecycleTable where first day from list will be FROM_DAY
    if (chosenDays.size()>=1) {
        addToTaskLifecycleTable(db, taskID, chosenDays);

    }
}
catch (SQLiteException e)
{return false;}
        catch (Exception e)
        {return false;}


        
        return true;
    }

    /* here im adding the earliest day from chosen days to db like DAY_FROM in tasklifecycle
     * i must find out the earliest day
      * logic is simple:
      *   the first day from list is earliest (its day in which user clicked "+" in MainActivity)
      *   although user can choose date from calendar earlier then first
      *   that's why i compere first and last (day from calendar adding always last) days in getEarlierDay in AddTaskFragment where is dateFormat*/
    private static void addToTaskLifecycleTable(SQLiteDatabase db, long taskID, ArrayList<String> dates) {
        String earliestDay;

        try
        {
            earliestDay = AddTaskFragment.getEarliestDay(dates);
        }
        catch (Exception e)
        {
            earliestDay = dates.get(0);
        }

        long dayID = getDayFromString(db, earliestDay);

        ContentValues value = new ContentValues();
            value.put("TASK_ID", taskID);
            value.put("DAY_FROM_ID", dayID);
            value.put("DAY_TO_ID", -1);
            long id = db.insert("TaskLifecycle", null, value);


    }



    public static boolean editTask(SQLiteDatabase db, String taskID, boolean isToggleEdit, String text, int priority,
                                   ArrayList<String> chosenDays, List<String> checkedGoals,
                                   String remindTime, int remindTone) {
        try {

            updateTaskTable(db, taskID, text, priority);
            String remindId = getRemindIdFromTaskId(db, taskID);
            updateRemindTable(db, remindId, remindTime, remindTone);


            if (isToggleEdit)// if user has changed repeating days (toggles) app must update they in db
            {
                updateRepeatingTable(db, taskID, chosenDays, remindId);
                updateTaskLifecycleTable(db, taskID, chosenDays);
            }


                updateTaskToGoalTable(db, taskID, checkedGoals);


        }
        catch (SQLiteException e)
        {return false;}
        catch (Exception e)
        {return false;}



        return true;

    }

    public static ArrayList<String> getAllTasksForAutoComplete(SQLiteDatabase db)
    {
        ArrayList<String> tasks = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT TASK_TEXT FROM Tasks" , null);
        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                String taskText = cursor.getString(0);
tasks.add(taskText);
                cursor.moveToNext();
            }
        }

        return tasks;
    }

    private static void updateTaskLifecycleTable(SQLiteDatabase db, String taskID, ArrayList<String> chosenDays) {
        try {
            String dayFrom = getDayFromByTaskId(db, taskID);// get exist day in db
            chosenDays.add(dayFrom);
            String earliestDay = AddTaskFragment.getEarliestDay(chosenDays);// find out the earliest day from chosen days and day from db

            if (earliestDay.equals(dayFrom))//if earliest day is day from db, doing nothing
                return;

            else // if in list is earlier day than in db - update data in db
            {
                updateTaskDayFromInLifecycleTable(db, taskID, earliestDay);
            }
        }
        catch (Exception e){}

    }

    public static boolean isInDoneTasksTable(SQLiteDatabase db, long taskId, String day)
    {
        long dayId = addToDateTable(db, day);
        Cursor cursor = db.query ("DoneTasks",
                new String[] {"_id"},
                "TASK_ID = ? AND DAY_ID = ?",
                new String[] {taskId+"", dayId+""},
                null, null,null);

        if (cursor.moveToFirst())
            return true;


        return false;
    }

    public static void deleteFromDoneTasks(SQLiteDatabase db, long taskId, String day)
    {
        long dayId = addToDateTable(db, day);
        int count = db.delete("DoneTasks", "TASK_ID" + " = ?" + " AND DAY_ID" + " = ?", new String[] { taskId+"", dayId+"" });
    }

    public static void addToDoneTasks(SQLiteDatabase db, long taskId, String day)
    {
        if (isInDoneTasksTable(db, taskId,day ))
            return;

        long dayId = addToDateTable(db, day);
        ContentValues value = new ContentValues();
            value.put("TASK_ID", taskId);
            value.put("DAY_ID", dayId);
            long id = db.insert("DoneTasks", null, value);
            value.clear();

    }

    private static void updateTaskDayFromInLifecycleTable(SQLiteDatabase db, String taskID, String earliestDay) {

String dayFromId= getDayFromId(db, earliestDay);
        ContentValues value = new ContentValues();
        value.put("DAY_FROM_ID", dayFromId);
        int updCount = db.update("TaskLifecycle", value, "TASK_ID = ?",
                new String[] { taskID });


        if (updCount<1)
            throw new SQLiteException("cant update RemindTable");
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
    public static ArrayList<Long> getTaskIDByString(SQLiteDatabase db, String taskName) throws SQLiteException
    {
    ArrayList<Long> idList = new ArrayList<>();
        Cursor cursor = db.query ("Tasks",
                new String[] {"_id"},
                "TASK_TEXT = ?",
                new String[] {taskName},
                null, null,null);

        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                idList.add(cursor.getLong(0));
                cursor.moveToNext();
            }
        }

        return idList;
    }

    /* Must be changed in future*/
    public static boolean isTaskNameExistInThisDay(SQLiteDatabase db, String taskName, ArrayList<String> days, String sendedTaskId) throws SQLiteException
    {
        ArrayList<Long> taskIdList = getTaskIDByString(db, taskName);
        if (taskIdList.isEmpty())
            return false;

        for (Long taskID : taskIdList) {
            for (String day : days) {

                long dayID = addToDateTable(db, day);

                if (isInRepeatingTable(db, dayID, taskID)) {

                    if (sendedTaskId != null) {

                        if (!sendedTaskId.equals(taskID + "")) {
                            return true;
                        }
                        else
                            break;
                    }

                    else
                    if (!isInDeletedTable(db, taskID, dayID)) {// task could be deleted from this day, if its true you can add the same
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isInRepeatingTable(SQLiteDatabase db, long dayID, long taskID)
    {
        Cursor cursor = db.query("Repeating",
                new String[]{"_id"},
                "TASK_ID = ? AND DAY_ID = ?",
                new String[]{taskID + "", dayID + ""},
                null, null, null);

        if (cursor.moveToFirst() )
        return true;

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
      ArrayList<Task> inProgressTasks = new ArrayList<>();
        ArrayList<Task> doneTasks = new ArrayList<>();

        long dayID = addToDateTable(db, day);
        long dayOfWeekId = addToDateTable(db, dayOfWeek);

       ArrayList<Long> tasksIdList =  getTasksListFromRepeatingTable(db, dayID, dayOfWeekId);

        for (Long taskId : tasksIdList)
        {
            String taskText = getTaskTextFromId(db, taskId+"");
            int priority = getTaskPriorityFromID(db, taskId);
            boolean isDone = isInDoneTasksTable(db, taskId, day);
            ArrayList<String> goals = getTaskGoals(db, taskId+"");
            Task task = new Task(taskId, taskText, priority, isDone, goals);

            // if task is done put it to the end of list (and listView). If now done - to the top
            if (isDone)
                doneTasks.add(task);
            else
                inProgressTasks.add(task);

        }

        doneTasks = sortTaskList(doneTasks);// sort list
        inProgressTasks = sortTaskList(inProgressTasks);// sort list

        inProgressTasks.addAll(doneTasks);//merge two lists

        return inProgressTasks;
    }

    private static ArrayList<Task> sortTaskList(ArrayList<Task> taskList) {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                int priority1 = task1.getPriority();
                int priority2 = task2.getPriority();

                return priority1 == priority2 ? 0 : priority1 > priority2 ? -1 : 1;
            }
        });

        return taskList;
    }

    /* First id in Days table is day when app was created
    * here i'm getting this day*/
    public static String getCreatingDay(SQLiteDatabase db) throws SQLiteException
    {
        String id = "1";
        Cursor cursor = db.query ("Days",
                new String[] {"DAY"},
                "_id = ?",
                new String[] {id},
                null, null,null);

        if (cursor .moveToFirst())
            return cursor.getString(0);

         throw new SQLiteException();


    }

    public static DayStatistic getStatisticForDay(SQLiteDatabase db, String day)
    {
        long dayId = getDayFromString(db, day);

        // -1 means that day is absent in dayTable, thats why there are no one tasks for this day. That's why it returns empty object
        if (dayId==-1)
            return new DayStatistic(0, 0);


        Cursor cursor = db.query ("Statistic",
                new String[] {"COUNT_DONE", "COUNT_IN_PROGRESS"},
                "DAY_ID = ?",
                new String[] {dayId+""},
                null, null,null);

        if (cursor .moveToFirst())
        {
          int countDone = cursor.getInt(0);
            int counInProgress = cursor.getInt(1);
            return new DayStatistic(countDone, counInProgress);
        }
        else
        {
            DayStatistic dayStatistic = addDayToStatistic(db, day);
            return dayStatistic;
        }


    }

    public static DayStatistic addDayToStatistic(SQLiteDatabase db, String day)
    {

        long dayId = getDayFromString(db, day);
        int dayOfWeek = AddTaskFragment.getDayOfWeek(day, Constants.DATEFORMAT);

        //get all tasks for this day
        ArrayList<Long> taskList = getTasksListFromRepeatingTable(db, dayId, dayOfWeek);

        int countDone = 0, countInProgress = 0;
        // here i'm counting done and in progress tasks for this day
        for (Long taskId : taskList)
        {
            boolean isDone = isInDoneTasksTable(db, taskId, day);
            int priority = getPriorityFromTaskId(db, taskId+"");
            int countGoals = getTaskGoals(db, taskId+"").size() *2;// points for task. Every attached to it goal give to 2 points

            // you cant get more than 4 point from goals (more than 2 goals not counting)
            if (countGoals > 4)
                countGoals =2;

            // if any goal is not attach, count 1. Not 0, because bellow are multiplying
            if (countGoals ==0)
                countGoals = 1;

            if (isDone)
                countDone += (priority * countGoals);// it means that every task multiplies on its priority (1-3) and attached goals (1 goal = 2 points, but not more 4 points)

            else
                countInProgress += (priority * countGoals);// not done goals counting like done one
        }

        /* inserting information in DB*/
        ContentValues value = new ContentValues();
        value.put("DAY_ID", dayId);
        value.put("COUNT_DONE", countDone);
        value.put("COUNT_IN_PROGRESS", countInProgress);
       // db.insert("Statistic", null, value);

        return new DayStatistic(countDone, countInProgress);
    }

    public static ArrayList<Long> getTasksListFromRepeatingTable(SQLiteDatabase db, long dayID, long dayOfWeekId) {
       ArrayList<Long> list1 = getListFromRepeatingTable(db, dayID);// get tasks for single day
        ArrayList<Long> list2 = getListFromRepeatingTable(db, dayOfWeekId);// get repeating tasks for day

        /* merge 2 lists with id*/
        ArrayList<Long> allTasks = new ArrayList<>();
        allTasks.addAll(list1);
        allTasks.addAll(list2);

        allTasks = new ArrayList<>(new LinkedHashSet<>(allTasks));// delete duplicates in list, if exists

        //return only tasks not deleted in this day
        return checkOutAllTasksOnDeleting(db, allTasks, dayID);

    }

    public static ArrayList<Long> getListFromRepeatingTable(SQLiteDatabase db, long dayID)
    {
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

    public static ArrayList<Long> checkOutAllTasksOnDeleting(SQLiteDatabase db, ArrayList<Long> idList, long dayID)
    {
        ArrayList<Long> tasksList = new ArrayList<>();
        for (Long taskId : idList)
        {
             /* two checks below are very important
                     * first of all for repeating every week tasks
                      * first method because user could delete task from single day and it mustn't add to this days tasks
                       * second - when user set task repeating every week on Monday, it will be adding for whole Mondays after start of program
                       * this method except this, and check is day during TaskLifecycle*/

            boolean isDeleted = isInDeletedTable(db, taskId, dayID);
            boolean checkAvailable = isBetweenDayFromDayTo(db, taskId, dayID);

            //if task was not deleted for this day and is between DayFrom DayTo add him
            if (!isDeleted && checkAvailable) {
                tasksList.add(taskId);

            }
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


    private static boolean isBetweenDayFromDayTo(SQLiteDatabase db, long taskId, long dayID) {
       try {
            String dayFrom = getDayFromByTaskId(db, taskId+"");

            String dayTo = getDayToByTaskId(db, taskId+"");
            String taskDay = getDayFromId(db, dayID+"");



        /* method compareTwoDates return:
        * -1 if right variable is higher
        * 0 - same
        * 1 - left is higher
         */
            if (AddTaskFragment.compareTwoDates(taskDay, dayFrom)>=0)// if date is after DAY_FROM
            {
                if (dayTo.equals("-1"))// if Day_To is not set/ It means that task available for all days after dayFrom
                    return true;
                else
                {
                    if ( AddTaskFragment.compareTwoDates(taskDay, dayTo) < 0)//check is day before dayTo or not
                        return true;

                    return false;
                }

            }

        }
        catch (Exception e){return false;}


        return false;
    }

    private static String getDayToByTaskId(SQLiteDatabase db, String taskID) throws SQLiteException {
        Cursor cursor = db.query ("TaskLifecycle",
                new String[] {"DAY_TO_ID"},
                "TASK_ID = ?",
                new String[] {taskID},
                null, null,null);

        if (cursor.moveToFirst())
        {
            String dayId = cursor.getString(0);

            if (dayId.equals("-1"))
                return "-1";

            return getDayFromId(db, dayId);
        }

        throw new SQLiteException("cant getDayToByTaskId");
    }

    private static String getDayFromByTaskId(SQLiteDatabase db, String taskID) throws SQLiteException {
        Cursor cursor = db.query ("TaskLifecycle",
                new String[] {"DAY_FROM_ID"},
                "TASK_ID = ?",
                new String[] {taskID},
                null, null,null);

        if (cursor.moveToFirst())
        {
            String dayId = cursor.getString(0);
            return getDayFromId(db, dayId);
        }

        throw new SQLiteException("cant getDayFromByTaskId");
    }

    private static boolean isInDeletedTable(SQLiteDatabase db, long taskId, long dayID) {
        Cursor cursor = db.query("DeletedTasks",
                new String[]{"_id"},
                "TASK_ID = ? AND DAY_ID = ?",
                new String[]{taskId+"", dayID+""},
                null, null, null);
        if (cursor.moveToFirst())
            return true;

        return false;
    }

    public static boolean deleteTaskFromDay(SQLiteDatabase db, long taskId, String day)
    {
        try {
            long dayID = getDayFromString(db, day);

            ContentValues value = new ContentValues();
            value.put("TASK_ID", taskId);
            value.put("DAY_ID", dayID);

            long id = db.insert("DeletedTasks", null, value);

            if (id < 0)
                throw new SQLiteException("can't delete single task");
        }
        catch (Exception e){return false;}

return true;
    }


    public static boolean deleteTaskFromAllDays(SQLiteDatabase db, long taskId, String day) {

        try {
            long dayToId = addToDateTable(db, day);
            ContentValues value = new ContentValues();
            value.put("DAY_TO_ID", dayToId);
            int updCount = db.update("TaskLifecycle", value, "TASK_ID = ?",
                    new String[] { taskId+"" });


            if (updCount<1)
                throw new SQLiteException("cant delete from whole days");
        }

        catch (SQLiteException e){return false;}

       return true;
    }

    /* Here you can update rows in Preference and Notification tables*/
    public static void updatePreferences(SQLiteDatabase db, String table, String tableRow, String value, long id)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(tableRow, value);

        db.update(table, contentValues, Constants.DB_TABLE_ID + " = ?",
                new String[] { id+"" });
    }


}



