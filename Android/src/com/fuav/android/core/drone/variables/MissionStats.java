package com.fuav.android.core.drone.variables;

import com.fuav.android.core.drone.DroneInterfaces.DroneEventsType;
import com.fuav.android.core.drone.DroneVariable;
import com.fuav.android.core.drone.autopilot.MavLinkDrone;

public class MissionStats extends DroneVariable {
    private double distanceToWp = 0;
    private int currentWP = -1;
    private int lastReachedWP = -1;

    public MissionStats(MavLinkDrone myDrone) {
        super(myDrone);
    }

    public void setDistanceToWp(double disttowp) {
        this.distanceToWp = disttowp;
    }

    public void setWpno(int seq) {
        if (seq != currentWP) {
            this.currentWP = seq;
            myDrone.notifyDroneEvent(DroneEventsType.MISSION_WP_UPDATE);
        }
    }

    public void setLastReachedWaypointNumber(int seq) {
        if (seq != lastReachedWP) {
            this.lastReachedWP = seq;
            myDrone.notifyDroneEvent(DroneEventsType.MISSION_WP_REACHED);
        }
    }

    public int getCurrentWP() {
        return currentWP;
    }

    public int getLastReachedWP(){
        return lastReachedWP;
    }

    public double getDistanceToWP() {
        return distanceToWp;
    }

}
