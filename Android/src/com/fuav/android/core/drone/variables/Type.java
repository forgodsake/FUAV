package com.fuav.android.core.drone.variables;

import com.fuav.android.core.drone.DroneInterfaces;
import com.fuav.android.core.drone.DroneInterfaces.DroneEventsType;
import com.fuav.android.core.drone.DroneVariable;
import com.fuav.android.core.drone.autopilot.MavLinkDrone;

import com.MAVLink.enums.MAV_TYPE;

public class Type extends DroneVariable implements DroneInterfaces.OnDroneListener{

    private static final int DEFAULT_TYPE = MAV_TYPE.MAV_TYPE_GENERIC;

	private int type = DEFAULT_TYPE;
	private String firmwareVersion = null;

	public Type(MavLinkDrone myDrone) {
		super(myDrone);
        myDrone.addDroneListener(this);
	}

	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			myDrone.notifyDroneEvent(DroneEventsType.TYPE);
		}
	}

	public int getType() {
		return type;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String message) {
        if(firmwareVersion == null || !firmwareVersion.equals(message)) {
            firmwareVersion = message;
            myDrone.notifyDroneEvent(DroneEventsType.FIRMWARE);
        }
	}

    public static boolean isCopter(int type){
        switch (type) {
            case MAV_TYPE.MAV_TYPE_TRICOPTER:
            case MAV_TYPE.MAV_TYPE_QUADROTOR:
            case MAV_TYPE.MAV_TYPE_HEXAROTOR:
            case MAV_TYPE.MAV_TYPE_OCTOROTOR:
            case MAV_TYPE.MAV_TYPE_HELICOPTER:
                return true;

            default:
                return false;
        }
    }

    public static boolean isPlane(int type){
        return type == MAV_TYPE.MAV_TYPE_FIXED_WING;
    }

    public static boolean isRover(int type){
        return type == MAV_TYPE.MAV_TYPE_GROUND_ROVER;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch(event){
            case DISCONNECTED:
                setType(DEFAULT_TYPE);
                break;
        }
    }
}