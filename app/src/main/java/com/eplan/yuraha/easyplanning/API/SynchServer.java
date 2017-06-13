package com.eplan.yuraha.easyplanning.API;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.eplan.yuraha.easyplanning.Constants;
import com.eplan.yuraha.easyplanning.MainActivity;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* get new data from app and upd server */
public class SynchServer {

    private static MainDTO mainDTO;
    /* send new data from app to server */
    public static void synchServerWithApp(SQLiteDatabase db, ArrayList<SynchObject> synchObjects, ApiService api) throws IOException {
        System.out.println("synch server with app");

            mainDTO = new MainDTO();
        System.out.println(synchObjects);
            for (SynchObject so : synchObjects)
            {
                populateMainDTOForSending(db, so);
            }


        System.out.println("synch server: " + mainDTO);
            Call<MainDTO> call = api.saveList(mainDTO, MainActivity.userId);
            handleResult(call, db);

        System.out.println("end synch server");

    }

    public static void showAdapterTable(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM Adapter" , null);
        if (cursor .moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                System.out.println("{tid: " + cursor.getInt(1) + ", lid: " + cursor.getLong(2) + ", sid: " + cursor.getLong(3) );

                cursor.moveToNext();
            }
        }

        cursor.close();
    }

    private static void populateMainDTOForSending(final SQLiteDatabase db, SynchObject so) {
         try {

        int tableId = so.getTableId();
        if (so.getRowId()!=0) {
            switch (tableId) {
                case 1:putTask(db, so);break;
                case 2:putReminding(db, so);break;
                case 3:putDay(db, so);break;
                case 4:putGoal(db, so);break;
                case 5:putDoneTask(db, so);break;
                case 6:putInProgressTask(db, so);break;
                case 7:putRepeating(db, so);break;
                case 8:putMonthRepeating(db, so);break;
                case 9:putInProgressGoal(db, so);break;
                case 10:putTaskToGoal(db, so);break;
                case 11:putDoneGoal(db, so);break;
                case 12:putDeletedTask(db, so);break;
                case 13:putTaskLifecycle(db, so);break;
                case 14:putNotification(db, so);break;
            }
        }
         }
         catch (Exception e){e.printStackTrace();}
    }

    private static void handleResult(Call<MainDTO> call, final SQLiteDatabase db) throws IOException {
        Response<MainDTO> response = call.execute();
        MainDTO dto = response.body();
        System.out.println("result : "+dto);
        if (dto!=null)
            saveSids(dto, db);

         else
           throw new RuntimeException();
    }

    private static void finishSynch(SQLiteDatabase db, long localId, long sid, int tableId)
    {
       DBSynchronizer.addToAdapter(db, localId, sid, tableId);
    }

    private static void saveSids(MainDTO dto, SQLiteDatabase db) {
        saveLocalSids(dto, db);
        saveGlobalSids(dto, db);

        for (Map.Entry<Long, Integer> deletedItems : dto.deletedItems.entrySet())
            removeDeletedItemsFromSynchTable(db, deletedItems.getKey(), deletedItems.getValue());



    }

    private static void saveGlobalSids(MainDTO dto, SQLiteDatabase db) {
        for (Reminding entity : dto.getRemindingList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.REMINDING_TABLE));

        for (DTOTask entity : dto.getDtoTaskList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.TASK_TABLE));

        for (Day entity : dto.getDayList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.DAYS_TABLE_ID);

        for (DeletedTask entity : dto.getDeletedTasksList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.DELETED_TASKS_TABLE));

        for (DoneGoal entity : dto.getDoneGoalList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.DONE_GOALS_TABLE));

        for (DoneTask entity : dto.getDoneTaskList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.DONE_TASKS_TABLE));

        for (DTOGoal entity : dto.getDtoGoalList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.GOALS_TABLE));

        for (InProgressGoal entity : dto.getInProgressGoalList()) {
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE));
        }

        for (InProgressTask entity : dto.getInProgressTaskList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.IN_PROGRESS_TASKS_TABLE));

        for (MonthRepeating entity : dto.getMonthRepeatingList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE));

        for (Notification entity : dto.getNotificationList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE));

        for (Repeating entity : dto.getRepeatingList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.REPEATING_TABLE));

        for (TaskLifecycle entity : dto.getTaskLifecycleList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.TASK_LIFECYCLE_TABLE));

        for (TaskToGoal entity : dto.getTaskToGoalList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE));
    }

    private static void saveLocalSids(MainDTO dto, SQLiteDatabase db) {
        for (Reminding entity : dto.dtoWithLocalIds.getRemindingList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.REMINDING_TABLE));

        for (DTOTask entity : dto.dtoWithLocalIds.getDtoTaskList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.TASK_TABLE));

        for (Day entity : dto.dtoWithLocalIds.getDayList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.DAYS_TABLE_ID);

        for (DeletedTask entity : dto.dtoWithLocalIds.getDeletedTasksList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.DELETED_TASKS_TABLE));

        for (DoneGoal entity : dto.dtoWithLocalIds.getDoneGoalList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.DONE_GOALS_TABLE));

        for (DoneTask entity : dto.dtoWithLocalIds.getDoneTaskList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.DONE_TASKS_TABLE));

        for (DTOGoal entity : dto.dtoWithLocalIds.getDtoGoalList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.GOALS_TABLE));

        for (InProgressGoal entity : dto.dtoWithLocalIds.getInProgressGoalList()) {
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE));
        }

        for (InProgressTask entity : dto.dtoWithLocalIds.getInProgressTaskList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.IN_PROGRESS_TASKS_TABLE));

        for (MonthRepeating entity : dto.dtoWithLocalIds.getMonthRepeatingList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE));

        for (Notification entity : dto.dtoWithLocalIds.getNotificationList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE));

        for (Repeating entity : dto.dtoWithLocalIds.getRepeatingList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.REPEATING_TABLE));

        for (TaskLifecycle entity : dto.dtoWithLocalIds.getTaskLifecycleList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.TASK_LIFECYCLE_TABLE));

        for (TaskToGoal entity : dto.dtoWithLocalIds.getTaskToGoalList())
            finishSynch(db, entity.getLocalId(), entity.getSid(), Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE));
    }

    private static void removeDeletedItemsFromSynchTable(SQLiteDatabase db, Long sid, Integer tableId) {
        long localId =0;
        try {
             localId = DBSynchronizer.getLocalId(db, sid, tableId);
            DBSynchronizer.addToAdapter(db, localId, sid, tableId);
        }
        catch (SQLiteException e){}


    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    private static void putTask(final SQLiteDatabase db, final SynchObject so)  {
        DTOTask entity = DBSynchronizer.getTask(db, so.getRowId());
        entity.setLocalId(so.getRowId());
       HashSet<DTOTask> set = mainDTO.getDtoTaskList();
        set.add(entity);
        mainDTO.setDtoTaskList(set);

    }

    private static void putDay(final SQLiteDatabase db, final SynchObject so) {
        Day entity = DBSynchronizer.getDay(db, so.getRowId());
        entity.setLocalId(so.getRowId());
        HashSet<Day> set = mainDTO.getDayList();
        set.add(entity);
        mainDTO.setDayList(set);

    }

    private static void putGoal(final SQLiteDatabase db, final SynchObject so) {
        mainDTO = DBSynchronizer.getGoal(db, so.getRowId(), mainDTO);
    }

    private static void putReminding(final SQLiteDatabase db, final SynchObject so)  {
       mainDTO = DBSynchronizer.getReminding(db, so.getRowId(), mainDTO);
    }

    private static void putRepeating(final SQLiteDatabase db, final SynchObject so) {
        mainDTO = DBSynchronizer.getRepeating(db, so.getRowId(), mainDTO);

    }

    private static void putDeletedTask(final SQLiteDatabase db, final SynchObject so) {
       mainDTO = DBSynchronizer.getDeletedTask(db, so.getRowId(), mainDTO);

    }

    private static void putDoneGoal(final SQLiteDatabase db, final SynchObject so) {
        mainDTO = DBSynchronizer.getDoneGoal(db, so.getRowId(), mainDTO);
    }

    private static void putDoneTask(final SQLiteDatabase db, final SynchObject so) {
        mainDTO= DBSynchronizer.getDoneTask(db, so.getRowId(), mainDTO);

    }

    private static void putInProgressGoal(final SQLiteDatabase db, final SynchObject so) {
        mainDTO = DBSynchronizer.getInProgressGoal(db, so.getRowId(), mainDTO);
    }

    private static void putInProgressTask(final SQLiteDatabase db, final SynchObject so) {
        mainDTO = DBSynchronizer.getInProgressTask(db, so.getRowId(), mainDTO);
    }

    private static void putMonthRepeating(final SQLiteDatabase db, final SynchObject so) {
        mainDTO = DBSynchronizer.getMonthRepeating(db, so.getRowId(), mainDTO);
    }

    private static void putNotification(final SQLiteDatabase db, final SynchObject so) {
       mainDTO = DBSynchronizer.getNotification(db, so.getRowId(), mainDTO);
    }

    private static void putTaskLifecycle(final SQLiteDatabase db, final SynchObject so) {
       mainDTO = DBSynchronizer.getTaskLifecycle(db, so.getRowId(), mainDTO);
    }

    private static void putTaskToGoal(final SQLiteDatabase db, final SynchObject so) {
        mainDTO = DBSynchronizer.getTaskToGoal(db, so.getRowId(), mainDTO);
    }

    public static void addToDeletedItems(SQLiteDatabase db, long localId, int tableId) {
 SynchObject so = new SynchObject(0, localId, tableId);

        // item can be created and deleted before synch, thats why there are no sid in Adapter. This value shouldnt send on server
        if (DBSynchronizer.isExistInAdapterByLocalId(db, so)) {
            long sid=  DBSynchronizer.getGlobalSIdFromDb(db, localId, tableId);
            mainDTO.deletedItems.put(sid, tableId);
        }

        else
            removeDeletedItemsFromSynchTable(db, localId, tableId);
    }
}
