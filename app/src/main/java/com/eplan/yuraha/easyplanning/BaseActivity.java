package com.eplan.yuraha.easyplanning;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Yura on 09.02.2017.
 */

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    protected ArrayList<String> taskTexts;

    protected DrawerLayout drawer;
    private SearchCursorAdapter adapter;
    protected Toolbar toolbar;
    protected ActionBarDrawerToggle toggle;
    protected AppBarLayout appBarLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nav_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);

appBarLayout =  (AppBarLayout) findViewById(R.id.appBar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
         toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);

        navigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                } else {
                    //show hamburger
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toggle.syncState();
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            drawer.openDrawer(GravityCompat.START);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        initSearch(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    /* search initialize by data only if user click on search view*/
    private void initSearch(Menu menu) {
       final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        myActionMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                createSearchQueries(myActionMenuItem);
                return true;
            }
        });


    }

    private void createSearchQueries(MenuItem myActionMenuItem) {

        fillInTaskList();
        SPDatabase spDatabase = new SPDatabase(this);
        final SQLiteDatabase db = spDatabase.getReadableDatabase();

        Cursor cursor = DBHelper.getAllTaskNames(db, "");

        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();

        adapter = new SearchCursorAdapter(this, cursor, 0);

        searchView.setSuggestionsAdapter(adapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("searchQuery", query);
                startActivity(intent);
                db.close();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.length()>=2) {
                    adapter.setFilterQueryProvider(new FilterQueryProvider() {
                        @Override
                        public Cursor runQuery(CharSequence constraint) {
                            return DBHelper.getAllTaskNames(db, constraint.toString());
                        }
                    });
                }
                return false;
            }
        });
    }

    private void fillInTaskList() {

        SPDatabase spDb = new SPDatabase(getApplicationContext());
        taskTexts = DBHelper.getAllTasksForAutoComplete(spDb.getReadableDatabase());

        spDb.close();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_statistic) {
            Intent intent = new Intent(BaseActivity.this, StatisticActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(BaseActivity.this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_goals) {
            Intent intent = new Intent(BaseActivity.this, GoalsActivity.class);
            startActivity(intent);

        } else if (id == R.id.go_to_my_tasks) {
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about_us) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
