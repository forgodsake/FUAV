package com.fuav.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.fuav.android.R;

import java.io.InputStream;

public class AnimateActivity extends Activity {

    private ImageView imageViewleft;
    private ImageView imageViewright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animate_layout);
        imageViewleft = (ImageView) findViewById(R.id.imageView3);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = getResources().openRawResource(R.drawable.left);
        imageViewleft.setImageBitmap(BitmapFactory.decodeStream(is, null, opt));
        imageViewright = (ImageView) findViewById(R.id.imageView4);
        InputStream is1 = getResources().openRawResource(R.drawable.right);
        imageViewright.setImageBitmap(BitmapFactory.decodeStream(is1, null, opt));
        AnimationSet animationSetLeft = new AnimationSet(false);
        Animation alphaAnimation = new AlphaAnimation(1.0f,0.4f);
        Animation transAnimation = new TranslateAnimation(0f,-716f,0f,0f);
        transAnimation.setInterpolator(new AccelerateInterpolator());
        animationSetLeft.addAnimation(alphaAnimation);
        animationSetLeft.addAnimation(transAnimation);
        animationSetLeft.setDuration(1200);
        animationSetLeft.setFillAfter(true);
        imageViewleft.startAnimation(animationSetLeft);
        AnimationSet animationSetRight = new AnimationSet(false);
        Animation alphaAnimation2 = new AlphaAnimation(1.0f,0.4f);
        Animation transAnimation2 = new TranslateAnimation(0f,716f,0f,0f);
        transAnimation2.setInterpolator(new AccelerateInterpolator());
        animationSetRight.addAnimation(alphaAnimation2);
        animationSetRight.addAnimation(transAnimation2);
        animationSetRight.setDuration(1200);
        animationSetRight.setFillAfter(true);
        imageViewright.startAnimation(animationSetRight);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(AnimateActivity.this,HomeActivity.class));
                finish();
            }
        },1200);
    }
}
