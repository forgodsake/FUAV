package com.fuav.android.fragments.widget;


import android.content.Intent;

import com.fuav.android.activities.FlightActivity;
import com.fuav.android.fragments.FlightMapFragment;
import com.fuav.android.maps.DPMap;
import com.o3dr.services.android.lib.coordinate.LatLong;


public class MiniMapFragment extends FlightMapFragment implements DPMap.OnMapClickListener{


    public MiniMapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onMapClick(LatLong coord) {
        startActivity(new Intent(getActivity(), FlightActivity.class));
    }
}
