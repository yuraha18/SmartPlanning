package com.eplan.yuraha.easyplanning;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yuraha18 on 3/12/2017.
 */

/* This class creates all Notifications and set them in AlarmManager for Repeating
* in the right time AlarmManager calls NotificationPublisher (extends BroadcastReceiver)
* and it creates Notification (using getNotification method from this class)
* taskId equals NotificationId and _id from from Table Notifications is PendingId
* getNotification method creates the same Notification for single taskId
* although PendingIds are unique because they creates unique alarms for every day from RepeatingTable
* There are methods for Notifications in SettingsFragment and MainActivity
* in settingsFragment they receive preference states (isSetNotifications, Ringtone, Vibration and other)
 * There is method for firstly (only when app creates ) adding Notification for
 * reminding about every day planning in MainActivity */

public class ManagerNotifications {

    public static boolean createNotifications(SQLiteDatabase readableDb, Context context, String taskId) {

        try {
            System.out.println(taskId);
              /* key 0 belongs to reminding about every day planning */
            if ("0".equals(taskId))
            {
                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
                String remindTime = preference.getString(Constants.PREF_SET_REMIND_TIME, "22:00");
               setUpEveryDayRepeatingAlarm(context, remindTime);
                return true;
            }


            ArrayList<String> chosenDays = DBHelper.getDaysForTaskId(readableDb, taskId);
            int repeatMonthDay = DBHelper.getDayOfMonthFromMonthRepeating(readableDb, taskId);

            String dayFrom = DBHelper.getDayFromByTaskId(readableDb, taskId);
            String todaysDay = TaskListFragment.getTodaysDay();
            String remindTime = DBHelper.getRemindTimeForTaskId(readableDb, taskId);

            /* if there are repeatingMonth day i must call special method*/
            if (repeatMonthDay>0 && repeatMonthDay<=31)
                setUpMonthRepeatableAlarm(readableDb, context, dayFrom, todaysDay, repeatMonthDay, taskId, remindTime);

            else
                setUpNotifications(readableDb, context, dayFrom, todaysDay, taskId, chosenDays, remindTime);

            return true;
        }
     catch (Exception e){return false;}



    }

    /* this method using for setUp monthly repeating
    * there are some trick:
    * if todays date is bigger then day when task was created I send todays day
    * it means that dayFrom was in past and notification must be repeating from todays day
    * else means that task must be repeated in future and thats why send dayFrom*/
    private static void setUpMonthRepeatableAlarm(SQLiteDatabase readableDb, Context context, String dayFrom, String todaysDay, int repeatMonthDay, String taskId, String remindTime) {
        if (AddTaskFragment.compareTwoDates(todaysDay, dayFrom)<=0)
            createMonthlyRepeatableAlarm(readableDb, todaysDay, context, remindTime, repeatMonthDay,  taskId);

        else
            createMonthlyRepeatableAlarm(readableDb, dayFrom, context,  remindTime, repeatMonthDay, taskId);
    }

