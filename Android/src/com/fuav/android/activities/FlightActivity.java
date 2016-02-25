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

import com.fuav.android.R;
import com.fuav.android.fragments.FlightDataFragment;
import com.fuav.android.fragments.FlightMapFragment;
import com.fuav.android.fragments.actionbar.ActionBarTelemFragment;
import com.fuav.android.fragments.widget.video.VideoControlFragment;
import com.fuav.android.fragments.widget.video.VideoFragment;
import com.fuav.android.utils.prefs.DroidPlannerPrefs;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class FlightActivity extends DrawerNavigationUI implements SlidingUpPanelLayout.PanelSlideListener,View.OnClickListener {

    private FlightDataFragment flightData;
    private FragmentManager fm;
    private int index = 0;

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

        fm = getSupportFragmentManager();

        //Add the flight data fragment
//        flightData = (FlightDataFragment) fm.findFragmentById(R.id.flight_data_container);
        if(flightData == null){
            Bundle args = new Bundle();
            args.putBoolean(FlightDataFragment.EXTRA_SHOW_ACTION_DRAWER_TOGGLE, true);

            flightData = new FlightDataFragment();
            flightData.setArguments(args);
            fm.beginTransaction().add(R.id.flight_data_container, flightData).commit();
        }

        setVisible();
        if (findViewById(R.id.video_view2).getVisibility()==View.VISIBLE){
            fm.beginTransaction().replace(getVideoView(),new VideoFragment()).commit();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        findViewById(R.id.controlview).setOnClickListener(this);
        setVisible();
        if (findViewById(R.id.video_view).getVisibility()==View.VISIBLE){
            fm.beginTransaction().replace(getVideoView(),new VideoFragment()).commit();
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

    public void setVisible(){
        DroidPlannerPrefs pre = new DroidPlannerPrefs(this);
        if(pre.getMapProviderName().equals("GOOGLE_MAP")){
            findViewById(R.id.video_view).setVisibility(View.VISIBLE);
            findViewById(R.id.video_view2).setVisibility(View.GONE);
        }else{
            findViewById(R.id.video_view2).setVisibility(View.VISIBLE);
            findViewById(R.id.video_view).setVisibility(View.GONE);
        }
    }

    public int getVideoView(){
        int i ;
        DroidPlannerPrefs pre = new DroidPlannerPrefs(this);
        if(pre.getMapProviderName().equals("GOOGLE_MAP")){
            i = R.id.video_view;
        }else{
            i = R.id.video_view2;
        }
        return i;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.controlview:
                if (index%2==0){
                    fm.beginTransaction().replace(getVideoView(),new FlightMapFragment()).commit();
                    fm.beginTransaction().replace(R.id.flight_data_container,new VideoControlFragment()).commit();
                }else{
                    fm.beginTransaction().replace(getVideoView(),new VideoFragment()).commit();
                    fm.beginTransaction().replace(R.id.flight_data_container,new FlightDataFragment()).commit();
                }
                index++;
                break;
            default:
                break;
        }
    }
}
