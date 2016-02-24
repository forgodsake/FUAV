package com.fuav.android.fragments.widget.video;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.demo.sdk.DisplayView;
import com.fuav.android.R;
import com.fuav.android.fragments.widget.VideoControlCompFragment;
import com.fuav.android.utils.VideoThread;

public class VideoControlFragment extends Fragment {

    private View view;
    private DisplayView displayView;
    private ImageView shot_switch_left;
    private ImageView shot_switch_right;
    private ImageView video;
    private ImageView camera;
    private int index = 0;


    public VideoControlFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video, container, false);
        displayView = (DisplayView) view.findViewById(R.id.full_video_view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Animation animationl = new TranslateAnimation(0f,200f,0f,0f);
        animationl.setDuration(800);
        final Animation animationr = new TranslateAnimation(0f,-200f,0f,0f);
        animationr.setDuration(800);
        shot_switch_left = (ImageView) view.findViewById(R.id.shot_switch_left);
        shot_switch_right = (ImageView) view.findViewById(R.id.shot_switch_right);
        video = (ImageView) view.findViewById(R.id.video);
        camera = (ImageView) view.findViewById(R.id.camera);
        shot_switch_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(index%2==0){
                    shot_switch_left.startAnimation(animationl);
                    video.setImageResource(R.drawable.video_off);
                    camera.setImageResource(R.drawable.camera_on);
                    shot_switch_left.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            shot_switch_right.setVisibility(View.VISIBLE);
                        }
                    }, 800);//delay 2000ms
                    index++;
                }
            }
        });
        shot_switch_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(index%2==1){
                    shot_switch_right.startAnimation(animationr);
                    video.setImageResource(R.drawable.video_on);
                    camera.setImageResource(R.drawable.camera_off);
                    shot_switch_right.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            shot_switch_left.setVisibility(View.VISIBLE);
                        }
                    }, 800);//delay 2000ms
                    index++;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new VideoThread(displayView,getActivity()).start();
            FragmentManager fm = getChildFragmentManager();
            fm.beginTransaction().replace(R.id.mission_view, new VideoControlCompFragment()).commit();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
