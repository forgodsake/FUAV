package com.fuav.android.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;

import com.fuav.android.R;
import com.fuav.android.view.FullScreenVideoView;

public class VideoPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        setContentView(R.layout.activity_video_play);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        if(path!=null){
            final FullScreenVideoView videoView = (FullScreenVideoView) findViewById(R.id.videoplay);
            ViewCompat.setTransitionName(videoView,"VIDEO");
            videoView.setVideoPath(path);
            videoView.setMediaController(new MediaController(this));
            videoView.start();
            videoView.requestFocus();
        }
    }
}
