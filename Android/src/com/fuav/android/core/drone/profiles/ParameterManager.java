package com.fuav.android.core.drone.profiles;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.SparseBooleanArray;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_param_value;
import com.o3dr.services.android.lib.drone.property.Parameter;

import com.fuav.android.core.MAVLink.MavLinkParameters;
import com.fuav.android.core.drone.DroneInterfaces;
import com.fuav.android.core.drone.DroneInterfaces.DroneEventsType;
import com.fuav.android.core.drone.DroneInterfaces.OnDroneListener;
import com.fuav.android.core.drone.DroneVariable;
import com.fuav.android.core.drone.autopilot.MavLinkDrone;
import com.fuav.android.utils.file.IO.ParameterMetadataLoader;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

/**
 * Class to manage the communication of parameters to the MAV.
 * <p/>
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 */
public class ParameterManager extends DroneVariable implements OnDroneListener {

    private static final long TIMEOUT = 1000l; //milliseconds

    private final Runnable parametersReceiptStartNotification = new Runnable() {
        @Override
        public void run() {
            if (parameterListener != null)
                parameterListener.onBeginReceivingParameters();
        }
    };

    public final Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            onParameterStreamStopped();
        }
    };
    private final Runnable parametersReceiptEndNotification = new Runnable() {
        @Override
        public void run() {
            if (parameterListener != null)
                parameterListener.onEndReceivingParameters();
        }
    };

    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    private int expectedParams;

    private final SparseBooleanArray paramsRollCall = new SparseBooleanArray();
    private final ConcurrentHashMap<String, Parameter> parameters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ParameterMetadata> parametersMetadata = new ConcurrentHashMap<>();

    private DroneInterfaces.OnParameterManagerListener parameterListener;

    private final Handler watchdog;
    private final Context context;

    public ParameterManager(MavLinkDrone myDrone, Context context, Handler handler) {
        super(myDrone);
        this.context = context;
        this.watchdog = handler;
        myDrone.addDroneListener(this);
        refreshParametersMetadata();
    }

    public void refreshParameters() {
        if (isRefreshing.compareAndSet(false, true)) {
            expectedParams = 0;
            parameters.clear();
            paramsRollCall.clear();

            notifyParametersReceiptStart();

            MavLinkParameters.requestParametersList(myDrone);
            resetWatchdog();
        }
    }

    public Map<String, Parameter> getParameters() {
        //Update the cache if it's stale. Parameters download is expensive, but we assume the caller knows what it's
        // doing.
        if (parameters.isEmpty())
            refreshParameters();

        return parameters;
    }

    /**
     * Try to process a Mavlink message if it is a parameter related message
     *
     * @param msg Mavlink message to process
     * @return Returns true if the message has been processed
     */
    public boolean processMessage(MAVLinkMessage msg) {
        if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
            processReceivedParam((msg_param_value) msg);
            return true;
        }
        return false;
    }

    protected void processReceivedParam(msg_param_value m_value) {
        // collect params in parameter list
        Parameter param = new Parameter(m_value.getParam_Id(), m_value.param_value, m_value.param_type);
        loadParameterMetadata(param);

        parameters.put(param.getName().toLowerCase(Locale.US), param);
        int paramIndex = m_value.param_index;
        if (paramIndex == -1) {
            // update listener
            notifyParameterReceipt(param, 0, 1);

            notifyParametersReceiptEnd();
            return;
        }

        paramsRollCall.append(paramIndex, true);
        expectedParams = m_value.param_count;

        // update listener
        notifyParameterReceipt(param, paramIndex, m_value.param_count);

        // Are all parameters here? Notify the listener with the parameters
        if (parameters.size() >= m_value.param_count) {
            killWatchdog();
            isRefreshing.set(false);

            notifyParametersReceiptEnd();
        } else {
            resetWatchdog();
        }
    }

    private void reRequestMissingParams(int howManyParams) {
        for (int i = 0; i < howManyParams; i++) {
            if (!paramsRollCall.get(i)) {
                MavLinkParameters.readParameter(myDrone, i);
            }
        }
    }

    public void sendParameter(Parameter parameter) {
        MavLinkParameters.sendParameter(myDrone, parameter);
    }

    public void readParameter(String name) {
        MavLinkParameters.readParameter(myDrone, name);
    }

    public Parameter getParameter(String name) {
        if (TextUtils.isEmpty(name))
            return null;

        return parameters.get(name.toLowerCase(Locale.US));
    }

    private void onParameterStreamStopped() {
        if (expectedParams > 0) {
            reRequestMissingParams(expectedParams);
            resetWatchdog();
        } else {
            isRefreshing.set(false);
        }
    }

    private void resetWatchdog() {
        watchdog.removeCallbacks(watchdogCallback);
        watchdog.postDelayed(watchdogCallback, TIMEOUT);
    }

    private void killWatchdog() {
        watchdog.removeCallbacks(watchdogCallback);
        isRefreshing.set(false);
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case HEARTBEAT_FIRST:
                refreshParameters();
                break;

            case DISCONNECTED:
            case HEARTBEAT_TIMEOUT:
                killWatchdog();
                break;

            case TYPE:
                refreshParametersMetadata();
                break;

            default:
                break;

        }
    }

    private void refreshParametersMetadata() {
        //Reload the vehicle parameters metadata
        String metadataType = myDrone.getFirmwareType().getParameterMetadataGroup();
        if (!TextUtils.isEmpty(metadataType)) {
            try {
                ParameterMetadataLoader.load(context, metadataType, this.parametersMetadata);
            } catch (Exception e) {
                Timber.e(e, e.getMessage());
            }
        }

        if (parametersMetadata.isEmpty() || parameters.isEmpty())
            return;

        for (Parameter parameter : parameters.values()) {
            loadParameterMetadata(parameter);
        }
    }

    private void loadParameterMetadata(Parameter parameter){
        ParameterMetadata metadata = parametersMetadata.get(parameter.getName());
        if (metadata != null) {
            parameter.setDisplayName(metadata.getDisplayName());
            parameter.setDescription(metadata.getDescription());
            parameter.setUnits(metadata.getUnits());
            parameter.setRange(metadata.getRange());
            parameter.setValues(metadata.getValues());
        }
    }

    public void setParameterListener(DroneInterfaces.OnParameterManagerListener parameterListener) {
        this.parameterListener = parameterListener;
    }

    private void notifyParametersReceiptStart() {
        if (parameterListener != null)
            watchdog.post(parametersReceiptStartNotification);
    }

    private void notifyParametersReceiptEnd() {
        if (parameterListener != null)
            watchdog.post(parametersReceiptEndNotification);
    }

    private void notifyParameterReceipt(final Parameter parameter, final int index, final int count) {
        if (parameterListener != null) {
            watchdog.post(new Runnable() {
                @Override
                public void run() {
                    if (parameterListener != null)
                        parameterListener.onParameterReceived(parameter, index, count);
                }
            });
        }
    }
}
