package com.eplan.yuraha.easyplanning.ListAdapters;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.eplan.yuraha.easyplanning.AddTask;
import com.eplan.yuraha.easyplanning.AddTaskFragment;
import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.DoneGoalsListFragment;
import com.eplan.yuraha.easyplanning.InProgressGoalListFragment;
import com.eplan.yuraha.easyplanning.MainActivity;
import com.eplan.yuraha.easyplanning.R;
import com.eplan.yuraha.easyplanning.TonesDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.lujun.androidtagview.TagContainerLayout;

/**
 * Created by yuraha18 on 2/24/2017.
 */

public class TasksListViewAdapter extends BaseAdapter {
    public ArrayList<Task> tasksList;
    LayoutInflater inflater;
    private SQLiteDatabase readableDb ;
    AppCompatActivity activity;
    ImageView priorityBell;
    ImageView moreButton;
    Context context;
    TextView taskText;
    ImageView isDone;
    TagContainerLayout tagContainerLayout;
    long taskId;

    private String[] months ;
    public TasksListViewAdapter(LayoutInflater inflater, ArrayList<Task> list, SQLiteDatabase readableDb, AppCompatActivity activity, Context context){
        super();
        this.inflater = inflater;
        this.activity = activity;
        this.context = context;
        tasksList = list;
        this.readableDb = readableDb;

    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return tasksList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return tasksList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        convertView=inflater.inflate(R.layout.task_item, null);

        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.taskFragmentLayout);
        priorityBell=(ImageView) convertView.findViewById(R.id.priority_bell);
        taskText=(TextView) convertView.findViewById(R.id.task_text);
        isDone=(ImageView) convertView.findViewById(R.id.done_task);
        tagContainerLayout=(TagContainerLayout) convertView.findViewById(R.id.tagcontainerLayout);
        moreButton = (ImageView) convertView.findViewById(R.id.action_more_fragment);

        fillInTextViews(position, convertView);
        return convertView;
    }

    private void fillInTextViews(final int position, View convertView) {
        Task task = tasksList.get(position);
        int priority = task.getPriority();

        switch (priority)
        {
            case 1: priorityBell.setColorFilter(ContextCompat.getColor(context,R.color.lowPriority));break;
            case 2:  priorityBell.setColorFilter(ContextCompat.getColor(context,R.color.middlePriority));break;
            case 3:  priorityBell.setColorFilter(ContextCompat.getColor(context,R.color.highPriority));break;
        }

        taskText.setText(task.getTaskText());

        if (task.isDone())
            isDone.setColorFilter(ContextCompat.getColor(context,R.color.lowPriority));

        else
            isDone.setColorFilter(ContextCompat.getColor(context,R.color.highPriority));

       tagContainerLayout.setTags(task.getGoals());
        setOnClickListenerForMoreButton(position);
    }

    private void setOnClickListenerForMoreButton(final int position) {
        moreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.task_fragment_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int i = item.getItemId();
                        switch (i) {

                            case R.id.edit_task:
                                editTask(position);
                                break;
                            case R.id.delete_task:
                                deleteTaskDialog(position);
                        }

                        return true;
                    }

                });

                popup.show();

            }
        });
    }

    private void deleteTaskDialog(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(R.string.deleteGoalDialogTitle)
                .setPositiveButton(R.string.acceptWithQuestion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteTask(position);
                    }
                })
                .setNegativeButton(R.string.declineWithQuestion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();
    }

    private void deleteTask(int position) {
        long goalId = tasksList.get(position).getId();
        boolean result = DBHelper.deleteTask(readableDb, goalId);

        if (result) {
            tasksList.remove(position);
            dataHasBeenChanged();
        }
        else
            Toast.makeText(context, context.getResources().getString(R.string.cantDeleteGoalException), Toast.LENGTH_LONG).show();
    }

    private void editTask(int position) {
        long taskId = tasksList.get(position).getId();
//activity.callAddTaskFragment(position, taskId);// use it cause I cant create this fragment from this fragment

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        AddTaskFragment taskFragment = new AddTaskFragment();//create new fragment
        Bundle bundle = new Bundle();
        bundle.putString("id", "1");//create data for sending with fragment
        taskFragment.setArguments(bundle);// sending is
        ft.replace(R.id.addTaskFrame, taskFragment, "fr1");
        ft.addToBackStack("fr2");
        ft.commit();

    }
    public void dataHasBeenChanged()
    {
        notifyDataSetChanged();
    }



}
