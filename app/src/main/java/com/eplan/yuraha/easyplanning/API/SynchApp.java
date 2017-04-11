package com.eplan.yuraha.easyplanning.API;


import android.content.Context;
import android.content.SharedPreferences;
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
import com.eplan.yuraha.easyplanning.dto.Time;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* get new data from server and upd app */
public class SynchApp {

    /* send new data from server to app */
    public static void synchAppWithServer(final SQLiteDatabase db, final ApiService api, final Context context)
    {

        System.out.println("synch app with server");
        long lastSynch = getLastSynchTime(context);

        System.out.println("ls " + lastSynch);

        Call<List<SynchObject>> call = api.getSynchData(MainActivity.userId, lastSynch);
        call.enqueue(new Callback<List<SynchObject>>() {
            @Override
            public void onResponse(Call<List<SynchObject>> call, Response<List<SynchObject>> response) {
                if (response.body()!=null && response.body().size()>0)
                {

                    getDataFromServer(db, api, response.body(), context);
                }
            }

            @Override
            public void onFailure(Call<List<SynchObject>> call, Throwable t) {

            }
        });
    }

    private static void getDataFromServer(SQLiteDatabase db, ApiService api, List<SynchObject> synchObjects, Context context)  {

db.beginTransaction();
        try {
            System.out.println(synchObjects);
            synchObjects = SynchServer.sortSynchObjects(new ArrayList<>(synchObjects));
            synchObjects = filtrateSynchData(new ArrayList<>(synchObjects), context);
            System.out.println(synchObjects);
            for (SynchObject so : synchObjects)
                synchRow(db, api, so, context);

            System.out.println("updating last synch ");
            updSynchTime(api, context);
            System.out.println("synch was successful");
            db.setTransactionSuccessful();
            long lastSynch = getLastSynchTime(context);

            System.out.println("ls " + lastSynch);
        }
        catch (IOException e){e.printStackTrace();
        }
        finally {
            db.endTransaction();

        }
    }

    private static List<SynchObject> filtrateSynchData(ArrayList<SynchObject> synchObjects, Context context) {
        List<SynchObject> filtratedList = new ArrayList<>();
        for (SynchObject so : synchObjects)
        {
            if (!DBSynchronizer.isExistInAdapter(so, context))
                filtratedList.add(so);
        }

        return filtratedList;
    }



    public static void updSynchTime(ApiService api, final Context context) {

        Call<Time> call = api.getCurrentTime();
        call.enqueue(new Callback<Time>() {
            @Override
            public void onResponse(Call<Time> call, Response<Time> response) {
               long currentTime = response.body().getCurrentTime();
                System.out.println("curt " + currentTime);
                if (currentTime==0 || response.body()==null)
                    throw new RuntimeException();

                SharedPreferences pref  = context.getSharedPreferences(Constants.PROPERTIES_PATH, Context.MODE_PRIVATE);
                 pref.edit().putLong(Constants.LAST_SYNCH_TIME, currentTime).apply();

            }

            @Override
            public void onFailure(Call<Time> call, Throwable t) {
                // if dont get synch time - throw exception which will throw me from transaction and didt end synch successfully
                throw new RuntimeException();
            }
        });



    }


    private static void synchRow(SQLiteDatabase db, ApiService api, SynchObject so, Context context) throws IOException {

        System.out.println(so);
        if (so == null)
            return;

        int tableId = so.getTableId();

        switch (tableId)
        {
            case 1: synchTask(db, api, so.getRowId()); break;
            case 2: synchReminding(db, api, so.getRowId());  break;
            case 3: synchDay(db, api, so.getRowId());  break;
            case 4: synchGoal(db, api, so.getRowId());  break;
            case 5: synchDoneTask(db, api, so.getRowId()); break;
            case 6: synchInProgressTask(db, api, so.getRowId());  break;
            case 7: synchRepeating(db, api, so.getRowId());  break;
            case 8: synchMonthRepeating(db, api, so.getRowId());  break;
            case 9: synchInProgressGoal(db, api, so.getRowId());  break;
            case 10: synchTaskToGoal(db, api, so.getRowId());  break;
            case 11: synchDoneGoal(db, api, so.getRowId()); break;
            case 12: synchDeletedTask(db, api, so.getRowId());  break;
            case 13: synchTaskLifecycle(db, api, so.getRowId(), context);  break;
            case 14: synchNotification(db, api, so.getRowId(), context);  break;
        }
    }


