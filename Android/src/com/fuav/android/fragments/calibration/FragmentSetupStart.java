package com.fuav.android.fragments.calibration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fuav.android.R;
import com.fuav.android.fragments.SetupRadioFragment;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;

public class FragmentSetupStart extends SetupSidePanel {

	private static final IntentFilter intentFilter = new IntentFilter();
	static {
		intentFilter.addAction(AttributeEvent.STATE_CONNECTED);
		intentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			switch (action) {
				case AttributeEvent.STATE_CONNECTED:
						btnStep.setEnabled(true);
					break;
				case AttributeEvent.STATE_DISCONNECTED:
					//Reset the screen, and disable the calibration button
					btnStep.setEnabled(false);
					break;
			}
		}
	};

	private TextView textTitle;
	private TextView textDesc;
	private Button btnStep;
	private int titleId;
	private int descId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final SetupRadioFragment setupFragment = (SetupRadioFragment) getParentFragment();

		final View view = inflater.inflate(R.layout.fragment_setup_panel_start, container, false);

		textTitle = (TextView) view.findViewById(R.id.setupTitle);
		textDesc = (TextView) view.findViewById(R.id.setupDesc);

		if (titleId != 0 && textTitle != null)
			textTitle.setText(titleId);
		if (descId != 0)
			textDesc.setText(descId);

		btnStep = (Button) view.findViewById(R.id.buttonStart);
		btnStep.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setupFragment != null) {
					setupFragment.doCalibrationStep(1);
				}
			}
		});

		return view;
	}

	@Override
	public void updateDescription(int idDescription) {
		this.descId = idDescription;
		if (textDesc != null)
			textDesc.setText(idDescription);
	}

	@Override
	public void updateTitle(int idTitle) {
		this.titleId = idTitle;
		if (textTitle != null)
			textTitle.setText(idTitle);
	}

	@Override
	public void onApiConnected() {
		Drone drone = getDrone();
		State droneState = drone.getAttribute(AttributeType.STATE);
		if (drone.isConnected() && !droneState.isFlying()) {
			btnStep.setEnabled(true);
		} else {
			btnStep.setEnabled(false);
		}
		getBroadcastManager().registerReceiver(broadcastReceiver, intentFilter);
	}

	@Override
	public void onApiDisconnected() {
		getBroadcastManager().unregisterReceiver(broadcastReceiver);
	}
}
