package com.stardust.ooxx;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.stardust.ooxx.until.DrawableSaver;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.greenrobot.eventbus.EventBus;

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

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference.getTitle().equals(getString(R.string.arr))) {
                Toast.makeText(getActivity(), R.string.lol, Toast.LENGTH_SHORT).show();
                return true;
            }
            if (preference.getTitle().equals(getString(R.string.djjzb))) {
                Toast.makeText(getActivity(), R.string.djjh, Toast.LENGTH_SHORT).show();
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

        private void browse(String url) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

}
