package com.eplan.yuraha.easyplanning;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
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
    private AutoCompleteTextView taskText;
    private int priority = 1;
    private ArrayList<String> chosenDays;
    private String dayFromCalendar;
    private boolean repeatEveryWeek = false;
    private String checkedGoals = "";//goals attached to task
    private String remindTime;
    private int remindTone = -1;
    private String calledDay;

    /* This variable is true after user clicking on Toggle button
    * its using when i update task in Db
    * if user didnt change toggles i not change Repeating table
    * else delete old data from thee and add updated*/
    private  boolean isToggleEdit;

    private static String dateFormat = "dd-MM-yyyy";

    private String lowPriority;
    private String middlePriority;
    private String highPriority ;
    private SQLiteDatabase writableDb ;
    private SQLiteDatabase readableDb ;

   private int mYear=-1, mMonth=-1, mDay=-1;// variables for openCalendarOnClick method


    private boolean isEdit;

    private static ArrayList<String> weekDays;

    static {
        weekDays = new ArrayList<>();
        weekDays.add("");// In android weeks start in Sunday
        weekDays.add("Sunday");
        weekDays.add("Monday");
        weekDays.add("Tuesday");
        weekDays.add("Wednesday");
        weekDays.add("Thursday");
        weekDays.add("Friday");
        weekDays.add("Saturday");
    }

    private int mHour=-1, mMinute=-1;// variables for TimePicker, keeps current time
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
        toggleButtons.add(initToggle(toggleSunday));//In Android weeks start from Sunday
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
                isToggleEdit = true;
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

        calledDay = getArguments().getString("calledDay");

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

        /* Button for choosing remembering tone (possibility adding different tones for different tasks) */
        Button setTone = (Button) view.findViewById(R.id.buttonSetTone);
        setTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToneOnClick(v);
            }
        });


         taskText = (AutoCompleteTextView) view.findViewById(R.id.taskName);
        List<String> allTasks = DBHelper.getAllTasksForAutoComplete(readableDb);
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, allTasks);
        taskText.setAdapter(autoCompleteAdapter);

        ImageButton buttonOpenCalendar = (ImageButton) view.findViewById(R.id.buttonOpenCalendar);
        buttonOpenCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isToggleEdit = true;
                openCalendarOnClick(v);
            }
        });

        Button buttonDone = (Button) view.findViewById(R.id.sendDataForAddTask);

        isEdit = getArguments().getBoolean("isEdit");
        if (isEdit)
        {

           final String taskId = getArguments().getString("taskID");
            fillInViewByDataFromDB(taskId);
            buttonDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTaskButton(taskId);
                }
            });
        }

        else {
            buttonDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addTaskButton();
                }
            });
        }



        return view;
    }

    private void fillInViewByDataFromDB(String taskId) {
        try {
            taskText.setText(DBHelper.getTaskTextFromId(readableDb, taskId));
            int priority = DBHelper.getPriorityFromTaskId(readableDb, taskId);
            prioritySpinner.setSelection(priority-1);// subtract 1 cause position stars from 0, my priority from 1
            checkTogglesByDbData(taskId);


            ArrayList<String> goals = DBHelper.getTaskGoals(readableDb, taskId);

            for (String goal : goals)
                checkedGoals+= goal +"|";

            remindTime = DBHelper.getRemindTimeForTaskId(readableDb, taskId);

            setTimeFromString(remindTime);
            remindTone = DBHelper.getRemindToneForTaskId(readableDb, taskId);

        }
       catch (SQLiteException e)
        {
            Toast.makeText(getActivity(), getResources().getString(R.string.cantParseDataFromViewsException), Toast.LENGTH_LONG).show();
        }


    }

    /* i have time like 13:40 and here Im parsing it and set hour and minute for TimePicker*/
    private void setTimeFromString(String remindTime) {
        try {
            String[] time = remindTime.split(":");
            mHour = Integer.parseInt(time[0]);
            mMinute = Integer.parseInt(time[1]);

        }
        catch (Exception e)
        {
            mHour = -1;
            mMinute =-1;
        }


    }

    private void checkTogglesByDbData(String taskId) {
        ArrayList<String> days = DBHelper.getDaysForTaskId(readableDb, taskId);
        boolean repeatingEveryWeek = false;

        for (String day : days)
        {

            int dayPosition =0;
            if (weekDays.contains(day))
            {
                repeatingEveryWeek = true;

                dayPosition  = weekDays.indexOf(day);

                toggleButtons.get(dayPosition).setChecked(true);

            }
            else
            {


                if (distanceBetweenTwoDates(calledDay, day) < 7 )
                {
                    dayPosition = getDayOfWeek(day, dateFormat);
                    toggleButtons.get(dayPosition).setChecked(true);
                    System.out.println(day + ":" + dayPosition);
                }
                else {
                    setDateFromString(day);
                }

            }


        }

        if (repeatingEveryWeek) {

            CheckBox repeatingCheckBox = (CheckBox) view.findViewById(R.id.repeatEveryWeekCheckBox);
            repeatingCheckBox.setChecked(true);
        }

    }

    private void setDateFromString(String day) {

        try {
            String[] date = day.split("-");
            mDay = Integer.parseInt(date[0]);
            mMonth = Integer.parseInt(date[1])-1;//days in Calendar starts from 0, im starting from 1
            mYear = Integer.parseInt(date[2]);
        }
        catch (Exception e){}
    }

    private int distanceBetweenTwoDates(String calledDay, String day) {
        Calendar calledDayCalendar = Calendar.getInstance();
        Calendar dayCalendar = Calendar.getInstance();

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        try {
            Date date = formatter.parse(calledDay);
            calledDayCalendar.setTime(date);
            date = formatter.parse(day);
            dayCalendar.setTime(date);

            int countDays = daysBetween(calledDayCalendar.getTime(),dayCalendar.getTime());

            return countDays < 0 ? countDays*(-1) : countDays;
        }
        catch (Exception e)
        {
            return -1;
        }

    }

    public int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }


    public static int getDayOfWeek(String day, String dateFormat) {
        try {
            DateFormat formatter = new SimpleDateFormat(dateFormat);
            Date date = formatter.parse(day);
            System.out.println(dateFormat);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            return calendar.get(Calendar.DAY_OF_WEEK);
        }

        catch (Exception e)
        {
            return 1;
        }



    }

    private void editTaskButton(String taskId) {

        if (!getTaskDataFromViews())
            return;

        /*If there are the same task in this day we prevent user and come out */
        boolean checkTaskInDB = DBHelper.isTaskNameExistInThisDay(readableDb, taskText.getText().toString(), chosenDays, taskId);
        if (checkTaskInDB)
        {
            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskAlreadyExistsInDB), Toast.LENGTH_LONG).show();
            return;
        }

        List<String> checkedGoalsList = new ArrayList<>();
        if (checkedGoals.length() > 0)
            checkedGoalsList = moveFromStringToList(checkedGoals);//i wanna send to DB list of goals, not string
        /* Adding new task
        * if something was wrong: show message for user and come out*/
        boolean isTaskEdited =  DBHelper.editTask(writableDb, taskId, isToggleEdit, taskText.getText().toString(), priority, chosenDays, checkedGoalsList, remindTime, remindTone);

        if (!isTaskEdited)
        {
            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskNotAddedInDB), Toast.LENGTH_LONG);
            return;
        }

