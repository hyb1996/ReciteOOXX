package com.stardust.ooxx;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cunoraz.gifview.library.GifView;
import com.stardust.ooxx.module.EggVideo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.greenrobot.eventbus.EventBus;

import cn.jzvd.JZUserAction;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

/**
 * Created by Stardust on 2017/5/24.
 */
@EFragment(R.layout.fragment_settings)
public class SettingsFragment extends Fragment {

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.key_theme))) {
                getActivity().recreate();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    @AfterViews
    void setUpUI() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment, new PrefFragment())
                .commit();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    public static class PrefFragment extends PreferenceFragment {

        private int mClickCount;
        private Handler mHandler = new Handler();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference.getTitle().equals(getString(R.string.arr))) {
                Toast.makeText(getActivity(), R.string.lol, Toast.LENGTH_SHORT).show();
                showGif();
                return true;
            }
            if (preference.getTitle().equals(getString(R.string.djjzb))) {
                mClickCount++;
                if (mClickCount % 3 == 0 || mClickCount == 1) {
                    Toast.makeText(getActivity(), R.string.djjh, Toast.LENGTH_SHORT).show();
                } else if (mClickCount % 7 == 0) {
                    showVideo();
                }
                return true;
            }
            if (preference.getTitle().equals(getString(R.string.web_version))) {
                browse("https://faded12.github.io/conversion/");
                return true;
            }
            if (preference.getTitle().equals(getString(R.string.zhihu))) {
                browse("https://www.zhihu.com/question/27371173/answer/152263546");
                return true;
            }
            if (preference.getTitle().equals(getString(R.string.background))) {
                EventBus.getDefault().post(MainActivity.MESSAGE_SELECT_BG);
                return true;
            }
            if (preference.getTitle().equals(getString(R.string.reset_background))) {
                EventBus.getDefault().post(MainActivity.MESSAGE_RESET_BG);
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void showGif() {
            final GifView gifView = (GifView) getActivity().findViewById(R.id.dn);
            gifView.setVisibility(View.VISIBLE);
            gifView.play();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gifView.setVisibility(View.GONE);
                    gifView.pause();
                }
            }, 2700);
        }

        private void showVideo() {
            JzvdStd jzvdStd = new JzvdStd(getActivity());
            jzvdStd.setUp("file://" + EggVideo.getVideoFile(getActivity()), "恶龙咆哮", Jzvd.SCREEN_WINDOW_NORMAL);
            jzvdStd.thumbImageView.setImageResource(R.drawable.egg_video_thumb);
            final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .customView(jzvdStd, false)
                    .show();
            Jzvd.setJzUserAction(new JZUserAction() {
                @Override
                public void onEvent(int type, Object url, int screen, Object... objects) {
                    if (type == JZUserAction.ON_ENTER_FULLSCREEN) {
                        dialog.dismiss();
                    }
                }
            });
        }

        private void browse(String url) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Jzvd.setJzUserAction(null);
        }
    }

}
