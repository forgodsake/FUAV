package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.util.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_ARTOO_INPUT_REPORT_MESSAGE;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_CABLE_CAM_OPTIONS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_CABLE_CAM_WAYPOINT;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_FOLLOW_OPTIONS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GET_BUTTON_SETTING;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_RECORD;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_REQUEST_STATE;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_SET_EXTENDED_REQUEST;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_SET_REQUEST;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_STATE;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_STATE_V2;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_LOCATION;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_RECORD_POSITION;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_SET_CURRENT_SHOT;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SET_BUTTON_SETTING;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SHOT_ERROR;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SHOT_OPTIONS;

/**
 * Utility class to generate tlv packet from received bytes.
 */
public class TLVMessageParser {

    private static final String TAG = TLVMessageParser.class.getSimpleName();

    public static List<TLVPacket> parseTLVPacket(byte[] packetData){
        if(packetData == null || packetData.length == 0)
            return null;

        return parseTLVPacket(ByteBuffer.wrap(packetData));
    }

    public static List<TLVPacket> parseTLVPacket(ByteBuffer packetBuffer) {
        final List<TLVPacket> packetList = new ArrayList<>();

        if (packetBuffer == null)
            return packetList;

        final int bufferSize = packetBuffer.limit();
        if(bufferSize <= 0)
            return packetList;

        final ByteOrder originalOrder = packetBuffer.order();
        packetBuffer.order(TLVPacket.TLV_BYTE_ORDER);

        int messageType = -1;
        try {

            while (packetBuffer.remaining() >= TLVPacket.MIN_TLV_PACKET_SIZE) {
                messageType = packetBuffer.getInt();
                final int messageLength = packetBuffer.getInt();

                int remaining = packetBuffer.remaining();
                Log.d(TAG, String.format("Received message %d of with value of length %d. Remaining buffer size is %d", messageType, messageLength, remaining));

                if (messageLength > remaining) {
                    break;
                }

                TLVPacket packet = null;
                packetBuffer.mark();

                switch (messageType) {
                    case TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT:
                    case TYPE_SOLO_MESSAGE_SET_CURRENT_SHOT: {
                        final int shotType = packetBuffer.getInt();
                        if (messageType == TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT)
                            packet = new SoloMessageShotGetter(shotType);
                        else
                            packet = new SoloMessageShotSetter(shotType);
                        break;
                    }

                    case TYPE_SOLO_MESSAGE_LOCATION: {
                        final double latitude = packetBuffer.getDouble();
                        final double longitude = packetBuffer.getDouble();
                        final float altitude = packetBuffer.getFloat();
                        packet = new SoloMessageLocation(latitude, longitude, altitude);
                        break;
                    }

                    case TYPE_SOLO_MESSAGE_RECORD_POSITION: {
                        packet = new SoloMessageRecordPosition();
                        break;
                    }

                    case TYPE_SOLO_CABLE_CAM_OPTIONS: {
                        final short camInterpolation = packetBuffer.getShort();
                        final short yawDirectionClockwise = packetBuffer.getShort();
                        final float cruiseSpeed = packetBuffer.getFloat();
                        packet = new SoloCableCamOptions(camInterpolation, yawDirectionClockwise, cruiseSpeed);
                        break;
                    }

                    case TYPE_SOLO_GET_BUTTON_SETTING:
                    case TYPE_SOLO_SET_BUTTON_SETTING: {
                        final int button = packetBuffer.getInt();
                        final int event = packetBuffer.getInt();
                        final int shotType = packetBuffer.getInt();
                        final int flightMode = packetBuffer.getInt();
                        if (messageType == TYPE_SOLO_GET_BUTTON_SETTING)
                            packet = new SoloButtonSettingGetter(button, event, shotType, flightMode);
                        else
                            packet = new SoloButtonSettingSetter(button, event, shotType, flightMode);
                        break;
                    }

                    case TYPE_SOLO_FOLLOW_OPTIONS: {
                        final float cruiseSpeed = packetBuffer.getFloat();
                        final int lookAtValue = packetBuffer.getInt();
                        packet = new SoloFollowOptions(cruiseSpeed, lookAtValue);
                        break;
                    }

                    case TYPE_SOLO_SHOT_OPTIONS: {
                        final float cruiseSpeed = packetBuffer.getFloat();
                        packet = new SoloShotOptions(cruiseSpeed);
                        break;
                    }

                    case TYPE_SOLO_SHOT_ERROR: {
                        final int errorType = packetBuffer.getInt();
                        packet = new SoloShotError(errorType);
                        break;
                    }

                    case TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR: {
                        final byte[] exceptionData = new byte[messageLength];
                        packetBuffer.get(exceptionData);
                        packet = new SoloMessageShotManagerError(new String(exceptionData));
                        break;
                    }

                    case TYPE_SOLO_CABLE_CAM_WAYPOINT: {
                        final double latitude = packetBuffer.getDouble();
                        final double longitude = packetBuffer.getDouble();
                        final float altitude = packetBuffer.getFloat();
                        final float degreesYaw = packetBuffer.getFloat();
                        final float pitch = packetBuffer.getFloat();

                        packet = new SoloCableCamWaypoint(latitude, longitude, altitude, degreesYaw, pitch);
                        break;
                    }

                    case TYPE_ARTOO_INPUT_REPORT_MESSAGE: {
                        final double timestamp = packetBuffer.getDouble();
                        final short gimbalY = packetBuffer.getShort();
                        final short gimbalRate = packetBuffer.getShort();
                        final short battery = packetBuffer.getShort();

                        packet = new ControllerMessageInputReport(timestamp, gimbalY, gimbalRate, battery);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_SET_REQUEST: {
                        final short command = packetBuffer.getShort();
                        final short value = packetBuffer.getShort();
                        packet = new SoloGoproSetRequest(command, value);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_RECORD: {
                        @SoloGoproConstants.RecordCommand final int command = packetBuffer.getInt();
                        packet = new SoloGoproRecord(command);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_STATE: {
                        packet = new SoloGoproState(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_STATE_V2:{
                        packet = new SoloGoproStateV2(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_REQUEST_STATE: {
                        packet = new SoloGoproRequestState();
                        break;
                    }

                    case TYPE_SOLO_GOPRO_SET_EXTENDED_REQUEST: {
                        final short command = packetBuffer.getShort();
                        final byte[] values = new byte[4];
                        packetBuffer.get(values);
                        packet = new SoloGoproSetExtendedRequest(command, values);
                        break;
                    }

                    default:
                        break;
                }

                if (packet != null && packet.getMessageLength() == messageLength) {
                    packetList.add(packet);
                } else {
                    packetBuffer.reset();
                    packetBuffer.position(packetBuffer.position() + messageLength);
                }
            }

        } catch (BufferUnderflowException e) {
            Log.e(TAG, "Invalid data for tlv packet of type " + messageType);
        }

        packetBuffer.order(originalOrder);
        return packetList;
    }

    //Private constructor to prevent instantiation
    private TLVMessageParser() {}
}
