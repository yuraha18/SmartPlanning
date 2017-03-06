package com.eplan.yuraha.easyplanning;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

public class SettingsActivity extends BaseActivity implements
        SettingsFragment.OnFragmentInteractionListener
        {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        final View contentView = inflater.inflate(R.layout.activity_settings, null, false);
        drawer.addView(contentView, 0);

        getFragmentManager().beginTransaction()
                .replace(R.id.activity_settings, new SettingsFragment(this)).commit();


    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


        }
