package mro.fantasy.applications;

import mro.fantasy.game.devices.DeviceType;
import mro.fantasy.game.devices.events.DeviceDataPackage;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utility class to simulate a device.
 *
 * @author Michael Rodenbuecher
 * @see UDPDeviceEventServiceImpl
 * @since 2022-08-13
 */
public class DeviceSimulator {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(DeviceSimulator.class);

    /**
     * The type of the device
     */
    protected DeviceType deviceType;

    /**
     * The IP address of the device.
     */
    protected InetAddress address;

    /**
     * The unique ID of the device. For a real device this is the MAC address od the device.
     */
    protected String deviceId;

    /**
     * The port to send data to.
     */
    protected int serverPort;

    /**
     * The port to receive data on.
     */
    protected int devicePort;

    /**
     * Socket receive data from the server via UDP
     */
    protected DatagramSocket socketListen;

    /**
     * Socket to send out data to the server via UDP
     */
    protected DatagramSocket socket;


    /**
     * Creates a new simulator.
     *
     * @param type       the type of the device
     * @param address    the IP address of the device.
     * @param deviceId   the unique ID of the device. For a real device this is the MAC address od the device.
     * @param serverPort the UDP port to send data to.
     * @param devicePort the UDP port to receive data on.
     *
     * @throws IllegalStateException if the underlying socket could not be created
     */
    public DeviceSimulator(DeviceType type, InetAddress address, String deviceId, int serverPort, int devicePort) {
        this.deviceType = type;
        this.address = address;
        this.deviceId = deviceId;
        this.serverPort = serverPort;
        this.devicePort = devicePort;
        try {
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create a datagram socket: ", e);
        }

    }

    /**
     * Constructs the header of an outgoing event and append the passed data. Afterwards this is send as a datagram packet to the server via UDP.
     *
     * @param eventId the id of the event that defines the data that is sent.
     * @param data    the event data
     *
     * @see UDPDeviceEventServiceImpl
     * @see DeviceDataPackage
     */
    public void sendData(int eventId, byte[] data) {
        DeviceDataPackage dataPackage = new DeviceDataPackage(deviceType, deviceId, eventId, data);

        LOG.debug("Try to send data package ::= [{}]", dataPackage);

        DatagramPacket datagramPacket = new DatagramPacket(dataPackage.getRaw(), dataPackage.getRaw().length, address, serverPort);


        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            LOG.warn("Could not send data package ::= [{}]:", dataPackage, e);
        }

    }

    public static void main(String[] args) throws UnknownHostException {
        DeviceSimulator simulator = new DeviceSimulator(DeviceType.BOARD_MODULE, InetAddress.getByName("192.168.2.181"), "17FEBA2BDF20", 4000, 6000);
        simulator.sendData(1, new byte[]{0});

    }

}
