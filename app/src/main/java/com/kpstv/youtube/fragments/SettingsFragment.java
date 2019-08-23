package com.kpstv.youtube.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import com.kpstv.youtube.R;
import com.kpstv.youtube.utils.YTutils;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("appSettings");
        addPreferencesFromResource(R.xml.settings);

        Preference update = findPreference("pref_update");
        update.setOnPreferenceClickListener(preference -> {
            new YTutils.CheckForUpdates(getActivity(),false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return false;
        });
    }
}
