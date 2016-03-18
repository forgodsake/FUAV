package com.fuav.android.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;

import com.fuav.android.R;
import com.fuav.android.fragments.support.AboutUsFragment;
import com.fuav.android.fragments.support.ContactUsFragment;
import com.fuav.android.fragments.support.FAQFragment;
import com.fuav.android.fragments.support.FeedBackFragment;
import com.fuav.android.fragments.support.ScopeOfServiceFragment;
import com.fuav.android.fragments.support.UserAgreementFragment;

public class SupportActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        setContentView(R.layout.activity_support);
        FragmentManager fragmentManager = getSupportFragmentManager();
        String detail = getIntent().getStringExtra("detail");
        switch (detail){
            case "ContactUs":
                fragmentManager.beginTransaction().add(R.id.support_frame,new ContactUsFragment()).commit();
                break;
            case "FAQ":
                fragmentManager.beginTransaction().add(R.id.support_frame,new FAQFragment()).commit();
                break;
            case "ScopeOfService":
                fragmentManager.beginTransaction().add(R.id.support_frame,new ScopeOfServiceFragment()).commit();
                break;
            case "AboutUs":
                fragmentManager.beginTransaction().add(R.id.support_frame,new AboutUsFragment()).commit();
                break;
            case "UserAgreement":
                fragmentManager.beginTransaction().add(R.id.support_frame,new UserAgreementFragment()).commit();
                break;
            case "FeedBack":
                fragmentManager.beginTransaction().add(R.id.support_frame,new FeedBackFragment()).commit();
                break;
            default:
                break;
        }
    }
}
