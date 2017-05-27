package com.stardust.ooxx;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stardust on 2017/5/23.
 */

public class BottomBarViewPagerHelper {

    private FragmentManager mFragmentManager;

    public interface OnItemStateChangeListener {
        void onItemSelected(View view, int position);

        void onItemUnselected(View view, int position);
    }

    private List<View> mBottomBars = new ArrayList<>();
    private List<Fragment> mFragments = new ArrayList<>();
    private ViewPager mViewPager;
    private int mSelectedPosition;
    private OnItemStateChangeListener mOnItemStateChangeListener;

    public BottomBarViewPagerHelper(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public BottomBarViewPagerHelper addBarWithPage(View bottomBarView, Fragment pageFragment) {
        final int position = mBottomBars.size();
        mBottomBars.add(bottomBarView);
        mFragments.add(pageFragment);
        bottomBarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(position);
            }
        });
        return this;
    }

    public BottomBarViewPagerHelper withViewPager(ViewPager pager) {
        mViewPager = pager;
        return this;
    }

    public BottomBarViewPagerHelper itemStateListener(OnItemStateChangeListener listener) {
        mOnItemStateChangeListener = listener;
        return this;
    }

    public void setUp() {
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (mOnItemStateChangeListener != null) {
                    mOnItemStateChangeListener.onItemUnselected(mBottomBars.get(mSelectedPosition), mSelectedPosition);
                    mOnItemStateChangeListener.onItemSelected(mBottomBars.get(position), position);
                }
                mSelectedPosition = position;
            }

        });
        mViewPager.setAdapter(new FragmentPagerAdapter(mFragmentManager) {

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }
        });
    }


}
