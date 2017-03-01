package com.eplan.yuraha.easyplanning.ListAdapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import java.util.ArrayList;
import java.util.List;

/* I had been extended here FragmentPagerAdapter before
* which save listView after its lose control (adding or editing goal or task)
* that's why I had must "speaking" with fragments and refresh list by hands
 * Although I have found out FragmentStatePagerAdapter always destroy ListView
 * after its losing control, that's why I haven't any problems with refreshing
 * data are new always
 * */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {


    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

}