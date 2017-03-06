package com.eplan.yuraha.easyplanning;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SQLiteDatabase readableDb ;
    Context context;
    private static final long ID=1;
    private HashMap<String, String> prefRows;
    private OnFragmentInteractionListener mListener;

    public SettingsFragment(Context context) {
        this.context = context;
        prefRows = new HashMap<>();
        prefRows.put(Constants.PREF_TURN_ON_NOTIFICATIONS, Constants.DB_PREF_IS_SET_NOTIFICATIONS);
        prefRows.put(Constants.PREF_LANGUAGE, Constants.DB_PREF_LANGUAGE);
        prefRows.put(Constants.PREF_THEME, Constants.DB_PREF_THEME);
        prefRows.put(Constants.PREF_RINGTONE, Constants.DB_NOTIF_RINGTONE);
        prefRows.put(Constants.PREF_VIBRATION, Constants.DB_NOTIF_VIBRATION);
        prefRows.put(Constants.PREF_REMIND_ME, Constants.DB_NOTIF_IS_SET_REMINDTIME);
        prefRows.put(Constants.PREF_SET_REMIND_TIME, Constants.DB_NOTIF_REMIND_TIME);
    }

    public SettingsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

        addPreferencesFromResource(R.xml.preferences);
        SPDatabase db = new SPDatabase(getActivity());
        readableDb = db.getReadableDatabase();


    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      // setPreferencesFromDB();
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    /* This method will be useful for future synchronization
    * It can set preferences from DB in users gadget
    * Thats why user will be able to have the same preferences in whole gadgets
    * Warning: here are some fitch: method cant update preferences from this fragment immediately
    * because it calls after creating view, but if you will change place for this one - it will update invisibly for user*/

    private void setPreferencesFromDB() {
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Cursor cursor = readableDb.rawQuery("SELECT * FROM Preference" , null);
        if (cursor.moveToFirst()) {
            System.out.println(cursor.getString(1));
          String language = cursor.getString(1);
            String theme = cursor.getString(2);
            boolean isSetNotification = cursor.getInt(3) == 1 ? true : false;
            String ringtone = cursor.getString(4);
            boolean vibration = cursor.getInt(5) == 1 ? true : false;
            boolean isRemindTimeSet = cursor.getInt(6) == 1 ? true : false;
            String remindTime = cursor.getString(7);

           preferences.putString(Constants.PREF_LANGUAGE, language);
          preferences.putString(Constants.PREF_THEME, theme);
            preferences.putBoolean(Constants.PREF_TURN_ON_NOTIFICATIONS, isSetNotification);
           preferences.putBoolean(Constants.PREF_VIBRATION, vibration);
            preferences.putBoolean(Constants.PREF_REMIND_ME, isRemindTimeSet);
            preferences.putString(Constants.PREF_SET_REMIND_TIME, remindTime);

         Preference ringtonePref = findPreference(Constants.PREF_RINGTONE);
         ringtonePref.setDefaultValue(Uri.parse(ringtone));
        }

          preferences.apply();
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

    /* Two bellow methods calls when user changes some preferences
     * they update information in DB after every change have done (in runtime)
      * they calls for single preference, after its changing by user
      *
      * I created they for future synchronization */

    //this method is useful only for ringtone preference. It not react on others preferences
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      DBHelper.updatePreferences(readableDb, Constants.DB_PREF_TABLE, Constants.DB_NOTIF_RINGTONE, newValue.toString(), ID);
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
String value = "0", tableRow;

        if (key.equals(Constants.PREF_VIBRATION) || key.equals(Constants.PREF_REMIND_ME)||
                key.equals(Constants.PREF_TURN_ON_NOTIFICATIONS)) {
            boolean check =  sharedPreferences.getBoolean(key, true);
            if (check)
                value = "1";

        }
        else
        {
            value = sharedPreferences.getString(key, "");
        }

        tableRow = prefRows.get(key);
        DBHelper.updatePreferences(readableDb, Constants.DB_PREF_TABLE, tableRow, value, ID);
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

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        RingtonePreference pref = (RingtonePreference) findPreference(Constants.PREF_RINGTONE);
        pref.setOnPreferenceChangeListener(this);
    }

    @Override
    public  void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
