package com.eplan.yuraha.easyplanning;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

public class AddGoalActivity extends BaseActivity {
    private String dayFromCalendar;
    private EditText goalName, goalNote;
    private SQLiteDatabase writableDb ;
    private SQLiteDatabase readableDb ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SPDatabase db = new SPDatabase(this);
        writableDb = db.getWritableDatabase();
        readableDb = db.getReadableDatabase();

        Intent intent = getIntent();
      boolean isEdit = intent.getBooleanExtra("edit", false);

        /* this activity can add new and edit exist goal
        * isEdit contains value:
        *  true - if we wanna edit exist goal + goalId for editing
        *  false - if we must add new goal*/


        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_add_goal, null, false);
        drawer.addView(contentView, 0);

        Button setDeadline = (Button) findViewById(R.id.setDeadline);
        setDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              openCalendarOnClick();
            }
        });

        goalName  = (EditText) findViewById(R.id.goalName);
        goalNote  = (EditText) findViewById(R.id.goalNote);


        final Button addGoal = (Button) findViewById(R.id.addGoal);

        if (isEdit)// if we wanna edit exist goal
        {
            final String goalId = String.valueOf(intent.getExtras().get("goalId"));
            final int positionInListView = intent.getIntExtra("positionInList", 0);
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


    }

    private void setDataForEditing(String goalId) {
        Goal goal = DBHelper.getGoalFromId(readableDb, goalId);
        goalName.setText(goal.getGoalName());
        goalNote.setText(goal.getGoalNote());
    }

    private void editGoalOnclick(final String goalId, final int positionInListView) {
        if (!AddTaskFragment.taskTextValidator(goalName, this))
            return;

        if (!AddTaskFragment.taskTextValidator(goalNote, this))
            return;

        if (DBHelper.isGoalExist(readableDb, goalName.getText().toString(), goalId))
        {
            Toast.makeText(this, getResources().getString(R.string.sameGoalExists), Toast.LENGTH_LONG).show();
            return;
        }

       boolean result = DBHelper.updateGoal(writableDb, goalName.getText().toString(), goalNote.getText().toString(), goalId);

        if (result) {
            Toast.makeText(this, getResources().getString(R.string.goalEditSuccessfully), Toast.LENGTH_LONG).show();
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    Intent data = new Intent();
                    data.putExtra("goalId", goalId+"");
                    data.putExtra("positionInList", positionInListView);

                    setResult(RESULT_OK, data);
                    finish();// end this activity and come back to previous
                }
            }, 800);
        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.taskNotAddedInDB), Toast.LENGTH_LONG).show();
        }
    }

    private void addGoalOnClick() {

        if (!AddTaskFragment.taskTextValidator(goalName, this))
            return;

        if (!AddTaskFragment.taskTextValidator(goalNote, this))
            return;

        if (DBHelper.isGoalExist(readableDb, goalName.getText().toString()))
        {
            Toast.makeText(this, getResources().getString(R.string.sameGoalExists), Toast.LENGTH_LONG).show();
            return;
        }

        if (dayFromCalendar==null)
        {
            Toast.makeText(this, getResources().getString(R.string.dontSetDeadline), Toast.LENGTH_LONG).show();
            return;
        }

        final long goalId = DBHelper.addGoalToDB(writableDb, goalName.getText().toString(), goalNote.getText().toString(), dayFromCalendar);

        if (goalId > 0) {
            Toast.makeText(this, getResources().getString(R.string.goalAddSuccessfully), Toast.LENGTH_LONG).show();
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    Intent data = new Intent();
                    data.putExtra("id", goalId+"");
                    setResult(RESULT_OK, data);
                    finish();// end this activity and come back to previous
                }
            }, 800);
        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.taskNotAddedInDB), Toast.LENGTH_LONG).show();
        }
    }

    private   void openCalendarOnClick() {
        final Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Launch Date Picker Dialog
        DatePickerDialog dpd = new DatePickerDialog(this,
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

    }

}
