package com.eplan.yuraha.easyplanning;


import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends BaseActivity implements
        TaskFragment.OnFragmentInteractionListener,
        PopupMenu.OnMenuItemClickListener{

    Spinner materialBetterSpinner ;

    String[] SPINNER_DATA = {"Завдання на сьогодні","Завдання на завтра","Вибрати день"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AppBarLayout relativeLayout = (AppBarLayout) findViewById(R.id.appBar);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);


        // Begin the transaction
       FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        TaskFragment taskFragment = new TaskFragment();//create new fragment
        Bundle bundle = new Bundle();
        bundle.putString("id", "1");//create data for sending with fragment
        taskFragment.setArguments(bundle);// sending is
        ft.replace(R.id.taskFragments, taskFragment);

// or ft.add(R.id.your_placeholder, new FooFragment());
// Complete the changes added above
        ft.commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTask.class);
                startActivity(intent);
            }
        });






        materialBetterSpinner = (Spinner)findViewById(R.id.day_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item, SPINNER_DATA);
        materialBetterSpinner.setAdapter(adapter);



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
