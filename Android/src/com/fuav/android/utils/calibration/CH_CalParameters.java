package com.fuav.android.utils.calibration;

import com.fuav.android.core.drone.autopilot.MavLinkDrone;

public class CH_CalParameters extends CalParameters {

	public CH_CalParameters(MavLinkDrone myDrone) {
		super(myDrone);
		calParameterNames.add("CH7_OPT");
		calParameterNames.add("CH8_OPT");
		calParameterNames.add("TUNE");
		calParameterNames.add("TUNE_LOW");
		calParameterNames.add("TUNE_HIGH");
	}
}