/* If adding was successful show message
* wait 0.8 sec and back to previos activity*/
        Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskEditSuccessfully), Toast.LENGTH_LONG).show();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                getFragmentManager().popBackStack();// end this fragment and come back to previous
            }
        }, 800);

    }

    /*This method called after user's clicking on "Done" button
     * add new task in DB */
    private void addTaskButton() {

        /*getTaskDataFromViews() parse all information from views
         * if something was wrong with they, we come out and cancel adding task */
       if (!getTaskDataFromViews())
           return;

        System.out.println("chosen days" + chosenDays);
        /*If there are the same task in this day we prevent user and come out */
boolean checkTaskInDB = DBHelper.isTaskNameExistInThisDay(readableDb, taskText.getText().toString(), chosenDays, null);
        if (checkTaskInDB)
        {
            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskAlreadyExistsInDB), Toast.LENGTH_LONG).show();
            return;
        }

        List<String> checkedGoalsList = new ArrayList<>();
        if (checkedGoals.length() > 0)
        checkedGoalsList = moveFromStringToList(checkedGoals);//i wanna send to DB list of goals, not string

        chosenDays = new ArrayList<>(new LinkedHashSet<>(chosenDays));// delete duplicates in list, if exists
        /* Adding new task
        * if something was wrong: show message for user and come out*/
      boolean isTaskAdded =  DBHelper.addTask(writableDb, taskText.getText().toString(), priority, chosenDays, checkedGoalsList, remindTime, remindTone);

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
                getFragmentManager().popBackStack();// end this fragment and come back to previous
            }
        }, 800);

    }

    private List<String> moveFromStringToList(String value) {
        String[] arr = value.split("\\|");
        List<String> list = Arrays.asList(arr);

        return list;

    }

    /*This method will be ask after user's clicking on DoneButton
     * it looking for taskAdd views values and put they in class fields
      * for adding to DB*/
    private boolean getTaskDataFromViews() {


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
                    chosenDays.add(weekDays.get(i));
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
        /* add called day like first day, it will be added to repeating table with others and to lifecycle table alone like FROMday
         * call only for AddTask */
       if (!isEdit)
        chosenDays.add(0, calledDay);

    }

    private void daysFromToggle() {
        Calendar calendar = Calendar.getInstance();
        Date date = getCalledDay();
        calendar.setTime(date);
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

    private Date getCalledDay() {
        Date date = new Date();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            date = formatter.parse(calledDay);
        }
        catch (Exception e){}

        return date;
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
    Bundle bundle = new Bundle();
    if (remindTone!=-1)
        bundle.putInt("position", remindTone);

    else
    bundle.putInt("position", 0);


    TonesDialogFragment fragment = new TonesDialogFragment();
    fragment.setTargetFragment(this, 2);
    fragment.setArguments(bundle);
    fragment.show(getFragmentManager(), fragment.getClass().getName());

}


    private   void openCalendarOnClick(View v) {
        final Calendar calendar = Calendar.getInstance();

        //if date hadnt been chosen before. Else - will be set date from DB
        if (mYear==-1 || mMonth==-1 || mDay==-1) {
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
        }

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

        if (mHour==-1 || mMinute==-1) {
            final Calendar calendar = Calendar.getInstance();
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        }

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


    public static String getEarliestDay(ArrayList<String> dates) {

dates.removeAll(weekDays);// i must remove all weekDays (Monday, Sunday...) from list because in comparing they will be cause of Exceptions

            Collections.sort(dates, new Comparator<String>() {
                @Override
                public int compare(String day1, String day2) {
                    return compareTwoDates(day1, day2);
                }
            });

        return dates.get(0);

    }

public static int compareTwoDates (String day1, String day2)
{
    int result = 0;
    Date date1;
    Date date2;
    try {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        date1 = formatter.parse(day1);
        date2 = formatter.parse(day2);
        result =  date1.compareTo(date2);
    }
    catch (ParseException e){
    }

    return result;
}

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    }


