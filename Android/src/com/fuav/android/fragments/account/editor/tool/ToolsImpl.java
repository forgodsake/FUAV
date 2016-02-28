package com.fuav.android.fragments.account.editor.tool;

import android.os.Bundle;

import com.fuav.android.dialogs.SupportYesNoDialog;
import com.fuav.android.proxy.mission.MissionProxy;
import com.fuav.android.proxy.mission.MissionSelection;
import com.fuav.android.proxy.mission.item.MissionItemProxy;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.List;

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
public abstract class ToolsImpl implements MissionSelection.OnSelectionUpdateListener, SupportYesNoDialog.Listener {

    protected MissionProxy missionProxy;
    protected final ToolsFragment toolsFragment;

    ToolsImpl(ToolsFragment fragment) {
        this.toolsFragment = fragment;
    }

    void setMissionProxy(MissionProxy missionProxy) {
        this.missionProxy = missionProxy;
    }

    void onSaveInstanceState(Bundle outState) {
    }

    void onRestoreInstanceState(Bundle savedState) {
    }

    public void onMapClick(LatLong point) {
        if (missionProxy == null) return;

        // If an mission item is selected, unselect it.
        missionProxy.selection.clearSelection();
    }

    public void onListItemClick(MissionItemProxy item) {
        if (missionProxy == null)
            return;

        if (missionProxy.selection.selectionContains(item)) {
            missionProxy.selection.clearSelection();
        } else {
            toolsFragment.setTool(ToolsFragment.EditorTools.NONE);
            missionProxy.selection.setSelectionTo(item);
        }
    }

    public void onPathFinished(List<LatLong> path) {
    }

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {

    }

    public abstract EditorToolsFragment.EditorTools getEditorTools();

    public abstract void setup();

    @Override
    public void onDialogYes(String dialogTag){

    }

    @Override
    public void onDialogNo(String dialogTag){

    }

}
