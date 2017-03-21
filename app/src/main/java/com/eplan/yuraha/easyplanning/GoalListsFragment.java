package com.eplan.yuraha.easyplanning;


import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.ListAdapters.Goal;
import com.eplan.yuraha.easyplanning.ListAdapters.ViewPagerAdapter;

/**
 * Created by yuraha18 on 2/28/2017.
 */

public class GoalListsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SQLiteDatabase readableDb ;
    View mCustomView;
    GoalsActivity activity;
    private DoneGoalsListFragment doneGoalsListFragment;
    private InProgressGoalListFragment inProgressGoalListFragment;
    ViewGroup appBarLayout;
    FloatingActionButton fab;

    private OnFragmentInteractionListener mListener;

    public GoalListsFragment(AppBarLayout appBarLayout, GoalsActivity goalsActivity, FloatingActionButton fab)
    {
        this.appBarLayout = appBarLayout;
this.activity = goalsActivity;
        this.fab = fab;
    }

    public GoalListsFragment()
    {

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
    public static GoalListsFragment newInstance(String param1, String param2) {
        GoalListsFragment fragment = new GoalListsFragment();
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

        View contentView = inflater.inflate(R.layout.goal_list_fragment, null, false);
        viewPager = (ViewPager) contentView.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        LayoutInflater mInflater=LayoutInflater.from(getActivity());
        mCustomView = mInflater.inflate(R.layout.tab_layout, null);
        tabLayout = (TabLayout) mCustomView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        appBarLayout.addView(mCustomView);

        return contentView;

    }

    private void setupViewPager(ViewPager viewPager) {
        doneGoalsListFragment = new DoneGoalsListFragment();
        inProgressGoalListFragment = new InProgressGoalListFragment(doneGoalsListFragment, activity, fab);

        ViewPagerAdapter adapter = new ViewPagerAdapter(activity.getSupportFragmentManager());
        adapter.addFragment(inProgressGoalListFragment, getResources().getString(R.string.inProgressGoalsTab));
        adapter.addFragment(doneGoalsListFragment, getResources().getString(R.string.doneGoalsTab));

        viewPager.setAdapter(adapter);
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

    @Override
    public void onPause() {
        super.onPause();
        appBarLayout.removeView(mCustomView);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupViewPager(viewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        fab.setVisibility(View.VISIBLE);
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
