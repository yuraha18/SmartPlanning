package com.eplan.yuraha.easyplanning;

/**
 * Created by Yura on 14.02.2017.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* DialogAlert for choosing goals for new task*/
public class DialogFragmentForGoals extends DialogFragment {
   String[] allGoals;
    private SQLiteDatabase readableDb ;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SPDatabase db = new SPDatabase(getActivity());
        readableDb = db.getReadableDatabase();

        ArrayList<String> goalsList = DBHelper.getAllGoalsInProgress(readableDb);
        allGoals = goalsList.toArray(new String[goalsList.size()]);

        final ArrayList<String> checkedGoals = new ArrayList<>();

        final boolean[] checkedItemsArray = parseCheckedItems();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getContext().getResources().getString(R.string.goals_dialog_frag_tones))
                .setMultiChoiceItems(allGoals, checkedItemsArray,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                checkedItemsArray[which] = isChecked;
                            }
                        })
                .setPositiveButton("Готово",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                for (int i = 0; i < allGoals.length; i++) {
                                    if (checkedItemsArray[i])
                                    checkedGoals.add(allGoals[i]);

                                }

                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList("value", checkedGoals);
                                intent.putExtras(bundle);
                              getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                            }
                        })

                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });

        return builder.create();
    }


    /*This method get chosen  before goals
      * and check it in ListItem
       * */
    private boolean[] parseCheckedItems()
    {
        ArrayList<String> checkedGoals = getArguments().getStringArrayList("checkedGoals");
        boolean[] checkItems = new boolean[allGoals.length];
        if (checkedGoals == null || checkedGoals.size() == 0)
            return checkItems;

       for (int i =0; i < allGoals.length; i++)
       {
           if (checkedGoals.contains(allGoals[i]))
               checkItems[i] = true;

           else
               checkItems[i] = false;
       }


        return checkItems;
    }
}
