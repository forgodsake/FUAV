package com.fuav.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.fuav.android.R;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String s = getSharedPreferences("pf",MODE_PRIVATE).getString("first","false");
                if (s!="false"){
                    startActivity(new Intent(SplashActivity.this,HomeActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(SplashActivity.this,WelcomeActivity.class));
                    finish();
                }
            }
        },2000);//delay 2000ms
    }
}
