package com.eplan.yuraha.easyplanning;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

import java.util.ArrayList;
import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TaskFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    List<String> allGoals;

    {
        allGoals = new ArrayList<>();
        allGoals.add("hastag13444");
        allGoals.add("doing_something_here");
        allGoals.add("one more tag");
        allGoals.add("bla-bla-bla");
    }

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
private View view = null;
    private SQLiteDatabase writableDb ;
    private SQLiteDatabase readableDb ;

    private OnFragmentInteractionListener mListener;

    public TaskFragment() {
        this.setRetainInstance(true);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskFragment newInstance(String param1, String param2) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private static String FRAGMENT_INSTANCE_NAME = "fragment";
    TaskFragment fragment = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        setHasOptionsMenu(true);
        SPDatabase db = new SPDatabase(getActivity());
        writableDb = db.getWritableDatabase();
        readableDb = db.getReadableDatabase();


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);


        view = inflater.inflate(R.layout.fragment_task, container, false);
       String taskId = getArguments().getString("id");
        setDataFromDB(taskId);
        ImageView popupButton = (ImageView) view.findViewById(R.id.action_more_fragment);
        // Ставим на неё "слушатель клика"
        popupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });



        TagContainerLayout mTagContainerLayout = (TagContainerLayout) view.findViewById(R.id.tagcontainerLayout);
        mTagContainerLayout.setTags(allGoals);
        return view;
    }

    private void setDataFromDB(String taskId) {
        Cursor cursor = readableDb.query("Tasks",
                new String[]{"TASK_TEXT","PRIORITY"},
                "_id = ?",
                new String[]{taskId},
                null, null, null);

        TextView taskText = (TextView) view.findViewById(R.id.task_text);
        ImageView priorityBell = (ImageView) view.findViewById(R.id.priority_bell);
        int priority = 0;
        if (cursor.moveToFirst())
        {
          taskText.setText(cursor.getString(0));
            priority = cursor.getInt(1);
        }

        switch (priority)
        {
            case 1: priorityBell.setColorFilter(ContextCompat.getColor(getContext(),R.color.lowPriority));break;
            case 2:  priorityBell.setColorFilter(ContextCompat.getColor(getContext(),R.color.middlePriority));break;
            case 3:  priorityBell.setColorFilter(ContextCompat.getColor(getContext(),R.color.highPriority));break;
        }


    }

    public void showPopupMenu(View v) {
        try {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) getActivity());

            // Ссылка на файл res/menu/popup_menu.xml
            popup.inflate(R.menu.task_fragment_menu);
            popup.show();
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

