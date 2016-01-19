package com.fuav.android.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.coordinate.LatLong;

import com.fuav.android.core.drone.DroneManager;
import com.fuav.android.core.gcs.location.Location;
import com.fuav.android.core.helpers.geoTools.GeoTools;
import com.fuav.android.core.drone.autopilot.MavLinkDrone;

public abstract class FollowHeadingAngle extends FollowWithRadiusAlgorithm {

    protected double angleOffset;
    protected final MavLinkDrone drone;

    protected FollowHeadingAngle(DroneManager droneMgr, Handler handler, double radius, double angleOffset) {
        super(droneMgr, handler, radius);
        this.angleOffset = angleOffset;

        this.drone = droneMgr.getDrone();
    }

    @Override
    public void processNewLocation(Location location) {
        LatLong gcsCoord = new LatLong(location.getCoord());
        double bearing = location.getBearing();

        LatLong goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing + angleOffset, radius);
        drone.getGuidedPoint().newGuidedCoord(goCoord);
    }

}