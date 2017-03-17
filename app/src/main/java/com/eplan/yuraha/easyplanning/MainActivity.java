package com.eplan.yuraha.easyplanning;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;


public class MainActivity extends BaseActivity implements
        PopupMenu.OnMenuItemClickListener,
        AddTaskFragment.OnFragmentInteractionListener,
        TaskListFragment.OnFragmentInteractionListener
{


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);

        setUpEveryDayNotification(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        TaskListFragment taskFragment = new TaskListFragment(this, fab);//create new fragment
        ft.replace(R.id.addTaskFrame, taskFragment, "fr1");
        ft.addToBackStack("fr1");
        ft.commit();

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
