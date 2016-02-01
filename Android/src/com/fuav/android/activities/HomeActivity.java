package com.fuav.android.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.fuav.android.R;
import com.fuav.android.activities.helpers.SuperUI;
import com.fuav.android.fragments.home.DeviceFragment;
import com.fuav.android.fragments.home.LibraryFragment;
import com.fuav.android.fragments.home.MallFragment;
import com.fuav.android.fragments.home.SupportFragment;

public class HomeActivity extends SuperUI implements View.OnClickListener{

    private ImageView imDevice,imMedia,imStore,imSupport;
    private FragmentManager manager;
    private MallFragment mallFragment;
    private int index=0;


    @Override
    protected int getToolbarId() {
        return 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content,new DeviceFragment()).commit();
        initViews();
    }

    private void initViews() {
        imDevice = (ImageView) findViewById(R.id.imageViewDevice);
        imDevice.setOnClickListener(this);
        imMedia = (ImageView) findViewById(R.id.imageViewMedia);
        imMedia.setOnClickListener(this);
        imStore = (ImageView) findViewById(R.id.imageViewStore);
        imStore.setOnClickListener(this);
        imSupport = (ImageView) findViewById(R.id.imageViewSupport);
        imSupport.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageViewDevice:
                manager.beginTransaction().replace(R.id.content,new DeviceFragment()).commit();
                index = 1;
                break;
            case R.id.imageViewMedia:
                manager.beginTransaction().replace(R.id.content,new LibraryFragment()).commit();
                index = 2;
                break;
            case R.id.imageViewStore:
                mallFragment = new MallFragment();
                manager.beginTransaction().replace(R.id.content,mallFragment).commit();
                index = 3;
                break;
            case R.id.imageViewSupport:
                manager.beginTransaction().replace(R.id.content,new SupportFragment()).commit();
                index = 4;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            if (null!=mallFragment&&index==3){
                WebView webView = (WebView)mallFragment.getView().findViewById(R.id.webView);
                if(webView.canGoBack())
                {
                    webView.goBack();//返回上一页面
                    return true;
                }
            }

        }
        return super.onKeyDown(keyCode, event);
    }
}
