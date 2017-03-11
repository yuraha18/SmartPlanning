package com.eplan.yuraha.easyplanning;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class Create dialogAlert for choosing tone for remembering
 */

public class DialogFragmentForRepeating extends DialogFragment {
    private String[] values;//array with tones titles
    private static String[] daysOfWeek;


    public Dialog onCreateDialog(Bundle savedInstanceState) {

        initArray();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);

        builder.setTitle(getContext().getResources().getString(R.string.repeatTitle))

                .setItems(values,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                System.out.println(item);
                                /* send item position to parentFragment*/
                                if (item == 4) {
                                    createDaysDialog();

                                }
                                else if (item == 7)
                                {
                                    openCalendarOnClick();
                                }
                                else {
                                    Intent intent = new Intent();
                                    intent.putExtra("value", item+"");
                                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

                                    final Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        public void run() {
                                            dismiss(); // when the task active then close the dialog
                                            timer.cancel(); // also just top the timer thread, otherwise,
                                            // you may receive a crash report
                                        }
                                    }, 400);//after choosing item program will wait 0.8sec and then close
                                }
                            }
                        });



        return builder.create();
    }

    private void openCalendarOnClick() {
        final Calendar calendar = Calendar.getInstance();
        String calledDay = getArguments().getString("calledDay");
        String dayFromCalendar = getArguments().getString("dayFromCalendar");
        Date calledDate;
        if (dayFromCalendar!=null)
            calledDate = AddTaskFragment.getDateFromString(dayFromCalendar, Constants.DATEFORMAT);
        else
            calledDate = AddTaskFragment.getDateFromString(calledDay, Constants.DATEFORMAT);

        calendar.setTime(calledDate);
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
                       String dayFromCalendar = dayOfMonth + "-"
                                + (monthOfYear + 1) + "-" + year;
                        Intent intent = new Intent();
                        intent.putExtra("dayFromCalendar", dayFromCalendar);
                        getTargetFragment().onActivityResult(4, Activity.RESULT_OK, intent);

                    }
                }, mYear, mMonth, mDay);
        dpd.getDatePicker().setMinDate(calledDate.getTime());

        dpd.show();
    }

    private void createDaysDialog() {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("checkedDays", getArguments().getStringArrayList("checkedDays"));
        DialogFragmentWithWeekDays fragment = new DialogFragmentWithWeekDays();
        fragment.setArguments(bundle);
        fragment.setTargetFragment(getTargetFragment(), 3);
        fragment.show(getFragmentManager(), fragment.getClass().getName());// show DialogFragment
    }

    private void initArray() {
        initWeekDays();
        String calledDay = getArguments().getString("calledDay");
        int dayOfWeek = AddTaskFragment.getDayOfWeek(calledDay, Constants.DATEFORMAT);
        int dayOfMonth = AddTaskFragment.getDayOfMonth(calledDay, Constants.DATEFORMAT);

        String repeatWeek = getContext().getString(R.string.repeatEveryWeekP1) + daysOfWeek[dayOfWeek] +
                getContext().getString(R.string.repeatEveryWeekP2);

        String repeatMonth = getContext().getString(R.string.repeatEveryMonthP1) + " " + dayOfMonth + " " +
                getContext().getString(R.string.repeatEveryMonthP2);

        values = new String[]{getContext().getString(R.string.repeatOnce),
                getContext().getString(R.string.repeatEveryDay),
                getContext().getString(R.string.repeatInWeekDays),
                getContext().getString(R.string.repeatInWeekEnds),
                getContext().getString(R.string.repeatChooseDays),
                repeatWeek,
                repeatMonth,
        getContext().getResources().getString(R.string.setDayForRepeating)};
    }

    private void initWeekDays() {
        daysOfWeek = new String[]{
                "",
                getContext().getString(R.string.everySunday),
                getContext().getString(R.string.everyMonday),
                getContext().getString(R.string.everyTuesday),
                getContext().getString(R.string.everyWednesday),
                getContext().getString(R.string.everyThursday),
                getContext().getString(R.string.everyFriday),
                getContext().getString(R.string.everySaturday)

        };
    }
}