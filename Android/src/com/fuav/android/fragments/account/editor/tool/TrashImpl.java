package com.fuav.android.fragments.account.editor.tool;

import android.content.Context;
import android.view.View;

import com.fuav.android.R;
import com.fuav.android.dialogs.SupportYesNoDialog;
import com.fuav.android.proxy.mission.item.MissionItemProxy;

import java.util.List;

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
class TrashImpl extends ToolsImpl implements View.OnClickListener {

    private static final String CLEAR_SELECTED_DIALOG_TAG = "clearSelectedWaypoints";
    private static final String CLEAR_MISSION_DIALOG_TAG = "clearMission";

    TrashImpl(ToolsFragment fragment) {
        super(fragment);
    }

    @Override
    public void onListItemClick(MissionItemProxy item) {
        if (missionProxy == null)
            return;


        missionProxy.selection.clearSelection();
        missionProxy.removeItem(item);

        if (missionProxy.getItems().size() <= 0) {
            toolsFragment.setTool(ToolsFragment.EditorTools.NONE);
        }
    }

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {
        super.onSelectionUpdate(selected);
    }

    @Override
    public EditorToolsFragment.EditorTools getEditorTools() {
        return EditorToolsFragment.EditorTools.TRASH;
    }

    @Override
    public void setup() {
        ToolsFragment.ToolListener listener = toolsFragment.listener;
        if (listener != null) {
            listener.enableGestureDetection(false);
            listener.skipMarkerClickEvents(false);
        }
    }

    private void doClearMissionConfirmation() {
        if (missionProxy == null || missionProxy.getItems().isEmpty())
            return;

        final Context context = toolsFragment.getContext();
        SupportYesNoDialog ynd = SupportYesNoDialog.newInstance(context, CLEAR_MISSION_DIALOG_TAG,
                context.getString(R.string
                        .dlg_clear_mission_title),
                context.getString(R.string.dlg_clear_mission_confirm));

        if (ynd != null) {
            ynd.show(toolsFragment.getChildFragmentManager(), CLEAR_MISSION_DIALOG_TAG);
        }
    }

    private void deleteSelectedItems() {
        final Context context = toolsFragment.getContext();
        SupportYesNoDialog ynd = SupportYesNoDialog.newInstance(context, CLEAR_SELECTED_DIALOG_TAG,
                context.getString(R.string.delete_selected_waypoints_title),
                context.getString(R.string.delete_selected_waypoints_confirm));

        if (ynd != null) {
            ynd.show(toolsFragment.getChildFragmentManager(), CLEAR_SELECTED_DIALOG_TAG);
        }
    }

    @Override
    public void onDialogYes(String dialogTag) {
        switch (dialogTag) {
            case CLEAR_SELECTED_DIALOG_TAG:
                if (missionProxy != null) {
                    missionProxy.removeSelection(missionProxy.selection);
                    if (missionProxy.selection.getSelected().isEmpty())
                        toolsFragment.setTool(ToolsFragment.EditorTools.NONE);
                }
                break;

            case CLEAR_MISSION_DIALOG_TAG:
                if (missionProxy != null) {
                    missionProxy.clear();
                    toolsFragment.setTool(ToolsFragment.EditorTools.NONE);
                }
                break;
        }
    }

    @Override
    public void onDialogNo(String dialogTag) {
        switch (dialogTag) {
            case CLEAR_SELECTED_DIALOG_TAG:
                if (missionProxy != null)
                    missionProxy.selection.clearSelection();
                break;

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_mission_button:
                doClearMissionConfirmation();
                break;

            case R.id.clear_selected_button:
                deleteSelectedItems();
                break;
        }
    }
}
