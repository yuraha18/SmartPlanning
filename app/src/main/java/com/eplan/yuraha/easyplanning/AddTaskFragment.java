package com.eplan.yuraha.easyplanning;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


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

    /* Variables keep information about new task
    * selected by user
    * will be send to DB*/
    private EditText taskText;
    private int priority = 1;
    private ArrayList<String> chosenDays;
    private String dayFromCalendar;
    private boolean repeatEveryWeek = false;
    private String checkedGoals;//goals attached to task
    private String remindTime;
    private int remindTone = 0;

    private String lowPriority;
    private String middlePriority;
    private String highPriority ;
    private SQLiteDatabase writableDb ;
    private SQLiteDatabase readableDb ;

    private String[] weekDays = new String[]{"","Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };// In android weeks start in Sunday

    private int mHour, mMinute;// variables for TimePicker, keeps current time
    protected ArrayList<ToggleButton> toggleButtons;

    private ArrayList<CharSequence> allGoals = new ArrayList<>();
    Spinner prioritySpinner ;
private View view;//link on main view


    /* Variables with priority values from strings.xml*/


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AddTaskFragment() {
       chosenDays = new ArrayList<>();

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

        toggleButtons.add(new ToggleButton(getContext()));/*I did this for supporting counting in all app. In this case Sunday will be 1 day of week, not 0
                                                           it.s new Object, not null. Because I dont want catch NullPointerException in future if I forget this*/
        toggleButtons.add(initToggle(toggleSunday));//In Android wekks start from Sunday
        toggleButtons.add(initToggle(toggleMonday));
        toggleButtons.add(initToggle(toggleTuesday));
        toggleButtons.add(initToggle(toggleWednesday));
        toggleButtons.add(initToggle(toggleThursday));
        toggleButtons.add(initToggle(toggleFriday));
        toggleButtons.add(initToggle(toggleSaturday));




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

        SPDatabase db = new SPDatabase(getActivity());
        writableDb = db.getWritableDatabase();
        readableDb = db.getReadableDatabase();

        lowPriority = getContext().getResources().getString(R.string.lowPriority);
        middlePriority = getContext().getResources().getString(R.string.middlePriority);
        highPriority = getContext().getResources().getString(R.string.highPriority);

        String[] SPINNER_DATA = {lowPriority, middlePriority, highPriority};

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


        EditText taskText = (EditText) view.findViewById(R.id.taskName);



        Button buttonDone = (Button) view.findViewById(R.id.sendDataForAddTask);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             addTaskButton();
            }
        });

        ImageButton buttonOpenCalendar = (ImageButton) view.findViewById(R.id.buttonOpenCalendar);
        buttonOpenCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCalendarOnClick(v);
            }
        });

        return view;
    }

    /*This method called after user's clicking on "Done" button
     * add new task in DB */
    private void addTaskButton() {

        /*getTaskDataFromViews() parse all information from views
         * if something was wrong with they, we come out and cancel adding task */
       if (!getTaskDataFromViews())
           return;

        /*If there are the same task in this day we prevent user and come out */
boolean checkTaskInDB = DBHelper.isTaskNameExistInThisDay(readableDb, taskText.getText().toString(), chosenDays);
        if (checkTaskInDB)
        {
            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskAlreadyExistsInDB), Toast.LENGTH_LONG).show();
            return;
        }

        /* Adding new task
        * if something was wrong: show messege for user and come out*/
      boolean isTaskAdded =  DBHelper.addTask(writableDb, taskText.getText().toString(), priority, chosenDays, checkedGoals, remindTime, remindTone);

        if (!isTaskAdded)
        {
            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskNotAddedInDB), Toast.LENGTH_LONG);
            return;
        }

