package com.eplan.yuraha.easyplanning.API;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.dto.DTOTask;

import java.util.ArrayList;


public class DBSynchronizer {

    /*Methods for synchronization and server */
    public static void addToSyncTable(SQLiteDatabase db, final Context context, long taskID, int tableId) {
        System.out.println("addToSyncTable");
        ContentValues value = new ContentValues();
        value.put("ROW_ID", taskID);
        value.put("TABLE_ID", tableId);


        db.insert("SyncData", null, value);

    }

    public static void deleteFromSynchTable(SQLiteDatabase db, long id)
    {
        db.delete("SyncData", "_id" + " = ?", new String[] { id+"" });
    }

    public static ArrayList<SynchObject> getAllSyncRows(SQLiteDatabase db) throws SQLiteException {

        ArrayList<SynchObject> synchObjects = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM SyncData" , null);
        if (cursor .moveToFirst()) {
            while (!cursor.isAfterLast()) {
                long id = cursor.getLong(0);
                long rowId = cursor.getLong(1);
                int tableId = cursor.getInt(2);
                SynchObject synchObject = new SynchObject(id, rowId, tableId);

                synchObjects.add(synchObject);
                cursor.moveToNext();
            }
        }

        cursor.close();
        return synchObjects;
    }

    public static DTOTask getTask(SQLiteDatabase db, long id)
    {

     DTOTask task  = null;
        Cursor cursor = db.query ("Tasks",
                new String[] {"TASK_TEXT", "PRIORITY", "SID"},
                "_id = ?",
                new String[] {id+""},
                null, null,null);

        if (cursor.moveToFirst()) {
            String text = cursor.getString(0);
            int priority = cursor.getInt(1);
            long sid = cursor.getLong(2);

            task = new DTOTask(sid, text, priority);

            cursor.close();

        }

        return task;
    }

    public static void updateTaskSID(SQLiteDatabase db, long taskId, long taskSID)
    {
        System.out.println("updtaskSID" + taskSID);
        System.out.println("updtaskID" + taskId);
        ContentValues value = new ContentValues();
        value.put("SID", taskSID);
        db.update("Tasks", value, "_id = ?", new String[] { taskId+"" });
        value.clear();
    }

    public static void saveTask(SQLiteDatabase db, DTOTask task, Context context)
    {
        long sid = task.getSid();
       boolean isExist = isTaskExist(db, sid);

        if (isExist)
            updateTask(db, task);

        else
            addTask(db, task, context);
    }

    private static void addTask(SQLiteDatabase db, DTOTask task, Context context) {
        DBHelper.addToTasksTable(db, context, task.getTaskText(), task.getPriority(),
                task.getSid());
    }

    private static void updateTask(SQLiteDatabase db, DTOTask task) {
        ContentValues value = new ContentValues();
        value.put("TASK_TEXT", task.getTaskText());
        value.put("PRIORITY", task.getPriority());
        int updCount = db.update("Tasks", value, "SID = ?",
                new String[] { task.getSid()+"" });
        value.clear();

        if (updCount<1)
            throw new SQLiteException("cant update Task");
    }

    private static boolean isTaskExist(SQLiteDatabase db, long sid) {
        boolean result = false;
        Cursor cursor = db.query("Tasks",
                new String[]{"_id"},
                "SID = ?",
                new String[]{sid+""},
                null, null, null);

        if (cursor.moveToFirst())
            result = true;

        cursor.close();

        return result;
    }


    public static ArrayList<DTOTask> allTasks(Context context)
    {
        SPDatabase sp = new SPDatabase(context);
       SQLiteDatabase db = sp.getReadableDatabase();

        ArrayList<DTOTask> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM Tasks" , null);
        if (cursor .moveToFirst()) {
            while (!cursor.isAfterLast() ) {
                DTOTask task = new DTOTask();
                task.setPriority(cursor.getInt(cursor.getColumnIndex("PRIORITY")));
                task.setTaskText(cursor.getString(cursor.getColumnIndex("TASK_TEXT")));
                task.setSid(cursor.getLong(cursor.getColumnIndex("SID")));

                list.add(task);
                cursor.moveToNext();
            }
        }

        cursor.close();

        return list;
    }


}
