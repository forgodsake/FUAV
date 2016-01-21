package com.fuav.android.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import com.fuav.android.core.mission.Mission;
import com.fuav.android.core.mission.MissionItemImpl;
import com.fuav.android.core.mission.MissionItemType;

import java.util.List;

/**
 * Created by Toby on 7/31/2015.
 */
public class DoJumpImpl extends MissionCMD{
    private int waypoint;
    private int repeatCount;

    public DoJumpImpl(MissionItemImpl item){
        super(item);
    }

    public DoJumpImpl(Mission mission) {
        super(mission);
    }

    public DoJumpImpl(msg_mission_item mavMsg, Mission mission){
        super(mission);
        unpackMAVMessage(mavMsg);
    }

    public DoJumpImpl(Mission mission, int waypoint, int repeatCount){
        super(mission);
        this.waypoint = waypoint;
        this.repeatCount  = repeatCount;
    }

    public int getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(int waypoint) {
        this.waypoint = waypoint;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        waypoint = (int)mavMsg.param1;
        repeatCount = (int)mavMsg.param2;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_DO_JUMP;
        mavMsg.param1 = waypoint;
        mavMsg.param2 = repeatCount;
        return list;
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.DO_JUMP;
    }
}