/* If adding was successful show message
* wait 0.8 sec and back to previos activity*/
        Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskAddSuccessfully), Toast.LENGTH_LONG).show();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
              getActivity().finish();// end this activity and come back to previous
            }
        }, 800);

    }

    /*This method will be ask after user's clicking on DoneButton
     * it looking for taskAdd views values and put they in class fields
      * for adding to DB*/
    private boolean getTaskDataFromViews() {

        taskText = (EditText) view.findViewById(R.id.taskName);
        if (!taskTextValidator(taskText, getContext()))
            return false;

        try {
            priority = parseTaskPriority(prioritySpinner.getSelectedItem().toString());

            CheckBox isRepeatingCheckBox = (CheckBox)  view.findViewById(R.id.repeatEveryWeekCheckBox);
            repeatEveryWeek = isRepeatingCheckBox.isChecked();

            parseInfoFromDaysToggles();//here we are calling method getting information from toggles and adding it to ArrayList


        }
        catch (ExceptionInInitializerError e)
        {
            Toast.makeText(getActivity(),
                    getContext().getResources().getString(R.string.cantParseDataFromViewsException), Toast.LENGTH_LONG).show();

            return false;
        }


        return true;
    }

    /* Parsing dayToggles and if add checked to String value for adding to DB
     * all values separate by '|' */
    private void parseInfoFromDaysToggles() {

        if (repeatEveryWeek)// it's mean that task will be repeating every week. That's why adding name of day will be enough for repeating
        {
            chosenDays.clear();
            for (int i =0; i< toggleButtons.size(); i++)
            {
                if (toggleButtons.get(i).isChecked())
                    chosenDays.add(weekDays[i]);
            }
        }

        else // if user wanna add task in single days without repeating every week
        {
            daysFromToggle();//call method creating dates from selectedToggles.
        }

        if (dayFromCalendar!=null)// if user also chosen date from DataPicker
        {
         chosenDays.add(dayFromCalendar);
        }

        if (chosenDays.isEmpty())
        {

        }

    }

    private void daysFromToggle() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);// get today's day of week (1 is Sunday)
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);// get today's day of year (51 is 20 February)
        chosenDays.clear();

        for (int i =1; i < toggleButtons.size(); i++)
        {
            int dayCount = 0;
            if (toggleButtons.get(i).isChecked())//if user checked day
            {
                if (dayOfWeek < i)// Example: today is Monday (2 day) I wonna set notification on Saturday (7 day). I must plus 5 day to Monday and this will be Saturday date
                {
                    dayCount = i - dayOfWeek;
                }
                else if (dayOfWeek > i)// Thursday (5 day) and notify on Monday (2 day). 7-5 = 2 (count of days to end of week) 2 + 2 (count days from start new week)
                {
                  dayCount = (7 - dayOfWeek) + i;
                }

                int newDate = dayOfYear + dayCount;// plusing count of day till checkedDay to today's day in year for creating newDate below
                Calendar newCalendar = Calendar.getInstance();
                if (newDate > 365) {// in the end of year
                    newDate -= 365;
                    newCalendar.set(Calendar.DAY_OF_YEAR, newDate);//create new date from new day of year
                    newCalendar.add(Calendar.YEAR, 1);// offset the year
                }
                else
                {
                    newCalendar.set(Calendar.DAY_OF_YEAR, newDate);
                }
                String remindDate = newCalendar.get(Calendar.DAY_OF_MONTH) + "-"
                                                     + (newCalendar.get(Calendar.MONTH)+1) + "-"
                                                     + newCalendar.get(Calendar.YEAR);// month is starting from 0 in Calendar, that's why +1
                chosenDays.add(remindDate);
            }
        }

    }


    /* Method get String value from spinner with task priority and return int equivalent*/
    private int parseTaskPriority(String s) {

        if (s.equals(lowPriority))
            return 1;
        else if (s.equals(middlePriority))
            return 2;
        else if (s.equals(highPriority))
            return 3;

        return 1;
    }


    protected static boolean taskTextValidator(EditText taskText, Context context) {
        String text = taskText.getText().toString();
        if (text.length() < 5)
        {
            taskText.setError(context.getResources().getString(R.string.shortLengthError));
            return false;
        }
        if (text.length() > 250)
        {
            taskText.setError(context.getResources().getString(R.string.longLengthError));
            return false;
        }


        return true;
    }


    /* Tone.OnClickListener call this method for creating ToneDialogFragment*/
private void setToneOnClick(View v)
{
    TonesDialogFragment fragment = new TonesDialogFragment();
    fragment.setTargetFragment(this, 2);
    fragment.show(getFragmentManager(), fragment.getClass().getName());

}


    private   void openCalendarOnClick(View v) {
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
                      remindTime =  hourOfDay+":"+minute;
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
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    checkedGoals = data.getStringExtra("value");break;
                case 2:
                  remindTone =  data.getIntExtra("value", 0);
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


