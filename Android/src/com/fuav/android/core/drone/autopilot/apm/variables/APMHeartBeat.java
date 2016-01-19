package com.fuav.android.core.drone.autopilot.apm.variables;

import android.os.Handler;

import com.fuav.android.core.drone.DroneInterfaces;
import com.fuav.android.core.drone.autopilot.MavLinkDrone;
import com.fuav.android.core.drone.autopilot.apm.ArduPilot;
import com.fuav.android.core.drone.variables.HeartBeat;

/**
 * Created by Fredia Huya-Kouadio on 10/24/15.
 */
public class APMHeartBeat extends HeartBeat {

    private static final long HEARTBEAT_IMU_CALIBRATION_TIMEOUT = 35000l; //ms

    protected static final int IMU_CALIBRATION = 3;

    public APMHeartBeat(ArduPilot myDrone, Handler handler) {
        super(myDrone, handler);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, MavLinkDrone drone){
        switch(event){
            case CALIBRATION_IMU:
                //Set the heartbeat in imu calibration mode.
                heartbeatState = IMU_CALIBRATION;
                restartWatchdog(HEARTBEAT_IMU_CALIBRATION_TIMEOUT);
                break;

            default:
                super.onDroneEvent(event, drone);
                break;
        }
    }

    @Override
    protected void onHeartbeatTimeout(){
        switch(heartbeatState){
            case IMU_CALIBRATION:
                restartWatchdog(HEARTBEAT_IMU_CALIBRATION_TIMEOUT);
                myDrone.notifyDroneEvent(DroneInterfaces.DroneEventsType.CALIBRATION_TIMEOUT);
                break;

            default:
                super.onHeartbeatTimeout();
                break;
        }
    }
}
