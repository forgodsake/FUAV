package com.fuav.android.core.mission.commands;

import java.util.List;

import com.fuav.android.core.mission.Mission;
import com.fuav.android.core.mission.MissionItemImpl;

import com.MAVLink.common.msg_mission_item;

public abstract class MissionCMD extends MissionItemImpl {

	public MissionCMD(Mission mission) {
		super(mission);
	}

	public MissionCMD(MissionItemImpl item) {
		super(item);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		return super.packMissionItem();
	}

}