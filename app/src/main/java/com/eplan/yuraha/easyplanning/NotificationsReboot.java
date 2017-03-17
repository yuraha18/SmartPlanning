package com.eplan.yuraha.easyplanning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

import java.util.HashSet;

/**
 * Created by yuraha18 on 3/13/2017.
 */

/* This class recreate all notifications from db after reboot*/
public class NotificationsReboot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SPDatabase db = new SPDatabase(context);
        SQLiteDatabase readableDb = db.getReadableDatabase();
        HashSet<Long> notificationIds = DBHelper.getAllNotifications(readableDb);
        for (long id : notificationIds)
        {
            ManagerNotifications.createNotifications(readableDb, context, id+"");
        }

        db.close();

    }
}
