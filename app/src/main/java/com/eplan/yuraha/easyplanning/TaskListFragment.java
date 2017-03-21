package com.eplan.yuraha.easyplanning;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.ListAdapters.Goal;
import com.eplan.yuraha.easyplanning.ListAdapters.InProgressListViewAdapter;
import com.eplan.yuraha.easyplanning.ListAdapters.Task;
import com.eplan.yuraha.easyplanning.ListAdapters.TasksListViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class TaskListFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private AppCompatActivity activity;

    public static final String dateFormat = "dd-MM-yyyy";
    ArrayList<Task> tasksList;
    TasksListViewAdapter listViewAdapter;
    private SQLiteDatabase readableDb ;
    ListView listView;
    private   String dayFromSpinner;
    private  ArrayAdapter<String> adapter;
    private int currentItem;
    private FloatingActionButton fab;
    private RatingBar ratingBar;

    public static String[] weekDays = new String[]{"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};


    Spinner materialBetterSpinner ;
    ArrayList<String> spinnerDatalist ;
    String searchQuery ;


    private OnFragmentInteractionListener mListener;

    public TaskListFragment(AppCompatActivity activity, FloatingActionButton fab, String searchQuery) {
        this.activity = activity;
        this.fab = fab;
        this.searchQuery = searchQuery;
    }

    public TaskListFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InProgressGoalsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskListFragment newInstance(String param1, String param2) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public String getDayFromSpinner() {
        return dayFromSpinner;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        SPDatabase db = new SPDatabase(getActivity());
        readableDb = db.getReadableDatabase();
        tasksList = new ArrayList<>();
        spinnerDatalist = new ArrayList<>();
        spinnerDatalist.add(getActivity().getResources().getString(R.string.todaysTasks));
        spinnerDatalist.add(getContext().getResources().getString(R.string.tomorrowsTasks));
        spinnerDatalist.add(getContext().getResources().getString(R.string.chooseDay));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task, container, false);


        materialBetterSpinner = (Spinner) view.findViewById(R.id.day_spinner);

        adapter = new ArrayAdapter<>(activity, R.layout.spinner_item, spinnerDatalist);
        materialBetterSpinner.setAdapter(adapter);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        if (searchQuery!=null && !adapter.getItem(0).equals(getResources().getString(R.string.search))) {
            adapter.insert(getResources().getString(R.string.search), 0);
            materialBetterSpinner.setSelection(0);
        }


dayFromSpinner = getTodaysDay(); // default value
        materialBetterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (searchQuery!=null)
                    setSwitchForSearch(position);

                else
                {
                    switch (position)
                    {
                        case 0:
                            dayFromSpinner = getTodaysDay();
                            break;
                        case 1:
                            dayFromSpinner = getTomorrowsDay();
                            break;
                        case 2:
                            openCalendarForSpinner();
                            break;

                        default:
                            dayFromSpinner = adapter.getItem(position);
                    }
                    setDayRating();//update day rating in ratingBar
                }


                tasksList.clear();


                if (searchQuery==null)
                fillInTasksList(dayFromSpinner);

                else {
                    fillInTasksListFromSearchQuery();
                }

                listViewAdapter.setNewData(tasksList);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listView = (ListView) view.findViewById(R.id.tasksListView);

        if (searchQuery!=null)
            fillInTasksListFromSearchQuery();

        boolean isSearch = false;
        if (searchQuery!=null)
            isSearch = true;

       listViewAdapter=new TasksListViewAdapter (this, inflater, tasksList, readableDb, activity, getContext(), isSearch, searchQuery, fab);
        listView.setAdapter(listViewAdapter);

        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar_small);
        setDayRating();

        initFabButton();



        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (searchQuery==null)
         setDayRating();

        fab.setVisibility(View.VISIBLE);
    }

    private void initFabButton()
    {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchQuery!=null)
                removeSearchFromSpinner();

                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                AddTaskFragment taskFragment = new AddTaskFragment(activity);//create new fragment
                Bundle bundle = new Bundle();
                bundle.putString("calledDay", dayFromSpinner);//create data for sending with fragment
                taskFragment.setArguments(bundle);// sending is
                ft.replace(R.id.addTaskFrame, taskFragment, "fr1");
                ft.addToBackStack("fr2");
                ft.commit();
                fab.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setSwitchForSearch(int position) {


        if (position >= 1) {
            removeSearchFromSpinner();
            position-=1;
            materialBetterSpinner.setSelection(position);
            setDayRating();//update day rating in ratingBar
            listViewAdapter.isSearch = false;
            listViewAdapter.setNewData(tasksList);
        }


    }

    private void fillInTasksListFromSearchQuery() {
        dayFromSpinner = getTodaysDay();//set default value
        tasksList = fillInTaskListForSearchQuery(readableDb, searchQuery);
    }

    public static ArrayList<Task> fillInTaskListForSearchQuery(SQLiteDatabase readableDb, String searchQuery) {
        ArrayList<Task> taskList = new ArrayList<>();

        try {
            Cursor cursor = DBHelper.getAllTaskNames(readableDb, searchQuery);
            if (cursor .moveToFirst()) {
                while (cursor.isAfterLast() == false) {
                    long taskId = cursor.getLong(cursor.getColumnIndex("_id"));
                    Task task = DBHelper.getTaskFromId(readableDb, taskId);
                    String dayTo = DBHelper.getDayToByTaskId(readableDb, taskId + "");
                    if (dayTo.equals("-1"))
                        taskList.add(task);

                    cursor.moveToNext();
                }

                cursor.close();
            }
        }
        catch (Exception e){}

        return taskList;
    }

    private void removeSearchFromSpinner()
    {
        searchQuery=null;
        adapter.notifyDataSetChanged();
        Object spinnerItem = adapter.getItem(0);
        adapter.remove((String)spinnerItem);
        adapter.notifyDataSetChanged();
    }

    public static String getTomorrowsDay() {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        int mYear = calendar.get(Calendar.YEAR);
        int  mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        return mDay + "-"
                + (mMonth + 1) + "-" + mYear;
    }

    private void openCalendarForSpinner() {

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
                        dayFromSpinner = dayOfMonth + "-"
                                + (monthOfYear + 1) + "-" + year;
insertDayFromSpinnerInAdapter();
                        adapter.notifyDataSetChanged();

                        materialBetterSpinner.setSelection(3);
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    private void insertDayFromSpinnerInAdapter() {

        if (dayFromSpinner==null)
        return;

        if (adapter.getCount()<4)
            adapter.insert(dayFromSpinner, 3);// i always set new date in spinner in 3 position

        else
        {
            if (dayFromSpinner.equals(adapter.getItem(3)))
                return;

            adapter.insert(dayFromSpinner, 3);
        }

    }


    public static String getTodaysDay() {
        final Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int  mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        return mDay + "-"
                + (mMonth + 1) + "-" + mYear;

    }

    private void fillInTasksList(String day) {
        Date before  = new Date();
        try {

            int dayOfWeek = AddTaskFragment.getDayOfWeek(day, dateFormat);
            System.out.println(day);
            tasksList = DBHelper.getAllTasksFromDay(readableDb, day, weekDays[dayOfWeek]);
        }
        catch (Exception e)
        {
        }
        Date after  = new Date();
        System.out.println("fill in time" + (after.getTime() - before.getTime()));

    }

    public void setDayRating()
    {
        float rating;
        if (searchQuery != null)
            rating =0;

        else {
            DayStatistic dayStatistic = DBHelper.getStatisticForDay(readableDb, dayFromSpinner);
            int countDone = dayStatistic.getCountDone();
            int countNotDone = dayStatistic.getCountInProgress();
            System.out.println(dayFromSpinner);
            float all = (countDone + countNotDone)*1f;
            rating = (countDone/all)*5;
        }

        ratingBar.setRating(rating);

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}


