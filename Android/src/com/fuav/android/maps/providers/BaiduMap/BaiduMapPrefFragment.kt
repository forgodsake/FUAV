package com.fuav.android.maps.providers.google_map

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceManager
import android.text.TextUtils
import android.widget.Toast
import com.baidu.mapapi.map.BaiduMap
import com.google.android.gms.maps.GoogleMap
import com.fuav.android.R
import com.fuav.android.dialogs.EditInputDialog
import com.fuav.android.maps.providers.DPMapProvider
import com.fuav.android.maps.providers.MapProviderPreferences
import com.fuav.android.maps.providers.google_map.GoogleMapPrefConstants.GOOGLE_TILE_PROVIDER
import com.fuav.android.maps.providers.google_map.GoogleMapPrefConstants.TileProvider

/**
 * This is the google map provider preferences. It stores and handles all preferences related to google map.
 */
public class BaiduMapPrefFragment : MapProviderPreferences(), EditInputDialog.Listener {

    override fun onOk(dialogTag: String?, input: CharSequence?) {
        throw UnsupportedOperationException()
    }

    companion object PrefManager {


        val MAP_TYPE_SATELLITE = "satellite"
        val MAP_TYPE_NORMAL = "normal"

        val PREF_MAP_TYPE = "pref_map_type"
        val DEFAULT_MAP_TYPE = MAP_TYPE_SATELLITE

        fun getMapType(context: Context?): Int {
            var mapType = GoogleMap.MAP_TYPE_SATELLITE
            context?.let {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                val selectedType = sharedPref.getString(PREF_MAP_TYPE, DEFAULT_MAP_TYPE)
                when(selectedType){
                    MAP_TYPE_NORMAL -> mapType = BaiduMap.MAP_TYPE_NORMAL
                    MAP_TYPE_SATELLITE -> mapType = BaiduMap.MAP_TYPE_SATELLITE
                    else -> mapType = GoogleMap.MAP_TYPE_SATELLITE
                }
            }

            return mapType
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences_baidu_maps)
        setupPreferences()
    }

    private fun setupPreferences() {
        val context = activity.applicationContext
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        setupGoogleTileProviderPreferences(sharedPref)
    }


    override fun onCancel(dialogTag: String) {}



    private fun setupGoogleTileProviderPreferences(sharedPref: SharedPreferences) {
        val mapTypeKey = PREF_MAP_TYPE
        val mapTypePref = findPreference(mapTypeKey)
        mapTypePref?.let {
            mapTypePref.summary = sharedPref.getString(mapTypeKey, DEFAULT_MAP_TYPE)
            mapTypePref.setOnPreferenceChangeListener { preference, newValue ->
                    mapTypePref.summary = newValue.toString()
                    true
            }
        }
    }


    override fun getMapProvider(): DPMapProvider? = DPMapProvider.百度地图
}
