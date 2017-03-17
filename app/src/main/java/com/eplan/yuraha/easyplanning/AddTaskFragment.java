package com.eplan.yuraha.easyplanning;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Spinner;
import android.widget.TextView;
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
    private ArrayList<String> chosenDays;//this list for saving items for adding to get
    private ArrayList<String> checkedDays;// this one save data from weekDays dialog fragment
    private ArrayList<String> checkedGoals;//goals attached to task
    private String remindTime;
    private String calledDay;
    private String dayFromCalendar;

    private boolean isDatesEdit;
    /* When user choose repeat every month one of this variable makes true
    * and send data to DB in special MonthRepeating table */
    private boolean isRepeatEveryMonth;

    /* This variable is true after user clicking on Toggle button
    * its using when i update task in Db
    * if user didnt change toggles i not change Repeating table
    * else delete old data from thee and add updated*/
    private  boolean isToggleEdit;

    private static final String dateFormat = "dd-MM-yyyy";
    public String[] daysOfWeek;
    private String lowPriority;
    private String middlePriority;
    private String highPriority ;
    private SQLiteDatabase writableDb ;
    private SQLiteDatabase readableDb ;
    StringBuilder textRepeatValue = new StringBuilder();

    TextView repeatText;
    private boolean isEdit;

    public static ArrayList<String> weekDays;
    private String[] weekDaysShort;



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
        checkedDays = new ArrayList<>();
        checkedGoals = new ArrayList<>();
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

        SPDatabase db = new SPDatabase(getActivity());
        writableDb = db.getWritableDatabase();
        readableDb = db.getReadableDatabase();
        initShortWeekDays();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_task, container, false);
        repeatText = (TextView) view.findViewById(R.id.repeatText);
        calledDay = getArguments().getString("calledDay");

        initSpinner();
        setAutoCompleteForTaskText();
        initDoneButton();

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

        ImageButton repeatButton = (ImageButton) view.findViewById(R.id.repeatImageButton);


        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRepeatTimeOnClick();
            }
        });

        return view;
    }

    private void initShortWeekDays()
    {
        weekDaysShort = new String[]{
                "",
                getContext().getResources().getString(R.string.sundayShort),
                getContext().getResources().getString(R.string.mondayShort),
                getContext().getResources().getString(R.string.tuesdayShort),
                getContext().getResources().getString(R.string.wednesdayShort),
                getContext().getResources().getString(R.string.thursdayShort),
                getContext().getResources().getString(R.string.fridayShort),
                getContext().getResources().getString(R.string.saturdayShort)
        };

    }

    private void setRepeatTimeOnClick() {
        Bundle args = new Bundle();
        args.putString("calledDay", calledDay);
        args.putString("dayFromCalendar", dayFromCalendar);
        args.putStringArrayList("checkedDays", checkedDays);
        DialogFragmentForRepeating fragment = new DialogFragmentForRepeating();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, 2);
        fragment.show(getFragmentManager(), fragment.getClass().getName());// show DialogFragment
    }

    private void initDoneButton() {
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
    }

    private void setAutoCompleteForTaskText() {
        taskText = (AutoCompleteTextView) view.findViewById(R.id.taskName);
        List<String> allTasks = DBHelper.getAllTasksForAutoComplete(readableDb);
        allTasks = new ArrayList<>(new LinkedHashSet<>(allTasks));// delete duplicates in list, if exists
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, allTasks);
        taskText.setAdapter(autoCompleteAdapter);
    }

    private void initSpinner() {
        lowPriority = getContext().getResources().getString(R.string.lowPriority);
        middlePriority = getContext().getResources().getString(R.string.middlePriority);
        highPriority = getContext().getResources().getString(R.string.highPriority);

        String[] SPINNER_DATA = {lowPriority, middlePriority, highPriority};
        prioritySpinner = (Spinner)view.findViewById(R.id.set_priority_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, SPINNER_DATA);
        prioritySpinner.setAdapter(adapter);
    }

    private void fillInViewByDataFromDB(String taskId) {
        try {
            taskText.setText(DBHelper.getTaskTextFromId(readableDb, taskId));
            int priority = DBHelper.getPriorityFromTaskId(readableDb, taskId);
            prioritySpinner.setSelection(priority-1);// subtract 1 cause position stars from 0, my priority from 1


            checkedGoals = DBHelper.getTaskGoals(readableDb, taskId);
            setRepeatTextByDbData(taskId);


            remindTime = DBHelper.getRemindTimeForTaskId(readableDb, taskId);

            setTimeFromString(remindTime);


        }
        catch (SQLiteException e)
        {
            Toast.makeText(getActivity(), getResources().getString(R.string.cantParseDataFromViewsException), Toast.LENGTH_LONG).show();
        }


    }

    /*the one and main aim of this method is setText in repeatText TextView
    * when user editing task. This TextView show user witch repeating days he has been chosen before
    * firstly method search in MonthRepeating, if there are any info it continue in repeating table
    * searching repeating days (Monday, Tuesday..), If also without result: search single days*/

    private void setRepeatTextByDbData(String taskId) {

        /* looking for in MonthRepeat table*/
        int dayOfMonth = DBHelper.getDayOfMonthFromMonthRepeating(readableDb, taskId);
        if (dayOfMonth > 0)
        {
            isRepeatEveryMonth = true;
            String text = getResources().getString(R.string.repeatEveryMonthP1) + " " + dayOfMonth +
                    " " + getResources().getString(R.string.repeatEveryMonthP2);
            repeatText.setText(text);
            return;
        }

        /* if there are any row for this task in MonthRepeat, try to look in Repeating table
        * where are repeating days*/
        setRepeatTextFromRepeatingTable(taskId);

    }


    private void setRepeatTextFromRepeatingTable(String taskId) {
        chosenDays.clear();
        ArrayList<String> repeatingDays = DBHelper.getDaysFromRepeatingTable(readableDb, taskId);
        repeatingDays =  new ArrayList<>(new LinkedHashSet<>(repeatingDays));// delete duplicates in list, if exists
        boolean check = false;

        for (String day : repeatingDays)
        {
            chosenDays.add(day);
            if (weekDays.contains(day))
            {
                check = true;// if there are some repeating days (Monday, Tuesday...) check making true
                int index = weekDays.indexOf(day);
                textRepeatValue.append(weekDaysShort[index] + ", ");
            }
        }

        if (!check) {// if there are are repeating days, set look one more time and show single days
            for (String day : repeatingDays) {
                try {
                    long task = Long.parseLong(taskId);
                    long dayId = DBHelper.getDayFromString(readableDb, day);
                    boolean isInDeleted = DBHelper.isInDeletedTable(readableDb, task, dayId);

                    if (!isInDeleted) {
                        String formattedDay = formatDate(day);// format date for more beautiful showing
                        textRepeatValue.append(formattedDay + ", ");
                    }
                }
                catch (Exception e){}

            }
        }

        if (textRepeatValue.length()>0) {
            textRepeatValue.setLength(textRepeatValue.length() - 2);// cut last "," from value
            repeatText.setText(textRepeatValue.toString());// set value for TextView
        }
    }

    /* create beautiful data format for textRepeater (TextView)*/
    private static String formatDate(String day) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT);
            Date date = formatter.parse(day);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM");
            return dateFormat.format(date);
        }
        catch (ParseException e){return "";}

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


    private static Calendar getCalendarFromStringDate(String day, String dateFormat)
    {
        try {
            DateFormat formatter = new SimpleDateFormat(dateFormat);
            Date date = formatter.parse(day);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        }
        catch (Exception e)
        {
            return Calendar.getInstance();
        }

    }

    public static int getDayOfWeek(String day, String dateFormat) {
        Calendar calendar = getCalendarFromStringDate(day, dateFormat);
        return calendar.get(Calendar.DAY_OF_WEEK);

    }

    public static int getDayOfMonth(String day, String dateFormat) {
        Calendar calendar = getCalendarFromStringDate(day, dateFormat);
        return calendar.get(Calendar.DAY_OF_MONTH);

    }

    private void editTaskButton(String taskId) {

        if (!getTaskDataFromViews())
            return;

        String todaysDay = TaskListFragment.getTodaysDay();
        String dayFrom = DBHelper.getDayFromByTaskId(readableDb, taskId);

        /* here some trick. I add task dayFrom before checking, because it will be added to db
        but without it not checked (dayFrom adding only in DBHelper.editTask). I remove it after checking */
        if (AddTaskFragment.compareTwoDates(dayFrom, todaysDay) >= 0 )
            chosenDays.add(0, dayFrom);

        /*If there are the same task in this day we prevent user and come out */
        boolean checkTaskInDB = DBHelper.isTaskNameExistInThisDay(readableDb, this, taskText.getText().toString(), chosenDays, taskId);
        if (checkTaskInDB)
        {
            // Toast with message shows from DBHelper, using static method foundOutSameTaskShowToast from this Fragment
            return;
        }

        if (AddTaskFragment.compareTwoDates(dayFrom, todaysDay) >= 0 )
        chosenDays.remove(0);// remove DayFrom

        chosenDays = new ArrayList<>(new LinkedHashSet<>(chosenDays));// delete duplicates in list, if exists
        boolean isTaskEdited =  DBHelper.editTask(writableDb, todaysDay, calledDay, taskId, taskText.getText().toString(), priority, chosenDays, isRepeatEveryMonth, checkedGoals, remindTime, getContext());

        /* if was some problem with editing show message and come out from method*/
        if (!isTaskEdited)
        {
            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskNotAddedInDB), Toast.LENGTH_LONG);
            return;
        }

