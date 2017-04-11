package com.eplan.yuraha.easyplanning.API;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.eplan.yuraha.easyplanning.Constants;
import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.ListAdapters.Goal;
import com.eplan.yuraha.easyplanning.ManagerNotifications;
import com.eplan.yuraha.easyplanning.dto.DTOGoal;
import com.eplan.yuraha.easyplanning.dto.DTOTask;
import com.eplan.yuraha.easyplanning.dto.Day;
import com.eplan.yuraha.easyplanning.dto.DeletedTask;
import com.eplan.yuraha.easyplanning.dto.DoneGoal;
import com.eplan.yuraha.easyplanning.dto.DoneTask;
import com.eplan.yuraha.easyplanning.dto.InProgressGoal;
import com.eplan.yuraha.easyplanning.dto.InProgressTask;
import com.eplan.yuraha.easyplanning.dto.MonthRepeating;
import com.eplan.yuraha.easyplanning.dto.Notification;
import com.eplan.yuraha.easyplanning.dto.Reminding;
import com.eplan.yuraha.easyplanning.dto.Repeating;
import com.eplan.yuraha.easyplanning.dto.TaskLifecycle;
import com.eplan.yuraha.easyplanning.dto.TaskToGoal;

import java.util.ArrayList;


public class DBSynchronizer {

    /*Methods for synchronization and server */
    public static void addToSyncTable(SQLiteDatabase db,  long taskID, int tableId) {
        System.out.println("addToSyncTable");
        ContentValues value = new ContentValues();
        value.put("ROW_ID", taskID);
        value.put("TABLE_ID", tableId);
        db.insert("SyncData", null, value);

    }

    public static void deleteFromSynchTable(SQLiteDatabase db, long localId, int tableId)
    {
        db.delete("SyncData", "ROW_ID = ? AND TABLE_ID = ?", new String[] { localId+"", tableId+"" });
    }

    public static ArrayList<SynchObject> getAllSyncRows(SQLiteDatabase db) throws SQLiteException {

        ArrayList<SynchObject> synchObjects = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM SyncData" , null);
        if (cursor .moveToFirst()) {
            while (!cursor.isAfterLast()) {
                long id = cursor.getLong(0);
                long rowId = cursor.getLong(1);
                int tableId = cursor.getInt(2);
                SynchObject synchObject = new SynchObject(id, rowId, tableId);

                synchObjects.add(synchObject);
                cursor.moveToNext();
            }
        }

        cursor.close();
        return synchObjects;
    }


