package com.eplan.yuraha.easyplanning.API;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.InternetListener;
import com.eplan.yuraha.easyplanning.MainActivity;
import com.eplan.yuraha.easyplanning.URL;
import com.eplan.yuraha.easyplanning.dto.DTOTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Synchronizer {

    public static void startSynchronization(Context context)
    {
        if (InternetListener.hasActiveInternetConnection(context))
        {
            System.out.println("synch start");
            SPDatabase spDatabase = new SPDatabase(context);
            SQLiteDatabase db = spDatabase.getReadableDatabase();

            ApiService api = getApi();
           SynchApp.synchAppWithServer(db, api, context);
            ArrayList<SynchObject> synchObjects = DBSynchronizer.getAllSyncRows(db);

            if (synchObjects.size()>0) {
              SynchServer.synchServerWithApp(db, synchObjects, api);
            }




        }
        System.out.println("internet off");
    }

    public static ApiService getApi()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);

        return service;
    }






}
