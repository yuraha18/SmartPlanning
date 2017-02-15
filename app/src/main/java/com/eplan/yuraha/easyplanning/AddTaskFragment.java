package com.eplan.yuraha.easyplanning;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddTaskFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddTaskFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private int mYear, mMonth, mDay, mHour, mMinute;// variables for TimePicker, keeps current time
    protected ArrayList<ToggleButton> toggleButtons;

    private String checkedGoals;//
    private ArrayList<CharSequence> allGoals = new ArrayList<>();
    Spinner prioritySpinner ;
private View view;//link on main view


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AddTaskFragment() {
    }

    {
        allGoals.add("Побувати в Києві");
        allGoals.add("Купити Subaru Imprezza");
        allGoals.add("Сімя");
        allGoals.add("Заробляти 1000$ в місяць");
    }

    /*Find out all toggles, add them to ArrayList and initialize
     * !!! Rewrite this class and xml file in future for dynamically adding toggles  */
    private void initializeAllToggles() {
   toggleButtons = new ArrayList<>();

        ToggleButton toggleMonday = (ToggleButton) view.findViewById(R.id.toggleMonday);
        ToggleButton toggleTuesday = (ToggleButton) view.findViewById(R.id.toggleTuesday);
        ToggleButton toggleWednesday = (ToggleButton) view.findViewById(R.id.toggleWednesday);
        ToggleButton toggleThursday = (ToggleButton) view.findViewById(R.id.toggleThursday);
        ToggleButton toggleFriday = (ToggleButton) view.findViewById(R.id.toggleFriday);
        ToggleButton toggleSaturday = (ToggleButton) view.findViewById(R.id.toggleSaturday);
        ToggleButton toggleSunday = (ToggleButton) view.findViewById(R.id.toggleSunday);

        toggleButtons.add(initToggle(toggleMonday));
        toggleButtons.add(initToggle(toggleTuesday));
        toggleButtons.add(initToggle(toggleWednesday));
        toggleButtons.add(initToggle(toggleThursday));
        toggleButtons.add(initToggle(toggleFriday));
        toggleButtons.add(initToggle(toggleSaturday));
        toggleButtons.add(initToggle(toggleSunday));



    }

    /*This method initialize send toggle:
     * change textColor in CheckedToggle */
    private ToggleButton initToggle(final ToggleButton toggle) {

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggle.setTextColor(Color.parseColor("#00E676"));
                }
                else
                {
                    toggle.setTextColor(Color.parseColor("#eeeeee"));
                }
            }
        });

        return toggle;
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
    public static AddTaskFragment newInstance(String param1, String param2) {
        AddTaskFragment fragment = new AddTaskFragment();
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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_task, container, false);
        initializeAllToggles();// add OnClick events for all dayToggles

        String[] SPINNER_DATA = {"Низький пріоритет","Середній пріоритет","Високий пріоритет"};

        prioritySpinner = (Spinner)view.findViewById(R.id.set_priority_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, SPINNER_DATA);
        prioritySpinner.setAdapter(adapter);

        /* Initialize button for choosing goals */
        Button setGoals = (Button) view.findViewById(R.id.setGoalsButton);
        setGoals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             goalOnClick(v);
            }
        });

         /* Initialize button for set time for Remembering about task */
final Button setRememberTime = (Button) view.findViewById(R.id.setRememberTime);
        setRememberTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
setRememberTimeOnClick(v);
            }
        });

        /* Button for choosing remembering tone (posibility adding different tones for different tasks) */
        Button setTone = (Button) view.findViewById(R.id.buttonSetTone);
        setTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToneOnClick(v);
            }
        });
        return view;
    }

    /* Tone.OnClickListener call this method for creating ToneDialogFragment*/
private void setToneOnClick(View v)
{
    TonesDialogFragment fragment = new TonesDialogFragment();
    fragment.setTargetFragment(this, 2);
    fragment.show(getFragmentManager(), fragment.getClass().getName());

}


    /* Remember.OnClickListener call this method for creating TimePickerDialog class*/
    public void setRememberTimeOnClick(View v)
    {
        // Get current time
        final Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),

                new TimePickerDialog.OnTimeSetListener() {

                    //This method return result with time chosen by customer
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                      Toast toast = Toast.makeText(getActivity(), hourOfDay+":"+minute, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }, mHour, mMinute, false);

        timePickerDialog.show();
    }

    /* Goal.OnClickListener call this method for creating DialogFragmentForGoals
    * method send to new Fragment data with before chosen goals (customer can edit his choose)
    * if it's first call, method send null
     */
    public void goalOnClick(View v)
    {
        Bundle args = new Bundle();// Create res for send
        args.putString("checkedGoals", checkedGoals);
        DialogFragmentForGoals fragment = new DialogFragmentForGoals();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, 1);
        fragment.show(getFragmentManager(), fragment.getClass().getName());// show DialogFragment
    }

    /* This method get result from DialogFragments
     * requestCode 1 mean result from GoalsDialogFragment, contains string with chosen goals
     * requestCode 2 mean result from TonesDialogFragment, contains int position chosen tone*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int toneCode = 0;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    checkedGoals = data.getStringExtra("value");break;
                case 2:
                  toneCode=  data.getIntExtra("value", 0);
                default:
                    break;
            }
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }








    }


