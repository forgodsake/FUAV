package com.fuav.android.fragments.devices;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fuav.android.R;


public class DeviceSmargleFragment extends Fragment implements View.OnClickListener{

    public DeviceSmargleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_device_smargle, container, false);

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
                Toast.makeText(getActivity(),"即将发布",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
