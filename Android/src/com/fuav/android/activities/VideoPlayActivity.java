package com.fuav.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.fuav.android.R;

public class VideoPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        if(path!=null){
//            path.replace("/storage/emulated/0/","/sdcard/");
            Toast.makeText(this,path,Toast.LENGTH_SHORT).show();
            VideoView videoView = (VideoView) findViewById(R.id.videoplay);
            videoView.setVideoPath(path);
            videoView.setMediaController(new MediaController(this));
            videoView.start();
            videoView.requestFocus();
        }
    }
}
