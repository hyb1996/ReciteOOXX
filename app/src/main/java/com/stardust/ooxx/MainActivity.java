package com.stardust.ooxx;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.stardust.ooxx.module.Passage;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.view_pager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @AfterViews
    public void setUpUI() {
        new BottomBarViewPagerHelper(getSupportFragmentManager())
                .addBarWithPage(findViewById(R.id.bottom_bar_translate), new TranslationFragment_())
                .addBarWithPage(findViewById(R.id.bottom_bar_star), new StarListFragment_())
                .addBarWithPage(findViewById(R.id.bottom_bar_settings), new SettingsFragment_())
                .itemStateListener(new BottomBarViewPagerHelper.OnItemStateChangeListener() {
                    @Override
                    public void onItemSelected(View view, int position) {
                        ImageView image = (ImageView) view;
                        image.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorBottomBarSelected));
                    }

                    @Override
                    public void onItemUnselected(View view, int position) {
                        ImageView image = (ImageView) view;
                        image.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorBottomBarNormal));
                    }
                })
                .withViewPager(mViewPager)
                .setUp();
    }


    @Subscribe
    public void showPassage(Passage passage) {
        mViewPager.setCurrentItem(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
