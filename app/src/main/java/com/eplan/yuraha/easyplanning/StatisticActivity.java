package com.eplan.yuraha.easyplanning;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StatisticActivity extends BaseActivity {

private String fromDate;
    private String toDate;
    private SQLiteDatabase readableDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        final View contentView = inflater.inflate(R.layout.activity_statistic, null, false);
        drawer.addView(contentView, 0);

        SPDatabase db = new SPDatabase(this);
        readableDb = db.getReadableDatabase();

        TextView countAllDoneTasks = (TextView) findViewById(R.id.countAllDoneTasks);
        TextView countAllInProgressTasks = (TextView) findViewById(R.id.countAllInProgressTasks);
        TextView overallProductivity = (TextView) findViewById(R.id.overallProductivity);

        String creatingDay = DBHelper.getCreatingDay(readableDb);// day when app was created
        String todaysDay = TaskListFragment.getTodaysDay();// day when user calls this method (started this activity)

        DayStatistic statData = countAllStatistic(todaysDay, creatingDay);
        fillInViews(statData, countAllDoneTasks, countAllInProgressTasks, overallProductivity);//here im filling in textViews contains whole statistic

        setButtonsClickListeners();

    }

    private void setButtonsClickListeners() {
        Button fromDateButton = (Button) findViewById(R.id.fromDateButton);
        Button toDateButton = (Button) findViewById(R.id.toDateButton);
        Button showStatisticButton = (Button) findViewById(R.id.showStatisticButton);

        fromDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCalendarOnClick("from");// variable inside means key
            }
        });

        toDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromDate==null)
                    Toast.makeText(StatisticActivity.this, getResources().getString(R.string.youMustSetFromDateBefore), Toast.LENGTH_LONG).show();

                else
                openCalendarOnClick("to");// variable inside means key
            }
        });

        showStatisticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromDate==null || toDate==null)
                {
                    Toast.makeText(StatisticActivity.this, getResources().getString(R.string.setDatesFirstly), Toast.LENGTH_LONG).show();
                }

                else
                    getStatisticForPeriod();
            }
        });
    }

    private void getStatisticForPeriod() {
        TextView countDoneTasks = (TextView) findViewById(R.id.countDoneTasksInPeriod);
        TextView countInProgressTasks = (TextView) findViewById(R.id.countInProgressTasksInPeriod);
        TextView overallProductivity = (TextView) findViewById(R.id.productivityInPeriod);

        // here i put variables like "to, from" because the will CountDown from toDate to fromDate
        DayStatistic statData = countAllStatistic(toDate, fromDate);
        fillInViews(statData, countDoneTasks, countInProgressTasks, overallProductivity);//here im filling in textViews contains whole statistic
    }

    private void openCalendarOnClick(final String whichCalendar) {
        final Calendar calendar = Calendar.getInstance();

        String todaysDay = TaskListFragment.getTodaysDay();// day when user calls this method (started this activity)

        Date fromDay;
        /* If this method calls for dayFrom code bellow set creatingDay like min day for spinner
        * if its toDay it set dayFrom like min. I did this for not allow choose day before fromDay */
        if (fromDate == null) {
            String creatingDay = DBHelper.getCreatingDay(readableDb);// day when app was created
            fromDay = getDateFromString(creatingDay);
        }

        else
            fromDay = getDateFromString(fromDate);


        Date todaysDate = getDateFromString(todaysDay);
        calendar.setTime(fromDay);

          int  mYear = calendar.get(Calendar.YEAR);
          int  mMonth = calendar.get(Calendar.MONTH);
          int  mDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Launch Date Picker Dialog
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // This method uses two methods: from and to date. Thats why this 'if' choosing in which variable save date
                        if (whichCalendar.equals("from")) {
                            fromDate = dayOfMonth + "-"
                                    + (monthOfYear + 1) + "-" + year;
                        }
                        else
                            toDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;

                    }
                }, mYear, mMonth, mDay);

// set min and max date for DataPicker, User cant choose bigger or lesser date
        dpd.getDatePicker().setMinDate(fromDay.getTime());
        dpd.getDatePicker().setMaxDate(todaysDate.getTime());
        dpd.show();
    }

    private Date getDateFromString(String day) {
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT);

        try {
           return formatter.parse(day);
        }
        catch (Exception e){return  new Date();}


    }

    private void fillInViews(DayStatistic statData, TextView countDoneTasks, TextView countInProgressTasks, TextView overallProductivity) {

        int countDone = statData.getCountDone();
        int countInProgress = statData.getCountInProgress();
        int allTasks = countDone + countInProgress;
        float productivity = 0;

        if (allTasks!=0)
         productivity = ((countDone*1.0f) / allTasks) *100;

        int productivityInt = (int ) productivity;// change productivity from float to int for better visibility

        String doneText = getResources().getString(R.string.countDoneTasks) + " " +countDone;
        String inProgressText = getResources().getString(R.string.countInProgressTasks) + " " +countInProgress ;
        String productivityText = getResources().getString(R.string.productivity) + " " +productivityInt + "%";

        countDoneTasks.setText(doneText);
        countInProgressTasks.setText(inProgressText);
        overallProductivity.setText(productivityText);

    }


    private DayStatistic countAllStatistic(String fromDay, String toDay) {
        String dayBeforeToDay = getPreviousDay(toDay);//will be use in while loop. Means previous day for ToDay variable

        int countDoneTasks = 0, countInProgressTasks=0;
        while (true)
        {

            DayStatistic day = DBHelper.getStatisticForDay(readableDb, fromDay);
           countDoneTasks += day.getCountDone();
            countInProgressTasks += day.getCountInProgress();
            fromDay = getPreviousDay(fromDay);

            // loop ends if this is the last day (when app was created) or can't parse previous day
            if (dayBeforeToDay.equals(fromDay) || fromDay.equals("-1") || dayBeforeToDay.equals("-1"))
                break;

        }


        return new DayStatistic(countDoneTasks, countInProgressTasks);
    }

    private String getPreviousDay(String calledDay) {
        try {
            final Calendar calendar = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT);

            Date date = formatter.parse(calledDay);
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_YEAR, -1);

            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);

            return mDay + "-"
                    + (mMonth + 1) + "-" + mYear;
        } catch (Exception e) {
            return "-1";
        }
    }
}

