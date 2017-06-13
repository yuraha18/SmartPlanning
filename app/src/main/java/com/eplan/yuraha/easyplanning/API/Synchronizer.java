package com.eplan.yuraha.easyplanning.API;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.InternetListener;
import com.eplan.yuraha.easyplanning.URL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Synchronizer {

    public static void startSynchronization(Context context)
    {
        if (InternetListener.hasActiveInternetConnection(context))
        {
            SPDatabase spDatabase = new SPDatabase(context);
            SQLiteDatabase db = spDatabase.getReadableDatabase();
            ApiService api = getApi();

         //   synchronize(db, api, context);
          //  db.close();
        }
        System.out.println("internet off");
    }

    private static void synchronize(SQLiteDatabase db, ApiService api, Context context) {
        db.beginTransaction();
        try {

            SynchApp.synchAppWithServer(db, api, context);
            ArrayList<SynchObject> synchObjects = DBSynchronizer.getAllSyncRows(db);
            if (synchObjects.size()>0)
                SynchServer.synchServerWithApp(db, synchObjects, api);

            SynchApp.updSynchTime(api, context);
            long lastSynch = SynchApp.getLastSynchTime(context);
            System.out.println("ls " + lastSynch);
            db.setTransactionSuccessful();

        }
        catch (Exception e){e.printStackTrace();}
        finally {
            db.endTransaction();

        }

    }


    public static ApiService getApi()
    {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(URL.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(getClient().newBuilder().build()).build();

        return  retrofit.create(ApiService.class);
    }

    private static OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();
        return client;
    }






}
