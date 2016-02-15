package com.fuav.android.api;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.fuav.android.communication.connection.AndroidMavLinkConnection;
import com.fuav.android.communication.connection.AndroidTcpConnection;
import com.fuav.android.communication.connection.AndroidUdpConnection;
import com.fuav.android.communication.connection.BluetoothConnection;
import com.fuav.android.communication.connection.usb.UsbConnection;
import com.fuav.android.core.MAVLink.connection.MavLinkConnection;
import com.fuav.android.core.MAVLink.connection.MavLinkConnectionListener;
import com.fuav.android.core.drone.DroneManager;
import com.fuav.android.core.survey.CameraInfo;
import com.fuav.android.exception.ConnectionException;
import com.fuav.android.utils.Utils;
import com.fuav.android.utils.analytics.GAUtils;
import com.fuav.android.utils.file.IO.CameraInfoLoader;
import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.model.IApiListener;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * 3DR Services background service implementation.
 */
public class DroidPlannerService extends Service {

    private static final String TAG = DroidPlannerService.class.getName();

    /**
     * Status bar notification id
     */
    private static final int FOREGROUND_ID = 101;

    /**
     * Set of actions to notify the local app's components of the service events.
     */
    public static final String ACTION_DRONE_CREATED = Utils.PACKAGE_NAME + ".ACTION_DRONE_CREATED";
    public static final String ACTION_DRONE_DESTROYED = Utils.PACKAGE_NAME + ".ACTION_DRONE_DESTROYED";
    public static final String ACTION_KICK_START_DRONESHARE_UPLOADS = Utils.PACKAGE_NAME + ".ACTION_KICK_START_DRONESHARE_UPLOADS";
    public static final String ACTION_RELEASE_API_INSTANCE = Utils.PACKAGE_NAME + ".action.RELEASE_API_INSTANCE";
    public static final String EXTRA_API_INSTANCE_APP_ID = "extra_api_instance_app_id";

    private static final IntentFilter networkFilter = new IntentFilter();

