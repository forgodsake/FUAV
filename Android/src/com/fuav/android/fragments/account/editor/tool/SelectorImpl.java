package com.fuav.android.fragments.account.editor.tool;

import android.view.View;
import android.widget.Toast;

import com.fuav.android.proxy.mission.item.MissionItemProxy;

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
class SelectorImpl extends ToolsImpl implements View.OnClickListener {

    SelectorImpl(ToolsFragment fragment) {
        super(fragment);
    }

    @Override
    public void onListItemClick(MissionItemProxy item) {
        if (missionProxy == null)
            return;

        if (missionProxy.selection.selectionContains(item)) {
            missionProxy.selection.removeItemFromSelection(item);
        } else {
            missionProxy.selection.addToSelection(item);
        }
    }

    private void selectAll() {
        if (missionProxy == null)
            return;

        missionProxy.selection.setSelectionTo(missionProxy.getItems());
        ToolsFragment.ToolListener listener = toolsFragment.listener;
        if (listener != null)
            listener.zoomToFitSelected();
    }

    @Override
    public EditorToolsFragment.EditorTools getEditorTools() {
        return EditorToolsFragment.EditorTools.SELECTOR;
    }

    @Override
    public void setup() {
        ToolsFragment.ToolListener listener = toolsFragment.listener;
        if (listener != null) {
            listener.enableGestureDetection(false);
            listener.skipMarkerClickEvents(false);
        }

        Toast.makeText(toolsFragment.getContext(), "Click on mission items to select them.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        selectAll();
    }
}