    /* */
    public static void synchTask(final SQLiteDatabase db, ApiService api, long sid) throws IOException {
        Call<DTOTask> call = api.getTask(sid);// this is global taskId (on server)
        DTOTask task = call.execute().body();

        if (task==null)
            throw new RuntimeException();

        long localId =  DBSynchronizer.saveTask(db, task);
        DBSynchronizer.addToAdapter(db, localId, task.getSid(), Constants.dbTables.get(Constants.TASK_TABLE));


    }

    public static void synchDay(final SQLiteDatabase db, ApiService api, final long sid) throws IOException {
        Call<Day> call = api.getDay(sid);// this is global taskId (on server)
        Day dto = call.execute().body();

        if (dto==null)
            throw new RuntimeException();

        long localId = DBSynchronizer.saveDay(db, dto);
        DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.DAYS_TABLE_ID);
    }

    public static void synchReminding(final SQLiteDatabase db, ApiService api, long sid) throws IOException {
        Call<Reminding> call = api.getReminding(sid);// this is global taskId (on server)

        Reminding dto = call.execute().body();

        if (dto==null)
            throw new RuntimeException();

        long localId = DBSynchronizer.saveReminding(db, dto);
        DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.REMINDING_TABLE));
    }

    public static void synchGoal(final SQLiteDatabase db, ApiService api, final long sid) throws IOException {
        Call<DTOGoal> call = api.getGoal(sid);// this is global taskId (on server)
        System.out.println(sid + "sid");
        DTOGoal dto = call.execute().body();

        if (dto==null)
            DBSynchronizer.removeGoal(db, sid);

        long localId = DBSynchronizer.saveGoal(db, dto);
        DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.GOALS_TABLE_ID);
    }

    public static void synchDeletedTask(final SQLiteDatabase db, ApiService api, long sid) {
        Call<DeletedTask> call = api.getDeletedTask(sid);// this is global taskId (on server)
        System.out.println("DeletedTask"+sid);
        call.enqueue(new Callback<DeletedTask>() {
            @Override
            public void onResponse(Call<DeletedTask> call, Response<DeletedTask> response) {
                DeletedTask dto = response.body();

                if (dto==null)
                    throw new RuntimeException();

               long localId = DBSynchronizer.saveDeletedTask(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.DELETED_TASKS_TABLE));
            }

            @Override
            public void onFailure(Call<DeletedTask> call, Throwable t) {
                throw new RuntimeException();
            }
        });
    }

    public static void synchDoneGoal(final SQLiteDatabase db, ApiService api, long sid) {
        Call<DoneGoal> call = api.getDoneGoal(sid);// this is global taskId (on server)
        System.out.println("DoneGoal"+sid);
        call.enqueue(new Callback<DoneGoal>() {
            @Override
            public void onResponse(Call<DoneGoal> call, Response<DoneGoal> response) {
                DoneGoal dto = response.body();

                if (dto==null)
                    throw new RuntimeException();

                long localId = DBSynchronizer.saveDoneGoal(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.DONE_GOALS_TABLE));
            }

            @Override
            public void onFailure(Call<DoneGoal> call, Throwable t) {
                throw new RuntimeException();
            }
        });
    }

    public static void synchDoneTask(final SQLiteDatabase db, ApiService api, final long sid) {
        Call<DoneTask> call = api.getDoneTask(sid);// this is global taskId (on server)
        System.out.println("DoneTask"+sid);
        call.enqueue(new Callback<DoneTask>() {
            @Override
            public void onResponse(Call<DoneTask> call, Response<DoneTask> response) {
                DoneTask dto = response.body();

                if (dto==null)
                    DBSynchronizer.removeDoneTask(db, sid);

                long localId = DBSynchronizer.saveDoneTask(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.DONE_TASKS_TABLE));
            }

            @Override
            public void onFailure(Call<DoneTask> call, Throwable t) {
                throw new RuntimeException();
            }
        });
    }

    public static void synchInProgressGoal(final SQLiteDatabase db, ApiService api, final long sid) {
        Call<InProgressGoal> call = api.getInProgressGoal(sid);// this is global taskId (on server)

        call.enqueue(new Callback<InProgressGoal>() {
            @Override
            public void onResponse(Call<InProgressGoal> call, Response<InProgressGoal> response) {
                InProgressGoal dto = response.body();

                if (dto==null)
                    DBSynchronizer.removeInProgressGoal(db, sid);

                long localId = DBSynchronizer.saveInProgressGoal(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.IN_PROGRESS_GOALS_TABLE));
            }

            @Override
            public void onFailure(Call<InProgressGoal> call, Throwable t) {
                throw new RuntimeException();
            }
        });
    }

    public static void synchInProgressTask(final SQLiteDatabase db, ApiService api,final long sid) {
        Call<InProgressTask> call = api.getInProgressTask(sid);// this is global taskId (on server)
        System.out.println("InProgressTask"+sid);
        call.enqueue(new Callback<InProgressTask>() {
            @Override
            public void onResponse(Call<InProgressTask> call, Response<InProgressTask> response) {
                InProgressTask dto = response.body();

                if (dto==null)
                    throw new RuntimeException();

                long localId = DBSynchronizer.saveInProgressTask(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.IN_PROGRESS_TASKS_TABLE));
            }

            @Override
            public void onFailure(Call<InProgressTask> call, Throwable t) {
                System.out.println("InProgressTask"+sid);
            }
        });
    }

    public static void synchMonthRepeating(final SQLiteDatabase db, ApiService api, final long sid) {
        Call<MonthRepeating> call = api.getMonthRepeating(sid);// this is global taskId (on server)
        System.out.println("MonthRepeating"+sid);
        call.enqueue(new Callback<MonthRepeating>() {
            @Override
            public void onResponse(Call<MonthRepeating> call, Response<MonthRepeating> response) {
                MonthRepeating dto = response.body();

                if (dto==null)
                    DBSynchronizer.removeMonthRepeating(db, sid);

                long localId = DBSynchronizer.saveMonthRepeating(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.MONTH_REPEATING_TABLE));
            }

            @Override
            public void onFailure(Call<MonthRepeating> call, Throwable t) {
                throw new RuntimeException();
            }
        });
    }

    public static void synchNotification(final SQLiteDatabase db, ApiService api, final long sid, final Context context) {
        Call<Notification> call = api.getNotification(sid);// this is global taskId (on server)
        System.out.println("Notification"+sid);
        call.enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                Notification dto = response.body();

                if (dto==null)
                    DBSynchronizer.removeNotification(db, sid, context);

                long localId = DBSynchronizer.saveNotification(db, dto, context);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.NOTIFICATIONS_TABLE));
            }

            @Override
            public void onFailure(Call<Notification> call, Throwable t) {
                throw new RuntimeException();
            }
        });
    }

    public static void synchRepeating(final SQLiteDatabase db, ApiService api, final long sid) {
        Call<Repeating> call = api.getRepeating(sid);// this is global taskId (on server)
        System.out.println("Repeating"+sid);
        call.enqueue(new Callback<Repeating>() {
            @Override
            public void onResponse(Call<Repeating> call, Response<Repeating> response) {
                Repeating dto = response.body();

                if (dto==null)
                    DBSynchronizer.removeRepeating(db, sid);

                long localId = DBSynchronizer.saveRepeating(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.REPEATING_TABLE));
            }

            @Override
            public void onFailure(Call<Repeating> call, Throwable t) {
                throw new RuntimeException();
            }
        });
    }

    public static void synchTaskLifecycle(final SQLiteDatabase db, ApiService api, long sid, final Context context) {
        Call<TaskLifecycle> call = api.getTaskLifecycle(sid);// this is global taskId (on server)
        System.out.println("TaskLifecycle"+sid);
        call.enqueue(new Callback<TaskLifecycle>() {
            @Override
            public void onResponse(Call<TaskLifecycle> call, Response<TaskLifecycle> response) {
                TaskLifecycle dto = response.body();

                System.out.println(response.body());
                if (dto==null)
                    throw new RuntimeException();

                long localId = DBSynchronizer.saveTaskLifecycle(db, dto, context);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.TASK_LIFECYCLE_TABLE));
            }

            @Override
            public void onFailure(Call<TaskLifecycle> call, Throwable t) {
                throw new RuntimeException();
            }
        });
    }

    public static void synchTaskToGoal(final SQLiteDatabase db, ApiService api, final long sid) {
        Call<TaskToGoal> call = api.getTaskToGoal(sid);// this is global taskId (on server)
        System.out.println("TaskToGoal"+sid);
        call.enqueue(new Callback<TaskToGoal>() {
            @Override
            public void onResponse(Call<TaskToGoal> call, Response<TaskToGoal> response) {
                TaskToGoal dto = response.body();

                if (dto==null)
                    DBSynchronizer.removeTaskToGoal(db, sid);

                long localId = DBSynchronizer.saveTaskToGoal(db, dto);
                DBSynchronizer.addToAdapter(db, localId, dto.getSid(), Constants.dbTables.get(Constants.TASK_TO_GOAL_TABLE));
            }

            @Override
            public void onFailure(Call<TaskToGoal> call, Throwable t) {
                throw new RuntimeException();
            }
        });
    }


    public static long getLastSynchTime(Context context) {

            SharedPreferences pref  = context.getSharedPreferences(Constants.PROPERTIES_PATH, Context.MODE_PRIVATE);

        return  pref.getLong(Constants.LAST_SYNCH_TIME, 0);
    }
}
