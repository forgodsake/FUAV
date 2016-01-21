package com.fuav.android.core.drone;

import com.fuav.android.core.drone.profiles.VehicleProfile;
import com.fuav.android.core.drone.variables.StreamRates;
import com.fuav.android.core.firmware.FirmwareType;

public interface Preferences {

	VehicleProfile loadVehicleProfile(FirmwareType firmwareType);

    StreamRates.Rates getRates();
}
