package com.eplan.yuraha.easyplanning;

import android.app.ActionBar;
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
import android.widget.Toast;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;
import com.eplan.yuraha.easyplanning.ListAdapters.Goal;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class GoalsActivity extends BaseActivity
        implements DoneGoalsListFragment.OnListFragmentInteractionListener,
InProgressGoalListFragment.OnFragmentInteractionListener,
        PopupMenu.OnMenuItemClickListener{
    private TabLayout  tabLayout;
    private ViewPager viewPager;
    private SQLiteDatabase readableDb ;
   private DoneGoalsListFragment doneGoalsListFragment;
    private InProgressGoalListFragment inProgressGoalListFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SPDatabase db = new SPDatabase(this);
        readableDb = db.getReadableDatabase();

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View contentView = inflater.inflate(R.layout.activity_goals, null, false);
        drawer.addView(contentView, 0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GoalsActivity.this, AddGoalActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);


        LayoutInflater mInflater=LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.tab_layout, null);
        tabLayout = (TabLayout) mCustomView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        appBarLayout.addView(mCustomView);


    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        doneGoalsListFragment = new DoneGoalsListFragment();
        inProgressGoalListFragment = new InProgressGoalListFragment(doneGoalsListFragment);
        adapter.addFragment(inProgressGoalListFragment, getResources().getString(R.string.inProgressGoalsTab));
        adapter.addFragment(doneGoalsListFragment, getResources().getString(R.string.doneGoalsTab));
        viewPager.setAdapter(adapter);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
/* requestCode =1 mean that new goal has been added
* requestCode =2 edit goal*/
        if (requestCode == 1) {
         String goalId =   data.getStringExtra("id");
            Goal goal = DBHelper.getGoalFromId(readableDb, goalId);
            inProgressGoalListFragment.adapter.goalsList.add(0, goal);
            inProgressGoalListFragment.adapter.dataHasBeenChanged();
        }


        if (requestCode == 2)
        {
            String goalId =  data.getStringExtra("goalId");
           int position = data.getIntExtra("positionInList", 0);
            System.out.println(position);
            Goal goal = DBHelper.getGoalFromId(readableDb, goalId);
           inProgressGoalListFragment.adapter.goalsList.remove(position);
           inProgressGoalListFragment.adapter.goalsList.add(position, goal);
            inProgressGoalListFragment.adapter.dataHasBeenChanged();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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