    private static void createMonthlyRepeatableAlarm(SQLiteDatabase readableDb, String dayFrom, Context context, String remindTime, int repeatMonthDay, String taskId) {
     Date date = AddTaskFragment.getDateFromString(dayFrom, Constants.DATEFORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // get todays dayOfMonth. For 12 march it is 12
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        //set day when notification will be repeated
        calendar.set(Calendar.DAY_OF_MONTH, repeatMonthDay);

        /* if this day for this month in past, start notification from next month
        * for example notif must be 12 march, today is 14 march, thats why first notif will be from 12 April*/
        if (currentDayOfMonth > repeatMonthDay )
            calendar.add(Calendar.MONTH, 1);

/* this loop creates 12 repeating month
* there no other way to creating monthly repeating*/
        for (int i =0; i<12; i++)
        {
            long time = getDateWithTime(calendar.getTime(), remindTime);

            if (time < (System.currentTimeMillis()-60000)) {
                calendar.add(Calendar.MONTH, 1);break;
            }
            // adding new notif (if it not exist in db) also in this method
            String day = getStringFromDate(calendar);
            int pendingId = getPendingId(readableDb, day, taskId);

            PendingIntent pendingIntent = getPendingIntent(context, taskId, pendingId);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

            calendar.add(Calendar.MONTH, 1);
        }
    }

    private static int getPendingId(SQLiteDatabase readableDb, String day, String taskId) {
        long dayId = DBHelper.addToDateTable(readableDb, day);
        long pendingId = DBHelper.getNotificationId(readableDb, taskId, dayId+"");

        if (pendingId==-1)
            pendingId = DBHelper.addNotification(readableDb, taskId, day);

        System.out.println(pendingId + " pendingID");
        return (int) pendingId;
    }

    private static String getStringFromDate(Calendar calendar) {

        return calendar.get(Calendar.DAY_OF_MONTH)+"-"+ calendar.get(Calendar.MONTH) + "-"
                + calendar.get(Calendar.YEAR);
    }

    /* this one get all data, loop chosenDays and creates notification using special method
    * pendingIntent must be unique, in other case alarms will be overridden */
    private static void setUpNotifications(SQLiteDatabase readableDb, Context context, String dayFrom, String todaysDay, String taskId, ArrayList<String> chosenDays, String remindTime) {

        for (String day : chosenDays) {

            // adding new notif (if it not exist in db) also in this method
            int pendingId = getPendingId(readableDb, day, taskId);
            PendingIntent pendingIntent = getPendingIntent(context, taskId, pendingId);

            if (AddTaskFragment.weekDays.contains(day)){
                // trick like in createMonthlyRepeatableAlarm. If dayFrom before todays (in past)? create alarms from todays day
                if (AddTaskFragment.compareTwoDates(todaysDay, dayFrom)>=0)
                    createRepeatableAlarmManager(todaysDay, day, context, pendingIntent, remindTime);

                else
                    createRepeatableAlarmManager(dayFrom, day, context, pendingIntent, remindTime);
               }

            else
            {
                if (AddTaskFragment.compareTwoDates(day, todaysDay)>=0)
                    createSingleAlarmManager(day, context, pendingIntent, remindTime);
            }

        }

    }

    /* it creates pendingIntent for alarm. pendingId is _id from Notification table and unique
    * it means that pending is unique also, and will be not overridden*/
    private static PendingIntent getPendingIntent(Context context, String taskId, int pendingId) {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, Integer.parseInt(taskId));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, pendingId,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return pendingIntent;
    }

    /* create not repeatable alarm for single days (like 15/3/2017)
    * time < System.currentTimeMillis() checks notification must be added in future and added it or was in past and dont add
     * after rebooting and changing preferences system recreates all notifications from db
     * and this checking (and the same in bellow methods) here is for not setting notifications which was in past*/
    private static void createSingleAlarmManager(String day, Context context, PendingIntent pendingIntent, String remindTime) {
        Date dayDate = AddTaskFragment.getDateFromString(day, Constants.DATEFORMAT);
        long time = getDateWithTime(dayDate, remindTime);

        if (time < (System.currentTimeMillis()-60000))
            return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    /* create every week repeatable  notifications (for day like Tuesday)*/
    private static void createRepeatableAlarmManager(String calledDay, String day, Context context, PendingIntent pendingIntent, String remindTime) {
        Date dayDate = getNearestDay(calledDay, day);
        long time = getDateWithTime(dayDate, remindTime);
        /* it check from which day start notification
        * for example if now 14:50 and notif must be show in 13:00 means that its late for showing today
        * and thats why it will be started from next week
        * plusDaysToTime method plussing count of days to some date (in this cays it 7 days of week)*/
        if (time < (System.currentTimeMillis()-60000))
            time = plusDaysToTime(time, 7);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY*7 ,pendingIntent);

    }

