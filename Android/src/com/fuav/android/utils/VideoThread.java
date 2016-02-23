package com.fuav.android.utils;

import android.content.Context;

import com.demo.sdk.DisplayView;
import com.demo.sdk.Enums;
import com.demo.sdk.Module;
import com.demo.sdk.Player;

public class VideoThread extends Thread {
	
	private Context context;
	private static Module _module=null;
	private DisplayView _displayView;
	private static String _moduleIp = "192.168.100.106";

	public VideoThread(DisplayView displayView, Context context){
		this._displayView = displayView;
		this.context = context;
	}
    
	@Override
	public void run() {
		super.run();
		if (_module == null) {
			_module = new Module(context);
		} else {
			_module.setContext(context);
		}


		_module.setUsername("admin");
		_module.setPassword("admin");
		_module.setPlayerPort(554);
		_module.setModuleIp(_moduleIp);

		Player _player = _module.getPlayer();
		_player.setTimeout(10000);
		_player.setRecordFrameRate(10);

		_player.setDisplayView(_displayView);
		Enums.Pipe _pipe = Enums.Pipe.H264_PRIMARY;
		_player.play(_pipe, Enums.Transport.UDP);

		_displayView.setFullScreen(true);
	}
	
}
