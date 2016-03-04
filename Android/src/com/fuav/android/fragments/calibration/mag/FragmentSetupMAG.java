package com.fuav.android.fragments.calibration.mag;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fuav.android.R;
import com.fuav.android.core.drone.DroneManager;
import com.fuav.android.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import com.fuav.android.fragments.helpers.ApiListenerFragment;
import com.fuav.android.notifications.TTSNotificationProvider;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class FragmentSetupMAG extends ApiListenerFragment  {

	private static final IntentFilter intentFilter = new IntentFilter();
	static {
		intentFilter.addAction(AttributeEvent.CALIBRATION_MAG_CANCELLED);
		intentFilter.addAction(AttributeEvent.CALIBRATION_MAG_COMPLETED);
		intentFilter.addAction(AttributeEvent.CALIBRATION_MAG_PROGRESS);
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
						if (message != null){
							relayInstructions(message);
							}
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

	private int calibration_step = 0;

	private Button btnStep;
	private ProgressBar progress_bar;

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
				processCalibrationStep(calibration_step);
			}
		});

		progress_bar = (ProgressBar) view.findViewById(R.id.calibration_progress_bar);
	}

	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
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

	@Override
	public void onResume() {
		super.onResume();
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		scheduledExecutorService.scheduleAtFixedRate(
				new EchoServer(),
				0,
				100,
				TimeUnit.MILLISECONDS);
	}

	public class EchoServer implements Runnable{
		@Override
		public void run() {
			if(null!=DroneManager.getDrone()){
				MagnetometerCalibrationImpl magCalImpl = DroneManager.getDrone().getMagnetometerCalibration();
				Collection<MagnetometerCalibrationImpl.Info> calibrationInfo = magCalImpl.getMagCalibrationTracker().values();
				for (MagnetometerCalibrationImpl.Info info : calibrationInfo){
					Message message = Message.obtain();
					Bundle bundle = new Bundle();
					bundle.putInt("percent",info.getCalProgress().completion_pct);
					message.setData(bundle);
					message.what = 1;
					newhandler.sendMessage(message);
				}
			}
		}
	}

	private Handler newhandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case 1:
					int percent = msg.getData().getInt("percent");
					if(percent>30&&percent<60){
						progress_bar.setProgressDrawable(getResources().getDrawable(R.drawable.pstate_warning));
					}else if(percent>=60&&percent<99){
						progress_bar.setProgressDrawable(getResources().getDrawable(R.drawable.pstate_good));
					}else if(percent==99){
						calibration_step = 1;
						updateDescription(calibration_step);
					}
					progress_bar.setProgress(percent);
					break;
			}
		}
	};

	private void processMAVMessage(String message, boolean updateTime) {
		if (message.contains("Place") || message.contains("Calibration")) {

		}
	}

	public void updateDescription(int calibration_step) {

		if (btnStep != null) {
			if (calibration_step == 0)
				btnStep.setText(R.string.button_setup_calibrate);
			else if (calibration_step == 1)
				btnStep.setText(R.string.button_setup_send);
		}
	}


	private void relayInstructions(String instructions){
		final Activity activity = getActivity();
		if(activity == null) return;

		final Context context = activity.getApplicationContext();

		getBroadcastManager()
				.sendBroadcast(new Intent(TTSNotificationProvider.ACTION_SPEAK_MESSAGE)
						.putExtra(TTSNotificationProvider.EXTRA_MESSAGE_TO_SPEAK, instructions));

		Toast.makeText(context, instructions, Toast.LENGTH_SHORT).show();
	}

	private void processCalibrationStep(int step) {
		if (step == 0) {
			startCalibration();
		} else if (step == 1) {
			sendCalibration();
			Toast.makeText(getActivity(),"校准完成",Toast.LENGTH_SHORT).show();
			calibration_step = 0;
			updateDescription(calibration_step);
		}
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

}
