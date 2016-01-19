package com.fuav.android.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import com.fuav.android.core.mission.Mission;
import com.fuav.android.core.mission.MissionItemImpl;

import java.util.List;

public abstract class SpatialCoordItem extends MissionItemImpl {

    protected LatLongAlt coordinate;

    public SpatialCoordItem(Mission mission, LatLongAlt coord) {
        super(mission);
        this.coordinate = coord;
    }

    public SpatialCoordItem(MissionItemImpl item) {
        super(item);
        if (item instanceof SpatialCoordItem) {
            coordinate = ((SpatialCoordItem) item).getCoordinate();
        } else {
            coordinate = new LatLongAlt(0, 0, 0);
        }
    }

    public void setCoordinate(LatLongAlt coordNew) {
        coordinate = coordNew;
    }

    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.x = (float) coordinate.getLatitude();
        mavMsg.y = (float) coordinate.getLongitude();
        mavMsg.z = (float) coordinate.getAltitude();
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        setCoordinate(new LatLongAlt(mavMsg.x, mavMsg.y, mavMsg.z));
    }

    public void setAltitude(double altitude) {
        coordinate.setAltitude(altitude);
    }

    public void setPosition(LatLong position) {
        coordinate.set(position);
    }

}