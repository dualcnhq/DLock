package com.dualcnhq.sherlocked.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dualcnhq.sherlocked.fragments.CityListFragment;
import com.dualcnhq.sherlocked.fragments.PostFragment;
import com.dualcnhq.sherlocked.fragments.PrimaryContactFragment;

public class TabFragmentAdapter extends FragmentPagerAdapter {

    private static int NUM_ITEMS = 3;

    private String [] titles = {"Primary Contact", "City List", "Posts"};

    public TabFragmentAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return PrimaryContactFragment.newInstance();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return CityListFragment.newInstance();
            case 2: // Fragment # 1 - This will show SecondFragment
                return PostFragment.newInstance();
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

}