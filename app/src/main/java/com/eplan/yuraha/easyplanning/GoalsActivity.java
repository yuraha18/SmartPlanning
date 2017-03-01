package com.eplan.yuraha.easyplanning;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.ListAdapters.Goal;
import com.eplan.yuraha.easyplanning.ListAdapters.ViewPagerAdapter;


public class GoalsActivity extends BaseActivity
        implements DoneGoalsListFragment.OnListFragmentInteractionListener,
InProgressGoalListFragment.OnFragmentInteractionListener,
GoalListsFragment.OnFragmentInteractionListener,
        PopupMenu.OnMenuItemClickListener{

    private GoalListsFragment goalListsFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View contentView = inflater.inflate(R.layout.activity_goals, null, false);
        drawer.addView(contentView, 0);



        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        goalListsFragment = new GoalListsFragment(appBarLayout, this);//create new fragment
        ft.add(R.id.addGoalFrame, goalListsFragment, "goalsList");
        ft.addToBackStack("goalsList");
        ft.commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                AddGoalFragment taskFragment = new AddGoalFragment();//create new fragment
                Bundle bundle = new Bundle();
                bundle.putBoolean("isEdit", false);//create data for sending with fragment
                taskFragment.setArguments(bundle);// sending is
                taskFragment.setTargetFragment(goalListsFragment, 1);
                ft.replace(R.id.addGoalFrame, taskFragment, "AddGoal");
                ft.addToBackStack("AddGoal");
                ft.commit();

            }
        });




    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    /* create listener for popup menu in fragment goal
    * without it menu never shows*/
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try {
            switch (item.getItemId()) {
                //Если выбран пункт меню "В документах"
                case R.id.action_settings:
                    // Вызываем наш "демо-метод" (см. п. 6)... и т.д.
                    return true;
                default:
                    return false;
            }
        }
        catch ( Exception e)
        {
            return false;
        }

    }


}
