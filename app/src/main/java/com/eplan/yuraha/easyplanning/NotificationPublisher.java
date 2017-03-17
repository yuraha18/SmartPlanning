package com.eplan.yuraha.easyplanning;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

/* this class calls before sending notification to app bar
* and when AlarmManager calls in right time
**/
public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";

    private String notificationText;
    private int icon;

    // this method calls in tome setted in AlarmManager
    public void onReceive(Context context, Intent intent) {


        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        int taskId = intent.getIntExtra(NOTIFICATION_ID, -1);

        // send notif to app bar only if task was not deleted in this day and notification are turn on in preferences
        if (!isDeletedInThisDay(taskId, context) && isAllowNotifications(context)) {
            SPDatabase spDB = new SPDatabase(context);

            // 0 its code for every day reminding about planning. It has own values never changes
            if (taskId == 0) {
                notificationText = context.getString(R.string.timeForPlanning);
                icon = R.drawable.ic_information_white_24dp;
            }

            // in other cases get text and icon from db by TaskId (and NotificationId) they are the same
            else {
                notificationText = DBHelper.getTaskTextFromId(spDB.getReadableDatabase(), taskId + "");
                int priority = DBHelper.getPriorityFromTaskId(spDB.getReadableDatabase(), taskId + "");
                icon = ManagerNotifications.getPriorityIcon(priority);
            }

            Notification notification = ManagerNotifications.getNotification(context, notificationText, icon);
            notificationManager.notify(taskId, notification);
        }
    }



    private boolean isAllowNotifications(Context context) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        return preference.getBoolean(Constants.PREF_TURN_ON_NOTIFICATIONS, true);
    }

    private boolean isDeletedInThisDay(int taskId, Context context) {
        try {
            SPDatabase spDatabase = new SPDatabase(context);
            SQLiteDatabase readableDB = spDatabase.getReadableDatabase();
            String todaysDay = TaskListFragment.getTodaysDay();
            long todaysDayId = DBHelper.addToDateTable(readableDB, todaysDay);

            boolean result = DBHelper.isInDeletedTable(readableDB, taskId, todaysDayId);
            spDatabase.close();
            return result;

        }
        catch (Exception e){return false;}

    }
}
