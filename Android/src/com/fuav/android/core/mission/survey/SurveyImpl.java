package com.fuav.android.core.mission.survey;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.o3dr.services.android.lib.coordinate.LatLong;

import com.fuav.android.core.mission.Mission;
import com.fuav.android.core.mission.MissionItemImpl;
import com.fuav.android.core.mission.MissionItemType;
import com.fuav.android.core.mission.commands.CameraTriggerImpl;
import com.fuav.android.core.polygon.Polygon;
import com.fuav.android.core.survey.CameraInfo;
import com.fuav.android.core.survey.SurveyData;
import com.fuav.android.core.survey.grid.Grid;
import com.fuav.android.core.survey.grid.GridBuilder;

import java.util.ArrayList;
import java.util.List;

public class SurveyImpl extends MissionItemImpl {

    public Polygon polygon = new Polygon();
    public SurveyData surveyData = new SurveyData();
    public Grid grid;

    public SurveyImpl(Mission mission, List<LatLong> points) {
        super(mission);
        polygon.addPoints(points);
    }

    public void update(double angle, double altitude, double overlap, double sidelap) {
        surveyData.update(angle, altitude, overlap, sidelap);
    }

    public void setCameraInfo(CameraInfo camera) {
        surveyData.setCameraInfo(camera);
    }

    public void build() throws Exception {
        // TODO find better point than (0,0) to reference the grid
        grid = null;
        GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new LatLong(0, 0));
        polygon.checkIfValid();
        grid = gridBuilder.generate(true);
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        try {
            List<msg_mission_item> list = new ArrayList<msg_mission_item>();
            build();

            list.addAll((new CameraTriggerImpl(mission, surveyData.getLongitudinalPictureDistance())).packMissionItem());
            packGridPoints(list);
            list.addAll((new CameraTriggerImpl(mission, (0.0)).packMissionItem()));

            return list;
        } catch (Exception e) {
            return new ArrayList<msg_mission_item>();
        }
    }

    private void packGridPoints(List<msg_mission_item> list) {
        final double altitude = surveyData.getAltitude();
        for (LatLong point : grid.gridPoints) {
            msg_mission_item mavMsg = getSurveyPoint(point, altitude);
            list.add(mavMsg);
        }
    }

    protected msg_mission_item getSurveyPoint(LatLong point, double altitude){
        return packSurveyPoint(point, altitude);
    }

    public static msg_mission_item packSurveyPoint(LatLong point, double altitude) {
        msg_mission_item mavMsg = new msg_mission_item();
        mavMsg.autocontinue = 1;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
        mavMsg.x = (float) point.getLatitude();
        mavMsg.y = (float) point.getLongitude();
        mavMsg.z = (float) altitude;
        mavMsg.param1 = 0f;
        mavMsg.param2 = 0f;
        mavMsg.param3 = 0f;
        mavMsg.param4 = 0f;
        return mavMsg;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        // TODO Auto-generated method stub

    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.SURVEY;
    }

}
