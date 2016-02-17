package com.fuav.android.fragments.calibration.rc;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fuav.android.DroidPlannerApp;
import com.fuav.android.R;
import com.fuav.android.core.drone.DroneInterfaces;
import com.fuav.android.core.drone.autopilot.MavLinkDrone;
import com.fuav.android.fragments.SetupRadioFragment;
import com.fuav.android.fragments.calibration.FragmentSetupNext;
import com.fuav.android.fragments.calibration.FragmentSetupProgress;
import com.fuav.android.fragments.calibration.FragmentSetupStart;
import com.fuav.android.fragments.calibration.FragmentSetupSummary;
import com.fuav.android.fragments.calibration.SetupSidePanel;
import com.fuav.android.fragments.helpers.SuperSetupMainPanel;
import com.fuav.android.utils.calibration.CalParameters;
import com.fuav.android.utils.calibration.RC_CalParameters;
import com.fuav.android.view.FillBar.FillBar;
import com.fuav.android.view.RcStick.RcStick;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.property.Parameter;


public class FragmentSetupRC extends SuperSetupMainPanel {

	/**
	 * Minimum threshold for the RC value.
	 */
	private static final int RC_MIN = 900;

	/**
	 * Maximum threshold for the RC value.
	 */
	private static final int RC_MAX = 2100;

	private static final String[] RCStr = { "CH 1 ", "CH 2 ", "CH 3 ", "CH 4 ", "CH 5", "CH 6",
			"CH 7", "CH 8" };

	private int calibrationStep = 0;

	private DroidPlannerApp app;

	private FillBar bar1;
	private FillBar bar2;
	private FillBar bar3;
	private FillBar bar4;
	private FillBar bar5;
	private FillBar bar6;
	private FillBar bar7;
	private FillBar bar8;
	private TextView roll_pitch_text;
	private TextView thr_yaw_text;
	private TextView ch_5_text;
	private TextView ch_6_text;
	private TextView ch_7_text;
	private TextView ch_8_text;

	private RcStick stickLeft;
	private RcStick stickRight;

	private int data[] = new int[8];
	private int cMin[] = new int[8];
	private int cMid[] = new int[8];
	private int cMax[] = new int[8];

	@Override
	protected CalParameters getParameterHandler() {
		return new RC_CalParameters(drone);
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return getDefaultPanel();
	}

