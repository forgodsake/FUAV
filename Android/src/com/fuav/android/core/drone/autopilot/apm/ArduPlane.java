package com.fuav.android.core.drone.autopilot.apm;

import android.content.Context;
import android.os.Handler;

import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_TYPE;

import com.fuav.android.core.MAVLink.MAVLinkStreams;
import com.fuav.android.core.drone.DroneInterfaces;
import com.fuav.android.core.drone.LogMessageListener;
import com.fuav.android.core.firmware.FirmwareType;
import com.fuav.android.core.model.AutopilotWarningParser;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduPlane extends ArduPilot {

    public ArduPlane(Context context, MAVLinkStreams.MAVLinkOutputStream mavClient, Handler handler, AutopilotWarningParser warningParser, LogMessageListener logListener, DroneInterfaces.AttributeEventListener listener) {
        super(context, mavClient, handler, warningParser, logListener, listener);
    }

    @Override
    public int getType(){
        return MAV_TYPE.MAV_TYPE_FIXED_WING;
    }

    @Override
    protected void setType(int type){}

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.ARDU_PLANE;
    }

    @Override
    protected void processVfrHud(msg_vfr_hud vfrHud){
        //Nothing to do. Plane used GLOBAL_POSITION_INT to set altitude and speeds unlike copter
    }

    /**
     * Used to update the vehicle location, and altitude.
     * @param gpi
     */
    @Override
    protected void processGlobalPositionInt(msg_global_position_int gpi){
        if(gpi == null)
            return;

        super.processGlobalPositionInt(gpi);

        final double relativeAlt = gpi.relative_alt / 1000.0;

        final double groundSpeedX = gpi.vx / 100.0;
        final double groundSpeedY = gpi.vy / 100.0;
        final double groundSpeed = Math.sqrt(Math.pow(groundSpeedX, 2) + Math.pow(groundSpeedY, 2));

        final double climbRate = gpi.vz / 100.0;
        setAltitudeGroundAndAirSpeeds(relativeAlt, groundSpeed, groundSpeed, climbRate);
    }


}
