package com.fuav.android.fragments.devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fuav.android.R;
import com.fuav.android.activities.FlightActivity;


public class DeviceSeraphiFragment extends Fragment {

    public DeviceSeraphiFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_device_seraphi, container, false);
        view.findViewById(R.id.imageViewLogo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FlightActivity.class));
            }
        });
        return view;
    }




}
