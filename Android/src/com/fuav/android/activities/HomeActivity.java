package com.fuav.android.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuav.android.R;
import com.fuav.android.activities.helpers.SuperUI;
import com.fuav.android.fragments.home.DeviceFragment;
import com.fuav.android.fragments.home.LibraryFragment;
import com.fuav.android.fragments.home.MallFragment;
import com.fuav.android.fragments.home.SupportFragment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends SuperUI implements View.OnClickListener{

    private LinearLayout Device,Media,Store,Support;
    private ImageView imDevice,imMedia,imStore,imSupport;
    private TextView txDevice,txMedia,txStore,txSupport;
    private FragmentManager manager;
    private static int index = 0;

    @Override
    protected int getToolbarId() {
        return 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        setContentView(R.layout.activity_home);
        manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content,new DeviceFragment()).commit();
        initViews();
        executeFixedRate();
    }

    private void initViews() {
        Device = (LinearLayout) findViewById(R.id.ViewDevice);
        Device.setOnClickListener(this);
        Media = (LinearLayout) findViewById(R.id.ViewMedia);
        Media.setOnClickListener(this);
        Store = (LinearLayout) findViewById(R.id.ViewStore);
        Store.setOnClickListener(this);
        Support = (LinearLayout) findViewById(R.id.ViewSupport);
        Support.setOnClickListener(this);
        imDevice = (ImageView) findViewById(R.id.imageViewDevice);
        imMedia = (ImageView) findViewById(R.id.imageViewMedia);
        imStore = (ImageView) findViewById(R.id.imageViewStore);
        imSupport = (ImageView) findViewById(R.id.imageViewSupport);
        txDevice = (TextView) findViewById(R.id.textViewDevice);
        txMedia = (TextView) findViewById(R.id.textViewMedia);
        txStore = (TextView) findViewById(R.id.textViewStore);
        txSupport = (TextView) findViewById(R.id.textViewSupport);
        setDefault();
        imDevice.setEnabled(true);
        txDevice.setTextColor(Color.BLUE);
        Device.setBackgroundColor(getResources().getColor(R.color.wallet_holo_blue_light));
    }


    @Override
    public void onClick(View v) {
        setDefault();
        switch (v.getId()){
            case R.id.ViewDevice:
                imDevice.setEnabled(true);
                txDevice.setTextColor(Color.BLUE);
                Device.setBackgroundColor(getResources().getColor(R.color.wallet_holo_blue_light));
                manager.beginTransaction().replace(R.id.content,new DeviceFragment()).commit();
                break;
            case R.id.ViewMedia:
                imMedia.setEnabled(true);
                txMedia.setTextColor(Color.BLUE);
                Media.setBackgroundColor(getResources().getColor(R.color.wallet_holo_blue_light));
                manager.beginTransaction().replace(R.id.content,new LibraryFragment()).commit();
                break;
            case R.id.ViewStore:
                imStore.setEnabled(true);
                txStore.setTextColor(Color.BLUE);
                Store.setBackgroundColor(getResources().getColor(R.color.wallet_holo_blue_light));
                manager.beginTransaction().replace(R.id.content,new MallFragment()).commit();
                break;
            case R.id.ViewSupport:
                imSupport.setEnabled(true);
                txSupport.setTextColor(Color.BLUE);
                Support.setBackgroundColor(getResources().getColor(R.color.wallet_holo_blue_light));
                manager.beginTransaction().replace(R.id.content,new SupportFragment()).commit();
                break;
            default:
                break;
        }
    }

    void setDefault(){
        imDevice.setEnabled(false);
        imMedia.setEnabled(false);
        imStore.setEnabled(false);
        imSupport.setEnabled(false);
        txDevice.setTextColor(Color.BLACK);
        txMedia.setTextColor(Color.BLACK);
        txStore.setTextColor(Color.BLACK);
        txSupport.setTextColor(Color.BLACK);
        Device.setBackgroundColor(Color.WHITE);
        Media.setBackgroundColor(Color.WHITE);
        Store.setBackgroundColor(Color.WHITE);
        Support.setBackgroundColor(Color.WHITE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//        if(keyCode==KeyEvent.KEYCODE_BACK)
//        {
//                    return true;
//        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        index++;
        if(index==1){
            Toast.makeText(this,"press again to exit!",Toast.LENGTH_SHORT).show();
        }else if(index==2){
            finish();
        }
    }

    /**
     * 以固定周期频率执行任务
     */
    public static void executeFixedRate() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        index=0;
                    }
                },
                0,
                2000,
                TimeUnit.MILLISECONDS);
    }
}