    /* Methods for Synchronizing Tasks*/
    public static DTOTask getTask(SQLiteDatabase db, long id)
    {
        int priority = DBHelper.getPriorityFromTaskId(db, id+"");
        String text = DBHelper.getTaskTextFromId(db, id+"");
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.TASK_TABLE));

        return  new DTOTask(sid, text, priority);
    }

    public static long saveTask(SQLiteDatabase db, DTOTask task)
    {
        long sid = task.getSid();
        int tableId = Constants.dbTables.get(Constants.TASK_TABLE);
       boolean isExist = isRowExistInLocalDb(db, sid, tableId);

        long taskId;

        if (isExist)
            taskId =  updateTask(db, task);

        else
            taskId =  addTask(db, task);

        return taskId;
    }

    private static long addTask(SQLiteDatabase db, DTOTask task) {
        return DBHelper.addToTasksTable(db, task.getTaskText(), task.getPriority());
    }

    private static long updateTask(SQLiteDatabase db, DTOTask task) {
      long taskId =  getLocalIdFromDb(db, task.getSid(), Constants.dbTables.get(Constants.TASK_TABLE));
      DBHelper.updateTaskTable(db, taskId+"", task.getTaskText(), task.getPriority() );

        return taskId;
    }


    /* methods for synchronizing DaysTable */

    public static Day getDay(SQLiteDatabase db, long id)
    {
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.DAYS_TABLE));
        String day = DBHelper.getDayFromId(db, id+"");
        return new Day(sid, day);
    }

    /* becuse day cant be updated, only add new one*/
    public static long saveDay(SQLiteDatabase db, Day entity)
    {
        return DBHelper.addToDateTable(db, entity.getText());
    }

    /* methods for synchronizing for DeletedTask table */
    public static DeletedTask getDeletedTask(SQLiteDatabase db, long id)
    {
        DeletedTask localDT = DBHelper.getDeletedTaskWithLocalIds(db, id);
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.DELETED_TASKS_TABLE));
        long globalTaskId = getGlobalSIdFromDb(db, localDT.getTaskId(), Constants.dbTables.get(Constants.TASK_TABLE));
        long globalDayId = getGlobalSIdFromDb(db, localDT.getDayId(), Constants.dbTables.get(Constants.DAYS_TABLE));

        return  new DeletedTask(sid, globalTaskId, globalDayId);
    }

    public static long saveDeletedTask(SQLiteDatabase db, DeletedTask entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);
        long localDayId = getLocalId(db, entity.getDayId(), Constants.DAYS_TABLE_ID, 0);

       return DBHelper.addToDeletedTasks(db, localTaskId, localDayId);
    }


    /* methods for DoneGoal Table synchronization*/

    public static DoneGoal getDoneGoal(SQLiteDatabase db, long id)
    {
        DoneGoal localDG = DBHelper.getDoneGoalWithLocalIds(db, id);
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.DONE_GOALS_TABLE));
        long globalGoalId = getGlobalSIdFromDb(db, localDG.getGoalId(), Constants.dbTables.get(Constants.GOALS_TABLE));
        long globalDayId = getGlobalSIdFromDb(db, localDG.getDayId(), Constants.dbTables.get(Constants.DAYS_TABLE));

        return new DoneGoal(sid, globalGoalId, globalDayId);
    }

    public static long saveDoneGoal(SQLiteDatabase db, DoneGoal entity) throws RuntimeException
    {
        long localGoalId = getLocalId(db, entity.getGoalId(), Constants.GOALS_TABLE_ID, 0);
        long localDayId = getLocalId(db, entity.getDayId(), Constants.DAYS_TABLE_ID, 0);
        String day = DBHelper.getDayFromId(db, localDayId+"");

        long id = DBHelper.moveGoalToDone(db, localGoalId+"", day);

        if (id==0)
            throw new RuntimeException();

       return id;
    }

    /* methods for DoneTask Table synchronization*/
    public static DoneTask getDoneTask(SQLiteDatabase db, long id)
    {
        DoneTask localDT = DBHelper.getDoneTaskWithLocalIds(db,  id);

        if (localDT==null)
            return null;

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.DONE_TASKS_TABLE));
        long globalTaskId = getGlobalSIdFromDb(db, localDT.getTaskId(), Constants.dbTables.get(Constants.TASK_TABLE));
        long globalDayId = getGlobalSIdFromDb(db, localDT.getDayId(), Constants.dbTables.get(Constants.DAYS_TABLE));

        return new DoneTask(sid, globalTaskId, globalDayId);
    }

    public static long saveDoneTask(SQLiteDatabase db, DoneTask entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);
        long localDayId = getLocalId(db, entity.getDayId(), Constants.DAYS_TABLE_ID, 0);
        String day = DBHelper.getDayFromId(db, localDayId+"");

     return  DBHelper.addToDoneTasks(db, localTaskId, day);
    }

    public static void removeDoneTask(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.DONE_TASKS_TABLE), 0);
        db.delete("DoneTask", "_id" + " = ? " , new String[] { localId+""});
    }


    /* methods for Goal Table synchronization*/
    public static DTOGoal getGoal(SQLiteDatabase db, long id)
    {
        Goal goal = DBHelper.getGoalFromId(db, id+"");

        if (goal==null)
            return null;

        String text = goal.getGoalName();
        String note = goal.getGoalNote();
        long globalDayId = getGlobalSIdFromDb(db, Long.parseLong(goal.getDeadline()), Constants.dbTables.get(Constants.DAYS_TABLE));
        long sid = getGlobalSIdFromDb(db, id, Constants.GOALS_TABLE_ID);

        return  new DTOGoal(sid, text, note, globalDayId);
    }

    public static long saveGoal(SQLiteDatabase db, DTOGoal entity)
    {
        long sid = entity.getSid();
        boolean isExist = isRowExistInLocalDb(db, sid, Constants.GOALS_TABLE_ID);

        long id;
        if (isExist)
         id =   updateGoal(db, entity);

        else
         id =   addGoal(db, entity);

        return id;

    }

    private static long addGoal(SQLiteDatabase db, DTOGoal entity) {
        long localDayId = getLocalId(db, entity.getDeadline(), Constants.DAYS_TABLE_ID, 0);
        String day = DBHelper.getDayFromId(db, localDayId+"");

        long id = DBHelper.addGoalToDB(db, entity.getGoalText(), entity.getNotice(), day);

        if (id==-1)
            throw new SQLiteException();

       return id;
    }

    private static long updateGoal(SQLiteDatabase db, DTOGoal entity) {
        long localGoalId =  getLocalIdFromDb(db, entity.getSid(), Constants.GOALS_TABLE_ID);
        DBHelper.updateGoal(db, entity.getGoalText(), entity.getNotice(), localGoalId+"");

        return localGoalId;
    }

    public static void removeGoal(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.GOALS_TABLE_ID, 0);
      DBHelper.deleteGoal(db, localId+"");
    }


     /* methods for InProgressGoal Table synchronization */
     public static InProgressGoal getInProgressGoal(SQLiteDatabase db, long id)
     {
         long localGoalId = DBHelper.getInProgressGoal(db, id);

         if (localGoalId == 0)
             return null;

         long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE));
         long globalGoalId = getGlobalSIdFromDb(db, localGoalId, Constants.GOALS_TABLE_ID);

         return new InProgressGoal(sid, globalGoalId);
     }

    public static long saveInProgressGoal(SQLiteDatabase db, InProgressGoal entity)
    {
        long localGoalId = getLocalId(db, entity.getGoalId(), Constants.GOALS_TABLE_ID, 0);
       return DBHelper.addToInProgressGoals(db, localGoalId);
    }

    public static void removeInProgressGoal(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE), 0);
        db.delete("InProgressGoal", "_id" + " = ?", new String[] { localId+""});
    }


    /* methods for InProgressTask Table synchronization */

    public static InProgressTask getInProgressTask(SQLiteDatabase db, long id)
    {
        long localTaskId = DBHelper.getInProgressTask(db, id);
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.IN_PROGRESS_TASKS_TABLE));
        long globalTaskId = getGlobalSIdFromDb(db, localTaskId, Constants.TASK_TABLE_ID);

        return new InProgressTask(sid, globalTaskId);
    }

    public static long saveInProgressTask(SQLiteDatabase db, InProgressTask entity)
    {
        long localGoalId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);
        return DBHelper.addToInProgressTasks(db, localGoalId);
    }


    /* methods for MonthRepeating Table synchronization */
    public static MonthRepeating getMonthRepeating(SQLiteDatabase db, long id)
    {
        MonthRepeating localMR = DBHelper.getMonthRepeatingWithLocalIds(db, id);

        if (localMR==null)
            return null;

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE));
        long globalTaskId = getGlobalSIdFromDb(db, localMR.getTaskId(), Constants.dbTables.get(Constants.TASK_TABLE));

        return new MonthRepeating(sid, globalTaskId, localMR.getDayOfMonth());
    }

    public static long saveMonthRepeating(SQLiteDatabase db, MonthRepeating entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);

       return DBHelper.addMonthRepeating(db, localTaskId+"", entity.getDayOfMonth());
    }

    public static void removeMonthRepeating(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE), 0);
        db.delete("MonthRepeating", "_id" + " = ? " , new String[] { localId+""});
    }

     /* methods for Notification Table synchronization */
    public static Notification getNotification(SQLiteDatabase db, long id)
    {
        Notification localNotif = DBHelper.getNotificationWithLocalIds(db, id);

        if (localNotif==null)
            return null;

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE));
        long globalTaskId = getGlobalSIdFromDb(db, localNotif.getTaskId(), Constants.TASK_TABLE_ID);
        long globalDayId = getGlobalSIdFromDb(db, localNotif.getDayId(), Constants.DAYS_TABLE_ID);

        return new Notification(sid, globalTaskId, globalDayId);
    }

    public static long saveNotification(SQLiteDatabase db, Notification entity, Context context)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);
        long localDayId = getLocalId(db, entity.getDayId(), Constants.DAYS_TABLE_ID, 0);
        String day = DBHelper.getDayFromId(db, localDayId+"");

      long id =  DBHelper.addNotification(db, localTaskId+"", day);
        ManagerNotifications.cancelNotifications(db, localTaskId+"", context);
        ManagerNotifications.createNotifications(db, context, localTaskId+"");

        return id;
    }


    public static void removeNotification(SQLiteDatabase db, long id, Context context)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE), 0);
        long taskId = DBHelper.getTaskIdFromNotificationId(db, localId);
        db.delete("Notification", "_id" + " = ? " , new String[] { localId+""});

        ManagerNotifications.cancelNotifications(db, taskId+"", context);
        ManagerNotifications.createNotifications(db, context, taskId+"");

    }


    /* methods for Reminding Table synchronization */
    public static Reminding getReminding(SQLiteDatabase db, long id)
    {
        Reminding localRem = DBHelper.getRemindingWithLocalIds(db, id);
        String time = localRem.getTime();
        long globalTaskId = getGlobalSIdFromDb(db, localRem.getTaskId(), Constants.dbTables.get(Constants.TASK_TABLE));
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.REMINDING_TABLE));

        return  new Reminding(sid, globalTaskId, time);
    }

    public static long saveReminding(SQLiteDatabase db, Reminding entity)
    {
        long sid = entity.getSid();
        boolean isExist = isRowExistInLocalDb(db, sid, Constants.dbTables.get(Constants.REMINDING_TABLE));
   long id;
        if (isExist)
          id =  updateReminding(db, entity);

        else
           id = addReminding(db, entity);

        return id;

    }

    private static long addReminding(SQLiteDatabase db, Reminding entity) {

       long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);

      return   DBHelper.addToRemindingTable(db, localTaskId, entity.getTime());
    }

    private static long updateReminding(SQLiteDatabase db, Reminding entity) {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);

        DBHelper.updateRemindTable(db, localTaskId+"", entity.getTime());
        return getLocalId(db, entity.getSid(), Constants.dbTables.get(Constants.REMINDING_TABLE), 0);
    }


    /* methods for Repeating Table synchronization */
    public static Repeating getRepeating(SQLiteDatabase db, long id)
    {
        Repeating localRep = DBHelper.getRepeatingWithLocalIds(db, id);

        if (localRep==null)
            return null;

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.REPEATING_TABLE));
        long globalTaskId = getGlobalSIdFromDb(db, localRep.getTaskId(), Constants.TASK_TABLE_ID);
        long globalDayId = getGlobalSIdFromDb(db, localRep.getDayId(), Constants.DAYS_TABLE_ID);

        return new Repeating(sid, globalTaskId, globalDayId);
    }

    public static long saveRepeating(SQLiteDatabase db, Repeating entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);
        long localDayId = getLocalId(db, entity.getTaskId(), Constants.DAYS_TABLE_ID, 0);
       return DBHelper.addToRepeatingTable(db, localTaskId, localDayId);
    }

    public static void removeRepeating(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.REPEATING_TABLE), 0);
        db.delete("Repeating", "_id" + " = ? " , new String[] { localId+""});
    }

    /* methods for TaskLifecycle Table synchronization */
    public static TaskLifecycle getTaskLifecycle(SQLiteDatabase db, long id)
    {
        TaskLifecycle localRem = DBHelper.getTaskLifecycleWithLocalIds(db, id);
        System.out.println("taskId " + localRem.getTaskId());
        long globalTaskId = getGlobalSIdFromDb(db, localRem.getTaskId(), Constants.TASK_TABLE_ID);
        long globalDayFromId = getGlobalSIdFromDb(db, localRem.getDayFromId(), Constants.DAYS_TABLE_ID);
        long globalDayToId = getGlobalSIdFromDb(db, localRem.getDayToId(), Constants.DAYS_TABLE_ID);
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.TASK_LIFECYCLE_TABLE));

        return  new TaskLifecycle(sid, globalTaskId, globalDayFromId, globalDayToId);
    }

    public static long saveTaskLifecycle(SQLiteDatabase db, TaskLifecycle entity, Context context)
    {
        long sid = entity.getSid();
        boolean isExist = isRowExistInLocalDb(db, sid, Constants.dbTables.get(Constants.TASK_LIFECYCLE_TABLE));
 long id;
        if (isExist)
          id =  deleteFromAllDays(db, entity, context);

        else
          id =  addTaskLifecycle(db, entity, context);

        return id;
    }

    private static long addTaskLifecycle(SQLiteDatabase db, TaskLifecycle entity, Context context) {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);
        long localDayId = getLocalId(db, entity.getDayFromId(), Constants.DAYS_TABLE_ID, 0);
        String day = DBHelper.getDayFromId(db, localDayId+"");

       long id = DBHelper.addToTaskLifecycleTable(db, localTaskId, day);
      //  deleteFromAllDays(db, entity,context);

        return id;
    }


    public static boolean isExistInAdapter(SynchObject so, Context context) {
        SPDatabase spDatabase = new SPDatabase(context);
        SQLiteDatabase db = spDatabase.getReadableDatabase();
        long sid =  so.getRowId();
        int tableId = so.getTableId();
        boolean result = DBHelper.isExistInAdapter(db, sid, tableId);
        db.close();
        return result;
    }

    private static long deleteFromAllDays(SQLiteDatabase db, TaskLifecycle entity, Context context) {
        if (entity.getDayToId()>0) {
            long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);
            long localDayToId = getLocalId(db, entity.getDayToId(), Constants.DAYS_TABLE_ID, 0);
            DBHelper.deleteTaskFromAllDays(db, localTaskId+"", localDayToId+"", context);
            return localTaskId;
        }

        return getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);
    }


    /* methods for TaskToGoal Table synchronization */
    public static TaskToGoal getTaskToGoal(SQLiteDatabase db, long id)
    {
        TaskToGoal localTTG = DBHelper.getTaskToGoalWithLocalIds(db, id);

        if (localTTG==null)
            return null;

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE));
        long globalTaskId = getGlobalSIdFromDb(db, localTTG.getTaskId(), Constants.TASK_TABLE_ID);
        long globalGoalId = getGlobalSIdFromDb(db, localTTG.getGoalId(), Constants.GOALS_TABLE_ID);

        return new TaskToGoal(sid, globalTaskId, globalGoalId);
    }

    public static long saveTaskToGoal(SQLiteDatabase db, TaskToGoal entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID, 0);
        long localGoalId = getLocalId(db, entity.getTaskId(), Constants.GOALS_TABLE_ID, 0);
      return DBHelper.insertTaskToGoal(db, localTaskId, localGoalId);
    }

    public static void removeTaskToGoal(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE), 0);
        db.delete("TaskToGoal", "_id" + " = ? " , new String[] { localId+""});
    }

    /* Methods for converting global id's (sid) to local id's*/
    public static long getLocalId(SQLiteDatabase db, long sid, int tableId, int counter)
    {
        long id = getLocalIdFromDb(db, sid, tableId);

        if (id <=0 )
            throw new SQLiteException();

        return id;
    }

    private static long getLocalIdFromDb(SQLiteDatabase db, long sid, long tableId)
    {
        Cursor cursor = db.query ("Adapter",
                new String[] {"LOCAL_ID"},
                "TABLE_ID = ? AND SID = ?",
                new String[] {tableId+"", sid+""},
                null, null,null);

        if (cursor.moveToFirst()) {
            long rowId = cursor.getLong(0);
            cursor.close();
            return rowId;
        }
        cursor.close();

        return 0;
    }

    public static long getGlobalSIdFromDb(SQLiteDatabase db, long localId, long tableId)
    {
        Cursor cursor = db.query ("Adapter",
                new String[] {"SID"},
                "TABLE_ID = ? AND LOCAL_ID = ?",
                new String[] {tableId+"", localId+""},
                null, null,null);

        if (cursor.moveToFirst()) {
            long sid = cursor.getLong(0);
            cursor.close();
            return sid;
        }
        cursor.close();

        return 0;
    }

    private static boolean isRowExistInLocalDb(SQLiteDatabase db, long sid, int tableId) {
        Cursor cursor = db.query ("Adapter",
                new String[] {"LOCAL_ID"},
                "TABLE_ID = ? AND SID = ?",
                new String[] {tableId+"", sid+""},
                null, null,null);

        boolean result = false;

        if (cursor.moveToFirst()) {
            long rowId = cursor.getLong(0);

            if (rowId!=0)
                result = true;
        }

        cursor.close();

        return result;
    }

    public static void addToAdapter(SQLiteDatabase db, long localId, long sid, int tableId) {
        ContentValues value = new ContentValues();
        value.put("TABLE_ID", tableId);
        value.put("LOCAL_ID", localId);
        value.put("SID", sid);
         db.insert("Adapter", null, value);
        value.clear();
    }

    public static void updateSidInAdapter(SQLiteDatabase db, long localId, long sid, int tableId) {
        ContentValues value = new ContentValues();
        value.put("SID", sid);
        db.update("Adapter", value, "TABLE_ID = ? AND LOCAL_ID = ?",
                new String[] { tableId+"", localId+"" });

        value.clear();
    }


}
