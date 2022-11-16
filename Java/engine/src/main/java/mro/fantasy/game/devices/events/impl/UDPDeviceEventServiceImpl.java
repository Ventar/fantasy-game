package mro.fantasy.game.devices.events.impl;

import mro.fantasy.game.devices.events.DeviceDataPackage;
import mro.fantasy.game.devices.events.DeviceEventHandler;
import mro.fantasy.game.devices.events.DeviceEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

/**
 * Implementation of the event service which listens on a UDP port to receive events from the devices of the game. The service has access to all device event handlers, converts all
 * incoming datagram packages to {@link DeviceDataPackage}s and offer them to the {@link DeviceEventHandler#handle(DeviceDataPackage)} method.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-13
 */
@Service
//@PropertySource("classpath:application.properties")
public class UDPDeviceEventServiceImpl extends Thread implements DeviceEventService {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(UDPDeviceEventServiceImpl.class);

    /**
     * Socket to receive data from the devices.
     */
    private DatagramSocket socket;

    /**
     * UDP port that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     */
    @Value("${game.event.udp.port}")
    private int eventUDPPort;

    /**
     * UDP address that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     */
    @Value("${game.ip.address}")
    private String ipAddress;

    /**
     * Size of the datagram package that is used to read the UDP game events.
     */
    @Value("${game.event.udp.buffer.bytes}")
    private int eventUDPBufferBytes;

    /**
     * Event handler which are interested in incoming events from devices.
     */
    @Autowired
    private List<DeviceEventHandler> eventHandler;

    /**
     * Threadpool to handle device related tasks.
     */
    @Autowired
    private EventThreadPool executor;

    /**
     * Initializes the event service by creating the datagram socket and starting the service as a background thread.
     */
    @PostConstruct
    private void postConstruct() {
        try {
            LOG.debug("Try to open UDP event listener on  ::= [{}:{}]", ipAddress, eventUDPPort);
            socket = new DatagramSocket(eventUDPPort, InetAddress.getByName(ipAddress));
            super.setName("DEVICES");
            super.start();
            LOG.debug("Started service ::= [{}] with ::= [{}] device event handler", getClass().getSimpleName(), eventHandler.size());

        } catch (Exception e) {
            throw new IllegalStateException("Cannot start device event service", e);
        }
    }

    /**
     * Opens a UDP socket connection and listen for incoming datagram packets from devices which are connected to the game server.
     */
    @Override
    public void run() {
        while (true) {
            try {

                byte[] buf = new byte[eventUDPBufferBytes];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                LOG.trace("Received UDP packet of length ::= [{}] from ::= [{}]", packet.getLength(), packet.getAddress());

                // pass the incoming data to a new thread to process it there and free up the socket for the next event.

                executor.execute(() -> {
                    try {
                        DeviceDataPackage dataPackage = new DeviceDataPackage(packet.getData());
                        LOG.debug("Received device event ::= [{}]", dataPackage);

                        eventHandler.forEach(handler -> handler.handle(dataPackage));    // offer the event to all registered event handler.
                    } catch (Exception e) {
                        LOG.debug("Error during processing of UDP event: ", e);
                    }
                });

            } catch (Exception e) {
                LOG.debug("Error during UDP handling", e);
            }
        }
    }

}
