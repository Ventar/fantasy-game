package mro.fantasy.game;

import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration parameters for the game.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-13
 */
@Configuration
@PropertySource("classpath:application.properties")
public class GameConfig {

    /**
     * Log seperator string :)
     */
    public static final String LOG_SEPERATOR = "----------------------------------------------------------------------------";

    /**
     * UDP port that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     */
    @Value("${game.event.udp.port}")
    private int eventUDPPort;

    /**
     * UDP address that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     */
    @Value("${game.event.udp.address}")
    private String eventUDPAddress;


    /**
     * Size of the datagram package that is used to read the UDP game events.
     */
    @Value("${game.event.udp.buffer.bytes}")
    private int eventUDPBufferBytes;

    /**
     * Returns UDP port that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     *
     * @return the port
     */
    public int getEventUDPPort() {
        return eventUDPPort;
    }

    /**
     * Returns UDP address that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     *
     * @return the address
     */
    public String getEventUDPAddress() {
        return eventUDPAddress;
    }

    /**
     * Returns the size of the datagram package that is used to read the UDP game events.
     *
     * @return the size
     */
    public int getEventUDPBufferBytes() {
        return eventUDPBufferBytes;
    }
}
