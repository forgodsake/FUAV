package com.fuav.android.core.mission.commands;

import java.util.List;

import com.fuav.android.core.mission.Mission;
import com.fuav.android.core.mission.MissionItemImpl;
import com.fuav.android.core.mission.MissionItemType;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.GRIPPER_ACTIONS;
import com.MAVLink.enums.MAV_CMD;

public class EpmGripperImpl extends MissionCMD {

	private boolean release = true;

	public EpmGripperImpl(MissionItemImpl item) {
		super(item);
	}

	public EpmGripperImpl(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public EpmGripperImpl(Mission mission, boolean release) {
		super(mission);
		this.release = release;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_GRIPPER;
		mavMsg.param2 = release ? GRIPPER_ACTIONS.GRIPPER_ACTION_RELEASE : GRIPPER_ACTIONS.GRIPPER_ACTION_GRAB;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		if (mavMsg.param2 == GRIPPER_ACTIONS.GRIPPER_ACTION_GRAB) {
			release = false;
		} else if (mavMsg.param2 == GRIPPER_ACTIONS.GRIPPER_ACTION_RELEASE) {
			release = true;
		}
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.EPM_GRIPPER;
	}

	public boolean isRelease() {
		return release;
	}

	public void setAsRelease(boolean release) {
		this.release = release;
	}
}