package com.eplan.yuraha.easyplanning.API;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.eplan.yuraha.easyplanning.Constants;
import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
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
import com.eplan.yuraha.easyplanning.dto.Time;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* get new data from server and upd app */
public class SynchApp {

    /* send new data from server to app */
    public static void synchAppWithServer(final SQLiteDatabase db, final ApiService api, final Context context)throws Exception{
        long a = 01;
        System.out.println(a +"code");
        System.out.println("synch app with server");
        long lastSynch = getLastSynchTime(context);

        System.out.println("ls " + lastSynch);

        Call<MainDTO> call = api.getSynchData(MainActivity.userId, lastSynch);
        Response<MainDTO> response;

            response = call.execute();
            if (response.body()!=null )
                getDataFromServer(db, api, response.body(), context);


    }

    private static void getDataFromServer(SQLiteDatabase db, ApiService api, MainDTO mainDTO, Context context) throws Exception {
              synchRow(db, mainDTO, context);
    }

    public static void updSynchTime(ApiService api, final Context context) throws IOException {

        Call<Time> call = api.getCurrentTime();
        Response<Time> response = call.execute();
        long currentTime = response.body().getCurrentTime();
        System.out.println("curt " + currentTime);
        if (currentTime==0 )
            throw new RuntimeException();

        SharedPreferences pref  = context.getSharedPreferences(Constants.PROPERTIES_PATH, Context.MODE_PRIVATE);
        pref.edit().putLong(Constants.LAST_SYNCH_TIME, currentTime).apply();
    }


    private static void synchRow(SQLiteDatabase db, MainDTO mainDTO, Context context) throws Exception { System.out.println("di " + mainDTO.deletedItems);
        System.out.println(mainDTO);
        synchTaskList(db, mainDTO.getDtoTaskList());
        synchRemindingList(db, mainDTO.getRemindingList());
        synchDayList(db, mainDTO.getDayList());
        synchGoalList(db, mainDTO.getDtoGoalList());
        synchDoneTaskList(db,  mainDTO.getDoneTaskList());
        synchInProgressTaskList(db, mainDTO.getInProgressTaskList());
        synchRepeatingList(db, mainDTO.getRepeatingList());
        synchMonthRepeatingList(db, mainDTO.getMonthRepeatingList());
        synchInProgressGoalList(db, mainDTO.getInProgressGoalList());
        synchTaskToGoalList(db, mainDTO.getTaskToGoalList());
        synchDoneGoalList(db, mainDTO.getDoneGoalList());
        synchDeletedTaskList(db, mainDTO.getDeletedTasksList());
        synchTaskLifecycleList(db, mainDTO.getTaskLifecycleList(), context);
        synchNotificationList(db, mainDTO.getNotificationList(), context);
        deleteItems(db, mainDTO.deletedItems, context);


    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void synchTaskList(final SQLiteDatabase db, HashSet<DTOTask> set) {

        for (DTOTask task : set) {

            if (task==null)
                throw new RuntimeException();

            long localId = DBSynchronizer.saveTask(db, task);
            int tableId = Constants.dbTables.get(Constants.TASK_TABLE);
            DBSynchronizer.addToAdapter(db, localId, task.getSid(), tableId);
        }

    }

    public static void synchDayList(final SQLiteDatabase db, HashSet<Day> set) {

        for (Day dto : set) {

            if (dto==null)
                throw new RuntimeException();

            long localId = DBSynchronizer.saveDay(db, dto);
            DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.DAYS_TABLE_ID);
        }
    }

