package mro.fantasy.game.devices.events.impl;

import mro.fantasy.game.devices.events.DeviceMessage;
import mro.fantasy.game.devices.events.DeviceEventHandler;
import mro.fantasy.game.devices.events.DeviceEventService;
import mro.fantasy.game.engine.events.impl.EventThreadPool;
import mro.fantasy.game.utils.NetworkConfiguration;
import mro.fantasy.game.utils.ServiceThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;

/**
 * Implementation of the event service which listens on a UDP port to receive events from the devices of the game. The service has access to all device event handlers, converts all
 * incoming datagram packages to {@link DeviceMessage}s and offer them to the {@link DeviceEventHandler#handle(DeviceMessage)} method.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-13
 */
@Service
public class UDPDeviceEventServiceImpl extends ServiceThread implements DeviceEventService {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(UDPDeviceEventServiceImpl.class);

    /**
     * Socket to receive data from the devices.
     */
    private DatagramSocket socket;

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
     * Network utilities to get IP and MAC address
     */
    @Autowired
    private NetworkConfiguration networkConfiguration;

    @Override
    public void start() {
        try {

            LOG.info("");
            LOG.info("---------------------------------------------------------------------------------");
            LOG.info("INITIALIZE DEVICE EVENT SERVICE");
            LOG.info("---------------------------------------------------------------------------------");
            LOG.info("");

            LOG.debug("Try to open UDP event listener on  ::= [{}:{}]", networkConfiguration.getAdapterIPAddress(), networkConfiguration.getEventUDPPort());
            socket = new DatagramSocket(networkConfiguration.getEventUDPPort(), networkConfiguration.getAdapterINetAddress());
            super.setName("DEVICES");
            super.setLogger(LOG);
            super.start();
            LOG.debug("Started service ::= [{}] with ::= [{}] device event handler", getClass().getSimpleName(), eventHandler.size());

        } catch (Exception e) {
            throw new IllegalStateException("Cannot start device event service", e);
        }

        LOG.debug("Device event service successfully initialized...");
    }

    /**
     * Opens a UDP socket connection and listen for incoming datagram packets from devices which are connected to the game server.
     */
    @Override
    public void work() throws Exception {

        byte[] buf = new byte[networkConfiguration.getEventUDPBufferBytes()];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        LOG.trace("Received UDP packet of length ::= [{}] from ::= [{}]", packet.getLength(), packet.getAddress());

        // pass the incoming data to a new thread to process it there and free up the socket for the next event.

        executor.execute(() -> {
            try {
                DeviceMessage dataPackage = DeviceMessage.parse(packet.getData());
                LOG.debug("[{}] - Received device event ::= [{}]", dataPackage.getDeviceId(), dataPackage);

                eventHandler.forEach(handler -> handler.handle(dataPackage));    // offer the event to all registered event handler.
            } catch (Exception e) {
                LOG.debug("Error during processing of UDP event: ", e);
            }
        });

    }

}
