package com.fuav.android.fragments.account.editor.tool;

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
class NoneImpl extends ToolsImpl {

    NoneImpl(ToolsFragment fragment) {
        super(fragment);
    }

    @Override
    public EditorToolsFragment.EditorTools getEditorTools() {
        return EditorToolsFragment.EditorTools.NONE;
    }

    @Override
    public void setup() {
        ToolsFragment.ToolListener listener = toolsFragment.listener;
        if (listener != null) {
            listener.enableGestureDetection(false);
            listener.skipMarkerClickEvents(false);
        }
    }
}
