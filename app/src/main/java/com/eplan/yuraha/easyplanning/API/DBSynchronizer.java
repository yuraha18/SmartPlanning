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
import com.eplan.yuraha.easyplanning.dto.MainDTO;
import com.eplan.yuraha.easyplanning.dto.MonthRepeating;
import com.eplan.yuraha.easyplanning.dto.Notification;
import com.eplan.yuraha.easyplanning.dto.Reminding;
import com.eplan.yuraha.easyplanning.dto.Repeating;
import com.eplan.yuraha.easyplanning.dto.TaskLifecycle;
import com.eplan.yuraha.easyplanning.dto.TaskToGoal;

import java.util.ArrayList;
import java.util.HashSet;


public class DBSynchronizer {

    /*Methods for synchronization and server */
    public static void addToSyncTable(SQLiteDatabase db,  long taskID, int tableId) {
        ContentValues value = new ContentValues();
        value.put("ROW_ID", taskID);
        value.put("TABLE_ID", tableId);
        db.insert("SyncData", null, value);

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
        return DBHelper.addToTasksTable(db, task.getTaskText(), task.getPriority(), true);
    }

    private static long updateTask(SQLiteDatabase db, DTOTask task) {
      long taskId =  getLocalIdFromDb(db, task.getSid(), Constants.dbTables.get(Constants.TASK_TABLE));
      DBHelper.updateTaskTable(db, taskId+"", task.getTaskText(), task.getPriority(), true );

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
        return DBHelper.addToDateTable(db, entity.getText(), true);
    }

