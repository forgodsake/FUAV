package com.fuav.android.fragments.widget;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fuav.android.R;
import com.fuav.android.dialogs.SlideToUnlockDialog;
import com.fuav.android.fragments.control.BaseFlightControlFragment;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoControlFragment extends BaseFlightControlFragment {


    private Button button_take_off;
    private Button button_go_home;
    private Button button_hover;
    private Button button_follow_me;
    private int index= 0;


    public VideoControlFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_control, container, false);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        button_take_off= (Button) view.findViewById(R.id.button_take_off);
        button_take_off.setOnClickListener(this);
        button_go_home= (Button) view.findViewById(R.id.button_go_home);
        button_go_home.setOnClickListener(this);
        button_hover= (Button) view.findViewById(R.id.button_hover);
        button_hover.setOnClickListener(this);
        button_follow_me= (Button) view.findViewById(R.id.button_follow_me);
        button_follow_me.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        initBackground();
        final Drone drone = getDrone();
        switch (v.getId()){
            case R.id.button_take_off:
                if(index%2==0){
                    button_take_off.setBackgroundResource(R.drawable.button_land);
                    getArmingConfirmation();
                    getTakeOffConfirmation();
                }else{
                    getDrone().changeVehicleMode(VehicleMode.COPTER_LAND);
                }
                index++;
                break;
            case R.id.button_go_home:
                button_go_home.setBackgroundResource(R.drawable.go_home_on);;
                getDrone().changeVehicleMode(VehicleMode.COPTER_RTL);
                break;
            case R.id.button_hover:
                button_hover.setBackgroundResource(R.drawable.hover_on);
                final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
                if (followState.isEnabled()) {
                    drone.disableFollowMe();
                }

                drone.pauseAtCurrentLocation();
                break;
            case R.id.button_follow_me:
                button_follow_me.setBackgroundResource(R.drawable.follow_me_on);
                toggleFollowMe();
                break;
            default:
                break;
        }

    }

    @Override
    public boolean isSlidingUpPanelEnabled(Drone drone) {
        return false;
    }

    private void initBackground() {
        button_take_off.setBackgroundResource(R.drawable.button_take_off);
        button_go_home.setBackgroundResource(R.drawable.button_go_home);
        button_hover.setBackgroundResource(R.drawable.button_hover);
    }

    private void getArmingConfirmation() {
        SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("arm", new Runnable() {
            @Override
            public void run() {
                getDrone().arm(true);
            }
        });
        unlockDialog.show(getChildFragmentManager(), "Slide To Arm");
    }

    private void getTakeOffConfirmation() {
        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("take off", new Runnable() {
            @Override
            public void run() {
                final double takeOffAltitude = getAppPrefs().getDefaultAltitude();
                getDrone().doGuidedTakeoff(takeOffAltitude);
            }
        });
        unlockDialog.show(getChildFragmentManager(), "Slide to take off");
    }
}
