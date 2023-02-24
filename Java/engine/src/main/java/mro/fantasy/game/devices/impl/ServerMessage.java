package mro.fantasy.game.devices.impl;

import mro.fantasy.game.devices.events.DeviceMessage;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Counterpart to the {@link DeviceMessage}, i.e. the data that is sent from the server to a device with the following format.
 *
 * <pre>{@code
 * part -  | HEADER     |  DATA
 * byte -  | 0          |  [1+]
 * data -  | eventId    |  [data]
 * } </pre>
 * <p>
 * Since a device can only receive data from the server it does not need information about the device id like the server does.
 *
 * @author Michael Rodenbuecher
 * @since 2023-02-24
 */
public class ServerMessage extends AbstractMessage {

    public static final Logger LOG = LoggerFactory.getLogger(ServerMessage.class);

    public ServerMessage (ServerMessageType messageType, byte[] data) {
        this.eventId = messageType.getEventId();
        this.data = data;
    }


    public ServerMessage (ServerMessageType messageType) {
        this.eventId = messageType.getEventId();
        this.data = new byte[] {0};
    }

    /**
     * Creates a new UDP packet to be sent to a device
     * @param inetAddress the address of the device
     * @param udpPort the device UDP port
     * @return
     */
    DatagramPacket toDatagramPacket(InetAddress inetAddress, int udpPort) {
        byte[] raw = new byte[1 + data.length];
        raw[0] = (byte) eventId;
        System.arraycopy(data, 0, raw, 1, data.length);
        if (LOG.isTraceEnabled()) {
            String s = "[";
            for (int i = 0; i < raw.length; i++) {
                s += Byte.toUnsignedInt(raw[i]);
                if (i < raw.length - 1) {
                    s += ", ";
                }
            }
            s+= "]";
            LOG.trace("Send raw data: {}", s);
        }

        return new DatagramPacket(raw,raw.length, inetAddress, udpPort);
    }


}
