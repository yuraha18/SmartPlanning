package com.eplan.yuraha.easyplanning.API;


import android.database.sqlite.SQLiteDatabase;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* get new data from app and upd server */
public class SynchServer {

    /* send new data from app to server */
    public static void synchServerWithApp(SQLiteDatabase db, ArrayList<SynchObject> synchObjects, ApiService api)  {
        System.out.println("synch server with app");
        try {
            synchObjects = sortSynchObjects(synchObjects);
            for (SynchObject so : synchObjects)
            {
                sendDataToServer(db, so, api);
            }
        }
        catch (IOException e){}


    }

    public static ArrayList<SynchObject> sortSynchObjects(ArrayList<SynchObject> synchObjects) {
        Collections.sort(synchObjects, new Comparator<SynchObject>() {
            @Override
            public int compare(SynchObject o1, SynchObject o2) {
                return o1.getTableId() > o2.getTableId() ? 1: o1.getTableId()==o2.getTableId() ? 0 : -1;
            }
        });

        System.out.println("after sort:");
        for (SynchObject object : synchObjects)
        {
            System.out.println(object.getTableId());
        }
        return synchObjects;
    }


    private static void sendDataToServer(SQLiteDatabase db, SynchObject so, ApiService api) throws IOException {
      // try {
        System.out.println(so);
            int tableId = so.getTableId();
            switch (tableId)
            {
                case 1: synchTask(db, so, api); break;
                case 2: synchReminding(db, so, api);  break;
                case 3: synchDay(db, so, api);  break;
                case 4: synchGoal(db, so, api);  break;
                case 5: synchDoneTask(db, so, api); break;
                case 6: synchInProgressTask(db, so, api);  break;
                case 7: synchRepeating(db, so, api);  break;
                case 8: synchMonthRepeating(db, so, api);  break;
                case 9: synchInProgressGoal(db, so, api);  break;
                case 10: synchTaskToGoal(db, so, api);  break;
                case 11: synchDoneGoal(db, so, api); break;
                case 12: synchDeletedTask(db, so, api);  break;
                case 13: synchTaskLifecycle(db, so, api);  break;
                case 14: synchNotification(db, so, api);  break;
            }
       // }
       // catch (Exception e){}
    }

    private static void synchTask(final SQLiteDatabase db, final SynchObject so, ApiService api) throws IOException {
        DTOTask task = DBSynchronizer.getTask(db, so.getRowId());
        Call<DTOTask> call = api.saveTask(task, MainActivity.userId);

            DTOTask response = call.execute().body();
            long localId = so.getRowId(); // this is local task Id in app
            System.out.println(response);
            long sid = response.getSid();
            int tableId=  Constants.TASK_TABLE_ID;
            DBSynchronizer.addToAdapter(db, localId, sid, tableId);
            DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
    }

    private static void synchGoal(final SQLiteDatabase db, final SynchObject so, ApiService api) throws IOException {
        DTOGoal entity = DBSynchronizer.getGoal(db, so.getRowId());

        System.out.println(entity +" entity");

        if (entity == null) {
            long sid =  DBSynchronizer.getGlobalSIdFromDb(db, so.getRowId(), so.getTableId());
            api.removeGoal(sid);
        }

        Call<DTOGoal> call = api.saveGoal(entity, MainActivity.userId);
        DTOGoal response = call.execute().body();
        long localId = so.getRowId(); // this is local task Id in app

        long sid = response.getSid();

        int tableId=  Constants.dbTables.get(Constants.GOALS_TABLE);
        DBSynchronizer.addToAdapter(db, localId, sid, tableId);
        DBSynchronizer.deleteFromSynchTable(db, localId, tableId);

    }

    private static void synchDay(final SQLiteDatabase db, final SynchObject so, ApiService api) throws IOException {
        Day entity = DBSynchronizer.getDay(db, so.getRowId());
        System.out.println(entity);
        Call<Day> call = api.saveDays(entity, MainActivity.userId);

        Day responce = call.execute().body();
        long localId = so.getRowId(); // this is local task Id in ap

        long sid = responce.getSid();
        int tableId=  Constants.DAYS_TABLE_ID;
        DBSynchronizer.addToAdapter(db, localId, sid, tableId);
        DBSynchronizer.deleteFromSynchTable(db, localId, tableId);

    }

    private static void synchReminding(final SQLiteDatabase db, final SynchObject so, ApiService api) throws IOException {
        Reminding entity = DBSynchronizer.getReminding(db, so.getRowId());
        Call<Reminding> call = api.saveReminding(entity, MainActivity.userId);
        Reminding response = call.execute().body();
        long localId = so.getRowId(); // this is local task Id in app

        long sid = response.getSid();
        int tableId=  Constants.dbTables.get(Constants.REMINDING_TABLE);
        DBSynchronizer.addToAdapter(db, localId, sid, tableId);
        DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
    }

