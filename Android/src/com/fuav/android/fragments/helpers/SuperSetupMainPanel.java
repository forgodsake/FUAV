package com.fuav.android.fragments.helpers;

import android.os.Bundle;
import android.widget.Toast;

import com.fuav.android.R;
import com.fuav.android.core.drone.DroneInterfaces;
import com.fuav.android.core.drone.DroneManager;
import com.fuav.android.core.drone.autopilot.MavLinkDrone;
import com.fuav.android.fragments.SetupRadioFragment;
import com.fuav.android.fragments.calibration.FragmentSetupProgress;
import com.fuav.android.fragments.calibration.SetupMainPanel;
import com.fuav.android.fragments.calibration.SetupSidePanel;
import com.fuav.android.utils.calibration.CalParameters;

public abstract class SuperSetupMainPanel extends SetupMainPanel implements CalParameters.OnCalibrationEvent,
		DroneInterfaces.OnDroneListener {

	protected MavLinkDrone drone;
	protected CalParameters parameters;

	protected abstract CalParameters getParameterHandler();

	protected abstract SetupSidePanel getDefaultPanel();

	protected abstract void updatePanelInfo();

	protected abstract void updateCalibrationData();

	protected void onInitialize() {
	}// can be overridden if necessary

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.drone = DroneManager.getDrone();
		parameters = getParameterHandler();
		parameters.setOnCalibrationEventListener(this);
		onInitialize();
	}

	@Override
	public void onStart() {
		super.onStart();
		doCalibrationStep(0);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(drone!=null){
			drone.addDroneListener(this);
		}else{
			Toast.makeText(getActivity(),"无人机未连接",Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if(drone!=null){
			drone.removeDroneListener(this);
		}

	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, MavLinkDrone drone) {
		switch (event) {
			case PARAMETER:
				if (parameters != null) {
				parameters.processReceivedParam();
				}
			default:
				break;
		}
	}

	@Override
	public void onReadCalibration() {
		doCalibrationStep(0);
		updatePanelInfo();
	}

	@Override
	public void onSentCalibration() {
		doCalibrationStep(0);
	}

	@Override
	public void onCalibrationData(int index, int count, boolean isSending) {
		if (sidePanel instanceof FragmentSetupProgress && parameters != null) {
			String title;
			if (isSending) {
				title = getResources().getString(R.string.setup_sf_desc_uploading);
			} else {
				title = getResources().getString(R.string.setup_sf_desc_downloading);
			}

			((FragmentSetupProgress) sidePanel).updateProgress(index + 1, count, title);
		}
	}

	@Override
	public void doCalibrationStep(int step) {
		switch (step) {
		case 3:
			uploadCalibrationData();
			break;
		case 0:
		default:
			sidePanel = getInitialPanel();
		}
	}

	protected SetupSidePanel getInitialPanel() {

		if (parameters != null && !parameters.isParameterDownloaded()
				&& drone.getMavClient().isConnected()) {
			downloadCalibrationData();
		} else {
			sidePanel = getDefaultPanel();
			((SetupRadioFragment) getParentFragment()).changeSidePanel(sidePanel);

		}
		return sidePanel;
	}

	private SetupSidePanel getProgressPanel(boolean isSending) {
		sidePanel = ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupProgress());

		if (isSending) {
			sidePanel.updateTitle(R.string.progress_title_uploading);
			sidePanel.updateDescription(R.string.progress_desc_uploading);
		} else {
			sidePanel.updateTitle(R.string.progress_title_downloading);
			sidePanel.updateDescription(R.string.progress_desc_downloading);
		}

		return sidePanel;
	}

	private void uploadCalibrationData() {
		if (parameters == null || !drone.getMavClient().isConnected())
			return;

		sidePanel = getProgressPanel(true);

		updateCalibrationData();
//		parameters.sendCalibrationParameters();
	}

	private void downloadCalibrationData() {
		if (parameters == null || !drone.getMavClient().isConnected())
			return;
		sidePanel = getProgressPanel(false);
		parameters.getCalibrationParameters(drone);
	}

	protected int getSpinnerIndexFromValue(int value, int[] valueList) {
		for (int i = 0; i < valueList.length; i++) {
			if (valueList[i] == value)
				return i;
		}
		return -1;
	}

}