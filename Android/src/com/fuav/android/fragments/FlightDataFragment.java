package com.fuav.android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuav.android.R;
import com.fuav.android.activities.DrawerNavigationUI;
import com.fuav.android.dialogs.SlideToUnlockDialog;
import com.fuav.android.fragments.control.BaseFlightControlFragment;
import com.fuav.android.fragments.control.FlightControlManagerFragment;
import com.fuav.android.fragments.widget.video.MiniWidgetSoloLinkVideo;
import com.fuav.android.utils.analytics.GAUtils;
import com.fuav.android.utils.prefs.AutoPanMode;
import com.fuav.android.view.SlidingDrawer;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.ErrorType;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;

/**
 * Created by Fredia Huya-Kouadio on 8/27/15.
 */
public class FlightDataFragment extends BaseFlightControlFragment implements SlidingDrawer.OnDrawerOpenListener, SlidingDrawer.OnDrawerCloseListener,View.OnClickListener {

    public static final String EXTRA_SHOW_ACTION_DRAWER_TOGGLE = "extra_show_action_drawer_toggle";
    private static final boolean DEFAULT_SHOW_ACTION_DRAWER_TOGGLE = false;

    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;

    /**
     * Determines how long the failsafe view is visible for.
     */
    private static final long WARNING_VIEW_DISPLAY_TIMEOUT = 10000l; //ms

    private static final String ACTION_FLIGHT_ACTION_BUTTON = "Copter flight action button";

    private static final String DRONIE_CREATION_DIALOG_TAG = "Confirm dronie creation";

    private static final IntentFilter eventFilter = new IntentFilter();


    private void updateFlightModeButtons() {
    }

    private void setupButtonsByFlightState() {
    }

    private void updateFollowButton() {
    }


    private int orangeColor;

    static {
        eventFilter.addAction(AttributeEvent.AUTOPILOT_ERROR);
        eventFilter.addAction(AttributeEvent.AUTOPILOT_MESSAGE);
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
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.AUTOPILOT_ERROR:
                    String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    final ErrorType errorType = ErrorType.getErrorById(errorName);
                    onAutopilotError(errorType);
                    break;

                case AttributeEvent.AUTOPILOT_MESSAGE:
                    final int logLevel = intent.getIntExtra(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE_LEVEL, Log.VERBOSE);
                    final String message = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE);
                    onAutopilotError(logLevel, message);
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

