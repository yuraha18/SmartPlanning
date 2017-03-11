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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Yura on 17.02.2017.
 */

public class DBHelper {

    public static boolean addTask(SQLiteDatabase db, String text, int priority,
                                  ArrayList<String> chosenDays, String calledDay, boolean isRepeatEveryMonth, List<String> checkedGoals,
                                  String remindTime){
try {
    long taskID = addToTasksTable(db, text, priority);
     addToRemindingTable(db, taskID, remindTime);

    // if user chosen repeat every week, add info about repeating day in MonthRepeating table
    if (isRepeatEveryMonth) {
        addToMonthRepeatingTable(db, taskID+"", calledDay);
    }
// in other case, set repeating days
    else {
        for (String day : chosenDays) {
            long dayID = addToDateTable(db, day);
            addToRepeatingTable(db, taskID, dayID);
        }
    }

    addToInProgressTasks(db, taskID);

    if (checkedGoals.size() > 0)
    addToTaskToGoalTable(db, taskID, checkedGoals);

    //create new row in TaskLifecycleTable where first day from list will be FROM_DAY
        addToTaskLifecycleTable(db, taskID, calledDay);


}
catch (SQLiteException e)
{return false;}
        catch (Exception e)
        {return false;}


        
        return true;
    }

    private static void addToMonthRepeatingTable(SQLiteDatabase db, String taskID, String calledDay) {
      int dayOfMonth =  AddTaskFragment.getDayOfMonth(calledDay, Constants.DATEFORMAT);
        ContentValues value = new ContentValues();
        value.put("TASK_ID", taskID);
        value.put("DAY_OF_MONTH", dayOfMonth);
         db.insert("MonthRepeating", null, value);
    }

    /* here im adding the earliest day from chosen days to db like DAY_FROM in tasklifecycle
     * i must find out the earliest day
      * logic is simple:
      *   the first day from list is earliest (its day in which user clicked "+" in MainActivity)
      *   although user can choose date from calendar earlier then first
      *   that's why i compere first and last (day from calendar adding always last) days in getEarlierDay in AddTaskFragment where is dateFormat*/
    private static void addToTaskLifecycleTable(SQLiteDatabase db, long taskID, String day) {
        String earliestDay;

        long dayID = addToDateTable(db, day);

        ContentValues value = new ContentValues();
            value.put("TASK_ID", taskID);
            value.put("DAY_FROM_ID", dayID);
            value.put("DAY_TO_ID", -1);
            long id = db.insert("TaskLifecycle", null, value);


    }



    public static boolean editTask(SQLiteDatabase db, String todaysDay, String calledDay, String taskID, String text, int priority,
                                   ArrayList<String> chosenDays, boolean isRepeatEveryMonth, List<String> checkedGoals,
                                   String remindTime) {
        try {
            /* here are some trick, hard for understanding
            * in bellow if I compare two dates: dayFrom task and todays day
            * it help me understand: was task in past or only will be in future. It's important
            * for history. If task was before today, i set DayTO in its LifeCycle and create new task (old task will be
            * visible only in history), new one will be able from today.
            * if task never shows (dayFrom > todays), i only update him (because there are info for history)
            * im updating only for economy space in db*/

           String dayFrom = getDayFromByTaskId(db, taskID);
            if (AddTaskFragment.compareTwoDates(dayFrom, todaysDay) >= 0 )
            {
                chosenDays.add(dayFrom);
                updateTask(db, calledDay,taskID,text, priority, chosenDays, isRepeatEveryMonth, checkedGoals,remindTime);
            }


            else
            {
                deleteTaskFromAllDays(db, taskID, todaysDay);// set DayTo in taskLifecycle
                addTask(db, text, priority, chosenDays, todaysDay, isRepeatEveryMonth, checkedGoals, remindTime);
            }
        }
        catch (SQLiteException e)
        {return false;}
        catch (Exception e)
        {return false;}



        return true;

    }

    private static void updateTask(SQLiteDatabase db, String calledDay, String taskID, String text, int priority, ArrayList<String> chosenDays, boolean isRepeatEveryMonth, List<String> checkedGoals, String remindTime) {

        updateTaskTable(db, taskID, text, priority);
        updateRemindTable(db, taskID, remindTime);


        db.beginTransaction();
        try {
            System.out.println(chosenDays);
            if (isRepeatEveryMonth) {
                deleteFromRepeatingTable(db, taskID);
                updateRepeatEveryMonthTable(db, taskID, calledDay);
            } else {
                deleteFromRepeatMonthTable(db, taskID);
                updateRepeatingTable(db, taskID, chosenDays);

            }

            db.setTransactionSuccessful();
        }
        catch (SQLiteException e)
        {}
        finally {
            db.endTransaction();
        }

        updateTaskToGoalTable(db, taskID, checkedGoals);
    }

