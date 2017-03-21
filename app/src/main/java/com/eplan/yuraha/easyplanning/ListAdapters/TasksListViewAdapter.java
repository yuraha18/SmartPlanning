package com.eplan.yuraha.easyplanning.ListAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TextView;
import android.widget.Toast;


import com.eplan.yuraha.easyplanning.AddTaskFragment;
import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.R;
import com.eplan.yuraha.easyplanning.TaskListFragment;

import java.util.ArrayList;

import co.lujun.androidtagview.TagContainerLayout;

/**
 * Created by yuraha18 on 2/24/2017.
 */

public class TasksListViewAdapter extends BaseAdapter {
    public ArrayList<Task> tasksList;
    LayoutInflater inflater;
    private SQLiteDatabase readableDb ;

    private TaskListFragment parentFragment;

    AppCompatActivity activity;
    ImageView priorityBell;
    ImageView moreButton;
    Context context;
    TextView taskText;
    FloatingActionButton fab;
    public boolean isSearch;
    String searchQuery;
    ImageView isDone;
    TagContainerLayout tagContainerLayout;


    private String[] months ;
    public TasksListViewAdapter(TaskListFragment parentFragment, LayoutInflater inflater, ArrayList<Task> list, SQLiteDatabase readableDb, AppCompatActivity activity, Context context, boolean isSearch, String searchQuery, FloatingActionButton fab){
        super();
        this.parentFragment = parentFragment;
        this.inflater = inflater;
        this.activity = activity;
        this.context = context;
        tasksList = list;
        this.readableDb = readableDb;
        this.isSearch = isSearch;
        this.searchQuery = searchQuery;
        this.fab = fab;

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

public void setNewData(ArrayList<Task> list)
{
   tasksList = list;
    notifyDataSetChanged();
}
{

}
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        convertView=inflater.inflate(R.layout.task_item, null);


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

        String todaysDay = parentFragment.getTodaysDay();
        String dayFromSpinner = parentFragment.getDayFromSpinner();

        if (AddTaskFragment.compareTwoDates(dayFromSpinner, todaysDay) >= 0) {// if dayromspinner is higher or same for Todays
            setOnClickListenerForMoreButton(position);
            setOnClickListenerForIsDoneButton(position);
            moreButton.setVisibility(View.VISIBLE);
        }
        else
            moreButton.setVisibility(View.INVISIBLE);

        if (isSearch)
            isDone.setVisibility(View.INVISIBLE);

    }

    private void setOnClickListenerForIsDoneButton(final int position) {
        isDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               long taskId = tasksList.get(position).getId();

                if (DBHelper.isInDoneTasksTable(readableDb, taskId, parentFragment.getDayFromSpinner()))
                {
                    DBHelper.deleteFromDoneTasks(readableDb, taskId, parentFragment.getDayFromSpinner());
                    isDone.setColorFilter(ContextCompat.getColor(context,R.color.highPriority));
                }
                else
                {
                    DBHelper.addToDoneTasks(readableDb, taskId, parentFragment.getDayFromSpinner());
                    isDone.setColorFilter(ContextCompat.getColor(context,R.color.lowPriority));
                    Toast.makeText(activity, activity.getResources().getString(R.string.goodJob), Toast.LENGTH_SHORT).show();
                }
                parentFragment.setDayRating();//update day rating in ratingBar
                updateListView();
            }
        });
    }

    private void setOnClickListenerForMoreButton(final int position) {
        moreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.task_fragment_menu, popup.getMenu());

                if (isSearch)
                {
                   MenuItem item = popup.getMenu().findItem(R.id.delete_task);
                    item.setVisible(false);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int i = item.getItemId();
                        switch (i) {

                            case R.id.edit_task:
                                editTask(position);
                                break;
                            case R.id.delete_task:
                                deleteTask(position);
                                break;
                            case R.id.delete_all_tasks:
                                deleteAllTasksDialog(position);

                        }

                        return true;
                    }

                });

                popup.show();

            }
        });
    }

    private void deleteAllTasksDialog(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(R.string.youReallyWantDeleteTaskInAllDays)
                .setPositiveButton(R.string.acceptWithQuestion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteTaskFromAllDays(position);
                        parentFragment.setDayRating();//update day rating in ratingBar
                    }
                })
                .setNegativeButton(R.string.declineWithQuestion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        dialog.show();
    }

    private void deleteTaskFromAllDays(int position) {
        long taskId = tasksList.get(position).getId();

        // 3rd parameter is current day (when user are deleting task), not dayFromSpinner
        boolean result = DBHelper.deleteTaskFromAllDays(readableDb, taskId+"", parentFragment.getTodaysDay(), parentFragment.getContext());

        if (!result)
            Toast.makeText(context, context.getResources().getString(R.string.cantDeleteException), Toast.LENGTH_LONG).show();

        updateListView();
    }

    private void deleteTask(int position) {
      boolean result =  DBHelper.deleteTaskFromDay(readableDb, tasksList.get(position).getId(), parentFragment.getDayFromSpinner());

        if (!result) {
            Toast.makeText(context, context.getResources().getString(R.string.cantDeleteException), Toast.LENGTH_LONG).show();
            return;
        }
        parentFragment.setDayRating();//update day rating in ratingBar
        updateListView();
    }

    private void updateListView() {
        ArrayList<Task> updatedTaskList;
        if (isSearch && searchQuery!=null)
            updatedTaskList = TaskListFragment.fillInTaskListForSearchQuery(readableDb, searchQuery);


        else {
            int dayOfWeek = AddTaskFragment.getDayOfWeek(parentFragment.getDayFromSpinner(), TaskListFragment.dateFormat);
            updatedTaskList = DBHelper.getAllTasksFromDay(readableDb, parentFragment.getDayFromSpinner(), TaskListFragment.weekDays[dayOfWeek]);
        }

        setNewData(updatedTaskList);
    }

    private void editTask(int position) {

        long taskId = tasksList.get(position).getId();
        AddTaskFragment taskFragment = new AddTaskFragment(activity);//create new fragment
        Bundle bundle = new Bundle();
        bundle.putBoolean("isEdit", true);//create data for sending with fragment
        bundle.putString("taskID", taskId+"");
        bundle.putString("calledDay", parentFragment.getDayFromSpinner());
        taskFragment.setArguments(bundle);// sending is
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.addTaskFrame, taskFragment, "editTask").addToBackStack("editTask").commit();
    fab.setVisibility(View.INVISIBLE);
    }
}
