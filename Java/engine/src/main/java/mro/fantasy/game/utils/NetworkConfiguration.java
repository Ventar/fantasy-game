package mro.fantasy.game.utils;

import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * Utility class to work with network adapters for the communication between the game server and the hardware devices.
 *
 * @author Michael Rodenbuecher
 * @since 2022-11-21
 */
@Component
public class NetworkConfiguration {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NetworkConfiguration.class);

    /**
     * IP address that is used by the server for communication to the devices. Some server may have multiple network adapter (especially during development) so that this field can
     * be set by the application properties manually to ensure that all devices are in the same network.
     */
    @Value("${game.ip.address}")
    private String serverIPAddress;

    /**
     * UDP port that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     */
    @Value("${game.device.event.udp.port}")
    private int eventUDPPort;

    /**
     * Size of the datagram package that is used to read the UDP game events.
     */
    @Value("${game.device.event.udp.buffer.bytes}")
    private int eventUDPBufferBytes;

    /**
     * Converted {@link #serverIPAddress} representation.
     */
    private InetAddress serverINetAddress;

    /**
     * The MAC address of the network adapter that is used by the IP address configured for the server.
     */
    private String macAddress;

    @PostConstruct
    private void postConstruct() {
        getMACAddress();
    }

    /**
     * Returns the MAC address of the network adapter that is used by the IP address configured for the server.
     *
     * @return the MAC address
     */
    public String getMACAddress() {
        if (macAddress == null) {
            try {
                NetworkInterface ni = NetworkInterface.getByInetAddress(getAdapterINetAddress());
                byte[] hardwareAddress = ni.getHardwareAddress();
                String[] hexadecimal = new String[hardwareAddress.length];
                for (int i = 0; i < hardwareAddress.length; i++) {
                    hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
                }
                this.macAddress = String.join("-", hexadecimal).replaceAll("-", "");;
                LOG.debug("Retrieved MAC address ::= [{}] for adapter with IP ::= [{}]", this.macAddress, this.serverIPAddress);
            } catch (Exception e) {
                LOG.warn("\n\n !!! Could not determine MAC ADDRESS, use default one: ", e);
                this.macAddress = "000000";
            }
        }

        return this.macAddress;
    }

    /**
     * Returns the IP address that is used by the server for communication to the devices. Some server may have multiple network adapter (especially during development) so that
     * this field can be set by the application properties manually to ensure that all devices are in the same network.
     *
     * @return the IP address
     */
    public String getAdapterIPAddress() {
        // TODO: Add logic to determine the IP address automatically
        return serverIPAddress;
    }

    /**
     * Returns the IP address that is used by the server for communication to the devices. Some server may have multiple network adapter (especially during development) so that
     * this field can be set by the application properties manually to ensure that all devices are in the same network.
     *
     * @return the IP address
     */
    public InetAddress getAdapterINetAddress() {

        if (serverINetAddress == null) {
            try {
                this.serverINetAddress = InetAddress.getByName(getAdapterIPAddress());
            } catch (Exception e) {
                LOG.warn("\n\n !!! Could not determine INET ADDRESS, use: ", e);
            }
        }

        return serverINetAddress;
    }

    /**
     * Returns the UDP port that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     *
     * @return the port
     */
    public int getEventUDPPort() {
        return eventUDPPort;
    }

    /**
     * The number of bytes to exchange {@link mro.fantasy.game.devices.events.DeviceDataPackage}s.
     *
     * @return the UDP buffer size
     */
    public int getEventUDPBufferBytes() {
        return eventUDPBufferBytes;
    }
}