    private static void updateRepeatingTable(SQLiteDatabase db, String taskID, ArrayList<String> chosenDays) {
            db.delete("Repeating", "TASK_ID" + " = ?", new String[] { taskID });

            for (String day : chosenDays) {
                long dayID = addToDateTable(db, day);
                addToRepeatingTable(db, Long.parseLong(taskID), dayID );
            }

    }

    private static void updateRepeatEveryMonthTable(SQLiteDatabase db, String taskID, String calledDay) {
        deleteFromRepeatMonthTable(db, taskID);
        addToMonthRepeatingTable(db,taskID, calledDay);

    }

    private static void deleteFromRepeatMonthTable(SQLiteDatabase db, String taskID) {
        db.delete("MonthRepeating", "TASK_ID" + " = ? " , new String[] { taskID});
    }

    private static void deleteFromRepeatingTable(SQLiteDatabase db, String taskID) {
        db.delete("Repeating", "TASK_ID" + " = ? " , new String[] { taskID});
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
             db.insert("DoneTasks", null, value);
            value.clear();

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

    private static void updateRemindTable(SQLiteDatabase db, String taskID, String remindTime) {
            ContentValues value = new ContentValues();
            value.put("TIME", remindTime);
            int updCount = db.update("Reminding", value, "TASK_ID = ?",
                    new String[] { taskID });


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

    public static long addToRepeatingTable(SQLiteDatabase db, long taskID, long dayID) throws SQLiteException
    {
        ContentValues value = new ContentValues();
        value.put("TASK_ID", taskID);
        value.put("DAY_ID", dayID);
        long repeatID = db.insert("Repeating", null, value);


        if (repeatID < 0)
            throw new SQLiteException("id=-1 in addToRepeatingTable");
        return repeatID;
    }

    public static long addToRemindingTable(SQLiteDatabase db, long taskID, String time) throws SQLiteException
    {
        ContentValues value = new ContentValues();
        value.put("TASK_ID", taskID);
        value.put("TIME", time);
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
    public static boolean isTaskNameExistInThisDay(SQLiteDatabase db, AddTaskFragment fragment, String taskName, ArrayList<String> days, String sendedTaskId) throws SQLiteException
    {
        try {
            ArrayList<Long> taskIdList = getTaskIDByString(db, taskName);
            long sendedId = -1;
            if (sendedTaskId!=null)
                sendedId = Long.parseLong(sendedTaskId);

            if (taskIdList.isEmpty())
                return false;

            for (Long taskID : taskIdList) {
                for (String day : days) {


                    if (taskID == sendedId) {
                        break;
                    }

                    if (isTaskInRepeatingTable(db, taskID, day, taskName))
                    {
                        fragment.foundOutSameTaskShowToast(day);
                        return true;
                    }
                }
            }
        }

        catch (Exception e){}

        return false;
    }

    /* two below methods looking for the same taskName in chosen days
    * REWRITE IN FUTURE REALIZE */
    private static boolean isTaskInRepeatingTable(SQLiteDatabase db, Long taskID, String day, String taskName) {

        /* if im sending simple day, like 5-10-2017 im in this if
          * it gets all task names from method similar to gettingAllTasksForDay and check if there are
           * the same taskName*/
        if (!AddTaskFragment.weekDays.contains(day)) {
          ArrayList<String> taskNamesList = getTaskNamesFromRepeatingTable(db, day);

            if (taskNamesList.contains(taskName))
                return true;

            return false;
        }

            long dayId = getDayFromString(db, day);
            int dayOfMonth = AddTaskFragment.getDayOfMonth(day, Constants.DATEFORMAT);

            ArrayList<Long> taskIds = new ArrayList<>();
            taskIds.addAll(getListFromRepeatingTable(db, dayId));
            taskIds.addAll(getListFromMonthRepeatingTable(db, dayOfMonth));

            taskIds = checkOutAllTasksOnDeleting(db, taskIds, dayId);

            if (taskIds.contains(taskID))
                return true;

            return false;

    }

    /* this method gets all tasks for day*/
    private static ArrayList<String> getTaskNamesFromRepeatingTable(SQLiteDatabase db, String day) {
        ArrayList<String> namesList = new ArrayList<>();
        long dayId = addToDateTable(db, day);
        int dayOfWeek = AddTaskFragment.getDayOfWeek(day, Constants.DATEFORMAT);// get day of week for called day
        String weekDay = AddTaskFragment.weekDays.get(dayOfWeek);// gets name for this day of week (4 - Thursday)
        long dayOfWeekId = addToDateTable(db, weekDay);// gets from db dayId for this day (for example, Thursday)
        int dayOfMonth = AddTaskFragment.getDayOfMonth(day, Constants.DATEFORMAT);

        // gets all tasks for this day, This method i also use in getAllTasksFor day method
        ArrayList<Long> list = getTasksListFromRepeatingTable(db, dayId, dayOfWeekId, dayOfMonth);

        for (Long taskId : list)
        {
            String taskText = getTaskTextFromId(db, taskId+"");
            namesList.add(taskText);
        }
        return namesList;
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
            
            if (goalId < 0)
                throw new SQLiteException("id=-1 in addGoalToDB");

            addToInProgressGoals(db, goalId);

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

    /* Delete goal mean that it's dayTo in Lifecycle is filed
    * its using for saving data for history
    * goal deletes from DB only in one case: if it attached to any tasks*/
    public static boolean deleteGoal(SQLiteDatabase db, String goalId)
    {
        try {

            /* if there are attached tasks to this goal, delete from InProgressGoals and it will
            never shows in GoalActivity tabs, but will be able for history */
            if (isGoalAttachedToTasks(db, goalId)) {
                db.delete("InProgressGoals", "GOAL_ID" + " = ?", new String[] { goalId});
            }

            // if there are any tasks attached, delete it
            else 
                db.delete("Goals","_id=? ",new String[]{goalId});

            return true;
        }
        catch (Exception e){return false;}

    }

    public static int getDayOfMonthFromMonthRepeating (SQLiteDatabase db, String taskId)
    {

        Cursor cursor = db.query ("MonthRepeating",
                new String[] {"DAY_OF_MONTH"},
                "TASK_ID = ?",
                new String[] {taskId},
                null, null,null);

        if (cursor.moveToFirst())
            return cursor.getInt(0);

        return -1;


    }

public static ArrayList<String> getDaysFromRepeatingTable(SQLiteDatabase db, String taskId)
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
    private static boolean isGoalAttachedToTasks(SQLiteDatabase db, String goalId) {
        Cursor cursor = db.query ("TaskToGoal",
                new String[] {"_id"},
                " GOAL_ID = ? " ,
                new String[] {goalId},
                null, null,null);

        if (cursor.moveToFirst())
        return true;

        return false;
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
        int dayOfMonth = AddTaskFragment.getDayOfMonth(day, Constants.DATEFORMAT);

       ArrayList<Long> tasksIdList =  getTasksListFromRepeatingTable(db, dayID, dayOfWeekId, dayOfMonth);

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
        try {
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
        catch (Exception e){return new DayStatistic(0,0);}



    }

    public static DayStatistic addDayToStatistic(SQLiteDatabase db, String day)
    {

        long dayId = getDayFromString(db, day);
        int dayOfWeek = AddTaskFragment.getDayOfWeek(day, Constants.DATEFORMAT);
        int dayOfMonth = AddTaskFragment.getDayOfMonth(day, Constants.DATEFORMAT);

        //get all tasks for this day
        ArrayList<Long> taskList = getTasksListFromRepeatingTable(db, dayId, dayOfWeek, dayOfMonth);

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

    public static ArrayList<Long> getTasksListFromRepeatingTable(SQLiteDatabase db, long dayID, long dayOfWeekId, int dayOfMonth) {
       ArrayList<Long> list1 = getListFromRepeatingTable(db, dayID);// get tasks for single day
        ArrayList<Long> list2 = getListFromRepeatingTable(db, dayOfWeekId);// get repeating tasks for day
        ArrayList<Long> list3 = getListFromMonthRepeatingTable(db, dayOfMonth);// get tasks for this day of month

        /* merge 2 lists with id*/
        ArrayList<Long> allTasks = new ArrayList<>();
        allTasks.addAll(list1);
        allTasks.addAll(list2);
        allTasks.addAll(list3);

        allTasks = new ArrayList<>(new LinkedHashSet<>(allTasks));// delete duplicates in list, if exists
        //return only tasks not deleted in this day
        return checkOutAllTasksOnDeleting(db, allTasks, dayID);

    }

    private static ArrayList<Long> getListFromMonthRepeatingTable(SQLiteDatabase db, int dayOfMonth) {
        ArrayList<Long> idList = new ArrayList<>();

        Cursor cursor = db.query ("MonthRepeating",
                new String[] {"TASK_ID"},
                "DAY_OF_MONTH = ?",
                new String[] {dayOfMonth+""},
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

        Cursor cursor = db.query ("Reminding",
                new String[] {"TIME"},
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

    public static String getDayFromByTaskId(SQLiteDatabase db, String taskID) throws SQLiteException {
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

    public static boolean isInDeletedTable(SQLiteDatabase db, long taskId, long dayID) {
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


    public static boolean deleteTaskFromAllDays(SQLiteDatabase db, String taskId, String day) {

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



