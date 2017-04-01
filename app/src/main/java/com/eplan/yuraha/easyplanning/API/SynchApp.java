package com.eplan.yuraha.easyplanning.API;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.eplan.yuraha.easyplanning.Constants;
import com.eplan.yuraha.easyplanning.MainActivity;
import com.eplan.yuraha.easyplanning.URL;
import com.eplan.yuraha.easyplanning.dto.DTOTask;
import com.eplan.yuraha.easyplanning.dto.Time;

import java.io.SyncFailedException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/* get new data from server and upd app */
public class SynchApp {

    /* send new data from server to app */
    public static void synchAppWithServer(final SQLiteDatabase db, final ApiService api, final Context context)
    {

        System.out.println("synch app with server");
        long lastSynch = getLastSynchTime(context);

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

    private static void getDataFromServer(SQLiteDatabase db, ApiService api, List<SynchObject> synchObjects, Context context) {

db.beginTransaction();
        try {
            for (SynchObject so : synchObjects)
            {
                System.out.println(so + " sended");
                synchRow(db, api, so, context);
            }

            updSynchTime(api, context);
            System.out.println("synch was successful");
            db.setTransactionSuccessful();
        }
        catch (Exception e){}
        finally {
            db.endTransaction();
        }
    }

    private static void updSynchTime(ApiService api, final Context context) {
        Call<Time> call = api.getCurrentTime();
        call.enqueue(new Callback<Time>() {
            @Override
            public void onResponse(Call<Time> call, Response<Time> response) {
               long currentTime = response.body().getCurrentTime();
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


    private static void synchRow(SQLiteDatabase db, ApiService api, SynchObject so, Context context) {

        if (so == null)
            return;

        int tableId = so.getTableId();

        switch (tableId)
        {
            case 1: synchTask(db, api, so, context); break;
        }
    }

    private static void synchTask(final SQLiteDatabase db, ApiService api, SynchObject so, final Context context) {
        Call<DTOTask> call = api.getTask(so.getId());// this is global taskId (on server)

        call.enqueue(new Callback<DTOTask>() {
            @Override
            public void onResponse(Call<DTOTask> call, Response<DTOTask> response) {
               DTOTask task = response.body();

                if (task==null)
                    throw new RuntimeException();

                 DBSynchronizer.saveTask(db, task, context);
            }

            @Override
            public void onFailure(Call<DTOTask> call, Throwable t) {
              throw new RuntimeException();
            }
        });
    }


    private static long getLastSynchTime(Context context) {

            SharedPreferences pref  = context.getSharedPreferences(Constants.PROPERTIES_PATH, Context.MODE_PRIVATE);

        return  pref.getLong(Constants.LAST_SYNCH_TIME, 0);
    }
}
