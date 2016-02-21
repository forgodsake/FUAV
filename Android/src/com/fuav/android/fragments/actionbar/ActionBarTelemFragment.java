package com.fuav.android.fragments.actionbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.fuav.android.R;
import com.fuav.android.dialogs.SelectionListDialog;
import com.fuav.android.fragments.SettingsFragment;
import com.fuav.android.fragments.helpers.ApiListenerFragment;
import com.fuav.android.utils.Utils;
import com.fuav.android.utils.prefs.DroidPlannerPrefs;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.gcs.returnToMe.ReturnToMeState;
import com.o3dr.services.android.lib.util.MathUtils;

import org.beyene.sius.unit.length.LengthUnit;

import java.util.Locale;

/**
 * Created by Fredia Huya-Kouadio on 1/14/15.
 */
public class ActionBarTelemFragment extends ApiListenerFragment {

    private final static IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.BATTERY_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
        eventFilter.addAction(AttributeEvent.GPS_COUNT);
        eventFilter.addAction(AttributeEvent.GPS_FIX);
        eventFilter.addAction(AttributeEvent.SIGNAL_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
        eventFilter.addAction(AttributeEvent.ALTITUDE_UPDATED);

        eventFilter.addAction(SettingsFragment.ACTION_PREF_HDOP_UPDATE);
        eventFilter.addAction(SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE);

        eventFilter.addAction(DroidPlannerPrefs.ACTION_PREF_RETURN_TO_ME_UPDATED);
        eventFilter.addAction(AttributeEvent.RETURN_TO_ME_STATE_UPDATE);
        eventFilter.addAction(AttributeEvent.HOME_UPDATED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null)
                return;

            switch (intent.getAction()) {
                case AttributeEvent.BATTERY_UPDATED:
                    updateBatteryTelem();
                    break;

                case AttributeEvent.STATE_CONNECTED:
                    updateAllTelem();
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    updateAllTelem();
                    break;

                case DroidPlannerPrefs.ACTION_PREF_RETURN_TO_ME_UPDATED:
                case AttributeEvent.RETURN_TO_ME_STATE_UPDATE:
                case AttributeEvent.GPS_POSITION:
                case AttributeEvent.HOME_UPDATED:
                    updateHomeTelem();
                    break;

                case AttributeEvent.GPS_COUNT:
                case AttributeEvent.GPS_FIX:
                    updateGpsTelem();
                    break;

                case AttributeEvent.SIGNAL_UPDATED:
                    updateSignalTelem();
                    break;

                case AttributeEvent.STATE_VEHICLE_MODE:
                case AttributeEvent.TYPE_UPDATED:
                    updateFlightModeTelem();
                    break;

                case SettingsFragment.ACTION_PREF_HDOP_UPDATE:
                    updateGpsTelem();
                    break;

                case SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE:
                    updateHomeTelem();
                    break;

                case AttributeEvent.ALTITUDE_UPDATED:
                    updateAltitudeTelem();
                    break;

                default:
                    break;
            }
        }
    };

    private DroidPlannerPrefs appPrefs;

    private TextView homeTelem;
    private TextView altitudeTelem;

    private TextView gpsTelem;
    private PopupWindow gpsPopup;

    private ImageView batteryTelem;
    private PopupWindow batteryPopup;

    private ImageView signalTelem;
    private PopupWindow signalPopup;

    private TextView flightModeTelem;

    private String emptyString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action_bar_telem, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyString = getString(R.string.empty_content);

        final Context context = getActivity().getApplicationContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final int popupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        final int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        final Drawable popupBg = getResources().getDrawable(android.R.color.transparent);

        homeTelem = (TextView) view.findViewById(R.id.bar_home);
        homeTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Launch dialog to allow the user to select between rtl and rtm
                final SelectionListDialog selectionDialog = SelectionListDialog.newInstance(new ReturnToHomeAdapter(context, getDrone(), appPrefs));
                Utils.showDialog(selectionDialog, getChildFragmentManager(), "Return to home type", true);
            }
        });

        altitudeTelem = (TextView) view.findViewById(R.id.bar_altitude);

        gpsTelem = (TextView) view.findViewById(R.id.bar_gps);
        final View gpsPopupView = inflater.inflate(R.layout.popup_info_gps, (ViewGroup) view, false);
        gpsPopup = new PopupWindow(gpsPopupView, popupWidth, popupHeight, true);
        gpsPopup.setBackgroundDrawable(popupBg);
        gpsTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsPopup.showAsDropDown(gpsTelem);
            }
        });

        batteryTelem = (ImageView) view.findViewById(R.id.bar_battery);
        final View batteryPopupView = inflater.inflate(R.layout.popup_info_power, (ViewGroup) view, false);
        batteryPopup = new PopupWindow(batteryPopupView, popupWidth, popupHeight, true);
        batteryPopup.setBackgroundDrawable(popupBg);
        batteryTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batteryPopup.showAsDropDown(batteryTelem);
            }
        });

        signalTelem = (ImageView) view.findViewById(R.id.bar_signal);
        final View signalPopupView = inflater.inflate(R.layout.popup_info_signal, (ViewGroup) view, false);
        signalPopup = new PopupWindow(signalPopupView, popupWidth, popupHeight, true);
        signalPopup.setBackgroundDrawable(popupBg);
        signalTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signalPopup.showAsDropDown(signalTelem);
            }
        });

        flightModeTelem = (TextView) view.findViewById(R.id.bar_flight_mode);
        flightModeTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Launch dialog to allow the user to select vehicle modes
                final Drone drone = getDrone();

                final SelectionListDialog selectionDialog = SelectionListDialog.newInstance(new FlightModeAdapter(context, drone));
                Utils.showDialog(selectionDialog, getChildFragmentManager(), "Flight modes selection", true);
            }
        });

        appPrefs = new DroidPlannerPrefs(context);
    }



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onApiConnected() {

        updateAllTelem();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    private void updateAllTelem() {
        updateFlightModeTelem();
        updateSignalTelem();
        updateGpsTelem();
        updateHomeTelem();
        updateBatteryTelem();
        updateAltitudeTelem();
    }

    private void updateFlightModeTelem() {
        final Drone drone = getDrone();

        final boolean isDroneConnected = drone.isConnected();
        final State droneState = drone.getAttribute(AttributeType.STATE);
        if (isDroneConnected) {
            flightModeTelem.setText(droneState.getVehicleMode().getLabel());
        } else {
            flightModeTelem.setText(emptyString);
        }
    }

    private void updateSignalTelem() {
        final Drone drone = getDrone();

        final View popupView = signalPopup.getContentView();
        TextView rssiView = (TextView) popupView.findViewById(R.id.bar_signal_rssi);
        TextView remRssiView = (TextView) popupView.findViewById(R.id.bar_signal_remrssi);
        TextView noiseView = (TextView) popupView.findViewById(R.id.bar_signal_noise);
        TextView remNoiseView = (TextView) popupView.findViewById(R.id.bar_signal_remnoise);
        TextView fadeView = (TextView) popupView.findViewById(R.id.bar_signal_fade);
        TextView remFadeView = (TextView) popupView.findViewById(R.id.bar_signal_remfade);

        final Signal droneSignal = drone.getAttribute(AttributeType.SIGNAL);
        if (!drone.isConnected() || !droneSignal.isValid()) {

            rssiView.setText("RSSI: " + emptyString);
            remRssiView.setText("RemRSSI: " + emptyString);
            noiseView.setText("Noise: " + emptyString);
            remNoiseView.setText("RemNoise: " + emptyString);
            fadeView.setText("Fade: " + emptyString);
            remFadeView.setText("RemFade: " + emptyString);
        } else {
            final int signalStrength = (int) droneSignal.getSignalStrength();
            final int signalIcon;
            if (signalStrength >= 100)
                signalIcon = R.drawable.wifi_100;
            else if (signalStrength >= 75)
                signalIcon = R.drawable.wifi_75;
            else if (signalStrength >= 50)
                signalIcon = R.drawable.wifi_50;
            else if (signalStrength >= 25)
                signalIcon = R.drawable.wifi_25;
            else
                signalIcon = R.drawable.wifi_0;

            signalTelem.setImageResource(signalIcon);

            rssiView.setText(String.format("RSSI %2.0f dB", droneSignal.getRssi()));
            remRssiView.setText(String.format("RemRSSI %2.0f dB", droneSignal.getRemrssi()));
            noiseView.setText(String.format("Noise %2.0f dB", droneSignal.getNoise()));
            remNoiseView.setText(String.format("RemNoise %2.0f dB", droneSignal.getRemnoise()));
            fadeView.setText(String.format("Fade %2.0f dB", droneSignal.getFadeMargin()));
            remFadeView.setText(String.format("RemFade %2.0f dB", droneSignal.getRemFadeMargin()));
        }

        signalPopup.update();
    }

    private void updateGpsTelem() {
        final Drone drone = getDrone();
        final boolean displayHdop = appPrefs.shouldGpsHdopBeDisplayed();

        final View popupView = gpsPopup.getContentView();
        TextView satNoView = (TextView) popupView.findViewById(R.id.bar_gps_satno);
        TextView hdopStatusView = (TextView) popupView.findViewById(R.id.bar_gps_hdop_status);
        hdopStatusView.setVisibility(displayHdop ? View.GONE : View.VISIBLE);

        final String update;
        if (!drone.isConnected()) {
            update = (displayHdop ? "hdop: " : "") + emptyString;
            satNoView.setText("S: " + emptyString);
            hdopStatusView.setText("hdop: " + emptyString);
        } else {
            Gps droneGps = drone.getAttribute(AttributeType.GPS);
            final String fixStatus = droneGps.getFixStatus();

            if (displayHdop) {
                update = String.format(Locale.ENGLISH, "hdop: %.1f", droneGps.getGpsEph());
            } else {
                update = String.format(Locale.ENGLISH, "%s", fixStatus);
            }

            switch (fixStatus) {
                case Gps.LOCK_3D:
                case Gps.LOCK_3D_DGPS:
                case Gps.LOCK_3D_RTK:
                    break;

                case Gps.LOCK_2D:
                case Gps.NO_FIX:
                default:
                    break;
            }

            satNoView.setText(String.format(Locale.ENGLISH, "S: %d", droneGps.getSatellitesCount()));
            if (appPrefs.shouldGpsHdopBeDisplayed()) {
                hdopStatusView.setText(String.format(Locale.ENGLISH, "%s", fixStatus));
            } else {
                hdopStatusView.setText(String.format(Locale.ENGLISH, "hdop: %.1f", droneGps.getGpsEph()));
            }
        }

        gpsTelem.setText(update);
        gpsPopup.update();
    }

    private void updateHomeTelem() {
        final Drone drone = getDrone();

        String update = getString(R.string.empty_content);

        if (drone.isConnected()) {
            final Gps droneGps = drone.getAttribute(AttributeType.GPS);
            final Home droneHome = drone.getAttribute(AttributeType.HOME);
            if (droneGps.isValid() && droneHome.isValid()) {
                LengthUnit distanceToHome = getLengthUnitProvider().boxBaseValueToTarget
                        (MathUtils.getDistance2D(droneHome.getCoordinate(), droneGps.getPosition()));
                update = String.format("%s", distanceToHome);

                final ReturnToMeState returnToMe = drone.getAttribute(AttributeType.RETURN_TO_ME_STATE);
                switch (returnToMe.getState()) {

                    case ReturnToMeState.STATE_UPDATING_HOME:
                        //Change the home telemetry icon
                        break;

                    case ReturnToMeState.STATE_USER_LOCATION_INACCURATE:
                    case ReturnToMeState.STATE_USER_LOCATION_UNAVAILABLE:
                    case ReturnToMeState.STATE_WAITING_FOR_VEHICLE_GPS:
                    case ReturnToMeState.STATE_ERROR_UPDATING_HOME:
                        break;
                }
            }
        }

        homeTelem.setText(update);
    }

    private void updateBatteryTelem() {
        final Drone drone = getDrone();

        final View batteryPopupView = batteryPopup.getContentView();
        final TextView dischargeView = (TextView) batteryPopupView.findViewById(R.id.bar_power_discharge);
        final TextView currentView = (TextView) batteryPopupView.findViewById(R.id.bar_power_current);
        final TextView remainView = (TextView) batteryPopupView.findViewById(R.id.bar_power_remain);

        Battery droneBattery;
        final int batteryIcon;
        if (!drone.isConnected() || ((droneBattery = drone.getAttribute(AttributeType.BATTERY)) == null)) {
            dischargeView.setText("D: " + emptyString);
            currentView.setText("C: " + emptyString);
            remainView.setText("R: " + emptyString);
            batteryIcon = R.drawable.power_full;
        } else {
            Double discharge = droneBattery.getBatteryDischarge();
            String dischargeText;
            if (discharge == null) {
                dischargeText = "D: " + emptyString;
            } else {
                dischargeText = "D: " + electricChargeToString(discharge);
            }

            dischargeView.setText(dischargeText);

            final double battRemain = droneBattery.getBatteryRemain();
            remainView.setText(String.format(Locale.ENGLISH, "R: %2.0f %%", battRemain));
            currentView.setText(String.format("C: %2.1f A", droneBattery.getBatteryCurrent()));



            if (battRemain >= 100) {
                batteryIcon = R.drawable.power_full;
            } else if (battRemain >= 87.5) {
                batteryIcon = R.drawable.power_full;
            } else if (battRemain >= 75) {
                batteryIcon = R.drawable.power_75;
            } else if (battRemain >= 62.5) {
                batteryIcon = R.drawable.power_75;
            } else if (battRemain >= 50) {
                batteryIcon = R.drawable.power_50;
            } else if (battRemain >= 37.5) {
                batteryIcon = R.drawable.power_50;
            } else if (battRemain >= 25) {
                batteryIcon = R.drawable.power_25;
            } else if (battRemain >= 12.5) {
                batteryIcon = R.drawable.power_25;
            } else {
                batteryIcon = R.drawable.power_0;
            }
        }

        batteryPopup.update();
        batteryTelem.setImageResource(batteryIcon);
    }

    private String electricChargeToString(double chargeInmAh) {
        double absCharge = Math.abs(chargeInmAh);
        if (absCharge >= 1000) {
            return String.format(Locale.US, "%2.1f Ah", chargeInmAh / 1000);
        } else {
            return String.format(Locale.ENGLISH, "%2.0f mAh", chargeInmAh);
        }
    }

    private void updateAltitudeTelem() {
        final Drone drone = getDrone();
        final Altitude altitude = drone.getAttribute(AttributeType.ALTITUDE);
        if (altitude != null) {
            double alt = altitude.getAltitude();
            LengthUnit altUnit = getLengthUnitProvider().boxBaseValueToTarget(alt);

            this.altitudeTelem.setText(altUnit.toString());
        }

        if(getDrone()==null){
            this.altitudeTelem.setText("--");
        }
    }

}
