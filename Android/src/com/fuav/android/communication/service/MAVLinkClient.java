package com.fuav.android.communication.service;

import android.content.Context;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.model.ICommandListener;

import com.fuav.android.api.MavLinkServiceApi;
import com.fuav.android.core.MAVLink.MAVLinkStreams;
import com.fuav.android.core.MAVLink.connection.MavLinkConnection;
import com.fuav.android.core.MAVLink.connection.MavLinkConnectionListener;
import com.fuav.android.core.MAVLink.connection.MavLinkConnectionTypes;
import com.fuav.android.core.drone.CommandTracker;
import com.fuav.android.data.SessionDB;
import com.fuav.android.utils.file.FileUtils;

import java.io.File;
import java.util.Date;

/**
 * Provide a common class for some ease of use functionality
 */
public class MAVLinkClient implements MAVLinkStreams.MAVLinkOutputStream {

    private static final int DEFAULT_SYS_ID = 255;
    private static final int DEFAULT_COMP_ID = 190;

    private static final String TLOG_PREFIX = "log";

    /**
     * Maximum possible sequence number for a packet.
     */
    private static final int MAX_PACKET_SEQUENCE = 255;

    private final MavLinkConnectionListener mConnectionListener = new MavLinkConnectionListener() {

        @Override
        public void onStartingConnection() {
            listener.notifyStartingConnection();
        }

        @Override
        public void onConnect(long connectionTime) {
            startLoggingThread(connectionTime);
            listener.notifyConnected();
        }

        @Override
        public void onReceivePacket(final MAVLinkPacket packet) {
            listener.notifyReceivedData(packet);
        }

        @Override
        public void onDisconnect(long disconnectTime) {
            listener.notifyDisconnected();
            closeConnection();
        }

        @Override
        public void onComError(final String errMsg) {
            if (errMsg != null) {
                listener.onStreamError(errMsg);
            }
        }
    };

    private final MAVLinkStreams.MavlinkInputStream listener;
    private final MavLinkServiceApi mavLinkApi;
    private final SessionDB sessionDB;
    private final Context context;

    private int packetSeqNumber = 0;
    private final ConnectionParameter connParams;

    private CommandTracker commandTracker;

    public MAVLinkClient(Context context, MAVLinkStreams.MavlinkInputStream listener,
                         ConnectionParameter connParams, MavLinkServiceApi serviceApi) {
        this.context = context;
        this.listener = listener;
        this.mavLinkApi = serviceApi;
        this.connParams = connParams;
        this.sessionDB = new SessionDB(context);
    }

    public void setCommandTracker(CommandTracker commandTracker) {
        this.commandTracker = commandTracker;
    }

    @Override
    public void openConnection() {
        if (this.connParams == null)
            return;

        final String tag = toString();
        final int connectionStatus = mavLinkApi.getConnectionStatus(this.connParams, tag);
        if (connectionStatus != MavLinkConnection.MAVLINK_CONNECTED) {
            mavLinkApi.connectMavLink(this.connParams, tag, mConnectionListener);
        }
    }

    @Override
    public void closeConnection() {
        if (this.connParams == null)
            return;

        final String tag = toString();
        if (mavLinkApi.getConnectionStatus(this.connParams, tag) != MavLinkConnection.MAVLINK_DISCONNECTED) {
            mavLinkApi.disconnectMavLink(this.connParams, tag);
            stopLoggingThread(System.currentTimeMillis());
            listener.notifyDisconnected();
        }
    }

    @Override
    public void sendMavMessage(MAVLinkMessage message, ICommandListener listener) {
        sendMavMessage(message, DEFAULT_SYS_ID, DEFAULT_COMP_ID, listener);
    }

    @Override
    public void sendMavMessage(MAVLinkMessage message, int sysId, int compId, ICommandListener listener){
        if (this.connParams == null || message == null) {
            return;
        }

        final MAVLinkPacket packet = message.pack();
        packet.sysid = sysId;
        packet.compid = compId;
        packet.seq = packetSeqNumber;

        if(mavLinkApi.sendData(this.connParams, packet)) {
            packetSeqNumber = (packetSeqNumber + 1) % (MAX_PACKET_SEQUENCE + 1);

            if (commandTracker != null && listener != null) {
                commandTracker.onCommandSubmitted(message, listener);
            }
        }
    }

    @Override
    public boolean isConnected() {
        return this.connParams != null
                && mavLinkApi.getConnectionStatus(this.connParams, toString()) == MavLinkConnection.MAVLINK_CONNECTED;
    }

    public boolean isConnecting(){
        return this.connParams != null && mavLinkApi.getConnectionStatus(this.connParams,
                toString()) == MavLinkConnection.MAVLINK_CONNECTING;
    }

    @Override
    public void toggleConnectionState() {
        if (isConnected()) {
            closeConnection();
        } else {
            openConnection();
        }
    }

    private File getTLogDir(String appId) {
        return null;
    }

    private File getTempTLogFile(String appId, long connectionTimestamp) {
        return new File(getTLogDir(appId), getTLogFilename(connectionTimestamp));
    }

    private String getTLogFilename(long connectionTimestamp) {
        return TLOG_PREFIX + "_" + MavLinkConnectionTypes.getConnectionTypeLabel(this.connParams.getConnectionType()) +
                "_" + FileUtils.getTimeStamp(connectionTimestamp) + FileUtils.TLOG_FILENAME_EXT;
    }

    public void addLoggingFile(String appId){
        if(isConnecting() || isConnected()) {
            final File logFile = getTempTLogFile(appId, System.currentTimeMillis());
            mavLinkApi.addLoggingFile(this.connParams, appId, logFile.getAbsolutePath());
        }
    }

    public void removeLoggingFile(String appId){
        if(isConnecting() || isConnected()){
            mavLinkApi.removeLoggingFile(this.connParams, appId);
        }
    }

    private void startLoggingThread(long startTime) {
        //log into the database the connection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(connParams.getConnectionType());
        this.sessionDB.startSession(new Date(startTime), connectionType);
    }

    private void stopLoggingThread(long stopTime) {
        //log into the database the disconnection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(connParams.getConnectionType());
        this.sessionDB.endSession(new Date(stopTime), connectionType, new Date());
    }
}
