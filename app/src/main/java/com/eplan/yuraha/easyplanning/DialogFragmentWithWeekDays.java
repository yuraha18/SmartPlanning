package com.eplan.yuraha.easyplanning;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

/**
 * Created by yuraha18 on 3/9/2017.
 */

public class DialogFragmentWithWeekDays extends DialogFragment {

    private String[] days;
    private boolean[] checkedDays;

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        setDays();
        getCheckedItems();
        final ArrayList<String> checkedDaysList = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        builder.setTitle(getContext().getString(R.string.daysDialogTitle))
                .setCancelable(false)

                .setMultiChoiceItems(days, checkedDays,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                checkedDays[which] = isChecked;
                            }
                        })

                .setPositiveButton("Готово",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                for (int i = 0; i < days.length; i++) {
                                    if (checkedDays[i])
                                       checkedDaysList.add(i+"");

                                }

                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList("checkedValues", checkedDaysList);
                                Intent intent = new Intent();
                               intent.putExtras(bundle);
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                                }

                        });

        return builder.create();
    }

    private void getCheckedItems() {
        checkedDays = new boolean[days.length];
        ArrayList<String> days = new ArrayList<>();
        days.addAll(getArguments().getStringArrayList("checkedDays"));

        for (String day : days)
        {
            int position = Integer.parseInt(day);
            checkedDays[position] = true;
        }
    }

    private void setDays() {
        days = new String[]{
                getContext().getString(R.string.sunday),
                getContext().getString(R.string.monday),
                getContext().getString(R.string.tuesday),
                getContext().getString(R.string.wednesday),
                getContext().getString(R.string.thursday),
                getContext().getString(R.string.friday),
                getContext().getString(R.string.saturday)

        };
    }

}
