package com.fuav.android.core.drone.autopilot.px4;

import android.content.Context;
import android.os.Handler;

import com.fuav.android.core.MAVLink.MAVLinkStreams;
import com.fuav.android.core.drone.DroneInterfaces;
import com.fuav.android.core.drone.LogMessageListener;
import com.fuav.android.core.drone.autopilot.generic.GenericMavLinkDrone;
import com.fuav.android.core.firmware.FirmwareType;
import com.fuav.android.core.model.AutopilotWarningParser;

/**
 * Created by Fredia Huya-Kouadio on 9/10/15.
 */
public class Px4Native extends GenericMavLinkDrone {

    public Px4Native(Context context, Handler handler, MAVLinkStreams.MAVLinkOutputStream mavClient, AutopilotWarningParser warningParser, LogMessageListener logListener, DroneInterfaces.AttributeEventListener listener) {
        super(context, handler, mavClient, warningParser, logListener, listener);
    }

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.PX4_NATIVE;
    }

}
