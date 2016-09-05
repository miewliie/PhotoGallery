package com.augmentis.ayp.photogallery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

/**
 * Created by Apinya on 9/5/2016.
 */
public class PhotoSettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "PhotoSettingFragment";

        public static PhotoSettingFragment newInstance(){

            Bundle args = new Bundle();
            PhotoSettingFragment fragment = new PhotoSettingFragment();
            fragment.setArguments(args);
            return fragment;
        }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_photo_setting);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        onSharedPreferenceChanged(sharedPreferences, "use_gps");

    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {

        Boolean useGPS = PhotoGalleryPreference.getUseGPS(getActivity());
        Log.d(TAG, "" + useGPS);

        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "On shared preference changed");

        Preference preference = findPreference(key);

        if(preference instanceof SwitchPreferenceCompat){
            boolean value = sharedPreferences.getBoolean(key, false);
            preference.setSummary( value ? "ON" : "OFF" );

        }else {
            preference.setSummary(sharedPreferences.getString(key, ""));
        }

    }
}