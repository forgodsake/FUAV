package com.o3dr.services.android.lib.drone.connection;

/**
 * Contains constants used for the connection parameters.
 */
public class ConnectionType {

    /**
     *  USB connection type
     */
    public static final int TYPE_USB = 0;
    /**
     * Key used to retrieve the usb baud rate from the connection parameter bundle.
     */
    public static final String EXTRA_USB_BAUD_RATE = "extra_usb_baud_rate";
    /**
     * Default value for the usb baud rate.
     */
    public static final int DEFAULT_USB_BAUD_RATE = 57600;

    /**
     * UDP connection type
     */
    public static final int TYPE_UDP = 1;
    /**
     * Key used to retrieve the udp server port from the connection parameter bundle
     */
    public static final String EXTRA_UDP_SERVER_PORT = "extra_udp_server_port";
    /**
     * Default value for the upd server port.
     */
    public static final int DEFAULT_UDP_SERVER_PORT = 14550;

    /**
     * Key used to retrieve the ip address of the udp server to ping.
     */
    public static final String EXTRA_UDP_PING_RECEIVER_IP = "extra_udp_ping_receiver_ip";

    /**
     * Key used to retrieve the port of the udp server to ping.
     */
    public static final String EXTRA_UDP_PING_RECEIVER_PORT = "extra_udp_ping_receiver_port";

    /**
     * Ping payload.
     */
    public static final String EXTRA_UDP_PING_PAYLOAD = "extra_udp_ping_payload";

    /**
     * How often should the udp ping be performed.
     */
    public static final String EXTRA_UDP_PING_PERIOD = "extra_udp_ping_period";

    public static final long DEFAULT_UDP_PING_PERIOD = 10000l; //10 seconds

    /**
     * TCP connection type
     */
    public static final int TYPE_TCP = 2;
    /**
     * Key used to retrieve the tcp server ip from the connection parameter bundle
     */
    public static final String EXTRA_TCP_SERVER_IP = "extra_tcp_server_ip";
    /**
     * Key used to retrieve the tcp server port from the connection parameter bundle
     */
    public static final String EXTRA_TCP_SERVER_PORT = "extra_tcp_server_port";
    /**
     * Default value for the tcp server port.
     */
    public static final int DEFAULT_TCP_SERVER_PORT = 5763;
    /**
     * Bluetooth connection type
     */
    public static final int TYPE_BLUETOOTH = 3;
    /**
     * Key used to retrieve the bluetooth address from the connection parameter bundle
     */
    public static final String EXTRA_BLUETOOTH_ADDRESS = "extra_bluetooth_address";

}
