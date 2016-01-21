package com.fuav.android.dialogs.openfile;

import com.fuav.android.utils.file.IO.MissionReader;

public abstract class OpenMissionDialog extends OpenFileDialog {
	public abstract void waypointFileLoaded(MissionReader reader);

	@Override
	protected FileReader createReader() {
		return new MissionReader();
	}

	@Override
	protected void onDataLoaded(FileReader reader) {
		waypointFileLoaded((MissionReader) reader);
	}
}
