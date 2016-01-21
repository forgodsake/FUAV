package com.fuav.android.dialogs.openfile;

import com.o3dr.services.android.lib.drone.property.Parameter;

import java.util.List;

import com.fuav.android.utils.file.IO.ParameterReader;

public abstract class OpenParameterDialog extends OpenFileDialog {
	public abstract void parameterFileLoaded(List<Parameter> parameters);

	@Override
	protected FileReader createReader() {
		return new ParameterReader();
	}

	@Override
	protected void onDataLoaded(FileReader reader) {
		parameterFileLoaded(((ParameterReader) reader).getParameters());
	}
}