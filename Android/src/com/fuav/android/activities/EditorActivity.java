package com.fuav.android.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuav.android.R;
import com.fuav.android.activities.interfaces.OnEditorInteraction;
import com.fuav.android.core.drone.DroneManager;
import com.fuav.android.dialogs.SlideToUnlockDialog;
import com.fuav.android.dialogs.SupportEditInputDialog;
import com.fuav.android.dialogs.openfile.OpenFileDialog;
import com.fuav.android.dialogs.openfile.OpenMissionDialog;
import com.fuav.android.fragments.BlankFragment;
import com.fuav.android.fragments.EditorMapFragment;
import com.fuav.android.fragments.FlightMapFragment;
import com.fuav.android.fragments.SettingsFragment;
import com.fuav.android.fragments.account.editor.tool.EditorToolsFragment;
import com.fuav.android.fragments.account.editor.tool.EditorToolsFragment.EditorTools;
import com.fuav.android.fragments.account.editor.tool.EditorToolsImpl;
import com.fuav.android.fragments.actionbar.ActionBarTelemFragment;
import com.fuav.android.fragments.helpers.GestureMapFragment;
import com.fuav.android.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
import com.fuav.android.fragments.widget.video.VideoControlFragment;
import com.fuav.android.fragments.widget.video.VideoFragment;
import com.fuav.android.proxy.mission.MissionProxy;
import com.fuav.android.proxy.mission.MissionSelection;
import com.fuav.android.proxy.mission.item.MissionItemProxy;
import com.fuav.android.proxy.mission.item.fragments.MissionDetailFragment;
import com.fuav.android.utils.analytics.GAUtils;
import com.fuav.android.utils.file.FileStream;
import com.fuav.android.utils.file.IO.MissionReader;
import com.fuav.android.utils.location.CheckLocationSettings;
import com.fuav.android.utils.prefs.AutoPanMode;
import com.fuav.android.utils.prefs.DroidPlannerPrefs;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.FollowApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

import org.beyene.sius.unit.length.LengthUnit;

import java.util.List;

