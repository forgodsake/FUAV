package com.fuav.android.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.fuav.android.R;
import com.fuav.android.fragments.FlightMapFragment;
import com.fuav.android.fragments.actionbar.ActionBarTelemFragment;
import com.fuav.android.fragments.widget.VideoControlFragment;
import com.fuav.android.fragments.widget.video.FullVideoFragment;
import com.fuav.android.utils.prefs.DroidPlannerPrefs;

public class VideoActivity extends DrawerNavigationUI {

    private ImageView shot_switch_left;
    private ImageView shot_switch_right;
    private ImageView video;
    private ImageView camera;
    private FrameLayout control_frame;
    private int index = 0;


    @Override
    protected int getToolbarId() {
        return R.id.actionbar_container;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        setContentView(R.layout.activity_video);

        control_frame = (FrameLayout) findViewById(R.id.control_view);
        control_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoActivity.this, FlightActivity.class);
//              intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });


        final Animation animationl = new TranslateAnimation(0f,200f,0f,0f);
        animationl.setDuration(800);
        final Animation animationr = new TranslateAnimation(0f,200f,0f,0f);
        animationl.setDuration(800);
        shot_switch_left = (ImageView) findViewById(R.id.shot_switch_left);
        shot_switch_right = (ImageView) findViewById(R.id.shot_switch_right);
        video = (ImageView) findViewById(R.id.video);
        camera = (ImageView) findViewById(R.id.camera);
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
                if(index%2==0){
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
        FragmentManager fm = getSupportFragmentManager();
        FlightMapFragment flightMapFragment = new FlightMapFragment();

        DroidPlannerPrefs dpPrefs = new DroidPlannerPrefs(this);
        if(dpPrefs.getMapProviderName().equals("百度地图")){
            fm.beginTransaction().replace(R.id.map_view, flightMapFragment).commit();
        }else{
            fm.beginTransaction().replace(R.id.map_view2, flightMapFragment).commit();
        }
        fm.beginTransaction().replace(R.id.widget_view, new FullVideoFragment()).commit();
        fm.beginTransaction().replace(R.id.mission_view, new VideoControlFragment()).commit();

    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return 0;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void addToolbarFragment() {
        int toolbarId = getToolbarId();
        FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarTelem = fm.findFragmentById(toolbarId);
        if (actionBarTelem == null) {
            actionBarTelem = new ActionBarTelemFragment();
            fm.beginTransaction().add(toolbarId, actionBarTelem).commit();
        }
    }
}
