package mro.fantasy.game.devices.discovery.impl;

import mro.fantasy.game.devices.DeviceType;
import mro.fantasy.game.devices.events.DeviceDataPackage;
import mro.fantasy.game.devices.events.DeviceEventType;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Base class for hardware devices that offers basic UDP support to send datagramms from the server to the devices.
 *
 * @author Michael Rodebuecher
 * @since 2022-08-22
 */
public class AbstractDevice {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(AbstractDevice.class);

    /**
     * The IP address of the device.
     */
    private InetAddress deviceAddress;

    /**
     * The UDP port of the device.
     */
    private int deviceUDPPort;

    /**
     * The type of the device.
     */
    private DeviceType deviceType;

    /**
     * The unique ID / name of this device.
     */
    private String deviceId;

    /**
     * The socket to send out UDP messages to the device.
     */
    private DatagramSocket socket = new DatagramSocket();

    /**
     * Creates a new device.
     *
     * @param deviceId      the unique ID / name of this device.
     * @param deviceType    the type of the device.
     * @param deviceAddress the IP address of the device.
     * @param udpPort       the UDP port of the device.
     *
     * @throws IOException in case the UDP datagramm socket cannot be created
     */
    public AbstractDevice(String deviceId, DeviceType deviceType, InetAddress deviceAddress, int udpPort) throws IOException {
        this.deviceAddress = deviceAddress;
        this.deviceUDPPort = udpPort;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
    }

    /**
     * Constructs the header of an outgoing event and append the passed data. Afterwards this is sent as a datagram packet to the device via UDP.
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

        try {
            DatagramPacket datagramPacket = new DatagramPacket(dataPackage.getRaw(), dataPackage.getRaw().length, deviceAddress, deviceUDPPort);
            socket.send(datagramPacket);
        } catch (IOException e) {
            LOG.warn("Could not send data package ::= [{}]:", dataPackage, e);
        }

    }

    /**
     * Sends a {@link DeviceEventType#REGISTER} UDP message to the passed address with the ip address and UDP port of the server.
     *
     * @param deviceId      the id of the server device.
     * @param serverAddress the IP address of the server to send the message to
     * @param serverUDPPort the UDP port of the server to send the message to
     */
    public void sendRegister(String deviceId, String serverAddress, int serverUDPPort) {

        LOG.debug("Send register message to ::= [{}] on port ::= [{}]", deviceAddress, deviceUDPPort);

        byte[] data = new byte[6];                                                // data part of the DeviceDataPackage

        String[] ipParts = serverAddress.split("\\.");     // IP address
        data[0] = (byte) Integer.valueOf(ipParts[0]).intValue();
        data[1] = (byte) Integer.valueOf(ipParts[1]).intValue();
        data[2] = (byte) Integer.valueOf(ipParts[2]).intValue();
        data[3] = (byte) Integer.valueOf(ipParts[3]).intValue();

        data[4] = (byte) (serverUDPPort >>> 8);                                   // UDP port
        data[5] = (byte) (serverUDPPort);

        DeviceDataPackage ddp = new DeviceDataPackage(DeviceType.SERVER, deviceId, DeviceEventType.REGISTER.getEventId(), data);

        try {
            DatagramPacket datagramPacket = new DatagramPacket(ddp.getRaw(), ddp.getRaw().length, deviceAddress, deviceUDPPort);
            socket.send(datagramPacket);
            LOG.debug("Send datagram ::= [{}]", ddp);
        } catch (IOException e) {
            LOG.warn("Could not send data package ::= [{}]:", ddp, e);
        }

    }


}
