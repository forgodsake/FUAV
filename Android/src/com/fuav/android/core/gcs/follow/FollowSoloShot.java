package com.fuav.android.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloMessageLocation;

import com.fuav.android.core.drone.DroneManager;
import com.fuav.android.core.drone.autopilot.MavLinkDrone;
import com.fuav.android.core.drone.autopilot.apm.solo.ArduSolo;
import com.fuav.android.core.drone.autopilot.apm.solo.SoloComp;
import com.fuav.android.core.gcs.location.Location;
import com.fuav.android.core.gcs.roi.ROIEstimator;

/**
 * Created by Fredia Huya-Kouadio on 8/3/15.
 */
public class FollowSoloShot extends FollowAlgorithm {

    private final SoloComp soloComp;

    private final LatLongAlt locationCoord = new LatLongAlt(0, 0, 0);
    private final SoloMessageLocation locationSetter = new SoloMessageLocation(locationCoord);

    public FollowSoloShot(DroneManager droneMgr, Handler handler) {
        super(droneMgr, handler);
        ArduSolo drone = (ArduSolo) droneMgr.getDrone();
        this.soloComp = drone.getSoloComp();
    }

    @Override
    public void enableFollow() {
        super.enableFollow();
        soloComp.enableFollowDataConnection();
    }

    @Override
    public void disableFollow() {
        super.disableFollow();
        soloComp.disableFollowDataConnection();
    }

    @Override
    protected void processNewLocation(Location location) {
        if (location != null) {
            LatLongAlt receivedCoord = location.getCoord();

            locationCoord.set((LatLong)receivedCoord);
            locationSetter.setCoordinate(locationCoord);

            soloComp.updateFollowCenter(locationSetter);
        }
    }

    @Override
    public FollowModes getType() {
        return FollowModes.SOLO_SHOT;
    }

    @Override
    protected ROIEstimator initROIEstimator(MavLinkDrone drone, Handler handler) {
        return new SoloROIEstimator(drone, handler, ((ArduSolo)drone).getSoloComp());
    }

    protected static class SoloROIEstimator extends ROIEstimator {

        private final LatLongAlt locationCoord = new LatLongAlt(0, 0, 0);
        private final SoloMessageLocation locationSetter = new SoloMessageLocation(locationCoord);
        private final SoloComp soloComp;

        public SoloROIEstimator(MavLinkDrone drone, Handler handler, SoloComp soloComp) {
            super(drone, handler);
            this.soloComp = soloComp;
        }

        @Override
        public void enableFollow(){
            isFollowEnabled.set(true);
        }

        @Override
        public void disableFollow(){
            if(isFollowEnabled.compareAndSet(true, false)){
                realLocation = null;
                disableWatchdog();
            }
        }

        @Override
        protected long getUpdatePeriod(){
            return 40l;
        }

        @Override
        protected void sendUpdateROI(LatLong goCoord){
            locationCoord.set(goCoord);
            locationSetter.setCoordinate(locationCoord);
            soloComp.updateFollowCenter(locationSetter);
        }
    }
}
