package com.eplan.yuraha.easyplanning;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends BaseActivity implements
        PopupMenu.OnMenuItemClickListener,
        AddTaskFragment.OnFragmentInteractionListener,
        TaskListFragment.OnFragmentInteractionListener
{


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        final View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        TaskListFragment taskFragment = new TaskListFragment(this, fab);//create new fragment
        Bundle bundle = new Bundle();
        ft.replace(R.id.addTaskFrame, taskFragment, "fr1");
        ft.addToBackStack("fr1");
        ft.commit();


    }


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



    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
