package com.fuav.android.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.fuav.android.R;

public class PicPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        setContentView(R.layout.activity_pic_play);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        if(path!=null){
            ImageView image = (ImageView) findViewById(R.id.picplay);
            ViewCompat.setTransitionName(image,"TEST");
            Bitmap bm = BitmapFactory.decodeFile(path);
            image.setImageBitmap(bm);
        }
    }
}