    public static void synchRemindingList(final SQLiteDatabase db, HashSet<Reminding> set) {

        for (Reminding dto : set) {

            if (dto==null)
                throw new RuntimeException();


            long localId = DBSynchronizer.saveReminding(db, dto);
            DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.REMINDING_TABLE));
        }
    }

    public static void synchGoalList(final SQLiteDatabase db, HashSet<DTOGoal> set) {
        for (DTOGoal dto : set) {

            if (dto==null)
                throw new RuntimeException();

            long localId = DBSynchronizer.saveGoal(db, dto);
            DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.GOALS_TABLE_ID);
        }
    }

    public static void synchDeletedTaskList(final SQLiteDatabase db, HashSet<DeletedTask> set) {

        for (DeletedTask dto : set) {
            if (dto == null)
                throw new RuntimeException();

            long localId = DBSynchronizer.saveDeletedTask(db, dto);
            DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.DELETED_TASKS_TABLE));
        }
    }

    public static void synchDoneGoalList(final SQLiteDatabase db, HashSet<DoneGoal> set) {

            for (DoneGoal dto : set) {
                if (dto == null)
                    throw new RuntimeException();

                long goalLid = DBSynchronizer.getLocalId(db, dto.getGoalId(), Constants.GOALS_TABLE_ID);
                dto.setGoalId(goalLid);

                long dayLid = DBSynchronizer.getLocalId(db, dto.getDayId(), Constants.DAYS_TABLE_ID);
                dto.setDayId(dayLid);
                long localId = DBSynchronizer.saveDoneGoal(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.DONE_GOALS_TABLE));
            }
    }

    public static void synchDoneTaskList(final SQLiteDatabase db, HashSet<DoneTask> set) {

               for (DoneTask dto : set) {
                   if (dto == null)
                       throw new RuntimeException();

                   long localId = DBSynchronizer.saveDoneTask(db, dto);
                   DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.DONE_TASKS_TABLE));
               }
    }

    public static void synchInProgressGoalList(final SQLiteDatabase db, HashSet<InProgressGoal> set) {

                 for (InProgressGoal dto: set) {
                     if (dto == null)
                         throw new RuntimeException();

                     long goalLid = DBSynchronizer.getLocalId(db, dto.getGoalId(), Constants.GOALS_TABLE_ID);

                     if (!DBHelper.isExistInProgressGoal(db, goalLid)){
                     long localId = DBSynchronizer.saveInProgressGoal(db, dto);
                     DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE));
                 }
                 }

    }

    public static void synchInProgressTaskList(final SQLiteDatabase db, HashSet<InProgressTask> set) {

        for (InProgressTask dto : set) {
            if (dto == null)
                throw new RuntimeException();

            long localId = DBSynchronizer.saveInProgressTask(db, dto);
            DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.IN_PROGRESS_TASKS_TABLE));
        }
    }

    public static void synchMonthRepeatingList(final SQLiteDatabase db, HashSet<MonthRepeating> set) {

               for (MonthRepeating dto : set) {
                   if (dto == null)
                       throw new RuntimeException();

                   long localId = DBSynchronizer.saveMonthRepeating(db, dto);
                   DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE));
               }
    }

    public static void synchNotificationList(final SQLiteDatabase db, HashSet<Notification> set, final Context context) {

                for (Notification dto : set){
                    if (dto == null)
                        throw new RuntimeException();

                long localId = DBSynchronizer.saveNotification(db, dto, context);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE));
            }
    }

    public static void synchRepeatingList(final SQLiteDatabase db, HashSet<Repeating> set) {

for (Repeating dto : set){
    if (dto == null)
        throw new RuntimeException();

        long localId = DBSynchronizer.saveRepeating(db, dto);
        DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.REPEATING_TABLE));
    }
    }

    public static void synchTaskLifecycleList(final SQLiteDatabase db, HashSet<TaskLifecycle> set, final Context context) {


               for (TaskLifecycle dto : set) {
                   if (dto == null)
                       throw new RuntimeException();

                   long localId = DBSynchronizer.saveTaskLifecycle(db, dto, context);
                   DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.TASK_LIFECYCLE_TABLE));
               }
    }

    public static void synchTaskToGoalList(final SQLiteDatabase db, HashSet<TaskToGoal> set) {

               for (TaskToGoal dto : set){
                   if (dto == null)
                       throw new RuntimeException();

                long localId = DBSynchronizer.saveTaskToGoal(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE));
            }

    }

    private static void deleteItems(SQLiteDatabase db, HashMap<Long, Integer> deletedItems, Context context) {

        for (Map.Entry<Long, Integer> entry : deletedItems.entrySet())
        {
            try {
                int tableId = entry.getValue();
                long sid = entry.getKey();


                long localId =   DBSynchronizer.getLocalId(db, sid, tableId);
                if (localId!=-1) {
                    switch (tableId) {
                        case 4:
                            DBSynchronizer.removeGoal(db, localId);
                            break;
                        case 5:
                            DBSynchronizer.removeDoneTask(db, localId);
                            break;
                        case 7:
                            DBSynchronizer.removeRepeating(db, localId);
                            break;
                        case 8:
                            DBSynchronizer.removeMonthRepeating(db, localId);
                            break;
                        case 9:
                            DBSynchronizer.removeInProgressGoal(db, localId);
                            break;
                        case 10:
                            DBSynchronizer.removeTaskToGoal(db, localId);
                            break;
                        case 14:
                            DBSynchronizer.removeNotification(db, localId, context);
                            break;

                    }
            }

            }
            catch (Exception e){e.printStackTrace();}
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static long getLastSynchTime(Context context) {

            SharedPreferences pref  = context.getSharedPreferences(Constants.PROPERTIES_PATH, Context.MODE_PRIVATE);

        return  pref.getLong(Constants.LAST_SYNCH_TIME, 0);
    }
}
