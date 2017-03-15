package com.example.android.yrestaurants;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Kevin on 3/13/2017.
 */

public class SimpleFPAdapter extends FragmentPagerAdapter {

    /** Context of the app */
    private Context mContext;

    public SimpleFPAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    // Have 2 fragments to show
    @Override
    public int getCount() {
        return 2;
    }

    // Create each fragment associated with the position
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new RestaurantsFragment();
        }
        else {
            return new FavouritesFragment();
        }
    }

    // Get the title names for each tap
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Restaurants";
        }
        else {
            return "Favourites";
        }
    }
}
