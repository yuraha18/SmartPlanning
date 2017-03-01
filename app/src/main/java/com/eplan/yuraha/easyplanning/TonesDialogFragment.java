package com.eplan.yuraha.easyplanning;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class Create dialogAlert for choosing tone for remembering
 */

public class TonesDialogFragment extends DialogFragment {
   private String[] tonesArray;//array with tones titles

    {
        tonesArray = new String[]{"Nokia tone 1", "Nokia tone 2", "Nokia tone 3"};
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int position = getArguments().getInt("position");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getContext().getResources().getString(R.string.tones_dialog_frag_title))

                .setSingleChoiceItems(tonesArray, position,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                /* send item position to parentFragment*/
                                Intent intent = new Intent();
                                intent.putExtra("value", item);
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

                                final Timer timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    public void run() {
                                        dismiss(); // when the task active then close the dialog
                                        timer.cancel(); // also just top the timer thread, otherwise,
                                        // you may receive a crash report
                                    }
                                }, 800);//after choosing item program will wait 0.8sec and then close
                            }
                        });



        return builder.create();
    }
}
