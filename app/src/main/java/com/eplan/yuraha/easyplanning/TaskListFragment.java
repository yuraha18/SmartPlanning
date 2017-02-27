package com.eplan.yuraha.easyplanning;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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


public class TaskListFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private AppCompatActivity activity;


    ArrayList<Task> tasksList;
    TasksListViewAdapter listViewAdapter;
    private SQLiteDatabase readableDb ;
    ListView listView;


    Spinner materialBetterSpinner ;
    String[] SPINNER_DATA = {"Today's tasks","Завдання на завтра","Вибрати день"};

    private OnFragmentInteractionListener mListener;

    public TaskListFragment(AppCompatActivity activity) {
        this.activity = activity;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task, container, false);


        materialBetterSpinner = (Spinner) view.findViewById(R.id.day_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.spinner_item, SPINNER_DATA);
        materialBetterSpinner.setAdapter(adapter);

        listView = (ListView) view.findViewById(R.id.tasksListView);
        fillInTasksList("28-2-2017");


       listViewAdapter=new TasksListViewAdapter (inflater, tasksList, readableDb, activity, getContext());
        listView.setAdapter(listViewAdapter);


        return view;
    }

    private void fillInTasksList(String day) {
        try {
            tasksList = DBHelper.getAllTasksFromDay(readableDb, day);
        }
        catch (Exception e)
        {

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
