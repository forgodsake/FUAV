package com.fuav.android.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Gps;

import com.fuav.android.core.drone.DroneManager;
import com.fuav.android.core.gcs.location.Location;
import com.fuav.android.core.helpers.geoTools.GeoTools;

public class FollowLeash extends FollowWithRadiusAlgorithm {

    public FollowLeash(DroneManager droneMgr, Handler handler, double radius) {
        super(droneMgr, handler, radius);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEASH;
    }

    @Override
    protected void processNewLocation(Location location) {
        final LatLong locationCoord = location.getCoord();

        final Gps droneGps = (Gps) drone.getAttribute(AttributeType.GPS);
        final LatLong dronePosition = droneGps.getPosition();

        if (locationCoord == null || dronePosition == null) {
            return;
        }

        if (GeoTools.getDistance(locationCoord, dronePosition) > radius) {
            double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(locationCoord, dronePosition);
            LatLong goCoord = GeoTools.newCoordFromBearingAndDistance(locationCoord, headingGCStoDrone, radius);
            drone.getGuidedPoint().newGuidedCoord(goCoord);
        }
    }

}
