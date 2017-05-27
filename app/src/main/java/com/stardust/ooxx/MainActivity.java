package com.stardust.ooxx;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.stardust.app.OnActivityResultDelegate;
import com.stardust.ooxx.module.Passage;
import com.stardust.ooxx.until.DrawableSaver;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    public static final String MESSAGE_SELECT_BG = "com.stardust.ooxx.MainActivity.MESSAGE_SELECT_BG";
    public static final String MESSAGE_RESET_BG = "com.stardust.ooxx.MainActivity.MESSAGE_RESET_BG";

    @ViewById(R.id.view_pager)
    ViewPager mViewPager;

    @ViewById(R.id.root)
    View mContentView;

    private DrawableSaver mBackgroundDrawableSaver;
    private OnActivityResultDelegate.Mediator mMediator = new OnActivityResultDelegate.Mediator();
    private int mColorAccent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = getTheme(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_theme), "0"));
        setTheme(theme);
        EventBus.getDefault().register(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, a, true);
        mColorAccent = a.data;
    }

    private int getTheme(String theme) {
        switch (theme) {
            case "white":
                return R.style.WhiteAppTheme;
            case "black":
                return R.style.BlackAppTheme;
            default:
                return R.style.AppTheme;
        }
    }

    @AfterViews
    public void setUpUI() {
        setUpBottomViewPager();
        setUpBackground();
    }

    private void setUpBackground() {
        mBackgroundDrawableSaver = new DrawableSaver(this, "bg", mContentView.getBackground()) {
            @Override
            protected void applyDrawableToView(final Drawable drawable) {
                mContentView.post(new Runnable() {
                    @Override
                    public void run() {
                        mContentView.setBackground(drawable);
                    }
                });
            }
        };
    }

    private void setUpBottomViewPager() {
        new BottomBarViewPagerHelper(getFragmentManager())
                .addBarWithPage(findViewById(R.id.bottom_bar_translate), new TranslationFragment_())
                .addBarWithPage(findViewById(R.id.bottom_bar_star), new StarListFragment_())
                .addBarWithPage(findViewById(R.id.bottom_bar_settings), new SettingsFragment_())
                .itemStateListener(new BottomBarViewPagerHelper.OnItemStateChangeListener() {
                    @Override
                    public void onItemSelected(View view, int position) {
                        ImageView image = (ImageView) view;
                        image.setColorFilter(mColorAccent);
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

    @Subscribe
    public void onMessageEvent(String message) {
        if (message.equals(MESSAGE_SELECT_BG)) {
            mBackgroundDrawableSaver.select(this, mMediator);
        } else if (message.equals(MESSAGE_RESET_BG)) {
            mBackgroundDrawableSaver.reset();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mMediator.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