/* If adding was successful show message
* wait 0.8 sec and back to previous fragment*/
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

        chosenDays.add(calledDay);
        /*If there are the same task in one of chosen day we prevent user and come out
        * i'm sending 'this' for sending Toast warning from DBHelper*/
        boolean checkTaskInDB = DBHelper.isTaskNameExistInThisDay(readableDb, this, taskText.getText().toString(), chosenDays, null);

        if (checkTaskInDB)
        {
            // Toast with message shows from DBHelper, using static method foundOutSameTaskShowToast from this Fragment
            return;
        }

        chosenDays = new ArrayList<>(new LinkedHashSet<>(chosenDays));// delete duplicates in list, if exists


        /* Adding new task
        * if something was wrong: show message for user and come out*/
        boolean isTaskAdded =  DBHelper.addTask(writableDb, taskText.getText().toString(), priority, chosenDays, calledDay, isRepeatEveryMonth, checkedGoals, remindTime, getContext());

        if (!isTaskAdded)
        {
            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskNotAddedInDB), Toast.LENGTH_LONG);
            return;
        }

/* If adding was successful show message
* wait 0.8 sec and back to previous activity*/
        Toast.makeText(getActivity(), getContext().getResources().getString(R.string.taskAddSuccessfully), Toast.LENGTH_LONG).show();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                getFragmentManager().popBackStack();// end this fragment and come back to previous
            }
        }, 800);

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

    public void foundOutSameTaskShowToast(String day)
    {
        initWeekDays();
        if (weekDays.contains(day))
        {
            int index = weekDays.indexOf(day);
            day = daysOfWeek[index];
        }

        else
            day = formatDate(day);

        Toast.makeText(getContext(), getContext().getResources().getString(R.string.taskAlreadyExistsInDB)
                + " " + day, Toast.LENGTH_LONG).show();


    }


    /*This method will be ask after user's clicking on DoneButton
     * it looking for taskAdd views values and put they in class fields
      * for adding to DB*/
    private boolean getTaskDataFromViews() {


        if (!taskTextValidator(taskText, getContext()))
            return false;

        try {
            priority = parseTaskPriority(prioritySpinner.getSelectedItem().toString());


        }
        catch (ExceptionInInitializerError e)
        {
            Toast.makeText(getActivity(),
                    getContext().getResources().getString(R.string.cantParseDataFromViewsException), Toast.LENGTH_LONG).show();

            return false;
        }


        return true;
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
                        mHour = hourOfDay;
                        mMinute = minute;
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
        args.putStringArrayList("checkedGoals", checkedGoals);
        DialogFragmentForGoals fragment = new DialogFragmentForGoals();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, 1);
        fragment.show(getFragmentManager(), fragment.getClass().getName());// show DialogFragment
    }

    /* This method get result from DialogFragments
     * requestCode 1 mean result from GoalsDialogFragment, contains string with chosen goals */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {
                case 1:
                    checkedGoals = data.getStringArrayListExtra("value");
                    break;
                case 2:
                    isRepeatEveryMonth = false;
                    getRepeatingValues(data.getStringExtra("value"));
                    break;
                case 3:
                    isRepeatEveryMonth = false;
                    checkedDays = data.getStringArrayListExtra("checkedValues");
                    getDaysFromWeekDaysDialog();break;

                case 4:
                    chosenDays.clear();
                    isRepeatEveryMonth = false;
                    dayFromCalendar = data.getStringExtra("dayFromCalendar");
                    chosenDays.add(dayFromCalendar);
                    repeatText.setText(formatDate(dayFromCalendar));
                    break;
                default:
                    break;
            }
        }
    }

    private void getDaysFromWeekDaysDialog() {
        textRepeatValue.setLength(0);

        chosenDays.clear();
        for (String day : checkedDays)
        {
            int position = Integer.parseInt(day)+1;
            chosenDays.add(weekDays.get(position));
            textRepeatValue.append(weekDaysShort[position] + ",");
        }

        if (textRepeatValue.length()>0) {
            textRepeatValue.setLength(textRepeatValue.length() - 1);// cut last "," from value
            repeatText.setText(textRepeatValue.toString());// set value for TextView
        }


    }



    public static Date getDateFromString(String calledDay, String dateformat) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            Date date = formatter.parse(calledDay);
            return date;
        }
        catch (ParseException e){return new Date();}

    }


    private void getRepeatingValues(String value) {
        int position = Integer.parseInt(value);
        chosenDays.clear();
        textRepeatValue.setLength(0);// clear StringBuilder

        switch (position)
        {
            // one time repeating
            case 0:
                chosenDays.add(calledDay);
                textRepeatValue.append(getResources().getString(R.string.repeatOnce));
                break;


            //repeat every day
            case 1:
                setAllDays();

                break;
            // add days from monday to friday (weekday)
            case 2:
                setWeekDays();

                break;
            // add weekends. Week starts from Sunday (1-st index)
            case 3:
                chosenDays.add(weekDays.get(1));
                chosenDays.add(weekDays.get(7));
                textRepeatValue.append( weekDaysShort[7] + ",");
                textRepeatValue.append(weekDaysShort[1]);
                break;
                /* case 4 not exist, because if you choose 4-th position in dialog fragment you will be thrown to another
                * dialog fragment where you can choose repeating days. And this fragment returns result with another code to another method
                * case 5 mean that user wanna repeating every week*/
            case 5:
                repeatEveryWeek();
                textRepeatValue.append(getContext().getResources().getString(R.string.repeatEveryWeek));
                break;

            case 6:
                isRepeatEveryMonth = true;
                textRepeatValue.append(getContext().getResources().getString(R.string.repeatEveryMonth));
                break;
        }

        repeatText.setText(textRepeatValue.toString());


    }

    private void setAllDays() {
        setWeekDays();
        chosenDays.add(weekDays.get(7));
        chosenDays.add(weekDays.get(1));

        textRepeatValue.append(", " + weekDaysShort[7] + ",");
        textRepeatValue.append(weekDaysShort[1]);
    }

    private void setWeekDays() {
        chosenDays.add(weekDays.get(2));
        chosenDays.add(weekDays.get(3));
        chosenDays.add(weekDays.get(4));
        chosenDays.add(weekDays.get(5));
        chosenDays.add(weekDays.get(6));

        textRepeatValue.append(weekDaysShort[2] + ",");
        textRepeatValue.append(weekDaysShort[3] + ",");
        textRepeatValue.append(weekDaysShort[4] + ",");
        textRepeatValue.append(weekDaysShort[5] + ",");
        textRepeatValue.append(weekDaysShort[6]);
    }


    private void repeatEveryWeek() {
        int dayOfWeek = getDayOfWeek(calledDay, Constants.DATEFORMAT);
        chosenDays.add(weekDays.get(dayOfWeek));
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