    static {
        networkFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        networkFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
    }

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch(action){
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    NetworkInfo.State networkState = netInfo == null
                            ? NetworkInfo.State.DISCONNECTED
                            : netInfo.getState();

                    switch (networkState) {
                        case CONNECTED:
                            final WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                            final String wifiSSID = wifiInfo.getSSID();
                            Timber.i("Connected to " + wifiSSID);
                            break;

                        case DISCONNECTED:
                            Timber.i("Disconnected from wifi network.");
                            break;

                        case CONNECTING:
                            Timber.i( "Connecting to wifi network.");
                            break;
                    }
                    break;

                case WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION:
                    final boolean isConnected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
                    Timber.i("Supplicant connection " + (isConnected ? "established" : "broken"));
                    break;
            }
        }
    };

    /**
     * Used to broadcast service events.
     */
    private LocalBroadcastManager lbm;

    /**
     * Wifi wake lock.
     */
    private WifiManager.WifiLock wifiLock;

    /**
     * Stores drone api instances per connected client. The client are denoted by their app id.
     */
    final ConcurrentHashMap<String, DroneApi> droneApiStore = new ConcurrentHashMap<>();

    /**
     * Caches mavlink connections per connection type.
     */
    final ConcurrentHashMap<String, AndroidMavLinkConnection> mavConnections = new ConcurrentHashMap<>();

    /**
     * Caches drone managers per connection type.
     */
    final ConcurrentHashMap<ConnectionParameter, DroneManager> droneManagers = new ConcurrentHashMap<>();

    private DPServices dpServices;
    private DroneAccess droneAccess;
    private MavLinkServiceApi mavlinkApi;

    private CameraInfoLoader cameraInfoLoader;
    private List<CameraDetail> cachedCameraDetails;

    /**
     * Generate a drone api instance for the client denoted by the given app id.
     *
     * @param listener Used to retrieve api information.
     * @param appId    Application id of the connecting client.
     * @return a IDroneApi instance
     */
    DroneApi registerDroneApi(IApiListener listener, String appId) {
        if (listener == null)
            return null;

        DroneApi droneApi = new DroneApi(this, listener, appId);
        droneApiStore.put(appId, droneApi);
        lbm.sendBroadcast(new Intent(ACTION_DRONE_CREATED));
        updateForegroundNotification();
        return droneApi;
    }

    /**
     * Release the drone api instance attached to the given app id.
     *
     * @param appId Application id of the disconnecting client.
     */
    void releaseDroneApi(String appId) {
        if (appId == null)
            return;

        DroneApi droneApi = droneApiStore.remove(appId);
        if (droneApi != null) {
            Timber.d("Releasing drone api instance for " + appId);
            droneApi.destroy();
            lbm.sendBroadcast(new Intent(ACTION_DRONE_DESTROYED));
            updateForegroundNotification();
        }
    }

    /**
     * Establish a connection with a vehicle using the given connection parameter.
     *
     * @param connParams Parameters used to connect to the vehicle.
     * @param appId      Application id of the connecting client.
     * @param listener   Callback to receive drone events.
     * @return A DroneManager instance which acts as router between the connected vehicle and the listeneing client(s).
     * @throws ConnectionException
     */
    DroneManager connectDroneManager(ConnectionParameter connParams, String appId, DroneApi listener) throws ConnectionException {
        if (connParams == null || TextUtils.isEmpty(appId) || listener == null)
            return null;

        DroneManager droneMgr = droneManagers.get(connParams);
        if (droneMgr == null) {
            Timber.d("Generating new drone manager.");
            droneMgr = new DroneManager(getApplicationContext(), connParams, new Handler(Looper.getMainLooper()),
                    mavlinkApi);
            droneManagers.put(connParams, droneMgr);
        }

        Timber.d("Drone manager connection for " + appId);
        droneMgr.connect(appId, listener);
        return droneMgr;
    }

    /**
     * Disconnect the given client from the vehicle managed by the given drone manager.
     *
     * @param droneMgr Handler for the connected vehicle.
     * @param clientInfo    Info of the disconnecting client.
     * @throws ConnectionException
     */
    void disconnectDroneManager(DroneManager droneMgr, DroneApi.ClientInfo clientInfo) throws ConnectionException {
        if (droneMgr == null || clientInfo == null || TextUtils.isEmpty(clientInfo.appId))
            return;

        String appId = clientInfo.appId;
        Timber.d("Drone manager disconnection for " + appId);
        droneMgr.disconnect(clientInfo);
        if (droneMgr.getConnectedAppsCount() == 0) {
            Timber.d("Destroying drone manager.");
            droneMgr.destroy();
            droneManagers.remove(droneMgr.getConnectionParameter());
        }
    }

    /**
     * Setup a MAVLink connection using the given parameter.
     *
     * @param connParams  Parameter used to setup the MAVLink connection.
     * @param listenerTag Used to identify the connection requester.
     * @param listener    Callback to receive the connection events.
     */
    void connectMAVConnection(ConnectionParameter connParams, String listenerTag, MavLinkConnectionListener listener) {
        AndroidMavLinkConnection conn = mavConnections.get(connParams.getUniqueId());
        final int connectionType = connParams.getConnectionType();
        final Bundle paramsBundle = connParams.getParamsBundle();
        if (conn == null) {

            //Create a new mavlink connection

            switch (connectionType) {
                case ConnectionType.TYPE_USB:
                    final int baudRate = paramsBundle.getInt(ConnectionType.EXTRA_USB_BAUD_RATE,
                            ConnectionType.DEFAULT_USB_BAUD_RATE);
                    conn = new UsbConnection(getApplicationContext(), baudRate);
                    Timber.d("Connecting over usb.");
                    break;

                case ConnectionType.TYPE_BLUETOOTH:
                    //Retrieve the bluetooth address to connect to
                    final String bluetoothAddress = paramsBundle.getString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS);
                    conn = new BluetoothConnection(getApplicationContext(), bluetoothAddress);
                    Timber.d("Connecting over bluetooth.");
                    break;

                case ConnectionType.TYPE_TCP:
                    //Retrieve the server ip and port
                    final String tcpServerIp = paramsBundle.getString(ConnectionType.EXTRA_TCP_SERVER_IP);
                    final int tcpServerPort = paramsBundle.getInt(ConnectionType
                            .EXTRA_TCP_SERVER_PORT, ConnectionType.DEFAULT_TCP_SERVER_PORT);
                    conn = new AndroidTcpConnection(getApplicationContext(), tcpServerIp, tcpServerPort);
                    Timber.d("Connecting over tcp.");
                    break;

                case ConnectionType.TYPE_UDP:
                    final int udpServerPort = paramsBundle
                            .getInt(ConnectionType.EXTRA_UDP_SERVER_PORT, ConnectionType.DEFAULT_UDP_SERVER_PORT);
                    conn = new AndroidUdpConnection(getApplicationContext(), udpServerPort);
                    Timber.d("Connecting over udp.");
                    break;

                default:
                    Timber.e("Unrecognized connection type: %s", connectionType);
                    return;
            }

            mavConnections.put(connParams.getUniqueId(), conn);
        }

        if (connectionType == ConnectionType.TYPE_UDP) {
            final String pingIpAddress = paramsBundle.getString(ConnectionType.EXTRA_UDP_PING_RECEIVER_IP);
            if (!TextUtils.isEmpty(pingIpAddress)) {
                try {
                    final InetAddress resolvedAddress = InetAddress.getByName(pingIpAddress);

                    final int pingPort = paramsBundle.getInt(ConnectionType.EXTRA_UDP_PING_RECEIVER_PORT);
                    final long pingPeriod = paramsBundle.getLong(ConnectionType.EXTRA_UDP_PING_PERIOD,
                            ConnectionType.DEFAULT_UDP_PING_PERIOD);
                    final byte[] pingPayload = paramsBundle.getByteArray(ConnectionType.EXTRA_UDP_PING_PAYLOAD);

                    ((AndroidUdpConnection) conn).addPingTarget(resolvedAddress, pingPort, pingPeriod, pingPayload);

                } catch (UnknownHostException e) {
                    Timber.e(e, "Unable to resolve UDP ping server ip address.");
                }
            }
        }

        conn.addMavLinkConnectionListener(listenerTag, listener);
        if (conn.getConnectionStatus() == MavLinkConnection.MAVLINK_DISCONNECTED) {
            conn.connect();

            // Record which connection type is used.
            GAUtils.sendEvent(new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.MAVLINK_CONNECTION)
                    .setAction("MavLink connect")
                    .setLabel(connParams.toString()));
        }
    }

    /**
     * Disconnect the MAVLink connection for the given listener.
     *
     * @param connParams  Connection parameters
     * @param listenerTag Listener to be disconnected.
     */
    void disconnectMAVConnection(ConnectionParameter connParams, String listenerTag) {
        final AndroidMavLinkConnection conn = mavConnections.get(connParams.getUniqueId());
        if (conn == null)
            return;

        conn.removeMavLinkConnectionListener(listenerTag);

        if (conn.getMavLinkConnectionListenersCount() == 0 && conn.getConnectionStatus() !=
                MavLinkConnection.MAVLINK_DISCONNECTED) {
            Timber.d("Disconnecting...");
            conn.disconnect();

            GAUtils.sendEvent(new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.MAVLINK_CONNECTION)
                    .setAction("MavLink disconnect")
                    .setLabel(connParams.toString()));
        }
    }

    /**
     * Register a log listener.
     *
     * @param connParams      Parameters whose connection's data to log.
     * @param tag             Tag for the listener.
     * @param loggingFilePath File path for the logging file.
     */
    void addLoggingFile(ConnectionParameter connParams, String tag, String loggingFilePath) {
        AndroidMavLinkConnection conn = mavConnections.get(connParams.getUniqueId());
        if (conn == null)
            return;

        conn.addLoggingPath(tag, loggingFilePath);
    }

    /**
     * Unregister a log listener.
     *
     * @param connParams Connection parameters from whom to stop the logging.
     * @param tag        Tag for the listener.
     */
    void removeLoggingFile(ConnectionParameter connParams, String tag) {
        AndroidMavLinkConnection conn = mavConnections.get(connParams.getUniqueId());
        if (conn == null)
            return;

        conn.removeLoggingPath(tag);
    }

    /**
     * Retrieves the set of camera info provided by the app.
     *
     * @return a list of {@link CameraDetail} objects.
     */
    synchronized List<CameraDetail> getCameraDetails() {
        if (cachedCameraDetails == null) {
            List<String> cameraInfoNames = cameraInfoLoader.getCameraInfoList();

            List<CameraInfo> cameraInfos = new ArrayList<>(cameraInfoNames.size());
            for (String infoName : cameraInfoNames) {
                try {
                    cameraInfos.add(cameraInfoLoader.openFile(infoName));
                } catch (Exception e) {
                    Timber.e(e, e.getMessage());
                }
            }

            List<CameraDetail> cameraDetails = new ArrayList<>(cameraInfos.size());
            for (CameraInfo camInfo : cameraInfos) {
                cameraDetails.add(new CameraDetail(camInfo.name, camInfo.sensorWidth,
                        camInfo.sensorHeight, camInfo.sensorResolution, camInfo.focalLength,
                        camInfo.overlap, camInfo.sidelap, camInfo.isInLandscapeOrientation));
            }

            cachedCameraDetails = cameraDetails;
        }

        return cachedCameraDetails;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("Binding intent: " + intent);
        final String action = intent.getAction();
        if (IDroidPlannerServices.class.getName().equals(action)) {
            // Return binder to ipc client-server interaction.
            return dpServices;
        } else {
            // Return binder to the service.
            return droneAccess;
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();


        Timber.d("Creating 3DR Services.");

        final Context context = getApplicationContext();

        final WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiMgr.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG);

        Timber.i("Acquiring wifi wake lock.");
        wifiLock.acquire();

        mavlinkApi = new MavLinkServiceApi(this);
        droneAccess = new DroneAccess(this);
        dpServices = new DPServices(this);
        lbm = LocalBroadcastManager.getInstance(context);
        this.cameraInfoLoader = new CameraInfoLoader(context);

        updateForegroundNotification();

        registerReceiver(networkReceiver, networkFilter);
    }

    @SuppressLint("NewApi")
    private void updateForegroundNotification() {
        final Context context = getApplicationContext();

//        //Put the service in the foreground
//        final Notification.Builder notifBuilder = new Notification.Builder(context)
//                .setContentTitle("FUAV Service")
//                .setSmallIcon(R.drawable.ic_stat_notify)
//                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context,
//                        FlightActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0));
//
//        final int connectedCount = droneApiStore.size();
//        if (connectedCount > 0) {
//            if (connectedCount == 1) {
//                notifBuilder.setContentText("1 connected vehicle");
//            } else {
//                notifBuilder.setContentText(connectedCount + " connected vehicle");
//            }
//        }
//
//        final Notification notification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
//                ? notifBuilder.build()
//                : notifBuilder.getNotification();
//        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("Destroying 3DR Services.");

        unregisterReceiver(networkReceiver);

        for (DroneApi droneApi : droneApiStore.values()) {
            droneApi.destroy();
        }
        droneApiStore.clear();

        for (AndroidMavLinkConnection conn : mavConnections.values()) {
            conn.disconnect();
            conn.removeAllMavLinkConnectionListeners();
        }

        mavConnections.clear();
        dpServices.destroy();

//        stopForeground(true);

        if(wifiLock != null){
            Timber.i("Releasing wifi wake lock.");
            wifiLock.release();
            wifiLock = null;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_KICK_START_DRONESHARE_UPLOADS:
                    for (DroneManager droneMgr : droneManagers.values()) {
                        droneMgr.kickStartDroneShareUpload();
                    }
                    break;

                case ACTION_RELEASE_API_INSTANCE:
                    final String appId = intent.getStringExtra(EXTRA_API_INSTANCE_APP_ID);
                    releaseDroneApi(appId);
                    break;
            }
        }

        stopSelf();
        return START_NOT_STICKY;
    }

}