	@Override
	protected SetupSidePanel getDefaultPanel() {
		calibrationStep = 0;
		// setFillBarShowMinMax(false);
		sidePanel = new FragmentSetupStart();
		return sidePanel;
	}

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_rc_main;
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, MavLinkDrone drone) {
		super.onDroneEvent(event, drone);
		switch (event) {
		case RC_IN:
			updatePanelInfo();
			data = drone.getRC().in;
			Toast.makeText(getActivity(),"遥控通道一"+data[0],Toast.LENGTH_SHORT).show();
			break;
		case RC_OUT:
		default:
			break;
		}
	}

	@Override
	public void onReadCalibration() {
		doCalibrationStep(0);// show progress sidepanel
	}

	@Override
	public void setupLocalViews(View view) {
		stickLeft = (RcStick) view.findViewById(R.id.stickLeft);
		stickRight = (RcStick) view.findViewById(R.id.stickRight);

		bar1 = (FillBar) view.findViewById(R.id.fillBar_roll);
		bar2 = (FillBar) view.findViewById(R.id.fillBar_pitch);
		bar3 = (FillBar) view.findViewById(R.id.fillBar_throttle);
		bar4 = (FillBar) view.findViewById(R.id.fillBar_yaw);
		bar5 = (FillBar) view.findViewById(R.id.fillBar_ch_5);
		bar6 = (FillBar) view.findViewById(R.id.fillBar_ch_6);
		bar7 = (FillBar) view.findViewById(R.id.fillBar_ch_7);
		bar8 = (FillBar) view.findViewById(R.id.fillBar_ch_8);
		bar2.invertBar(true);

		roll_pitch_text = (TextView) view.findViewById(R.id.roll_pitch_text);
		thr_yaw_text = (TextView) view.findViewById(R.id.thr_yaw_text);
		ch_5_text = (TextView) view.findViewById(R.id.ch_5_text);
		ch_6_text = (TextView) view.findViewById(R.id.ch_6_text);
		ch_7_text = (TextView) view.findViewById(R.id.ch_7_text);
		ch_8_text = (TextView) view.findViewById(R.id.ch_8_text);

		bar1.setup(RC_MAX, RC_MIN);
		bar2.setup(RC_MAX, RC_MIN);
		bar3.setup(RC_MAX, RC_MIN);
		bar4.setup(RC_MAX, RC_MIN);
		bar5.setup(RC_MAX, RC_MIN);
		bar6.setup(RC_MAX, RC_MIN);
		bar7.setup(RC_MAX, RC_MIN);
		bar8.setup(RC_MAX, RC_MIN);
	}

	@Override
	public void doCalibrationStep(int step) {
		switch (step) {
		case 1:
			calibrationStep=1;
		case 2:
			sidePanel = getNextPanel();
			break;
		case 3: // Upload calibration data
			updateCalibrationData();
			break;
		case 0:
			break;
		default:
			sidePanel = getInitialPanel();
		}
	}

	private SetupSidePanel getCompletedPanel() {
		calibrationStep = 0;
		Bundle args = new Bundle();
		args.putString(FragmentSetupSummary.EXTRA_TEXT_SUMMARY, getCalibrationStr());
		sidePanel = ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupSummary());
		if (sidePanel != null) {
			sidePanel.setArguments(args);
		}
		return sidePanel;
	}

	private SetupSidePanel getNextPanel() {
		int textId = 0, descId = 0;

		switch (calibrationStep) {
		case 0:
			if (!parameters.isParameterDownloaded() && drone.getMavClient().isConnected()) {
				getProgressPanel(true);
				parameters.getCalibrationParameters(drone);
				return sidePanel;
			}
			setFillBarShowMinMax(true);
			textId = R.string.setup_radio_title_minmax;
			descId = R.string.setup_radio_desc_minmax;
			break;
		case 1:
			textId = R.string.setup_radio_title_middle;
			descId = R.string.setup_radio_desc_middle;
			break;
		case 3:
			sidePanel = getCompletedPanel();
			return sidePanel;
		}
		calibrationStep++;

		sidePanel = new FragmentSetupNext();
		sidePanel.updateTitle(textId);
		sidePanel.updateDescription(descId);

		return ((SetupRadioFragment) getParentFragment()).changeSidePanel(sidePanel);
	}

	private void getProgressPanel(boolean isSending) {
		sidePanel = new FragmentSetupProgress();
		if (isSending) {
			sidePanel.updateTitle(R.string.progress_title_uploading);
			sidePanel.updateDescription(R.string.progress_desc_uploading);
		} else {
			sidePanel.updateTitle(R.string.progress_title_downloading);
			sidePanel.updateDescription(R.string.progress_desc_downloading);
		}
	}

	@Override
	protected void updatePanelInfo() {
		data = drone.getRC().in;
		bar1.setValue(data[0]);
		bar2.setValue(data[1]);
		bar3.setValue(data[2]);
		bar4.setValue(data[3]);
		bar5.setValue(data[4]);
		bar6.setValue(data[5]);
		bar7.setValue(data[6]);
		bar8.setValue(data[7]);

		roll_pitch_text.setText("Roll: " + Integer.toString(data[0]) + "\nPitch: "
				+ Integer.toString(data[1]));
		thr_yaw_text.setText("Throttle: " + Integer.toString(data[2]) + "\nYaw: "
				+ Integer.toString(data[3]));
		ch_5_text.setText("CH 5: " + Integer.toString(data[4]));
		ch_6_text.setText("CH 6: " + Integer.toString(data[5]));
		ch_7_text.setText("CH 7: " + Integer.toString(data[6]));
		ch_8_text.setText("CH 8: " + Integer.toString(data[7]));

		float x, y;
		x = (data[3] - RC_MIN) / ((float) (RC_MAX - RC_MIN)) * 2 - 1;
		y = (data[2] - RC_MIN) / ((float) (RC_MAX - RC_MIN)) * 2 - 1;
		stickLeft.setPosition(x, y);

		x = (data[0] - RC_MIN) / ((float) (RC_MAX - RC_MIN)) * 2 - 1;
		y = (data[1] - RC_MIN) / ((float) (RC_MAX - RC_MIN)) * 2 - 1;
		stickRight.setPosition(x, -y);
	}

	private void setFillBarShowMinMax(boolean b) {
		bar1.setShowMinMax(b);
		bar2.setShowMinMax(b);
		bar3.setShowMinMax(b);
		bar4.setShowMinMax(b);
		bar5.setShowMinMax(b);
		bar6.setShowMinMax(b);
		bar7.setShowMinMax(b);
		bar8.setShowMinMax(b);
	}

	private String getCalibrationStr() {
		String txt = "RC #\t\tMIN\t\tMID\t\tMAX";

		cMin[0] = bar1.getMinValue();
		cMin[1] = bar2.getMinValue();
		cMin[2] = bar3.getMinValue();
		cMin[3] = bar4.getMinValue();
		cMin[4] = bar5.getMinValue();
		cMin[5] = bar6.getMinValue();
		cMin[6] = bar7.getMinValue();
		cMin[7] = bar8.getMinValue();

		cMax[0] = bar1.getMaxValue();
		cMax[1] = bar2.getMaxValue();
		cMax[2] = bar3.getMaxValue();
		cMax[3] = bar4.getMaxValue();
		cMax[4] = bar5.getMaxValue();
		cMax[5] = bar6.getMaxValue();
		cMax[6] = bar7.getMaxValue();
		cMax[7] = bar8.getMaxValue();

		if (data != null)
			cMid = data;

		for (int i = 0; i < 8; i++) {
			txt += "\n" + RCStr[i] + "\t";
			txt += "\t" + String.valueOf(cMin[i]) + "\t";
			txt += "\t" + String.valueOf(cMid[i]) + "\t";
			txt += "\t" + String.valueOf(cMax[i]);
		}

		return txt;
	}

	@Override
	public void updateCalibrationData() {

		for (int i = 1; i <=8; i++) {
			parameters.calParameterItems.add(new Parameter("RC" + i + "_MIN",0,4));
			parameters.calParameterItems.add(new Parameter("RC" + i + "_MAX",0,4));
			parameters.calParameterItems.add(new Parameter("RC" + i + "_TRIM",0,4));
			parameters.setParamValueByName("RC" + i + "_MIN", cMin[i-1]);
			parameters.setParamValueByName("RC" + i + "_MAX", cMax[i-1]);
			parameters.setParamValueByName("RC" + i + "_TRIM", cMid[i-1]);
		}

		setFillBarShowMinMax(false);
		getProgressPanel(true);
		Context context = getActivity();
		app = (DroidPlannerApp) context.getApplicationContext();
		Drone drone = app.getDrone();
		parameters.sendParameters(context,drone);
	}
}
