package com.fuav.android.utils.calibration;

import android.content.Context;
import android.widget.Toast;

import com.fuav.android.core.drone.autopilot.MavLinkDrone;
import com.o3dr.services.android.lib.drone.property.Parameter;

import java.util.ArrayList;
import java.util.List;

public class CalParameters {
	private MavLinkDrone myDrone;
	protected List<String> calParameterNames = new ArrayList<String>();
	public List<Parameter> calParameterItems = new ArrayList<Parameter>();
	private boolean isUpdating = false;
	private OnCalibrationEvent listener;
	private int paramCount = 0;
	private int uploadIndex = 0;

	public interface OnCalibrationEvent {
		public void onReadCalibration();

		public void onSentCalibration();

		public void onCalibrationData(int index, int count, boolean isSending);
	}

	public CalParameters(MavLinkDrone myDrone) {
		this.myDrone = myDrone;
	}

	public void setOnCalibrationEventListener(OnCalibrationEvent listener) {
		this.listener = listener;
	}

	public void processReceivedParam() {
//		if (myDrone == null) {
//			return;
//		}
//		Parameter param = myDrone.getParameters().getLastParameter();
//		if (param == null) {
//			return;
//		}

//		if (isUpdating) {
//			compareCalibrationParameter(param);
//		} else {
//			calParameterItems.add(param);
//			paramCount = calParameterItems.size();
//			readCalibrationParameter(calParameterItems.size());
//		}
	}

	private void compareCalibrationParameter(Parameter param) {
		Parameter paramRef = calParameterItems.get(uploadIndex);

		if (paramRef.name.equalsIgnoreCase(param.name) && paramRef.value == param.value) {
			uploadIndex++;
		}
//		sendCalibrationParameters();
	}

	public void getCalibrationParameters(MavLinkDrone drone) {
		this.myDrone = drone;
		calParameterItems.clear();
		paramCount = 0;
		readCalibrationParameter(0);
	}

	private void readCalibrationParameter(int seq) {
//		if (seq >= calParameterNames.size()) {
//			if (this.listener != null)
//				this.listener.onReadCalibration();
//			return;
//		}
//
//		if (myDrone != null)
//			myDrone.getParameters().ReadParameter(calParameterNames.get(seq));
//
//		if (this.listener != null) {
//			this.listener.onCalibrationData(seq, calParameterNames.size(), isUpdating);
//		}
	}

	public void sendCalibrationParameters() {
		isUpdating = true;
		if (calParameterItems.size() > 0 && uploadIndex < paramCount) {
			if (this.listener != null) {
				this.listener.onCalibrationData(uploadIndex, paramCount, isUpdating);
			}
			if (myDrone != null) {
				myDrone.getParameters().sendParameter(calParameterItems.get(uploadIndex));
			}
		} else {
			isUpdating = false;
			uploadIndex = 0;
			if (this.listener != null) {
				this.listener.onSentCalibration();
			}
		}
	}

	public void sendParameters(Context context) {
		isUpdating = true;
			if (this.listener != null) {
				this.listener.onCalibrationData(uploadIndex, paramCount, isUpdating);
			}
			if (myDrone != null) {
//				drone.writeParameters(new Parameters(calParameterItems));
				for (Parameter param : calParameterItems) {
					myDrone.getParameters().sendParameter(param);
				}
				Toast.makeText(context,"校准完成", Toast.LENGTH_SHORT).show();
			}
	}

	public boolean isParameterDownloaded() {
		return calParameterItems.size() == calParameterNames.size();
	}

	public double getParamValue(int paramIndex) {
		if (paramIndex >= calParameterItems.size())
			return -1;
		Parameter param = calParameterItems.get(paramIndex);
		return param.value;
	}

	public double getParamValueByName(String paramName) {
		for (Parameter param : calParameterItems) {
			if (param.name.contentEquals(paramName)) {
				return param.value;
			}
		}
		return -1;
	}

	public void setParamValue(int paramIndex, double value) {
		if (paramIndex >= calParameterItems.size())
			return;
		Parameter param = calParameterItems.get(paramIndex);
		param.value = value;
	}

	public void setParamValueByName(String paramName, double value) {
		for (Parameter param : calParameterItems) {
			if (param.name.contentEquals(paramName)) {
				param.value = value;
				return;
			}
		}
	}
}
