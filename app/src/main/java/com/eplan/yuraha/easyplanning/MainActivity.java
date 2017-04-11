package com.eplan.yuraha.easyplanning;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;

import com.eplan.yuraha.easyplanning.API.ApiService;
import com.eplan.yuraha.easyplanning.API.DBSynchronizer;
import com.eplan.yuraha.easyplanning.API.SynchApp;
import com.eplan.yuraha.easyplanning.API.SynchObject;
import com.eplan.yuraha.easyplanning.API.Synchronizer;
import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.dto.DTOTask;
import com.eplan.yuraha.easyplanning.dto.Day;
import com.eplan.yuraha.easyplanning.dto.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends BaseActivity implements
        PopupMenu.OnMenuItemClickListener,
        AddTaskFragment.OnFragmentInteractionListener,
        TaskListFragment.OnFragmentInteractionListener
{

    public static long userId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);


        setUpEveryDayNotification(getApplicationContext());
        String searchQuery = getIntent().getStringExtra("searchQuery");
        
        System.out.println("connection: " + InternetListener.hasActiveInternetConnection(this));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Synchronizer.startSynchronization(getApplicationContext());
            }
        });
        thread.setDaemon(true);
        thread.start();
       //   test();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        TaskListFragment taskFragment = new TaskListFragment(this, fab, searchQuery);//create new fragment
        ft.replace(R.id.addTaskFrame, taskFragment, "fr1");
        ft.addToBackStack("fr1");
        ft.commit();



    }

    private void test() {
        Day day = new Day(0, "Tuesday");
      Call<Day> call=  Synchronizer.getApi().saveDays(day, 0);

        call.enqueue(new Callback<Day>() {
            @Override
            public void onResponse(Call<Day> call, Response<Day> response) {
                System.out.println("on response");
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<Day> call, Throwable t) {

            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    /* calls when MainActivity creates
    * but did something only when app creates firstly*/
    public void setUpEveryDayNotification(Context context) {
        SPDatabase spDatabase = new SPDatabase(context);
        SQLiteDatabase readableDB = spDatabase.getReadableDatabase();

       int countOfNotifications = DBHelper.getNotificationsForTask(readableDB, "0").size();
 /* set up every day repeating for reminding about planning*/
        if (countOfNotifications==0) {
            ManagerNotifications.createNotifications(readableDB, context, "0");
            DBHelper.addNotification(readableDB, "0", "1");
        }

        readableDB.close();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try {
            switch (item.getItemId()) {
                //Если выбран пункт меню "В документах"
                case R.id.action_settings:
                    // Вызываем наш "демо-метод" (см. п. 6)... и т.д.
                    return true;
                default:
                    return false;
            }
        }
        catch ( Exception e)
        {
            return false;
        }

    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
