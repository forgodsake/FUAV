package com.fuav.android.fragments.calibration;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fuav.android.activities.ConfigurationActivity;
import com.fuav.android.fragments.helpers.ApiListenerFragment;

public abstract class SetupMainPanel extends ApiListenerFragment {

	protected ConfigurationActivity parentActivity;
	protected SetupSidePanel sidePanel;

	public abstract int getPanelLayout();

	public abstract SetupSidePanel getSidePanel();

	public abstract void setupLocalViews(View v);

	public abstract void doCalibrationStep(int step);

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof ConfigurationActivity)) {
			throw new IllegalStateException("Parent activity must be "
					+ ConfigurationActivity.class.getName());
		}

		parentActivity = (ConfigurationActivity) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		parentActivity = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaneState) {
		final View view = inflater.inflate(getPanelLayout(), container, false);

		setupLocalViews(view);

		return view;
	}

	public void setSidePanel(SetupSidePanel sidePanel) {
		this.sidePanel = sidePanel;
	}
}