/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends DrawerNavigationUI implements OnPathFinishedListener,
        EditorToolsFragment.EditorToolListener, MissionDetailFragment.OnMissionDetailListener,
        OnEditorInteraction, MissionSelection.OnSelectionUpdateListener, OnClickListener,
        OnLongClickListener, SupportEditInputDialog.Listener {

    private static final double DEFAULT_SPEED = 5; //meters per second.

    public static final int FOLLOW_SETTINGS_UPDATE = 147;

    private static final int FOLLOW_LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private static final long FOLLOW_LOCATION_UPDATE_INTERVAL = 30000; // ms
    private static final long FOLLOW_LOCATION_UPDATE_FASTEST_INTERVAL = 5000; // ms
    private static final float FOLLOW_LOCATION_UPDATE_MIN_DISPLACEMENT = 0; // m

    /**
     * Used to retrieve the item detail window when the activity is destroyed,
     * and recreated.
     */
    private static final String ITEM_DETAIL_TAG = "Item Detail Window";
    private static final String MISSION_UPLOAD_CHECK_DIALOG_TAG = "Mission Upload check.";

    /**
     * Determines how long the failsafe view is visible for.
     */
    private static final long WARNING_VIEW_DISPLAY_TIMEOUT = 10000l; //ms

    private static final String ACTION_FLIGHT_ACTION_BUTTON = "Copter flight action button";

    private static final String EXTRA_OPENED_MISSION_FILENAME = "extra_opened_mission_filename";

    private static final IntentFilter eventFilter = new IntentFilter();
    private static final String MISSION_FILENAME_DIALOG_TAG = "Mission filename";
    private int index = 0;
    private int showhidearrow = 0;
    private boolean showVideo;

    static {
        eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
        eventFilter.addAction(AttributeEvent.PARAMETERS_REFRESH_COMPLETED);
        eventFilter.addAction(AttributeEvent.STATE_ARMING);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_UPDATED);
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.FOLLOW_START);
        eventFilter.addAction(AttributeEvent.FOLLOW_STOP);
        eventFilter.addAction(AttributeEvent.FOLLOW_UPDATE);
        eventFilter.addAction(AttributeEvent.MISSION_DRONIE_CREATED);
        eventFilter.addAction(SettingsFragment.ACTION_LOCATION_SETTINGS_UPDATED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                case MissionProxy.ACTION_MISSION_PROXY_UPDATE:
                    updateMissionLength();
                    break;

                case AttributeEvent.MISSION_RECEIVED:
                    final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
                    if (planningMapFragment != null) {
                        planningMapFragment.zoomToFit();
                    }
                    break;

                case AttributeEvent.STATE_ARMING:
                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.STATE_DISCONNECTED:
                case AttributeEvent.STATE_UPDATED:
                    setupButtonsByFlightState();
                    break;
                case AttributeEvent.TYPE_UPDATED:
                    break;
                case AttributeEvent.STATE_VEHICLE_MODE:
                    updateFlightModeButtons();
                    break;

                case AttributeEvent.FOLLOW_START:
                case AttributeEvent.FOLLOW_STOP:
                    final FollowState followState = getDrone().getAttribute(AttributeType.FOLLOW_STATE);
                    if (followState != null) {
                        String eventLabel = null;
                        switch (followState.getState()) {
                            case FollowState.STATE_START:
                                eventLabel = "FollowMe enabled";
                                break;

                            case FollowState.STATE_RUNNING:
                                eventLabel = "FollowMe running";
                                break;

                            case FollowState.STATE_END:
                                eventLabel = "FollowMe disabled";
                                break;

                            case FollowState.STATE_INVALID:
                                eventLabel = "FollowMe error: invalid state";
                                break;

                            case FollowState.STATE_DRONE_DISCONNECTED:
                                eventLabel = "FollowMe error: drone not connected";
                                break;

                            case FollowState.STATE_DRONE_NOT_ARMED:
                                eventLabel = "FollowMe error: drone not armed";
                                break;
                        }

                        if (eventLabel != null) {
                            HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                    .setCategory(GAUtils.Category.FLIGHT)
                                    .setAction(ACTION_FLIGHT_ACTION_BUTTON)
                                    .setLabel(eventLabel);
                            GAUtils.sendEvent(eventBuilder);

                            Toast.makeText(EditorActivity.this, eventLabel, Toast.LENGTH_SHORT).show();
                        }
                    }

                 /* FALL - THROUGH */
                case AttributeEvent.FOLLOW_UPDATE:
                    updateFlightModeButtons();
                    updateFollowButton();
                    break;

                case AttributeEvent.MISSION_DRONIE_CREATED:
                    //Get the bearing of the dronie mission.
                    float bearing = intent.getFloatExtra(AttributeEventExtra.EXTRA_MISSION_DRONIE_BEARING, -1);
                    if (bearing >= 0) {

                    }
                    break;
                case SettingsFragment.ACTION_LOCATION_SETTINGS_UPDATED:
                    final int resultCode = intent.getIntExtra(SettingsFragment.EXTRA_RESULT_CODE, Activity.RESULT_OK);
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            // All required changes were successfully made. Enable follow me.
                            enableFollowMe(getDrone());
                            break;

                        case Activity.RESULT_CANCELED:
                            // The user was asked to change settings, but chose not to
                            Toast.makeText(EditorActivity.this, "Please update your location settings!", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
    };

    private void updateFollowButton() {
        FollowState followState = getDrone().getAttribute(AttributeType.FOLLOW_STATE);
        if (followState == null)
            return;

        switch (followState.getState()) {
            case FollowState.STATE_START:
                break;

            case FollowState.STATE_RUNNING:
                button_follow_me.setActivated(true);
                break;

            default:
                button_follow_me.setActivated(false);
                break;
        }
    }

    private void updateFlightModeButtons() {
        initBackground();

        State droneState = getDrone().getAttribute(AttributeType.STATE);
        if (droneState == null)
            return;

        final VehicleMode flightMode = droneState.getVehicleMode();
        if (flightMode == null)
            return;

        switch (flightMode) {
            case COPTER_AUTO:
                button_write.setActivated(true);
                break;

            case COPTER_GUIDED:
                final Drone drone = getDrone();
                final GuidedState guidedState = drone.getAttribute(AttributeType.GUIDED_STATE);
                final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
                if (guidedState.isInitialized() && !followState.isEnabled()) {
                    button_hover.setActivated(true);
                }
                break;

            case COPTER_BRAKE:
                button_hover.setActivated(true);
                break;

            case COPTER_RTL:
                button_go_home.setActivated(true);
                break;

            case COPTER_LAND:
                button_land.setActivated(true);
                break;
            default:
                break;
        }
    }

    private void setupButtonsByFlightState() {
        final State droneState = getDrone().getAttribute(AttributeType.STATE);
            if (droneState.isArmed()) {
                setupButtonsForArmed();
                if (droneState.isFlying()) {
//                    setupButtonsForFlying();
                } else {

                }
            } else {
                setupButtonsForDisarmed();
            }
    }

    private void setupButtonsForArmed() {
        button_take_off.setVisibility(View.GONE);
        button_land.setVisibility(View.VISIBLE);
    }

    private void setupButtonsForDisarmed() {
        button_take_off.setVisibility(View.VISIBLE);
        button_land.setVisibility(View.GONE);
    }

    /**
     * Used to provide access and interact with the
     * {@link com.fuav.android.proxy.mission.MissionProxy} object on the Android
     * layer.
     */
    private MissionProxy missionProxy;

    /*
     * View widgets.
     */
    private GestureMapFragment gestureMapFragment;
    private EditorToolsFragment editorToolsFragment;
    private MissionDetailFragment itemDetailFragment;
    private VideoFragment videoFragment = new VideoFragment();
    private VideoControlFragment videoControlFragment = new VideoControlFragment();
    private FlightMapFragment flightMapFragment =  new FlightMapFragment();
    private FragmentManager fragmentManager;
    private ImageView showhide;
    private Button button_take_off;
    private Button button_land;
    private Button button_go_home;
    private Button button_hover;
    private Button button_write;
    private Button button_follow_me;
    private TextView infoView;

    /**
     * If the mission was loaded from a file, the filename is stored here.
     */
    private String openedMissionFilename;

//    private EditorListFragment editorListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏

        setContentView(R.layout.activity_editor);

        showVideo = true;

        if (editorToolsFragment == null) {
            editorToolsFragment = new EditorToolsFragment();
            fragmentManager.beginTransaction().replace(R.id.editortools,editorToolsFragment).commit();
        }

        if (gestureMapFragment == null) {
            gestureMapFragment = new GestureMapFragment();
            fragmentManager.beginTransaction().add(R.id.editor_map_fragment, gestureMapFragment).commit();
        }

//      editorListFragment = (EditorListFragment) fragmentManager.findFragmentById(R.id.mission_list_fragment);

        infoView = (TextView) findViewById(R.id.editorInfoWindow);

        final Button zoomToFit = (Button) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(this);

        final Button mGoToMyLocation = (Button) findViewById(R.id.my_location_button);
        mGoToMyLocation.setOnClickListener(this);
        mGoToMyLocation.setOnLongClickListener(this);

        final Button mGoToDroneLocation = (Button) findViewById(R.id.drone_location_button);
        mGoToDroneLocation.setOnClickListener(this);
        mGoToDroneLocation.setOnLongClickListener(this);



        if (savedInstanceState != null) {
            openedMissionFilename = savedInstanceState.getString(EXTRA_OPENED_MISSION_FILENAME);
        }

        // Retrieve the item detail fragment using its tag
        itemDetailFragment = (MissionDetailFragment) fragmentManager.findFragmentByTag(ITEM_DETAIL_TAG);

        gestureMapFragment.setOnPathFinishedListener(this);
        openActionDrawer();

        showhide= (ImageView) findViewById(R.id.show_hide_arrow);
        showhide.setOnClickListener(this);
        button_take_off= (Button) findViewById(R.id.button_take_off);
        button_take_off.setOnClickListener(this);
        button_land= (Button) findViewById(R.id.button_land);
        button_land.setOnClickListener(this);
        button_go_home= (Button) findViewById(R.id.button_go_home);
        button_go_home.setOnClickListener(this);
        button_hover= (Button) findViewById(R.id.button_hover);
        button_hover.setOnClickListener(this);
        button_write= (Button) findViewById(R.id.button_write);
        button_write.setOnClickListener(this);
        button_follow_me= (Button)findViewById(R.id.button_follow_me);
        button_follow_me.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public String getMapName(){
        DroidPlannerPrefs pre = new DroidPlannerPrefs(this);
        return pre.getMapProviderName();
    }

    public boolean isSupportGooglePlay(){
        final int playStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(EditorActivity.this);
        final boolean isValid = playStatus == ConnectionResult.SUCCESS;
        return  isValid;
    }

    @Override
    protected float getActionDrawerTopMargin() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
    }

    /**
     * Account for the various ui elements and update the map padding so that it
     * remains 'visible'.
     */
    private void updateLocationButtonsMargin(boolean isOpened) {
        final View actionDrawer = getActionDrawer();
        if (actionDrawer == null)
            return;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        missionProxy = dpApp.getMissionProxy();
        if (missionProxy != null) {
            missionProxy.selection.addSelectionUpdateListener(this);
        }

        setupButtonsByFlightState();
        updateFlightModeButtons();
//        updateMissionLength();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();

        if (missionProxy != null)
            missionProxy.selection.removeSelectionUpdateListener(this);

        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onClick(View v) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();

        switch (v.getId()) {

            case R.id.zoom_to_fit_button:
                if (planningMapFragment != null) {
                    planningMapFragment.zoomToFit();
                }
                break;
            case R.id.drone_location_button:
                planningMapFragment.goToDroneLocation();
                break;
            case R.id.my_location_button:
                planningMapFragment.goToMyLocation();
                break;
            case R.id.show_hide_arrow:
                if(showhidearrow%2==0){
                    setGone(R.id.video_view);
                    setGone(R.id.video_control_view);
                }else{
                    setVisible(R.id.video_view);
                    setVisible(R.id.video_control_view);
                }
                showhidearrow++;
                break;
            case R.id.button_take_off:
                confirmConnect(R.id.button_take_off);
                break;
            case R.id.button_land:
                confirmConnect(R.id.button_land);
                break;
            case R.id.button_go_home:
                confirmConnect(R.id.button_go_home);
                break;
            case R.id.button_hover:
                confirmConnect(R.id.button_hover);
                break;
            case R.id.button_write:
                confirmConnect(R.id.button_write);
                break;
            case R.id.button_follow_me:
                confirmConnect(R.id.button_follow_me);
                break;
            case R.id.video_control_view:
                WindowManager manager = getWindowManager();
                float xtotal = manager.getDefaultDisplay().getWidth();
                float ytotal = manager.getDefaultDisplay().getHeight();
                float x = findViewById(R.id.video_view).getWidth();
                float y = findViewById(R.id.video_view).getHeight();
                float xscale = xtotal/x;
                float yscale = ytotal/y;
                if (index%2==0){
                        /** 设置缩放动画 */
                        final ScaleAnimation animation =new ScaleAnimation(1f, xscale+0.1f, 1f, yscale+0.1f,
                                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
                        animation.setDuration(510);//设置动画持续时间
                        /** 常用方法 */
//                    animation.setRepeatCount(int repeatCount);//设置重复次数
//                    animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
                        findViewById(R.id.video_view).startAnimation(animation);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if(!getMapName().equals("GOOGLE_MAP")||(isSupportGooglePlay())){
                                    fragmentManager.beginTransaction().replace(R.id.video_view,flightMapFragment).commit();
                                }else {
                                    fragmentManager.beginTransaction().replace(R.id.video_view,new BlankFragment()).commit();
                                }

                                if(findViewById(R.id.editor_map_fragment).getVisibility()==View.GONE) {
                                    setVisible(R.id.editor_map_fragment);
                                }
                                fragmentManager.beginTransaction().replace(R.id.editor_map_fragment,videoControlFragment).commit();
                                setGone(R.id.location_button_container);
                                setGone(R.id.editortools);
                                setGone(R.id.button_write);
                                setVisible(R.id.button_follow_me);
                                showVideo = false;
                            }
                        },500);
                }else{

                        /** 设置缩放动画 */
                        final ScaleAnimation animation =new ScaleAnimation(1f, xscale+0.1f, 1f, yscale+0.1f,
                                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
                        animation.setDuration(510);//设置动画持续时间
                        findViewById(R.id.video_view).startAnimation(animation);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fragmentManager.beginTransaction().replace(R.id.video_view,videoFragment).commit();
                                if(getMapName().equals("GOOGLE_MAP")&&!isSupportGooglePlay()) {
                                    findViewById(R.id.editor_map_fragment).setVisibility(View.GONE);
                                    findViewById(R.id.tips).setVisibility(View.VISIBLE);
                                }
                                fragmentManager.beginTransaction().replace(R.id.editor_map_fragment,gestureMapFragment).commit();
                                setVisible(R.id.location_button_container);
                                setVisible(R.id.editortools);
                                setVisible(R.id.button_write);
                                setGone(R.id.button_follow_me);
                                showVideo = true;
                            }
                        },500);
                }
                index++;
                break;
            default:
                break;
        }

    }

    void confirmConnect(int id){
        if(DroneManager.getDrone()!=null){
            switch (id){
                case R.id.button_take_off:
                    getArmingConfirmation();
                    break;
                case R.id.button_land:
                    getLandConfirmation();
                    break;
                case R.id.button_go_home:
                    getGoHomeConfirmation();
                    break;
                case R.id.button_hover:
                    getHoverConfirmation();
                    break;
                case R.id.button_write:
                    getAutoFlyConfirmation();
                    break;
                case R.id.button_follow_me:
                    getFolloeMeConfirmation();
                    break;
                default:
                    break;
            }
        }else{
            Toast.makeText(this,"无人机未连接",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();

        switch (view.getId()) {
            case R.id.drone_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.DRONE);
                return true;
            case R.id.my_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.USER);
                return true;
            default:
                return false;
        }
    }

    void setGone(int id){
        findViewById(id).setVisibility(View.GONE);
    }

    void setVisible(int id){
        findViewById(id).setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        editorToolsFragment.setToolAndUpdateView(getTool());
        setupTool();

        findViewById(R.id.video_control_view).setOnClickListener(this);

        if (showVideo){
            getSupportFragmentManager().beginTransaction().replace(R.id.video_view,new VideoFragment()).commit();
        }

        if(getMapName().equals("GOOGLE_MAP")){
            if(!isSupportGooglePlay()){
                if(showVideo){
                    findViewById(R.id.editor_map_fragment).setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_container;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_OPENED_MISSION_FILENAME, openedMissionFilename);
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_editor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_mission, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_mission:
                openMissionFile();
                return true;

            case R.id.menu_save_mission:
                saveMissionFile();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openMissionFile() {
        OpenFileDialog missionDialog = new OpenMissionDialog() {
            @Override
            public void waypointFileLoaded(MissionReader reader) {
                openedMissionFilename = getSelectedFilename();

                if(missionProxy != null) {
                    missionProxy.readMissionFromFile(reader);
                    gestureMapFragment.getMapFragment().zoomToFit();
                }
            }
        };
        missionDialog.openDialog(this);
    }

    @Override
    public void onOk(String dialogTag, CharSequence input) {
        final Context context = getApplicationContext();

        switch (dialogTag) {
            case MISSION_FILENAME_DIALOG_TAG:
                if (missionProxy.writeMissionToFile(input.toString())) {
                    Toast.makeText(context, R.string.file_saved_success, Toast.LENGTH_SHORT)
                            .show();

                    final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                            .setCategory(GAUtils.Category.MISSION_PLANNING)
                            .setAction("Mission saved to file")
                            .setLabel("Mission items count");
                    GAUtils.sendEvent(eventBuilder);

                    break;
                }

                Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onCancel(String dialogTag) {
    }

    private void saveMissionFile() {
        final String defaultFilename = TextUtils.isEmpty(openedMissionFilename)
                ? FileStream.getWaypointFilename("waypoints")
                : openedMissionFilename;

        final SupportEditInputDialog dialog = SupportEditInputDialog.newInstance(MISSION_FILENAME_DIALOG_TAG,
                getString(R.string.label_enter_filename), defaultFilename, true);

        dialog.show(getSupportFragmentManager(), MISSION_FILENAME_DIALOG_TAG);
    }

    private void updateMissionLength() {
        if (missionProxy != null) {

            double missionLength = missionProxy.getMissionLength();
            LengthUnit convertedMissionLength = unitSystem.getLengthUnitProvider().boxBaseValueToTarget(missionLength);
            double speedParameter = dpApp.getDrone().getSpeedParameter() / 100; //cm/s to m/s conversion.
            if (speedParameter == 0)
                speedParameter = DEFAULT_SPEED;

            int time = (int) (missionLength / speedParameter);

            String infoString = getString(R.string.editor_info_window_distance, convertedMissionLength.toString())
                    + ", " + getString(R.string.editor_info_window_flight_time, time / 60, time % 60);

            infoView.setVisibility(View.VISIBLE);
            infoView.setText(infoString);
            // Remove detail window if item is removed
            if (missionProxy.selection.getSelected().isEmpty() && itemDetailFragment != null) {
                removeItemDetail();
            }
            if(missionLength==0.0){
                infoView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onMapClick(LatLong point) {
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onMapClick(point);
    }

    public EditorTools getTool() {
        return editorToolsFragment.getTool();
    }

    public EditorToolsImpl getToolImpl() {
        return editorToolsFragment.getToolImpl();
    }

    @Override
    public void editorToolChanged(EditorTools tools) {
        setupTool();
    }

    @Override
    public void enableGestureDetection(boolean enable) {
        if (gestureMapFragment == null)
            return;

        if (enable)
            gestureMapFragment.enableGestureDetection();
        else
            gestureMapFragment.disableGestureDetection();
    }

    @Override
    public void skipMarkerClickEvents(boolean skip) {
        if (gestureMapFragment == null)
            return;

        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        if (planningMapFragment != null)
            planningMapFragment.skipMarkerClickEvents(skip);
    }

    private void setupTool() {
        final EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.setup();
//        editorListFragment.enableDeleteMode(toolImpl.getEditorTools() == EditorTools.TRASH);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateLocationButtonsMargin(itemDetailFragment != null);
    }

    @Override
    protected void addToolbarFragment(){
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarTelem = fm.findFragmentById(toolbarId);
        if (actionBarTelem == null) {
            actionBarTelem = new ActionBarTelemFragment();
            fm.beginTransaction().add(toolbarId, actionBarTelem).commit();
        }
    }

    private void showItemDetail(MissionDetailFragment itemDetail) {
        if (itemDetailFragment == null) {
            addItemDetail(itemDetail);
        } else {
            switchItemDetail(itemDetail);
        }

        editorToolsFragment.setToolAndUpdateView(EditorTools.NONE);
    }

    private void addItemDetail(MissionDetailFragment itemDetail) {
        itemDetailFragment = itemDetail;
        if (itemDetailFragment == null)
            return;

        fragmentManager.beginTransaction()
                .replace(getActionDrawerId(), itemDetailFragment, ITEM_DETAIL_TAG)
                .commit();
        updateLocationButtonsMargin(true);
    }

    public void switchItemDetail(MissionDetailFragment itemDetail) {
        removeItemDetail();
        addItemDetail(itemDetail);
    }

    private void removeItemDetail() {
        if (itemDetailFragment != null) {
            fragmentManager.beginTransaction().remove(itemDetailFragment).commit();
            itemDetailFragment = null;

            updateLocationButtonsMargin(false);
        }
    }

    @Override
    public void onPathFinished(List<LatLong> path) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        List<LatLong> points = planningMapFragment.projectPathIntoMap(path);
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onPathFinished(points);
    }

    @Override
    public void onDetailDialogDismissed(List<MissionItemProxy> itemList) {
        if (missionProxy != null) missionProxy.selection.removeItemsFromSelection(itemList);
    }

    @Override
    public void onWaypointTypeChanged(MissionItemType newType, List<Pair<MissionItemProxy,
            List<MissionItemProxy>>> oldNewItemsList) {
        missionProxy.replaceAll(oldNewItemsList);
    }

    private MissionDetailFragment selectMissionDetailType(List<MissionItemProxy> proxies) {
        if (proxies == null || proxies.isEmpty())
            return null;

        MissionItemType referenceType = null;
        for (MissionItemProxy proxy : proxies) {
            final MissionItemType proxyType = proxy.getMissionItem().getType();
            if (referenceType == null) {
                referenceType = proxyType;
            } else if (referenceType != proxyType
                    || MissionDetailFragment.typeWithNoMultiEditSupport.contains(referenceType)) {
                //Return a generic mission detail.
                return new MissionDetailFragment();
            }
        }

        return MissionDetailFragment.newInstance(referenceType);
    }

    @Override
    public void onItemClick(MissionItemProxy item, boolean zoomToFit) {
        if (missionProxy == null) return;

        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onListItemClick(item);

        if (zoomToFit) {
            zoomToFitSelected();
        }
    }

    @Override
    public void zoomToFitSelected() {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        List<MissionItemProxy> selected = missionProxy.selection.getSelected();
        if (selected.isEmpty()) {
            planningMapFragment.zoomToFit();
        } else {
            planningMapFragment.zoomToFit(MissionProxy.getVisibleCoords(selected));
        }
    }

    @Override
    public void onListVisibilityChanged() {
    }

    @Override
    protected boolean enableMissionMenus() {
        return true;
    }

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onSelectionUpdate(selected);

        final boolean isEmpty = selected.isEmpty();

        if (isEmpty) {
            removeItemDetail();
        } else {
            if (getTool() == EditorTools.SELECTOR)
                removeItemDetail();
            else {
                showItemDetail(selectMissionDetailType(selected));
            }
        }

        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        if (planningMapFragment != null)
            planningMapFragment.postUpdate();
    }

    private void getArmingConfirmation() {
        SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("arm", new Runnable() {
            @Override
            public void run() {
                getDrone().arm(true);
                final double takeOffAltitude = getAppPrefs().getDefaultAltitude();
                ControlApi.getApi(getDrone()).takeoff(takeOffAltitude, null);
            }
        });
        unlockDialog.show(getSupportFragmentManager(), "Slide To Arm");
    }

    private void getGoHomeConfirmation() {
        SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("GoHome", new Runnable() {
            @Override
            public void run() {
                VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_RTL);
            }
        });
        unlockDialog.show(getSupportFragmentManager(), "Slide To GoHome");
    }

    private void getHoverConfirmation() {
        SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("Hover", new Runnable() {
            @Override
            public void run() {
                final Drone drone = getDrone();
                final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
                if (followState.isEnabled()) {
                    FollowApi.getApi(drone).disableFollowMe();
                }
                ControlApi.getApi(drone).pauseAtCurrentLocation(null);
            }
        });
        unlockDialog.show(getSupportFragmentManager(), "Slide To Hover");
    }

    private void getLandConfirmation() {
        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("Land off", new Runnable() {
            @Override
            public void run() {
                VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_LAND);
            }
        });
        unlockDialog.show(getSupportFragmentManager(), "Slide to Land off");
    }

    private void getAutoFlyConfirmation() {
        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("Auto Fly", new Runnable() {
            @Override
            public void run() {
            }
        });
        unlockDialog.show(getSupportFragmentManager(), "Slide to Land off");
    }

    private void getFolloeMeConfirmation() {
        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("Folloe Me", new Runnable() {
            @Override
            public void run() {
                toggleFollowMe();
            }
        });
        unlockDialog.show(getSupportFragmentManager(), "Slide to Folloe Me");
    }

    protected void toggleFollowMe() {
        final Drone drone = getDrone();
        if (drone == null)
            return;

        final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
        if (followState.isEnabled()) {
            FollowApi.getApi(drone).disableFollowMe();
        } else {
            enableFollowMe(drone);
        }
    }

    private void enableFollowMe(final Drone drone) {
        if(drone == null)
            return;

        final LocationRequest locationReq = LocationRequest.create()
                .setPriority(FOLLOW_LOCATION_PRIORITY)
                .setFastestInterval(FOLLOW_LOCATION_UPDATE_FASTEST_INTERVAL)
                .setInterval(FOLLOW_LOCATION_UPDATE_INTERVAL)
                .setSmallestDisplacement(FOLLOW_LOCATION_UPDATE_MIN_DISPLACEMENT);

        final CheckLocationSettings locationSettingsChecker = new CheckLocationSettings(this, locationReq,
                new Runnable() {
                    @Override
                    public void run() {
                        drone.enableFollowMe(FollowType.LEASH);
                    }
                });

        locationSettingsChecker.check();
    }

    private void initBackground() {
        button_go_home.setActivated(false);
        button_land.setActivated(false);
        button_hover.setActivated(false);
        button_write.setActivated(false);
    }

}
