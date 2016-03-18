package com.fuav.android.fragments.widget.video;


import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demo.sdk.DisplayView;
import com.fuav.android.R;
import com.fuav.android.utils.VideoThread;
import com.fuav.android.utils.prefs.DroidPlannerPrefs;

public class VideoFragment extends Fragment {

    private DisplayView displayView;


    public VideoFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_widget_sololink_video, container, false);
        displayView = (DisplayView) view.findViewById(R.id.full_video_view);
        if(!getMapName().equals("GOOGLE_MAP")){
            GLSurfaceView surfaceView = (GLSurfaceView) displayView.getChildAt(0);
            surfaceView.setZOrderOnTop(true);
        }
        return view;
    }

    public String getMapName(){
        DroidPlannerPrefs pre = new DroidPlannerPrefs(getContext());
        return pre.getMapProviderName();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new VideoThread(displayView,getActivity()).start();
    }

}
