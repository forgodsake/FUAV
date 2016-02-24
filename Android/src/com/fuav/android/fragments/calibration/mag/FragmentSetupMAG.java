package com.fuav.android.fragments.calibration.mag;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.fuav.android.R;
import com.fuav.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import com.fuav.android.fragments.helpers.ApiListenerFragment;
import com.fuav.android.notifications.TTSNotificationProvider;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;


public class FragmentSetupMAG extends ApiListenerFragment implements MagnetometerCalibrationImpl.OnMagnetometerCalibrationListener {

	private final static long UPDATE_TIMEOUT_PERIOD = 100l; //ms
	private static final String EXTRA_UPDATE_TIMESTAMP = "extra_update_timestamp";

	private static final IntentFilter intentFilter = new IntentFilter();
	static {
		intentFilter.addAction(AttributeEvent.CALIBRATION_IMU);
		intentFilter.addAction(AttributeEvent.CALIBRATION_IMU_TIMEOUT);
		intentFilter.addAction(AttributeEvent.STATE_CONNECTED);
		intentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			switch (action) {
				case AttributeEvent.CALIBRATION_MAG_PROGRESS: {
					if (getDrone().isConnected()) {
						String message = intent.getStringExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_PROGRESS);
						if (message != null)
							relayInstructions(message);
					}
					break;
				}
				case AttributeEvent.STATE_CONNECTED:
					if (calibration_step == 0) {
						//Reset the screen, and enable the calibration button
						resetCalibration();
						btnStep.setEnabled(true);
					}
					break;
				case AttributeEvent.STATE_DISCONNECTED:
					//Reset the screen, and disable the calibration button
					btnStep.setEnabled(false);
					resetCalibration();
					break;
				case AttributeEvent.CALIBRATION_MAG_COMPLETED:
					if (getDrone().isConnected()) {
						String message = intent.getStringExtra(AttributeEventExtra.EXTRA_CALIBRATION_MAG_RESULT);
						if (message != null)
							relayInstructions(message);
					}
					break;
			}
		}
	};

	private long updateTimestamp;

	private int calibration_step = 0;

	private final Handler handler = new Handler();

	private Button btnStep;
	private Button btnSend;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setup_mag_main, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		btnStep = (Button) view.findViewById(R.id.buttonStep);
		btnStep.setEnabled(false);
		btnStep.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startCalibration();
			}
		});

		btnSend = (Button) view.findViewById(R.id.buttonSend);
		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendCalibration();
			}
		});

		if(savedInstanceState != null){
			updateTimestamp = savedInstanceState.getLong(EXTRA_UPDATE_TIMESTAMP);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putLong(EXTRA_UPDATE_TIMESTAMP, updateTimestamp);
	}

	private void resetCalibration(){
		calibration_step = 0;
		updateDescription(calibration_step);
	}


	private void startCalibration() {
		Drone dpApi = getDrone();
		if (dpApi.isConnected()) {
			dpApi.startMAGCalibration();
		}
	}



	private void processMAVMessage(String message, boolean updateTime) {
		if (message.contains("Place") || message.contains("Calibration")) {
			if(updateTime) {
				updateTimestamp = System.currentTimeMillis();
			}

			processOrientation(message);
		}
	}

	public void updateDescription(int calibration_step) {

		if (btnStep != null) {
			if (calibration_step == 0)
				btnStep.setText(R.string.button_setup_calibrate);
			else if (calibration_step == 2)
				btnStep.setText(R.string.button_setup_done);
			else
				btnStep.setText(R.string.button_setup_next);
		}

		if (calibration_step == 2 || calibration_step == 0) {
			handler.removeCallbacks(runnable);
		} else {
			handler.removeCallbacks(runnable);
			handler.postDelayed(runnable, UPDATE_TIMEOUT_PERIOD);
		}
	}

	private void processOrientation(String message) {
		if (message.contains("level"))
			calibration_step = 1;
		else if (message.contains("LEFT"))
			calibration_step = 2;

		String msg = message.replace("any key.", "'Next'");
		relayInstructions(msg);

		updateDescription(calibration_step);
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(this);
			handler.postDelayed(this, UPDATE_TIMEOUT_PERIOD);
		}
	};

	private void relayInstructions(String instructions){
		final Activity activity = getActivity();
		if(activity == null) return;

		final Context context = activity.getApplicationContext();

		getBroadcastManager()
				.sendBroadcast(new Intent(TTSNotificationProvider.ACTION_SPEAK_MESSAGE)
						.putExtra(TTSNotificationProvider.EXTRA_MESSAGE_TO_SPEAK, instructions));

		Toast.makeText(context, instructions, Toast.LENGTH_LONG).show();
	}

	private void sendCalibration() {
		Drone dpApi = getDrone();
		if (dpApi.isConnected()) {
			dpApi.sendCalibration();
		}
	}

	public static CharSequence getTitle(Context context) {
		return context.getText(R.string.setup_mag_title);
	}

	@Override
	public void onApiConnected() {
		Drone drone = getDrone();
		State droneState = drone.getAttribute(AttributeType.STATE);
		if (drone.isConnected() && !droneState.isFlying()) {
			btnStep.setEnabled(true);
			if (droneState.isCalibrating()) {
				processMAVMessage(droneState.getCalibrationStatus(), false);
			}
			else{
				resetCalibration();
			}
		} else {
			btnStep.setEnabled(false);
			resetCalibration();
		}

		getBroadcastManager().registerReceiver(broadcastReceiver, intentFilter);
	}

	@Override
	public void onApiDisconnected() {
		getBroadcastManager().unregisterReceiver(broadcastReceiver);
	}

	@Override
	public void onCalibrationCancelled() {

	}

	@Override
	public void onCalibrationProgress(msg_mag_cal_progress progress) {
		Toast.makeText(getActivity(),""+progress.completion_pct,Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCalibrationCompleted(msg_mag_cal_report result) {

	}
}
