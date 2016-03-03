package com.fuav.android.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.fuav.android.R;

public class PicPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_play);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        if(path!=null){
            ImageView imageView = (ImageView) findViewById(R.id.picplay);
            Bitmap bm = BitmapFactory.decodeFile(path);
            imageView.setImageBitmap(bm);
        }
    }
}