    private static void synchRepeating(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        Repeating entity = DBSynchronizer.getRepeating(db, so.getRowId());
        if (entity == null)
        {
            long sid =  DBSynchronizer.getGlobalSIdFromDb(db, so.getRowId(), so.getTableId());
            api.removeRepeating(sid);
        }

        System.out.println(entity + " entity");

        Call<Repeating> call = api.saveRepeating(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<Repeating>() {
                @Override
                public void onResponse(Call<Repeating> call, Response<Repeating> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.REPEATING_TABLE);
                    DBSynchronizer.addToAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<Repeating> call, Throwable t) {
                    System.out.println("failure synchRepeating");
                }
            });
        }

    }

    private static void synchDeletedTask(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        DeletedTask entity = DBSynchronizer.getDeletedTask(db, so.getRowId());
        Call<DeletedTask> call = api.saveDeletedTask(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<DeletedTask>() {
                @Override
                public void onResponse(Call<DeletedTask> call, Response<DeletedTask> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.DELETED_TASKS_TABLE);
                    DBSynchronizer.addToAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<DeletedTask> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

    private static void synchDoneGoal(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        DoneGoal entity = DBSynchronizer.getDoneGoal(db, so.getRowId());
        Call<DoneGoal> call = api.saveDoneGoal(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<DoneGoal>() {
                @Override
                public void onResponse(Call<DoneGoal> call, Response<DoneGoal> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.DONE_GOALS_TABLE);
                    DBSynchronizer.addToAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<DoneGoal> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

    private static void synchDoneTask(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        DoneTask entity = DBSynchronizer.getDoneTask(db, so.getRowId());
        if (entity == null)
        {
            long sid =  DBSynchronizer.getGlobalSIdFromDb(db, so.getRowId(), so.getTableId());
            api.removeDoneTask(sid);
        }

        Call<DoneTask> call = api.saveDoneTask(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<DoneTask>() {
                @Override
                public void onResponse(Call<DoneTask> call, Response<DoneTask> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.DONE_TASKS_TABLE);
                    DBSynchronizer.addToAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<DoneTask> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

    private static void synchInProgressGoal(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        InProgressGoal entity = DBSynchronizer.getInProgressGoal(db, so.getRowId());

        if (entity == null)
        {
          long sid =  DBSynchronizer.getGlobalSIdFromDb(db, so.getRowId(), so.getTableId());
          api.removeInProgressGoal(sid);
        }

        Call<InProgressGoal> call = api.saveInProgressGoal(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<InProgressGoal>() {
                @Override
                public void onResponse(Call<InProgressGoal> call, Response<InProgressGoal> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE);
                    DBSynchronizer.addToAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<InProgressGoal> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

    private static void synchInProgressTask(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        InProgressTask entity = DBSynchronizer.getInProgressTask(db, so.getRowId());
        Call<InProgressTask> call = api.saveInProgressTask(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<InProgressTask>() {
                @Override
                public void onResponse(Call<InProgressTask> call, Response<InProgressTask> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.IN_PROGRESS_TASKS_TABLE);
                    DBSynchronizer.addToAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<InProgressTask> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

    private static void synchMonthRepeating(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        MonthRepeating entity = DBSynchronizer.getMonthRepeating(db, so.getRowId());
        if (entity == null)
        {
            long sid =  DBSynchronizer.getGlobalSIdFromDb(db, so.getRowId(), so.getTableId());
            api.removeMonthRepeating(sid);
        }
        Call<MonthRepeating> call = api.saveMonthRepeating(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<MonthRepeating>() {
                @Override
                public void onResponse(Call<MonthRepeating> call, Response<MonthRepeating> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE);
                    DBSynchronizer.addToAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<MonthRepeating> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

    private static void synchNotification(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        Notification entity = DBSynchronizer.getNotification(db, so.getRowId());
        if (entity == null)
        {
            long sid =  DBSynchronizer.getGlobalSIdFromDb(db, so.getRowId(), so.getTableId());
            api.removeNotification(sid);
        }

        Call<Notification> call = api.saveNotification(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<Notification>() {
                @Override
                public void onResponse(Call<Notification> call, Response<Notification> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE);
                    DBSynchronizer.addToAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<Notification> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

    private static void synchTaskLifecycle(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        TaskLifecycle entity = DBSynchronizer.getTaskLifecycle(db, so.getRowId());
        System.out.println(entity);
        Call<TaskLifecycle> call = api.saveTaskLifecycle(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<TaskLifecycle>() {
                @Override
                public void onResponse(Call<TaskLifecycle> call, Response<TaskLifecycle> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.TASK_LIFECYCLE_TABLE);
                    DBSynchronizer.updateSidInAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<TaskLifecycle> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

    private static void synchTaskToGoal(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        TaskToGoal entity = DBSynchronizer.getTaskToGoal(db, so.getRowId());
        if (entity == null)
        {
            long sid =  DBSynchronizer.getGlobalSIdFromDb(db, so.getRowId(), so.getTableId());
            api.removeTaskToGoal(sid);
        }

        Call<TaskToGoal> call = api.saveTaskToGoal(entity, MainActivity.userId);

        if (call!=null)
        {

            call.enqueue(new Callback<TaskToGoal>() {
                @Override
                public void onResponse(Call<TaskToGoal> call, Response<TaskToGoal> response) {
                    long localId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    int tableId=  Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE);
                    DBSynchronizer.updateSidInAdapter(db, localId, sid, tableId);
                    DBSynchronizer.deleteFromSynchTable(db, localId, tableId);
                }

                @Override
                public void onFailure(Call<TaskToGoal> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

}
