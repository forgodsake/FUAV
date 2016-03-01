package com.fuav.android.fragments.widget.video;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.demo.sdk.DisplayView;
import com.demo.sdk.Player;
import com.fuav.android.R;
import com.fuav.android.fragments.widget.VideoControlCompFragment;
import com.fuav.android.utils.VideoThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class VideoControlFragment extends Fragment implements View.OnClickListener{

    private View view;
    private VideoThread videoThread;
    private DisplayView displayView;
    private ImageView shot_switch_left;
    private ImageView shot_switch_right;
    private ImageView shot_key;
    private ImageView video;
    private ImageView camera;
    private File _storagePath = Environment.getExternalStorageDirectory();
    private File _projectPath = new File(_storagePath.getPath() + "/FUAV");
    private int index = 0;
    private boolean _recording;


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
        shot_key = (ImageView)view.findViewById(R.id.shot_key_on);
        shot_key.setOnClickListener(this);
        shot_switch_left = (ImageView) view.findViewById(R.id.shot_switch_left);
        shot_switch_right = (ImageView) view.findViewById(R.id.shot_switch_right);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        shot_switch_left.measure(w, h);
        int loc = shot_switch_left.getMeasuredWidth();
        final Animation animationl = new TranslateAnimation(0f,(1.75f)*loc,0f,0f);
        animationl.setDuration(800);
        final Animation animationr = new TranslateAnimation(0f,(-1.75f)*loc,0f,0f);
        animationr.setDuration(800);
        video = (ImageView) view.findViewById(R.id.video);
        camera = (ImageView) view.findViewById(R.id.camera);
        shot_switch_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(index%2==0){
                    shot_switch_left.startAnimation(animationl);
                    shot_switch_left.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            shot_switch_right.setVisibility(View.VISIBLE);
                            video.setImageResource(R.drawable.video_off);
                            camera.setImageResource(R.drawable.camera_on);
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
                    shot_switch_right.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            shot_switch_left.setVisibility(View.VISIBLE);
                            video.setImageResource(R.drawable.video_on);
                            camera.setImageResource(R.drawable.camera_off);
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
        videoThread = new VideoThread(displayView,getActivity());
        videoThread.start();
        getChildFragmentManager().beginTransaction().replace(R.id.mission_view, new VideoControlCompFragment()).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.shot_key_on:
                Calendar c = Calendar.getInstance();
                int time = c.get(Calendar.MILLISECOND);
                Player _player = VideoThread._module.getPlayer();
                if(shot_switch_left.getVisibility()==View.VISIBLE){
                    Bitmap photo = _player.takePhoto();
                    if (photo == null) {
                        Toast.makeText(getActivity(),"拍照失败,请先连接相机.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String path = _projectPath.getPath() + "/" + Integer.toString(time) + ".jpeg";
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(path);
                        photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    } catch (IOException e) {

                    }
                      finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException ignored) {}
                    }
                    Toast.makeText(getActivity(),"照片已存储",Toast.LENGTH_SHORT).show();
                }else{
                    if (_recording) {
                        _player.endRecord();
                        _recording = false;
                        Toast.makeText(getActivity(),"结束录像",Toast.LENGTH_SHORT).show();
                    } else {
                        // file extension is mkv (default)
                        if (_player.beginRecord(_projectPath.getPath(), Integer.toString(time))) {
                            _recording = true;
                            Toast.makeText(getActivity(),"开始录像",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(),"录制失败,请先连接相机.",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }
}
