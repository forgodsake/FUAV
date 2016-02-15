package com.fuav.android.core.drone.autopilot;

import com.MAVLink.Messages.MAVLinkMessage;
import com.fuav.android.core.MAVLink.MAVLinkStreams;
import com.fuav.android.core.MAVLink.WaypointManager;
import com.fuav.android.core.drone.DroneInterfaces;
import com.fuav.android.core.drone.profiles.ParameterManager;
import com.fuav.android.core.drone.variables.Camera;
import com.fuav.android.core.drone.variables.GuidedPoint;
import com.fuav.android.core.drone.variables.MissionStats;
import com.fuav.android.core.drone.variables.RC;
import com.fuav.android.core.drone.variables.State;
import com.fuav.android.core.drone.variables.StreamRates;
import com.fuav.android.core.drone.variables.calibration.AccelCalibration;
import com.fuav.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import com.fuav.android.core.firmware.FirmwareType;
import com.fuav.android.core.mission.Mission;

public interface MavLinkDrone extends Drone {

    String PACKAGE_NAME = "com.fuav.android.core.drone.autopilot";

    String ACTION_REQUEST_HOME_UPDATE = PACKAGE_NAME + ".action.REQUEST_HOME_UPDATE";

    boolean isConnectionAlive();

    int getMavlinkVersion();

    void onMavLinkMessageReceived(MAVLinkMessage message);

    void addDroneListener(DroneInterfaces.OnDroneListener listener);

    void removeDroneListener(DroneInterfaces.OnDroneListener listener);

    void notifyDroneEvent(DroneInterfaces.DroneEventsType event);

    byte getSysid();

    byte getCompid();

    public ParameterManager getParameters();

    public RC getRC();

    State getState();

    ParameterManager getParameterManager();

    int getType();

    FirmwareType getFirmwareType();

    MAVLinkStreams.MAVLinkOutputStream getMavClient();

    WaypointManager getWaypointManager();

    Mission getMission();

    StreamRates getStreamRates();

    MissionStats getMissionStats();

    GuidedPoint getGuidedPoint();

    AccelCalibration getCalibrationSetup();

    MagnetometerCalibrationImpl getMagnetometerCalibration();

    String getFirmwareVersion();

    Camera getCamera();

}
