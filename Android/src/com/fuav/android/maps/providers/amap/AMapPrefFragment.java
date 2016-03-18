package com.fuav.android.maps.providers.amap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.amap.api.maps.AMap;
import com.fuav.android.R;
import com.fuav.android.dialogs.EditInputDialog;
import com.fuav.android.maps.providers.DPMapProvider;
import com.fuav.android.maps.providers.MapProviderPreferences;

/**
 * This is the google map provider preferences. It stores and handles all preferences related to google map.
 */
public class AMapPrefFragment extends MapProviderPreferences implements EditInputDialog.Listener {

    static final String MAP_TYPE_SATELLITE = "satellite";
    static final String MAP_TYPE_NORMAL = "normal";

    static final String PREF_MAP_TYPE = "pref_map_type";
    static final String DEFAULT_MAP_TYPE = MAP_TYPE_SATELLITE;

    public static class PrefManager {

         static int getMapType(Context context){
            int mapType ;
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String selectedType = sharedPref.getString(PREF_MAP_TYPE, DEFAULT_MAP_TYPE);
            switch(selectedType){
                case MAP_TYPE_NORMAL:
                    mapType = AMap.MAP_TYPE_NORMAL;
                    break;
                case MAP_TYPE_SATELLITE:
                    mapType = AMap.MAP_TYPE_SATELLITE;
                    break;
                default:
                    mapType = AMap.MAP_TYPE_SATELLITE;
            }

            return mapType;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_a_maps);
        setupPreferences();
    }

    private void setupPreferences() {
        Context context = getActivity().getApplicationContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        setupGaodeTileProviderPreferences(sharedPref);
    }

    private void setupGaodeTileProviderPreferences(SharedPreferences sharedPref) {
        String mapTypeKey = PREF_MAP_TYPE;
        final Preference mapTypePref = findPreference(mapTypeKey);
        mapTypePref.setSummary(sharedPref.getString(mapTypeKey, DEFAULT_MAP_TYPE));
        mapTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mapTypePref.setSummary(newValue.toString());
                return true;
            }
        });
    }


    @Override
    public void onOk(String dialogTag, CharSequence input) {

    }

    @Override
    public void onCancel(String dialogTag) {

    }

    @Override
    public DPMapProvider getMapProvider() {
        return DPMapProvider.高德地图;
    }
}
