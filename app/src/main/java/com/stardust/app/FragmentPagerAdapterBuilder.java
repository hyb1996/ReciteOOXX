package com.stardust.app;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stardust on 2017/5/24.
 */

public class FragmentPagerAdapterBuilder {

    private List<Fragment> mFragmentList = new ArrayList<>();
    private FragmentManager mFragmentManager;

    public FragmentPagerAdapterBuilder(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public FragmentPagerAdapterBuilder addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
        return this;
    }

    public FragmentPagerAdapter build() {
        return new FragmentPagerAdapter(mFragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList.size();
            }
        };
    }


}
