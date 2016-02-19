package com.fuav.android.fragments.calibration.mag;

import android.view.View;

import com.fuav.android.R;
import com.fuav.android.fragments.calibration.SetupMainPanel;
import com.fuav.android.fragments.calibration.SetupSidePanel;


public class FragmentSetupMAG extends SetupMainPanel {

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_mag_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setupLocalViews(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doCalibrationStep(int step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onApiConnected() {

	}

	@Override
	public void onApiDisconnected() {

	}
}
