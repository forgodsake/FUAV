package com.fuav.android.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.demo.sdk.DisplayView;
import com.demo.sdk.Enums;
import com.demo.sdk.Module;
import com.demo.sdk.Player;
import com.fuav.android.R;
import com.fuav.android.fragments.FlightDataFragment;
import com.fuav.android.fragments.actionbar.ActionBarTelemFragment;
import com.fuav.android.fragments.widget.video.MiniVideoFragment;
import com.fuav.android.utils.prefs.DroidPlannerPrefs;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class FlightActivity extends DrawerNavigationUI implements SlidingUpPanelLayout.PanelSlideListener {

    private FlightDataFragment flightData;
    private MiniVideoFragment miniVideo2;

    @Override
    public void onDrawerClosed() {
        super.onDrawerClosed();

        if (flightData != null)
            flightData.onDrawerClosed();
    }

    @Override
    public void onDrawerOpened() {
        super.onDrawerOpened();

        if (flightData != null)
            flightData.onDrawerOpened();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏

        setContentView(R.layout.activity_flight);

        final FragmentManager fm = getSupportFragmentManager();

        //Add the flight data fragment
        flightData = (FlightDataFragment) fm.findFragmentById(R.id.flight_data_container);
        if(flightData == null){
            Bundle args = new Bundle();
            args.putBoolean(FlightDataFragment.EXTRA_SHOW_ACTION_DRAWER_TOGGLE, true);

            flightData = new FlightDataFragment();
            flightData.setArguments(args);
            fm.beginTransaction().add(R.id.flight_data_container, flightData).commit();
        }

        DroidPlannerPrefs pre = new DroidPlannerPrefs(this);
        miniVideo2 = new MiniVideoFragment();

        if(!pre.getMapProviderName().equals("GOOGLE_MAP")){

            fm.beginTransaction()
                    .replace(R.id.video_view2, miniVideo2)
                    .commit();
        }else{

            fm.beginTransaction()
                    .remove(miniVideo2)
                    .commit();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        DroidPlannerPrefs pre = new DroidPlannerPrefs(this);

        final FragmentManager fm = getSupportFragmentManager();

        findViewById(R.id.controlview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FlightActivity.this,WidgetActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                fm.beginTransaction()
                        .remove(miniVideo2)
                        .commit();

            }
        });

//        MiniVideoFragment miniVideo = new MiniVideoFragment();

        if(pre.getMapProviderName().equals("GOOGLE_MAP")){

            findViewById(R.id.video_view).setVisibility(View.VISIBLE);

            Module _module= new Module(this);
            DisplayView _displayView = (DisplayView)findViewById(R.id.video_view);
            String _moduleIp = "192.168.100.108";


            _module.setUsername("admin");
            _module.setPassword("admin");
            _module.setPlayerPort(554);
            _module.setModuleIp(_moduleIp);

            Player _player = _module.getPlayer();
            _player.setTimeout(10000);

            _player.setDisplayView(_displayView);
            Enums.Pipe _pipe = Enums.Pipe.H264_PRIMARY;
            _player.play(_pipe, Enums.Transport.UDP);

            _displayView.setFullScreen(true);

//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.video_view, miniVideo)
//                    .commit();
        }else{

//            getSupportFragmentManager().beginTransaction()
//                    .remove(miniVideo)
//                    .commit();

            findViewById(R.id.video_view).setVisibility(View.GONE);
        }




    }



    @Override
    protected void onToolbarLayoutChange(int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom){
        if(flightData != null)
            flightData.updateActionbarShadow(bottom);
    }

    @Override
    protected void addToolbarFragment() {
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarTelem = fm.findFragmentById(toolbarId);
        if (actionBarTelem == null) {
            actionBarTelem = new ActionBarTelemFragment();
            fm.beginTransaction().add(toolbarId, actionBarTelem).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_flight_data;
    }

    @Override
    protected boolean enableMissionMenus() {
        return true;
    }

    @Override
    public void onPanelSlide(View view, float v) {

        //Update the bottom margin for the action drawer
        final View flightActionBar = ((ViewGroup)view).getChildAt(0);
        final int[] viewLocs = new int[2];
        flightActionBar.getLocationInWindow(viewLocs);
    }

    @Override
    public void onPanelCollapsed(View view) {

        //Reset the bottom margin for the action drawer
        final View flightActionBar = ((ViewGroup)view).getChildAt(0);
        final int[] viewLocs = new int[2];
        flightActionBar.getLocationInWindow(viewLocs);
    }

    @Override
    public void onPanelExpanded(View view) {
        //Update the bottom margin for the action drawer
        final View flightActionBar = ((ViewGroup)view).getChildAt(0);
        final int[] viewLocs = new int[2];
        flightActionBar.getLocationInWindow(viewLocs);
    }

    @Override
    public void onPanelAnchored(View view) {

    }

    @Override
    public void onPanelHidden(View view) {

        final View flightActionBar = ((ViewGroup)view).getChildAt(0);
        final int[] viewLocs = new int[2];
        flightActionBar.getLocationInWindow(viewLocs);
    }
}
