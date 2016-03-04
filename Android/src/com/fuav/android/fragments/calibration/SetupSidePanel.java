package com.fuav.android.fragments.calibration;

import com.fuav.android.fragments.helpers.ApiListenerFragment;

public abstract class SetupSidePanel extends ApiListenerFragment {
	public abstract void updateTitle(int idTitle);

	public abstract void updateDescription(int idDescription);

}
