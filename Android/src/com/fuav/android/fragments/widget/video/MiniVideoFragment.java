package com.fuav.android.fragments.widget.video;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demo.sdk.DisplayView;
import com.fuav.android.R;
import com.fuav.android.utils.VideoThread;

public class MiniVideoFragment extends Fragment {

    private DisplayView displayView;


    public MiniVideoFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mini_video, container, false);
        displayView = (DisplayView) view.findViewById(R.id.sololink_video_view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        new VideoThread(displayView,getActivity()).start();
    }
}