    /* this method get data like: 15/3/2017 (Wednesday) and looking for nearest day like Tuesday
    * in loop it adds 1 day to current day till it be the same for called from method arguments*/
    private static Date getNearestDay(String calledDay, String day) {
        int dayOfWeek = AddTaskFragment.weekDays.indexOf(day);
        Calendar calendar = Calendar.getInstance();
       Date calledDate = AddTaskFragment.getDateFromString(calledDay, Constants.DATEFORMAT);
        calendar.setTime(calledDate);


        while (calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            calendar.add(Calendar.DATE, 1);
        }

        return calendar.getTime();

    }



    /* it cancels notification, but not deleting from db
     * they deletes only when user wanna deleteTaskFromAllDays */
    public static boolean cancelNotifications(SQLiteDatabase db, String taskID,  Context context)
    {
        // key 0 it key for everyDayPlanning
        if ("0".equals(taskID))
        {
            cancelEveryDayRepeatingAlarm(context);
            return true;
        }

        // one task can have a lot of notifications. The same count chosenDays.size
        ArrayList<Integer> pendingIds = DBHelper.getNotificationsForTask(db, taskID);

        for (int pendingID : pendingIds) {
            /* must creates the same pendingIntent (will the same arguments) like in settingUp*/
          PendingIntent pendingIntent=  getPendingIntent(context, taskID, pendingID);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }

        return true;
    }

    /* its every day repeating notif for reminding about da planning
    * calls ones when app creates and many times when preference (RemindMe and its checkbox) changing*/
    public static void setUpEveryDayRepeatingAlarm(Context context, String remindTime)
    {
        PendingIntent pendingIntent=  getPendingIntent(context, "0", 0);

        Date date = AddTaskFragment.getDateFromString(TaskListFragment.getTodaysDay(), Constants.DATEFORMAT);
        long time = getDateWithTime(date, remindTime);

        // the same trick in createRepeatableAlarmManager
        if (time < (System.currentTimeMillis()-60000))
            time = plusDaysToTime(time, 1);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY,pendingIntent);
    }

    /* plussing some count of days to some time
     * using in setUpEveryDayRepeatingAlarm and  createRepeatableAlarmManager
     * main aim has described in createRepeatableAlarmManager*/
    private static long plusDaysToTime(long time, int countOfDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        calendar.add(Calendar.DAY_OF_YEAR, countOfDays);

        return calendar.getTimeInMillis();
    }

    public static void cancelEveryDayRepeatingAlarm(Context context)
    {
        PendingIntent pendingIntent=  getPendingIntent(context, "0", 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    /* get icon from priority*/
    public static int getPriorityIcon(int priority) {
        int icon;
        switch (priority)
        {
            case 1:icon = R.drawable.ic_low_priority_bell;break;
            case 2:icon = R.drawable.ic_middle_priority_bell;break;
            case 3:icon = R.drawable.ic_hight_priority_bell;break;
            default: icon = R.drawable.ic_low_priority_bell;break;
        }

        return icon;
    }

    /* knowing day for repeating its not all
    * notification must be setting in right time
    * this method parsing and adding remindTime (which user chosen when added task) to repeating date*/
    private static long getDateWithTime(Date date, String remindTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        String[] time = remindTime.split(":");
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);

        calendar.set(year, month, day, hour, minute);
        return calendar.getTimeInMillis();
    }

    /* create notification
    * called only from NotificationPublisher before showing in app bar
    * it allows create notif with only fresh text and priority bell, and sound
    * User can change this data after setting Alarms and if i did it when create alarm this data will be outdated*/
    public static Notification getNotification(Context context, String notificationText, int icon) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);

        builder.setSmallIcon(icon)
                .setContentIntent(contentIntent)
                .setTicker(notificationText)
                .setAutoCancel(true)
                .setContentTitle(context.getString(R.string.notificationTitle))
                .setContentText(notificationText);

        /* get info from preferences*/
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        String strRingtonePreference = preference.getString(Constants.PREF_RINGTONE, "DEFAULT_SOUND");
        Uri alarmSound = Uri.parse(strRingtonePreference);
        builder.setSound(alarmSound);
       boolean isSetVibration = preference.getBoolean(Constants.PREF_VIBRATION, false);

        if (isSetVibration)
       builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        return builder.getNotification();
    }

}
