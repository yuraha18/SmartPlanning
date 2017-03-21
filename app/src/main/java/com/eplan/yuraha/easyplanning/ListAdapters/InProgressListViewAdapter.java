package com.eplan.yuraha.easyplanning.ListAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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

import com.eplan.yuraha.easyplanning.AddGoalFragment;
import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.DoneGoalsListFragment;
import com.eplan.yuraha.easyplanning.GoalsActivity;
import com.eplan.yuraha.easyplanning.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuraha18 on 2/24/2017.
 */

public class InProgressListViewAdapter extends BaseAdapter {
    public ArrayList<Goal> goalsList;
    private SQLiteDatabase readableDb ;
    private DoneGoalsListFragment doneGoalsListFragment;
    Context context;
    TextView goalName;
    ImageView moreButton;
    TextView goalNote;
    TextView deadlineText;
    Fragment goalListsFragment;
    TextView timeToDeadline;
    String goalId;
    AppCompatActivity activity;
    FloatingActionButton fab;

    public InProgressListViewAdapter(ArrayList<Goal> list, SQLiteDatabase readableDb, Context context, DoneGoalsListFragment doneGoalsListFragment, AppCompatActivity activity, Fragment parentFragment, FloatingActionButton fab){
        super();
        this.context = context;
        goalsList = list;
        this.readableDb = readableDb;
        this.doneGoalsListFragment = doneGoalsListFragment;
        this.activity = activity;
        goalListsFragment = parentFragment;
        this.fab = fab;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return goalsList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return goalsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub


        final LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView=inflater.inflate(R.layout.fragment_item, null);

        goalName=(TextView) convertView.findViewById(R.id.goalName);
        goalNote=(TextView) convertView.findViewById(R.id.goalNote);
        deadlineText=(TextView) convertView.findViewById(R.id.goalDeadline);
        timeToDeadline=(TextView) convertView.findViewById(R.id.goalDeadlineDaysCount);
        moreButton = (ImageView)  convertView.findViewById(R.id.action_more_fragment);

        fillInTextViews(position, convertView);
        return convertView;
    }

    private void fillInTextViews(final int position, View convertView) {
        Goal goal = goalsList.get(position);
        goalId = goal.getId();
        goalName.setText(goal.getGoalName());
        goalNote.setText(goal.getGoalNote());
        setTimeToDeadline(goal.getDeadline(), convertView);
        moreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.goal_fragment_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int i = item.getItemId();
                        switch (i) {
                            case R.id.goal_is_Done:
                                makeGoalDoneDialog(position);
                                break;

                            case R.id.edit_goal:
                                editGoal(position);
                                break;
                            case R.id.delete_goal:
                                deleteGoalDialog(position);
                        }

                        return true;
                    }

                });

                popup.show();

            }
        });
    }

    private void makeGoalDoneDialog(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(R.string.makeDoneGoalDialogTitle)
                .setPositiveButton(R.string.acceptWithQuestion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        goalIsDone(position);
                    }
                })
                .setNegativeButton(R.string.declineWithQuestion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        dialog.show();
    }

    private void deleteGoalDialog(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(R.string.deleteGoalDialogTitle)
                .setPositiveButton(R.string.acceptWithQuestion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteGoal(position);
                    }
                })
                .setNegativeButton(R.string.declineWithQuestion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        dialog.show();
    }

    private void deleteGoal(int position) {
        String goalId = goalsList.get(position).getId();
        boolean result = DBHelper.deleteGoal(readableDb, goalId);

        if (result) {
            goalsList.remove(position);
            dataHasBeenChanged();
        }
        else
            Toast.makeText(context, context.getResources().getString(R.string.cantDeleteGoalException), Toast.LENGTH_LONG).show();
    }

    private void editGoal(int position) {
        String goalId = goalsList.get(position).getId();


        AddGoalFragment taskFragment = new AddGoalFragment(fab);//create new fragment
        Bundle bundle = new Bundle();
        bundle.putBoolean("isEdit", true);//create data for sending with fragment
        bundle.putString("goalID", goalId);
        bundle.putInt("positionInList", position);
        taskFragment.setArguments(bundle);// sending is
        taskFragment.setTargetFragment(goalListsFragment, 2);
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.addGoalFrame, taskFragment, "EditGoal").addToBackStack("EditGoal").commit();
    }


    private void goalIsDone(int position) {
        try {
             String goalID = goalsList.get(position).getId();
            String todaysDate = getCurrentDay();
            DBHelper.moveGoalToDone(readableDb, goalID, todaysDate);
            goalsList.remove(position);
            dataHasBeenChanged();
            Goal addedGoal = DBHelper.getGoalFromId(readableDb, goalID);
            doneGoalsListFragment.adapter.goalsList.add(0, addedGoal);
            doneGoalsListFragment.adapter.dataHasBeenChanged();
        }
        catch (Exception e){}

    }

    private String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH)+1;//Im using 12 count for months. Default month started from 0
        int year = calendar.get(Calendar.YEAR);
        String date = day + "-" + month + "-" + year;

        return date;
    }

    public void dataHasBeenChanged()
    {
        notifyDataSetChanged();
    }
    private void setTimeToDeadline(String deadlineId, View convertView) {

        try {
            long daysToDeadline = countDaysToDeadline(deadlineId);

            if (daysToDeadline < 10) {
                timeToDeadline.setTextColor(R.color.middlePriority);
                deadlineText.setTextColor(R.color.middlePriority);
                timeToDeadline.setText(daysToDeadline+"");
            }
            else if (daysToDeadline < 0) {
                daysToDeadline*=(-1);
                String text = context.getResources().getString(R.string.afterDeadline) + daysToDeadline
                        + context.getResources().getString(R.string.afterDeadlineP2);
                timeToDeadline.setTextColor(R.color.highPriority);
                timeToDeadline.setText(text);
                deadlineText.setText("");
            }
            else if (daysToDeadline==0)
            {
                timeToDeadline.setTextColor(R.color.highPriority);
                timeToDeadline.setText(context.getResources().getString(R.string.deadlineToday));
                deadlineText.setText("");
            }
            else
            {
                timeToDeadline.setText(daysToDeadline+"");
            }

        }
        catch (ParseException e)
        {
            TextView deadline  = (TextView) convertView.findViewById(R.id.goalDeadline);
            deadline.setText("");
            timeToDeadline.setText("");
        }
    }




    private long countDaysToDeadline(String deadlineId) throws ParseException {

        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH)+1;
        int year = today.get(Calendar.YEAR);

        String todaysDate = day+ "-" + month +"-" + year;
        String deadlineDate = DBHelper.getDayFromId(readableDb, deadlineId);

        Date date1 = myFormat.parse(todaysDate);
        Date date2 = myFormat.parse(deadlineDate);
        long diff = date2.getTime() - date1.getTime();
        long daysCount = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        return daysCount;
    }


}
