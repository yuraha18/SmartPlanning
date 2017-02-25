package com.eplan.yuraha.easyplanning;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.ListAdapters.DoneGoalsListViewAdapter;
import com.eplan.yuraha.easyplanning.ListAdapters.Goal;


import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DoneGoalsListFragment extends ListFragment {

    private ArrayList<Goal> goalsList;
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private SQLiteDatabase writableDb ;
    public DoneGoalsListViewAdapter adapter;


    private SQLiteDatabase readableDb ;
    private OnListFragmentInteractionListener mListener;


    public DoneGoalsListFragment() {

    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DoneGoalsListFragment newInstance(int columnCount) {
        DoneGoalsListFragment fragment = new DoneGoalsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }


        SPDatabase db = new SPDatabase(getActivity());
        readableDb = db.getReadableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        ListView listView = (ListView) view.findViewById(R.id.goalsListView);
        fillInGoalsList();

         adapter=new DoneGoalsListViewAdapter(inflater, goalsList, readableDb, getContext());
        listView.setAdapter(adapter);


        return view;
    }


    private void fillInGoalsList() {
        try {
            goalsList = DBHelper.getDoneGoalsList(readableDb);
        }
        catch (Exception e){}

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name

    }


}


