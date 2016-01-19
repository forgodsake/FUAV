package com.fuav.android.utils;

import android.content.Context;

import com.demo.sdk.DisplayView;
import com.demo.sdk.Enums;
import com.demo.sdk.Module;
import com.demo.sdk.Player;

public class VideoThread extends Thread {
	
	private Context context;
	private static Module _module=null;
	private Player _player;
	private DisplayView _displayView;
	private static String _moduleIp = "192.168.100.103";
	private Enums.Pipe _pipe = Enums.Pipe.H264_PRIMARY;
	
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

		_module.setModuleIp(_moduleIp);
		_module.setUsername("admin");
		_module.setPassword("admin");
		_module.setPlayerPort(554);

		_player = _module.getPlayer();
		_player.setTimeout(10000);
		_player.setRecordFrameRate(10);

		_player.setDisplayView(_displayView);
		_pipe = Enums.Pipe.H264_PRIMARY;
		_player.play(_pipe, Enums.Transport.UDP);
		_displayView.setFullScreen(true);
	}
	
}