                            Toast.makeText(getActivity(), eventLabel, Toast.LENGTH_SHORT).show();
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
                        final FlightControlManagerFragment parent = (FlightControlManagerFragment) getParentFragment();
                        if (parent != null) {
                            parent.updateMapBearing(bearing);
                        }
                    }
                    break;


            }
        }
    };



    private final Runnable hideWarningView = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);

            if (warningView != null && warningView.getVisibility() != View.GONE)
                warningView.setVisibility(View.GONE);
        }
    };

    private final Handler handler = new Handler();

    private View actionbarShadow;
    private TextView warningView;

    private FlightMapFragment mapFragment;

    private ImageView mGoToMyLocation;
    private ImageView mGoToDroneLocation;
    private Button button_take_off;
    private Button button_go_home;
    private Button button_hover;
    private Button button_write;
    private int index= 0;

    private DrawerNavigationUI navActivity;



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DrawerNavigationUI)
            navActivity = (DrawerNavigationUI) activity;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        navActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flight_data, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bundle arguments = getArguments();

        orangeColor = getResources().getColor(R.color.orange);

        actionbarShadow = view.findViewById(R.id.actionbar_shadow);


        warningView = (TextView) view.findViewById(R.id.failsafeTextView);

        setupMapFragment();

        getChildFragmentManager().beginTransaction().replace(R.id.video_view2,new MiniWidgetSoloLinkVideo()).commit();

        mGoToMyLocation = (ImageView) view.findViewById(R.id.my_location_button);
        mGoToDroneLocation = (ImageView) view.findViewById(R.id.drone_location_button);

        button_take_off= (Button) view.findViewById(R.id.button_take_off);
        button_take_off.setOnClickListener(this);
        button_go_home= (Button) view.findViewById(R.id.button_go_home);
        button_go_home.setOnClickListener(this);
        button_hover= (Button) view.findViewById(R.id.button_hover);
        button_hover.setOnClickListener(this);
        button_write= (Button) view.findViewById(R.id.button_write);
        button_write.setOnClickListener(this);

        mGoToMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED);
                }
            }
        });
        mGoToMyLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.USER);
                    return true;
                }
                return false;
            }
        });

        mGoToDroneLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED);
                }
            }
        });
        mGoToDroneLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DRONE);
                    return true;
                }
                return false;
            }
        });


    }

    public void updateActionbarShadow(int shadowHeight){
        if(actionbarShadow == null || actionbarShadow.getLayoutParams().height == shadowHeight)
            return;

        actionbarShadow.getLayoutParams().height = shadowHeight;
        actionbarShadow.requestLayout();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupMapFragment();
        updateMapLocationButtons(getAppPrefs().getAutoPanMode());
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        setupButtonsByFlightState();
        updateFlightModeButtons();
        updateFollowButton();

        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onDrawerClosed() {

    }

    @Override
    public void onDrawerOpened() {

    }

    /**
     * Used to setup the flight screen map fragment. Before attempting to
     * initialize the map fragment, this checks if the Google Play Services
     * binary is installed and up to date.
     */
    private void setupMapFragment() {
        final FragmentManager fm = getChildFragmentManager();
        if (mapFragment == null && isGooglePlayServicesValid(true)) {
            mapFragment = (FlightMapFragment) fm.findFragmentById(R.id.flight_map_fragment);
            if (mapFragment == null) {
                mapFragment = new FlightMapFragment();
                fm.beginTransaction().add(R.id.flight_map_fragment, mapFragment).commit();
            }
        }
    }

    private void updateMapLocationButtons(AutoPanMode mode) {
        mGoToMyLocation.setActivated(false);
        mGoToDroneLocation.setActivated(false);

        if (mapFragment != null) {
            mapFragment.setAutoPanMode(mode);
        }

        switch (mode) {
            case DRONE:
                mGoToDroneLocation.setActivated(true);
                break;

            case USER:
                mGoToMyLocation.setActivated(true);
                break;
            default:
                break;
        }
    }

    public void updateMapBearing(float bearing) {
        if (mapFragment != null)
            mapFragment.updateMapBearing(bearing);
    }


    private void onAutopilotError(ErrorType errorType) {
        if (errorType == null)
            return;

        final CharSequence errorLabel;
        switch (errorType) {
            case NO_ERROR:
                errorLabel = null;
                break;

            default:
                errorLabel = errorType.getLabel(getContext());
                break;
        }

        onAutopilotError(Log.ERROR, errorLabel);
    }

    private void onAutopilotError(int logLevel, CharSequence errorMsg) {
        if (TextUtils.isEmpty(errorMsg))
            return;

        switch (logLevel) {
            case Log.ERROR:
            case Log.WARN:
                handler.removeCallbacks(hideWarningView);

                warningView.setText(errorMsg);
                warningView.setVisibility(View.VISIBLE);
                handler.postDelayed(hideWarningView, WARNING_VIEW_DISPLAY_TIMEOUT);
                break;
        }
    }

    /**
     * Ensures that the device has the correct version of the Google Play
     * Services.
     *
     * @return true if the Google Play Services binary is valid
     */
    private boolean isGooglePlayServicesValid(boolean showErrorDialog) {
        // Check for the google play services is available
        final int playStatus = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getContext());
        final boolean isValid = playStatus == ConnectionResult.SUCCESS;

        if (!isValid && showErrorDialog) {
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(playStatus, getActivity(),
                    GOOGLE_PLAY_SERVICES_REQUEST_CODE, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (isAdded()) {
                                getActivity().finish();
                            }
                        }
                    });

            if (errorDialog != null)
                errorDialog.show();
        }

        return isValid;
    }

    public void setGuidedClickListener(FlightMapFragment.OnGuidedClickListener listener) {
        mapFragment.setGuidedClickListener(listener);
    }

    public void addMapMarkerProvider(DroneMap.MapMarkerProvider provider) {
        mapFragment.addMapMarkerProvider(provider);
    }

    public void removeMapMarkerProvider(DroneMap.MapMarkerProvider provider) {
        mapFragment.removeMapMarkerProvider(provider);
    }

    @Override
    public void onClick(View v) {
        initBackground();
        final Drone drone = getDrone();
        switch (v.getId()){
            case R.id.button_take_off:
                if(index%2==0){
                    button_take_off.setBackgroundResource(R.drawable.button_land);
                    getTakeOffConfirmation();
                    getArmingConfirmation();
                }else{
                    getDrone().changeVehicleMode(VehicleMode.COPTER_LAND);
                }
                index++;
                break;
            case R.id.button_go_home:
                button_go_home.setBackgroundResource(R.drawable.go_home_on);;
                getDrone().changeVehicleMode(VehicleMode.COPTER_RTL);
                break;
            case R.id.button_hover:
                button_hover.setBackgroundResource(R.drawable.hover_on);
                final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
                if (followState.isEnabled()) {
                    drone.disableFollowMe();
                }

                drone.pauseAtCurrentLocation();
                break;
            case R.id.button_write:
                break;
            default:
                break;
        }
    }

    private void initBackground() {
        button_take_off.setBackgroundResource(R.drawable.button_take_off);
        button_go_home.setBackgroundResource(R.drawable.button_go_home);
        button_hover.setBackgroundResource(R.drawable.button_hover);
    }

    @Override
    public boolean isSlidingUpPanelEnabled(Drone drone) {
        return false;
    }

    private void getArmingConfirmation() {
        SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("arm", new Runnable() {
            @Override
            public void run() {
                getDrone().arm(true);
            }
        });
        unlockDialog.show(getChildFragmentManager(), "Slide To Arm");
    }

    private void getTakeOffConfirmation() {
        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("take off", new Runnable() {
            @Override
            public void run() {
                final double takeOffAltitude = getAppPrefs().getDefaultAltitude();
                getDrone().doGuidedTakeoff(takeOffAltitude);
            }
        });
        unlockDialog.show(getChildFragmentManager(), "Slide to take off");
    }
}
