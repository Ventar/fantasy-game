package mro.fantasy.game.devices.impl;

import mro.fantasy.game.devices.events.DeviceMessage;
import mro.fantasy.game.devices.events.DeviceMessageType;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDevice.class);

    /**
     * The IP address of the device.
     */
    protected InetAddress deviceAddress;

    /**
     * The UDP port of the device.
     */
    protected int deviceUDPPort;

    /**
     * The type of the device.
     */
    protected DeviceType deviceType;

    /**
     * The unique ID / name of this device.
     */
    protected String deviceId;

    /**
     * The socket to send out UDP messages to the device.
     */
    protected DatagramSocket socket = new DatagramSocket();

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
     * The unique ID of the device
     *
     * @return the id
     */
    public String getId() {
        return deviceId;
    }

    /**
     * Constructs the header of an outgoing event and append the passed data. Afterwards this is sent as a datagram packet to the device via UDP.
     *
     * @param msgType the type of the message to send
     * @param data    the event data
     *
     * @see UDPDeviceEventServiceImpl
     * @see ServerMessage
     */
    public void sendData(ServerMessageType msgType, byte[] data) {
        ServerMessage msg = new ServerMessage(msgType, data);

        LOG.debug("[{}] - Try to send data package ::= [{}]", deviceId, msg);

        try {
            socket.send(msg.toDatagramPacket(deviceAddress, deviceUDPPort));
        } catch (IOException e) {
            LOG.warn("[{}] - Could not send data package ::= [{}]:", deviceId, msg, e);
        }

    }

    /**
     * Constructs the header of an outgoing event without any payload. Afterwards this is sent as a datagram packet to the device via UDP.
     *
     * @param msgType the type of the message to send
     *
     * @see UDPDeviceEventServiceImpl
     * @see DeviceMessage
     */
    public void sendData(ServerMessageType msgType) {
        ServerMessage msg = new ServerMessage(msgType);

        LOG.debug("[{}] - Try to send data package ::= [{}]", deviceId, msg);

        try {
            socket.send(msg.toDatagramPacket(deviceAddress, deviceUDPPort));
        } catch (IOException e) {
            LOG.warn("[{}] - Could not send data package ::= [{}]:", deviceId, msg, e);
        }

    }

    /**
     * Sends a {@link DeviceMessageType#REGISTER} UDP message to the passed address with the ip address and UDP port of the server.
     *
     * @param deviceId      the id of the server device.
     * @param serverAddress the IP address of the server to send the message to
     * @param serverUDPPort the UDP port of the server to send the message to
     */
    public void sendRegister(String serverAddress, int serverUDPPort) {

        LOG.debug("[{}] - Send register message to ::= [{}] on port ::= [{}]", this.deviceId, deviceAddress, deviceUDPPort);

        byte[] data = new byte[6];                                                // data part of the DeviceDataPackage

        String[] ipParts = serverAddress.split("\\.");     // IP address
        data[0] = (byte) Integer.valueOf(ipParts[0]).intValue();
        data[1] = (byte) Integer.valueOf(ipParts[1]).intValue();
        data[2] = (byte) Integer.valueOf(ipParts[2]).intValue();
        data[3] = (byte) Integer.valueOf(ipParts[3]).intValue();

        data[4] = (byte) (serverUDPPort >>> 8);                                   // UDP port
        data[5] = (byte) (serverUDPPort);

        try {
            sendData(ServerMessageType.REGISTER, data);
            // workaround for the weird arduino case were the first message is not handled.
            Thread.sleep(100);
            sendData(ServerMessageType.REGISTER, data);
        } catch (Exception e) {
            LOG.warn("Could not send data package:",  e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDevice that = (AbstractDevice) o;
        return Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }

    @Override
    public String toString() {
        return "AbstractDevice{" +
                       "deviceAddress=" + deviceAddress +
                       ", deviceUDPPort=" + deviceUDPPort +
                       ", deviceType=" + deviceType +
                       ", deviceId='" + deviceId + '\'' +
                       '}';
    }


    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket p = new DatagramPacket("Hello".getBytes(StandardCharsets.UTF_8), 5, InetAddress.getByName("192.168.51.58"), 4669);
        socket.send(p);
    }
}
