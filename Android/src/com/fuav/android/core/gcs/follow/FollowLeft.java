package com.fuav.android.core.gcs.follow;

import android.os.Handler;

import com.fuav.android.core.drone.DroneManager;

public class FollowLeft extends FollowHeadingAngle {

    public FollowLeft(DroneManager droneMgr, Handler handler, double radius) {
        super(droneMgr, handler, radius, -90.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEFT;
    }

}
