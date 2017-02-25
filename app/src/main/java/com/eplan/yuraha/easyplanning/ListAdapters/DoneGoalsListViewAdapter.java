package com.eplan.yuraha.easyplanning.ListAdapters;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.DoneGoalsListFragment;
import com.eplan.yuraha.easyplanning.InProgressGoalListFragment;
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

public class DoneGoalsListViewAdapter extends BaseAdapter {
    public ArrayList<Goal> goalsList;
    LayoutInflater inflater;
    private SQLiteDatabase readableDb ;
    Context context;
    TextView goalName;
    ImageView moreButton;
    TextView goalNote;
    TextView deadlineText;
    TextView timeToDeadline;
    String goalId;

    private String[] months ;
    public DoneGoalsListViewAdapter(LayoutInflater inflater, ArrayList<Goal> list, SQLiteDatabase readableDb, Context context){
        super();
        this.inflater = inflater;
        this.context = context;
        goalsList = list;
        this.readableDb = readableDb;
        initMonths();
    }

    private void initMonths() {
        months = new String[]//first value is empty cause months in my app stars from 1, not 0
                {
                        "",
                        context.getResources().getString(R.string.January),
                        context.getResources().getString(R.string.February),
                        context.getResources().getString(R.string.March),
                        context.getResources().getString(R.string.April),
                        context.getResources().getString(R.string.May),
                        context.getResources().getString(R.string.June),
                        context.getResources().getString(R.string.July),
                        context.getResources().getString(R.string.August),
                        context.getResources().getString(R.string.September),
                        context.getResources().getString(R.string.October),
                        context.getResources().getString(R.string.November),
                        context.getResources().getString(R.string.December)
                };
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
        convertView=inflater.inflate(R.layout.fragment_item, null);

        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.fragmentItemGoalLayout);
        goalName=(TextView) convertView.findViewById(R.id.goalName);
        goalNote=(TextView) convertView.findViewById(R.id.goalNote);
        deadlineText=(TextView) convertView.findViewById(R.id.goalDeadline);
        timeToDeadline=(TextView) convertView.findViewById(R.id.goalDeadlineDaysCount);
        moreButton = (ImageView)  convertView.findViewById(R.id.action_more_fragment);

        relativeLayout.removeView(timeToDeadline);
        relativeLayout.removeView(moreButton);
        fillInTextViews(position, convertView);
        return convertView;
    }

    private void fillInTextViews(final int position, View convertView) {
        Goal goal = goalsList.get(position);
        goalId = goal.getId();
        goalName.setText(goal.getGoalName());
        goalNote.setText(goal.getGoalNote());
        deadlineText.setText("");// if we will have some problems with parsing day when goal has been done? this field will be empty

        try {
         String day = DBHelper.getDayFromDoneGoalByGoalId(readableDb, goalId);// we get data in format 22-3-2016
            String[] dayParse = day.split("-");// parse date to 3 strings: day, month, year
            String dayInMonth = dayParse[0];
            int month = Integer.parseInt(dayParse[1]);
            String year = dayParse[2];
            String text = context.getResources().getString(R.string.whenGoalWasDone) + " " + dayInMonth + " " +
                    months[month] + " " + year;
            deadlineText.setText(text);//set text with day when goal has been done

        }
        catch (Exception e){}

    }



    public void dataHasBeenChanged()
    {
        notifyDataSetChanged();
    }



}
