package com.fuav.android.fragments.devices;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fuav.android.R;
import com.fuav.android.activities.EditorActivity;
import com.fuav.android.activities.helpers.SuperUI;
import com.fuav.android.core.drone.DroneManager;
import com.fuav.android.fragments.helpers.ApiListenerFragment;


public class DeviceSeraphiFragment extends ApiListenerFragment implements View.OnClickListener{



    public DeviceSeraphiFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_device_seraphi, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.imageViewLogo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.imageViewLogo:
                if(DroneManager.getDrone()==null){
                    ((SuperUI) getActivity()).toggleDroneConnection();
                }
                startActivity(new Intent(getActivity(), EditorActivity.class));
                break;
        }
    }


    @Override
    public void onApiConnected() {
        
    }

    @Override
    public void onApiDisconnected() {

    }
}
