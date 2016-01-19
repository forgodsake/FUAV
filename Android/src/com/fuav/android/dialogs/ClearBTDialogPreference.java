package com.fuav.android.dialogs;

import com.fuav.android.utils.prefs.DroidPlannerPrefs;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class ClearBTDialogPreference extends DialogPreference {

    public interface OnResultListener {
        void onResult(boolean result);
    }

	private DroidPlannerPrefs mAppPrefs;

    private OnResultListener listener;

	public ClearBTDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAppPrefs = new DroidPlannerPrefs(context.getApplicationContext());
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			mAppPrefs.setBluetoothDeviceAddress("");
		}

        if(listener != null)
            listener.onResult(positiveResult);
	}

    public void setOnResultListener(OnResultListener listener){
        this.listener = listener;
    }

}
