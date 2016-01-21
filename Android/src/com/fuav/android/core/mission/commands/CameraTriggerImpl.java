package com.fuav.android.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import com.fuav.android.core.mission.Mission;
import com.fuav.android.core.mission.MissionItemImpl;
import com.fuav.android.core.mission.MissionItemType;

import java.util.List;

public class CameraTriggerImpl extends MissionCMD {
    private double distance = (0);

    public CameraTriggerImpl(MissionItemImpl item) {
        super(item);
    }

    public CameraTriggerImpl(msg_mission_item msg, Mission mission) {
        super(mission);
        unpackMAVMessage(msg);
    }

    public CameraTriggerImpl(Mission mission, double triggerDistance) {
        super(mission);
        this.distance = triggerDistance;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST;
        mavMsg.param1 = (float) distance;
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        distance = (mavMsg.param1);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.CAMERA_TRIGGER;
    }

    public double getTriggerDistance() {
        return distance;
    }

    public void setTriggerDistance(double triggerDistance) {
        this.distance = triggerDistance;
    }
}