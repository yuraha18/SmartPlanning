package com.eplan.yuraha.easyplanning.API;


import android.database.sqlite.SQLiteDatabase;

import com.eplan.yuraha.easyplanning.MainActivity;
import com.eplan.yuraha.easyplanning.dto.DTOTask;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* get new data from app and upd server */
public class SynchServer {

    /* send new data from app to server */
    public static void synchServerWithApp(SQLiteDatabase db, ArrayList<SynchObject> synchObjects, ApiService api) {
        System.out.println("synch server with app");
        for (SynchObject so : synchObjects)
        {
            sendDataToServer(db, so, api);
        }

    }

    private static void sendDataToServer(SQLiteDatabase db, SynchObject so, ApiService api) {

        try {

            int tableId = so.getTableId();
            switch (tableId)
            {
                case 1: synchTask(db, so, api);break;
            }

        }
        catch (Exception e){}

    }

    private static void synchTask(final SQLiteDatabase db, final SynchObject so, ApiService api) {
        DTOTask task = DBSynchronizer.getTask(db, so.getRowId());
        Call<DTOTask> call = api.saveTask(task);

        if (call!=null)
        {

            call.enqueue(new Callback<DTOTask>() {
                @Override
                public void onResponse(Call<DTOTask> call, Response<DTOTask> response) {
                    long taskId = so.getRowId(); // this is local task Id in app

                    if (response.body()==null)
                        throw new RuntimeException();

                    long sid = response.body().getSid();

                    DBSynchronizer.updateTaskSID(db, taskId, sid);
                    DBSynchronizer.deleteFromSynchTable(db, taskId);
                }

                @Override
                public void onFailure(Call<DTOTask> call, Throwable t) {
                    System.out.println("failure");
                }
            });
        }

    }

}
