package com.eplan.yuraha.easyplanning;

/**
 * Created by Yura on 14.02.2017.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* DialogAlert for choosing goals for new task*/
public class DialogFragmentForGoals extends DialogFragment {
    final String[] allGoals;

    {
       allGoals  = new String[] {"Побувати в Києві", "Купити Subaru Imprezza","Сімя",  "Заробляти 1000$ в місяць"};
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle mArgs = getArguments();
        String myValue = mArgs.getString("checkedGoals");
        final boolean[] checkedItemsArray = parseCheckedItems(myValue);
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
                                StringBuilder state = new StringBuilder();
                                int counter = 0;
                                for (int i = 0; i < allGoals.length; i++) {
                                    if (checkedItemsArray[i])
                                    state.append(allGoals[i]+"|");

                                }

                                /* Cut last ',' from goalsString for simpler parsing in ParentFragment  */
                                if (state.length()>0)
                                    state.setLength(state.length()-1);

                                Intent intent = new Intent();
                                intent.putExtra("value", state.toString());
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
    private boolean[] parseCheckedItems(String value)
    {
        boolean[] checkItems = new boolean[allGoals.length];
        if (value == null)
            return checkItems;

        String[] checkedGoalsArray = value.split("\\|");
      List<String> checkedGoals =  Arrays.asList(checkedGoalsArray);


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