    /* methods for synchronizing for DeletedTask table */
    public static MainDTO getDeletedTask(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        DeletedTask localDT = DBHelper.getDeletedTaskWithLocalIds(db, id);

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.DELETED_TASKS_TABLE));
        long localTaskId = localDT.getTaskId();
        long localDayId = localDT.getDayId();
        long globalTaskId = getGlobalSIdFromDb(db, localTaskId, Constants.dbTables.get(Constants.TASK_TABLE));
        long globalDayId = getGlobalSIdFromDb(db, localDayId, Constants.dbTables.get(Constants.DAYS_TABLE));
        DeletedTask entity = new DeletedTask(sid, globalTaskId, globalDayId);
        entity.setLocalId(id);

        boolean isLocal = false;

        if (globalTaskId ==0){
            entity.setTaskId(localTaskId);
            isLocal= true;
        }

        if (globalDayId ==0){
            entity.setDayId(localDayId);
            isLocal = true;
        }

        if (isLocal){
            HashSet<DeletedTask> set = mainDTO.dtoWithLocalIds.getDeletedTasksList();
            set.add(entity);
            mainDTO.dtoWithLocalIds.setDeletedTasksList(set);
        }

        else {
            HashSet<DeletedTask> set = mainDTO.getDeletedTasksList();
            set.add(entity);
            mainDTO.setDeletedTasksList(set);
        }

        return  mainDTO;
    }

    public static long saveDeletedTask(SQLiteDatabase db, DeletedTask entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);
        long localDayId = getLocalId(db, entity.getDayId(), Constants.DAYS_TABLE_ID);

       return DBHelper.addToDeletedTasks(db, localTaskId, localDayId, true);
    }


    /* methods for DoneGoal Table synchronization*/

    public static MainDTO getDoneGoal(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        DoneGoal localDG = DBHelper.getDoneGoalWithLocalIds(db, id);
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.DONE_GOALS_TABLE));

        long localGoalId = localDG.getGoalId();
        long localDayId = localDG.getDayId();
        long globalGoalId = getGlobalSIdFromDb(db, localGoalId, Constants.dbTables.get(Constants.GOALS_TABLE));
        long globalDayId = getGlobalSIdFromDb(db, localDayId, Constants.dbTables.get(Constants.DAYS_TABLE));
        DoneGoal entity = new DoneGoal(sid, globalGoalId, globalDayId);
        entity.setLocalId(id);

        boolean isLocal = false;

        if (globalGoalId ==0){
            entity.setGoalId(localGoalId);
            isLocal=true;
        }

        if (globalDayId ==0){
            entity.setDayId(localDayId);
            isLocal=true;
        }

        if (isLocal)
        {
            HashSet<DoneGoal> set = mainDTO.dtoWithLocalIds.getDoneGoalList();
            set.add(entity);
            mainDTO.dtoWithLocalIds.setDoneGoalList(set);
        }

        else {
            HashSet<DoneGoal> set = mainDTO.getDoneGoalList();
            set.add(entity);
            mainDTO.setDoneGoalList(set);
        }


        return mainDTO;
    }

    public static long saveDoneGoal(SQLiteDatabase db, DoneGoal entity) throws RuntimeException
    {
        long localGoalId = getLocalId(db, entity.getGoalId(), Constants.GOALS_TABLE_ID);
        long localDayId = getLocalId(db, entity.getDayId(), Constants.DAYS_TABLE_ID);
        String day = DBHelper.getDayFromId(db, localDayId+"");

        long id = DBHelper.moveGoalToDone(db, localGoalId+"", day, true);

        if (id==0)
            throw new RuntimeException();

       return id;
    }

    /* methods for DoneTask Table synchronization*/
    public static MainDTO getDoneTask(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        DoneTask localDT = DBHelper.getDoneTaskWithLocalIds(db,  id);

        if (localDT==null) {
            SynchServer.addToDeletedItems(db, id, Constants.dbTables.get(Constants.DONE_TASKS_TABLE));
            return mainDTO;
        }

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.DONE_TASKS_TABLE));
        long localTaskId =localDT.getTaskId();
        long localDayId = localDT.getDayId();
        long globalTaskId = getGlobalSIdFromDb(db, localTaskId, Constants.dbTables.get(Constants.TASK_TABLE));
        long globalDayId = getGlobalSIdFromDb(db, localDayId, Constants.dbTables.get(Constants.DAYS_TABLE));
        DoneTask entity = new DoneTask(sid, globalTaskId, globalDayId);
        entity.setLocalId(id);
        boolean isLocal = false;

        if (globalTaskId ==0){
            entity.setTaskId(localTaskId);
            isLocal = true;
        }

        if (globalDayId ==0){
            entity.setDayId(localDayId);
            isLocal = true;
        }

        if (isLocal){
            HashSet<DoneTask> set = mainDTO.dtoWithLocalIds.getDoneTaskList();
            set.add(entity);
            mainDTO.dtoWithLocalIds.setDoneTaskList(set);
        }

        else {
            HashSet<DoneTask> set = mainDTO.getDoneTaskList();
            set.add(entity);
            mainDTO.setDoneTaskList(set);
        }
        return mainDTO;
    }

    public static long saveDoneTask(SQLiteDatabase db, DoneTask entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);
        System.out.println(entity);
        long localDayId = getLocalId(db, entity.getDayId(), Constants.DAYS_TABLE_ID);
        String day = DBHelper.getDayFromId(db, localDayId+"");

     return  DBHelper.addToDoneTasks(db, localTaskId, day, true);
    }

    public static void removeDoneTask(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.DONE_TASKS_TABLE));
        db.delete("DoneTask", "_id" + " = ? " , new String[] { localId+""});
    }


    /* methods for Goal Table synchronization*/
    public static MainDTO getGoal(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        Goal goal = DBHelper.getGoalFromId(db, id+"");

        if (goal==null) {
            SynchServer.addToDeletedItems(db, id, Constants.GOALS_TABLE_ID);
            return mainDTO;
        }

        String text = goal.getGoalName();
        String note = goal.getGoalNote();
        long localDayId = Long.parseLong(goal.getDeadline());
        long globalDayId = getGlobalSIdFromDb(db, localDayId, Constants.dbTables.get(Constants.DAYS_TABLE));
        long sid = getGlobalSIdFromDb(db, id, Constants.GOALS_TABLE_ID);
        DTOGoal entity = new DTOGoal(sid, text, note, globalDayId);
        entity.setLocalId(id);

        if (globalDayId==0)
        {
            HashSet<DTOGoal> set = mainDTO.dtoWithLocalIds.getDtoGoalList();
            entity.setDeadline(localDayId);
            set.add(entity);
            mainDTO.dtoWithLocalIds.setDtoGoalList(set);
        }

        else
        {
            HashSet<DTOGoal> set = mainDTO.getDtoGoalList();
            set.add(entity);
            mainDTO.setDtoGoalList(set);
        }

       return mainDTO;
    }

    public static long saveGoal(SQLiteDatabase db, DTOGoal entity)
    {
        System.out.println(entity);
        long sid = entity.getSid();
        boolean isExist = isRowExistInLocalDb(db, sid, Constants.GOALS_TABLE_ID);

        long id=0;
        if (isExist)
         id =   updateGoal(db, entity);

        else
         id =   addGoal(db, entity);

        return id;

    }

    private static long addGoal(SQLiteDatabase db, DTOGoal entity) {
        System.out.println( entity.getDeadline() +" deadline");
       SynchServer.showAdapterTable(db);
        long localDayId = getLocalId(db, entity.getDeadline(), Constants.DAYS_TABLE_ID);
        String day = DBHelper.getDayFromId(db, localDayId+"");

        long id = DBHelper.addGoalToDB(db, entity.getGoalText(), entity.getNotice(), day, true);

        if (id==-1)
            throw new SQLiteException();

       return id;
    }

    private static long updateGoal(SQLiteDatabase db, DTOGoal entity) {
        long localGoalId =  getLocalIdFromDb(db, entity.getSid(), Constants.GOALS_TABLE_ID);
        DBHelper.updateGoal(db, entity.getGoalText(), entity.getNotice(), localGoalId+"", true);

        return localGoalId;
    }

    public static void removeGoal(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.GOALS_TABLE_ID);
      DBHelper.deleteGoal(db, localId+"", true);
    }


     /* methods for InProgressGoal Table synchronization */
     public static MainDTO getInProgressGoal(SQLiteDatabase db, long id, MainDTO mainDTO)
     {
         long localGoalId = DBHelper.getInProgressGoal(db, id);

         if (localGoalId == 0){
             SynchServer.addToDeletedItems(db, id, Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE));
             return mainDTO;
         }

         long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE));
         long globalGoalId = getGlobalSIdFromDb(db, localGoalId, Constants.GOALS_TABLE_ID);
         InProgressGoal entity = new InProgressGoal(sid, globalGoalId);
         entity.setLocalId(id);

         if (globalGoalId ==0) {
             entity.setGoalId(localGoalId);
             HashSet<InProgressGoal> set = mainDTO.dtoWithLocalIds.getInProgressGoalList();
             set.add(entity);
             mainDTO.dtoWithLocalIds.setInProgressGoalList(set);
         }

         else {
             HashSet<InProgressGoal> set = mainDTO.getInProgressGoalList();
             set.add(entity);
             mainDTO.setInProgressGoalList(set);
         }

         return mainDTO;
     }

    public static long saveInProgressGoal(SQLiteDatabase db, InProgressGoal entity)
    {
        long localGoalId  = getLocalId(db, entity.getGoalId(), Constants.GOALS_TABLE_ID);
       return DBHelper.addToInProgressGoals(db, localGoalId, true);
    }

    public static void removeInProgressGoal(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE));
        db.delete("InProgressGoal", "_id" + " = ?", new String[] { localId+""});
    }


    /* methods for InProgressTask Table synchronization */

    public static MainDTO getInProgressTask(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        long localTaskId = DBHelper.getInProgressTask(db, id);
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.IN_PROGRESS_TASKS_TABLE));
        long globalTaskId = getGlobalSIdFromDb(db, localTaskId, Constants.TASK_TABLE_ID);
        InProgressTask entity = new InProgressTask(sid, globalTaskId);
        entity.setLocalId(id);

        if (globalTaskId ==0) {
          entity.setTaskId(localTaskId);
            HashSet<InProgressTask> set = mainDTO.dtoWithLocalIds.getInProgressTaskList();
            set.add(entity);
            mainDTO.dtoWithLocalIds.setInProgressTaskList(set);
        }

        else {
            HashSet<InProgressTask> set = mainDTO.getInProgressTaskList();
            set.add(entity);
            mainDTO.setInProgressTaskList(set);
        }

        return mainDTO;
    }

    public static long saveInProgressTask(SQLiteDatabase db, InProgressTask entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);
        return DBHelper.addToInProgressTasks(db, localTaskId, true);
    }


    /* methods for MonthRepeating Table synchronization */
    public static MainDTO getMonthRepeating(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        MonthRepeating localMR = DBHelper.getMonthRepeatingWithLocalIds(db, id);

        if (localMR==null) {
            SynchServer.addToDeletedItems(db, id, Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE));
            return mainDTO;
        }

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE));
        long localTaskId = localMR.getTaskId();
        long globalTaskId = getGlobalSIdFromDb(db, localTaskId, Constants.dbTables.get(Constants.TASK_TABLE));
        MonthRepeating entity = new MonthRepeating(sid, globalTaskId, localMR.getDayOfMonth());
        entity.setLocalId(id);

        if (globalTaskId ==0){
            entity.setTaskId(localTaskId);
            HashSet<MonthRepeating> set = mainDTO.dtoWithLocalIds.getMonthRepeatingList();
            set.add(entity);
            mainDTO.dtoWithLocalIds.setMonthRepeatingList(set);
        }

        else {
            HashSet<MonthRepeating> set = mainDTO.getMonthRepeatingList();
            set.add(entity);
            mainDTO.setMonthRepeatingList(set);
        }


        return mainDTO;
    }

    public static long saveMonthRepeating(SQLiteDatabase db, MonthRepeating entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);

       return DBHelper.addMonthRepeating(db, localTaskId+"", entity.getDayOfMonth(), true);
    }

    public static void removeMonthRepeating(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE));
        db.delete("MonthRepeating", "_id" + " = ? " , new String[] { localId+""});
    }

     /* methods for Notification Table synchronization */
    public static MainDTO getNotification(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        Notification localNotif = DBHelper.getNotificationWithLocalIds(db, id);

        if (localNotif==null) {
            SynchServer.addToDeletedItems(db, id, Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE));
            return mainDTO;
        }

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE));
        long localTaskId = localNotif.getTaskId();
        long localDayId = localNotif.getDayId();
        long globalTaskId = getGlobalSIdFromDb(db, localTaskId, Constants.TASK_TABLE_ID);
        long globalDayId = getGlobalSIdFromDb(db, localDayId, Constants.DAYS_TABLE_ID);
        Notification entity = new Notification(sid, globalTaskId, globalDayId);
        entity.setLocalId(id);
        boolean isLocal = false;

        if (globalTaskId ==0){
            entity.setTaskId(localTaskId);
            isLocal=true;
        }

        if (globalDayId ==0){
            entity.setDayId(localDayId);
            isLocal= true;
        }

        if (isLocal){
            HashSet<Notification> set = mainDTO.dtoWithLocalIds.getNotificationList();
            set.add(entity);
            mainDTO.dtoWithLocalIds.setNotificationList(set);
        }

        else {
            HashSet<Notification> set = mainDTO.getNotificationList();
            set.add(entity);
            mainDTO.setNotificationList(set);
        }

        return mainDTO;
    }

    public static long saveNotification(SQLiteDatabase db, Notification entity, Context context)
    {
        long localTaskId =0;// 0 is code from every dayRepeating
        if (entity.getTaskId()!=0)
        localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);

        String day= "-1";

        if (entity.getDayId()!=-1){
        long localDayId = getLocalId(db, entity.getDayId(), Constants.DAYS_TABLE_ID);
       day = DBHelper.getDayFromId(db, localDayId+"");}

      long id =  DBHelper.addNotification(db, localTaskId+"", day, true);
        ManagerNotifications.cancelNotifications(db, localTaskId+"", context);
        ManagerNotifications.createNotifications(db, context, localTaskId+"");

        return id;
    }


    public static void removeNotification(SQLiteDatabase db, long id, Context context)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE));
        long taskId = DBHelper.getTaskIdFromNotificationId(db, localId);
        db.delete("Notification", "_id" + " = ? " , new String[] { localId+""});

        ManagerNotifications.cancelNotifications(db, taskId+"", context);
        ManagerNotifications.createNotifications(db, context, taskId+"");

    }


    /* methods for Reminding Table synchronization */
    public static MainDTO getReminding(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        Reminding localRem = DBHelper.getRemindingWithLocalIds(db, id);
        String time = localRem.getTime();
        long localTaskId = localRem.getTaskId();
        long globalTaskId = getGlobalSIdFromDb(db, localRem.getTaskId(), Constants.dbTables.get(Constants.TASK_TABLE));
        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.REMINDING_TABLE));
        Reminding entity = new Reminding(sid, globalTaskId, time);
        entity.setLocalId(id);

        if (globalTaskId == 0) {
            HashSet<Reminding> set = mainDTO.dtoWithLocalIds.getRemindingList();
            entity.setTaskId(localTaskId);
            set.add(entity);
            mainDTO.dtoWithLocalIds.setRemindingList(set);
        }

        else {
            HashSet<Reminding> set = mainDTO.getRemindingList();
            set.add(entity);
            mainDTO.setRemindingList(set);
        }

        return  mainDTO;
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
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);
      return   DBHelper.addToRemindingTable(db, localTaskId, entity.getTime(), true);
    }

    private static long updateReminding(SQLiteDatabase db, Reminding entity) {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);

        DBHelper.updateRemindTable(db, localTaskId+"", entity.getTime(), true);
        return getLocalId(db, entity.getSid(), Constants.dbTables.get(Constants.REMINDING_TABLE));
    }


    /* methods for Repeating Table synchronization */
    public static MainDTO getRepeating(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        Repeating localRep = DBHelper.getRepeatingWithLocalIds(db, id);

        if (localRep==null){
            SynchServer.addToDeletedItems(db, id, Constants.dbTables.get(Constants.REPEATING_TABLE));
            return mainDTO;
        }

        long localTaskId = localRep.getTaskId();
        long localDayId = localRep.getDayId();
        long globalTaskId = getGlobalSIdFromDb(db, localTaskId , Constants.TASK_TABLE_ID);
        long globalDayId = getGlobalSIdFromDb(db, localDayId , Constants.DAYS_TABLE_ID);

        Repeating entity =  new Repeating(0, localRep.getTaskId(), localRep.getDayId());
        entity.setLocalId(id);
        boolean isLocal = false;

        if (globalTaskId == 0) {
            entity.setTaskId(localTaskId);
            isLocal = true;
        }

        if (globalDayId ==0 ){
            entity.setDayId(localTaskId);
            isLocal = true;
        }

        if (isLocal) {
            HashSet<Repeating> set = mainDTO.dtoWithLocalIds.getRepeatingList();
            set.add(entity);
            mainDTO.dtoWithLocalIds.setRepeatingList(set);
        }

        else {
            HashSet<Repeating> set = mainDTO.getRepeatingList();
            set.add(entity);
            mainDTO.setRepeatingList(set);
        }



          return mainDTO;

    }

    public static long saveRepeating(SQLiteDatabase db, Repeating entity)
    {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);
        long localDayId = getLocalId(db, entity.getDayId(), Constants.DAYS_TABLE_ID);
       return DBHelper.addToRepeatingTable(db, localTaskId, localDayId, true);
    }

    public static void removeRepeating(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.REPEATING_TABLE));
        db.delete("Repeating", "_id" + " = ? " , new String[] { localId+""});
    }

    /* methods for TaskLifecycle Table synchronization */
    public static MainDTO getTaskLifecycle(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        TaskLifecycle localRem = DBHelper.getTaskLifecycleWithLocalIds(db, id);

        long localTaskId = localRem.getTaskId();
        long localDayFromId = localRem.getDayFromId();
        long localDayToId = localRem.getDayToId();
        long globalTaskId = getGlobalSIdFromDb(db, localTaskId, Constants.TASK_TABLE_ID);
        long globalDayFromId = getGlobalSIdFromDb(db, localDayFromId, Constants.DAYS_TABLE_ID);
        long globalDayToId = getGlobalSIdFromDb(db, localDayToId, Constants.DAYS_TABLE_ID);

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.TASK_LIFECYCLE_TABLE));
        TaskLifecycle entity = new TaskLifecycle(sid, globalTaskId, globalDayFromId, globalDayToId);
        entity.setLocalId(id);
        boolean isLocal = false;

        if (globalTaskId==0){
            entity.setTaskId(localTaskId);
            isLocal=true;
        }

        if (globalDayFromId==0){
            entity.setDayFromId(localDayFromId);
            isLocal=true;
        }

        if (globalDayToId ==0){
            entity.setDayToId(localDayToId);
            isLocal=true;
        }

        if (isLocal){
            HashSet<TaskLifecycle> set = mainDTO.dtoWithLocalIds.getTaskLifecycleList();
            set.add(entity);
            mainDTO.dtoWithLocalIds.setTaskLifecycleList(set);
        }

        else {
            HashSet<TaskLifecycle> set = mainDTO.getTaskLifecycleList();
            set.add(entity);
            mainDTO.setTaskLifecycleList(set);
        }

        return  mainDTO;
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
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);
        long localDayId = getLocalId(db, entity.getDayFromId(), Constants.DAYS_TABLE_ID);
        String day = DBHelper.getDayFromId(db, localDayId+"");

       long id = DBHelper.addToTaskLifecycleTable(db, localTaskId, day, true);
      //  deleteFromAllDays(db, entity,context);

        return id;
    }


    public static boolean isExistInAdapter(SQLiteDatabase db, SynchObject so) {

        long sid =  so.getRowId();
        int tableId = so.getTableId();

        return DBHelper.isExistInAdapter(db, sid, tableId);
    }

    private static long deleteFromAllDays(SQLiteDatabase db, TaskLifecycle entity, Context context) {
        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);
        long taskLifeCycleId = DBHelper.getTaskLifecycleId(db, localTaskId+"");
        if (entity.getDayToId()>0) {
            long localDayToId = getLocalId(db, entity.getDayToId(), Constants.DAYS_TABLE_ID);
            DBHelper.deleteTaskFromAllDays(db, localTaskId+"", localDayToId+"", context, true);
            return taskLifeCycleId;
        }

        return taskLifeCycleId;
    }


    /* methods for TaskToGoal Table synchronization */
    public static MainDTO getTaskToGoal(SQLiteDatabase db, long id, MainDTO mainDTO)
    {
        TaskToGoal localTTG = DBHelper.getTaskToGoalWithLocalIds(db, id);

        if (localTTG==null) {
           SynchServer.addToDeletedItems(db, id, Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE));
            return mainDTO;
        }

        long sid = getGlobalSIdFromDb(db, id, Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE));
        long localTaskId = localTTG.getTaskId();
        long localGoalId =  localTTG.getGoalId();
        long globalTaskId = getGlobalSIdFromDb(db, localTaskId, Constants.TASK_TABLE_ID);
        long globalGoalId = getGlobalSIdFromDb(db,localGoalId, Constants.GOALS_TABLE_ID);
        TaskToGoal entity = new TaskToGoal(sid, globalTaskId, globalGoalId);
        entity.setLocalId(id);
        boolean isLocal = false;

        if (globalTaskId ==0) {
            entity.setTaskId(localTaskId);
            isLocal=true;
        }

        if (globalGoalId==0){
            entity.setGoalId(localGoalId);
            isLocal=true;
        }

        if (isLocal){
            HashSet<TaskToGoal> set = mainDTO.dtoWithLocalIds.getTaskToGoalList();
            set.add(entity);
            mainDTO.dtoWithLocalIds.setTaskToGoalList(set);
        }

        else {
            HashSet<TaskToGoal> set = mainDTO.getTaskToGoalList();
            set.add(entity);
            mainDTO.setTaskToGoalList(set);
        }


        return mainDTO;
    }

    public static long saveTaskToGoal(SQLiteDatabase db, TaskToGoal entity)
    {

        long localTaskId = getLocalId(db, entity.getTaskId(), Constants.TASK_TABLE_ID);
        long localGoalId = getLocalId(db, entity.getGoalId(), Constants.GOALS_TABLE_ID);
      return DBHelper.insertTaskToGoal(db, localTaskId, localGoalId, true);
    }

    public static void removeTaskToGoal(SQLiteDatabase db, long id)
    {
        long localId = getLocalId(db, id, Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE));
        db.delete("TaskToGoal", "_id" + " = ? " , new String[] { localId+""});
    }

    /* Methods for converting global id's (sid) to local id's*/
    public static long getLocalId(SQLiteDatabase db, long sid, int tableId)
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
        long sid = 0;
        Cursor cursor = db.query ("Adapter",
                new String[] {"SID"},
                "TABLE_ID = ? AND LOCAL_ID = ?",
                new String[] {tableId+"", localId+""},
                null, null,null);

        if (cursor.moveToFirst())
             sid = cursor.getLong(0);


        cursor.close();

        return sid;
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
        SynchObject so = new SynchObject();
        so.setRowId(sid);
        so.setTableId(tableId);
        if (!isExistInAdapter(db, so)) {
            ContentValues value = new ContentValues();
            value.put("TABLE_ID", tableId);
            value.put("LOCAL_ID", localId);
            value.put("SID", sid);
            db.insert("Adapter", null, value);
            value.clear();

            //DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
        }
    }

    public static boolean isExistInAdapterByLocalId(SQLiteDatabase db, SynchObject so) {
        long lid =  so.getRowId();
        int tableId = so.getTableId();

        return DBHelper.isExistInAdapterByLocalId(db, lid, tableId);
    }
}
