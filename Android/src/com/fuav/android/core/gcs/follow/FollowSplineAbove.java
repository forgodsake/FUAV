package com.fuav.android.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.coordinate.LatLong;

import com.fuav.android.core.drone.DroneManager;
import com.fuav.android.core.gcs.location.Location;
import com.fuav.android.core.drone.autopilot.MavLinkDrone;

/**
 * Created by fhuya on 1/5/15.
 */
public class FollowSplineAbove extends FollowAlgorithm {

    private final MavLinkDrone drone;

    @Override
    public void processNewLocation(Location location) {
        LatLong gcsLoc = new LatLong(location.getCoord());

        //TODO: some device (nexus 6) do not report the speed (always 0).. figure out workaround.
        double speed = location.getSpeed();
        double bearing = location.getBearing();
        double bearingInRad = Math.toRadians(bearing);
        double xVel = speed * Math.cos(bearingInRad);
        double yVel = speed * Math.sin(bearingInRad);
        drone.getGuidedPoint().newGuidedCoordAndVelocity(gcsLoc, xVel, yVel, 0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.SPLINE_ABOVE;
    }

    public FollowSplineAbove(DroneManager droneManager, Handler handler) {
        super(droneManager, handler);
        drone = droneManager.getDrone();
    }
}
