package com.eplan.yuraha.easyplanning;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.ListAdapters.Goal;
import com.eplan.yuraha.easyplanning.ListAdapters.InProgressListViewAdapter;

import java.util.ArrayList;


public class InProgressGoalListFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<Goal> goalsList;
    private DoneGoalsListFragment doneGoalsListFragment;

    private SQLiteDatabase readableDb ;
    InProgressListViewAdapter adapter;

    private OnFragmentInteractionListener mListener;

    public InProgressGoalListFragment(DoneGoalsListFragment doneGoalsListFragment) {
        this.doneGoalsListFragment = doneGoalsListFragment;
    }

    public InProgressGoalListFragment() {
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
    public static InProgressGoalListFragment newInstance(String param1, String param2) {
        InProgressGoalListFragment fragment = new InProgressGoalListFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        ListView listView = (ListView) view.findViewById(R.id.goalsListView);
        fillInGoalsList();

        adapter=new InProgressListViewAdapter(inflater, goalsList, readableDb, getContext(), doneGoalsListFragment, getActivity());
        listView.setAdapter(adapter);


        return view;
    }

    private void fillInGoalsList() {
        try {
            goalsList = DBHelper.getInProgressGoalsList(readableDb);

        }
        catch (Exception e){}

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
