package com.eplan.yuraha.easyplanning;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.ListAdapters.Goal;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class AddGoalFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String dayFromCalendar;
    private EditText goalName, goalNote;
    private SQLiteDatabase writableDb ;
    private SQLiteDatabase readableDb ;
    FloatingActionButton fab;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddGoalFragment(FloatingActionButton fab) {
        this.fab = fab;
    }

    public AddGoalFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddTaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddGoalFragment newInstance(String param1, String param2) {
        AddGoalFragment fragment = new AddGoalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SPDatabase db = new SPDatabase(getActivity());
        writableDb = db.getWritableDatabase();
        readableDb = db.getReadableDatabase();

        boolean isEdit = getArguments().getBoolean("isEdit");


        /* this activity can add new and edit exist goal
        * isEdit contains value:
        *  true - if we wanna edit exist goal + goalId for editing
        *  false - if we must add new goal*/


        View contentView = inflater.inflate(R.layout.activity_add_goal, null, false);

        Button setDeadline = (Button) contentView.findViewById(R.id.setDeadline);
        setDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCalendarOnClick();
            }
        });

        goalName  = (EditText) contentView.findViewById(R.id.goalName);
        goalNote  = (EditText) contentView.findViewById(R.id.goalNote);

        fab.setVisibility(View.INVISIBLE);


        final Button addGoal = (Button) contentView.findViewById(R.id.addGoal);

        if (isEdit)// if we wanna edit exist goal
        {
            final String goalId = getArguments().getString("goalID");
            final int positionInListView =getArguments().getInt("positionInList");
            setDataForEditing(goalId);


            addGoal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editGoalOnclick(goalId, positionInListView);
                }
            });

            setDeadline.setVisibility(View.GONE);// make setDeadline unvisible, cause you cant move deadline
        }
        else {
            addGoal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addGoalOnClick();
                }
            });
        }

        return contentView;
    }

    private void setDataForEditing(String goalId) {
        Goal goal = DBHelper.getGoalFromId(readableDb, goalId);
        goalName.setText(goal.getGoalName());
        goalNote.setText(goal.getGoalNote());
    }

    private void editGoalOnclick(final String goalId, final int positionInListView) {
        if (!AddTaskFragment.taskTextValidator(goalName, getActivity()))
            return;

        if (!AddTaskFragment.taskTextValidator(goalNote, getActivity()))
            return;

        if (DBHelper.isGoalExist(readableDb, goalName.getText().toString(), goalId))
        {
            Toast.makeText(getActivity(), getResources().getString(R.string.sameGoalExists), Toast.LENGTH_LONG).show();
            return;
        }

       boolean result = DBHelper.updateGoal(writableDb, goalName.getText().toString(), goalNote.getText().toString(), goalId);

        if (result) {
            Toast.makeText(getActivity(), getResources().getString(R.string.goalEditSuccessfully), Toast.LENGTH_LONG).show();
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    getFragmentManager().popBackStack();
                }
            }, 800);
        }
        else
        {
            Toast.makeText(getActivity(), getResources().getString(R.string.taskNotAddedInDB), Toast.LENGTH_LONG).show();
        }
    }

    private void addGoalOnClick() {

        if (!AddTaskFragment.taskTextValidator(goalName, getActivity()))
            return;

        if (!AddTaskFragment.taskTextValidator(goalNote, getActivity()))
            return;

        if (DBHelper.isGoalExist(readableDb, goalName.getText().toString()))
        {
            Toast.makeText(getActivity(), getResources().getString(R.string.sameGoalExists), Toast.LENGTH_LONG).show();
            return;
        }

        if (dayFromCalendar==null)
        {
            Toast.makeText(getActivity(), getResources().getString(R.string.dontSetDeadline), Toast.LENGTH_LONG).show();
            return;
        }

        final long goalId = DBHelper.addGoalToDB(writableDb, goalName.getText().toString(), goalNote.getText().toString(), dayFromCalendar);

        if (goalId > 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.goalAddSuccessfully), Toast.LENGTH_LONG).show();
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {

                    getFragmentManager().popBackStack();

                }
            }, 800);
        }
        else
        {
            Toast.makeText(getActivity(), getResources().getString(R.string.taskNotAddedInDB), Toast.LENGTH_LONG).show();
        }
    }

    private   void openCalendarOnClick() {
        final Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Launch Date Picker Dialog
        DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // Save date in class field
                        dayFromCalendar = dayOfMonth + "-"
                                + (monthOfYear + 1) + "-" + year;

                    }
                }, mYear, mMonth, mDay);
        dpd.show();
        dpd.getDatePicker().setMinDate(calendar.getTimeInMillis());

    }



}
