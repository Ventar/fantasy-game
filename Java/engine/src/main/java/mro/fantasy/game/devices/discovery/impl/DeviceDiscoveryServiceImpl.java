package mro.fantasy.game.devices.discovery.impl;

import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.board.impl.BoardModuleImpl;
import mro.fantasy.game.devices.discovery.DeviceDiscoveryEvent;
import mro.fantasy.game.devices.discovery.DeviceDiscoveryEventListener;
import mro.fantasy.game.devices.discovery.DeviceDiscoveryService;
import mro.fantasy.game.devices.events.impl.EventCallback;
import mro.fantasy.game.devices.events.impl.EventThreadPool;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Future;

import static mro.fantasy.game.devices.discovery.impl.MDNSServiceListener.registerMDNSListener;

/**
 * Service that uses the MDNS protocol to discover physical devices. The discovery service is used by the device controllers to get access the physical devices. Most of them
 * aggregate multiple and offer a single abstraction to the game engine.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-19
 */
@Service
public class DeviceDiscoveryServiceImpl implements DeviceDiscoveryService {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DeviceDiscoveryServiceImpl.class);

    /**
     * IP address that is used to start the {@link JmDNS} listener.
     */
    @Value("${game.ip.address}")
    private String serverIPAddress;

    @Value("${game.mac.address}")
    private String serverMacAddress;

    /**
     * UDP port that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     */
    @Value("${game.event.udp.port}")
    private int serverUDPPort;

    /**
     * Size of the datagram package that is used to read the UDP game events.
     */
    @Value("${game.event.udp.buffer.bytes}")
    private int eventUDPBufferBytes;

    /**
     * Threadpool to handle device related tasks.
     */
    @Autowired
    private EventThreadPool executor;

    /**
     * Socket to send out data to the devices via UDP
     */
    protected DatagramSocket socket;

    /**
     * Set with registered Listeners
     */
    private Set<DeviceDiscoveryEventListener> listenerSet = new HashSet<>();

    /**
     * A list of futures that wait to be resolved.
     */
    private List<EventCallback> callbacks = new ArrayList<>();


    /**
     * A set of board modules that was discoverd by the {@link #jmdns} service.
     */
    private Set<BoardModule> boardModule = new HashSet<>();

    /**
     * The implementation utility class for the mDNS service.
     */
    private JmDNS jmdns;

    /**
     * Starts the discovery of physical devices.
     *
     * @throws IOException if the discovery  process cannot be started.
     */
    @PostConstruct
    public void start() throws IOException {
        LOG.debug("Start discovery service on network adapter ::= [{}]", serverIPAddress);

        this.socket = new DatagramSocket();

        jmdns = JmDNS.create(InetAddress.getByName(serverIPAddress));

        registerMDNSListener(jmdns, BOARD_MDNS_TYPE, (serviceEvent) -> {

            try {
                BoardModuleImpl bordModule = new BoardModuleImpl(serviceEvent.getName(), serviceEvent.getInfo().getInetAddresses()[0], serviceEvent.getInfo().getPort());
                bordModule.sendRegister(serverMacAddress, serverIPAddress, serverUDPPort);
            } catch (IOException e) {
                LOG.warn("Cannot register board module with id ::= [{}]:", serviceEvent.getName(), e);
            }

        });

        registerMDNSListener(jmdns, PLAYER_MDNS_TYPE, (serviceEvent) -> {
            // var ctrl = new PlayerController(serviceEvent.getInfo().getName(), serviceEvent.getInfo().getInet4Addresses()[0], serviceEvent.getInfo().getPort());
            // ctrl.sendRegisterListenerMessage(StaticConfig.LOCAL_IP_ADDRESS, StaticConfig.UDP_PORT_CONTROLLER_EVENT);
            // ctrl.sendLEDState(Color.OFF);
            // playerController.add(ctrl);

            DeviceDiscoveryEvent event = new DeviceDiscoveryEvent();

            listenerSet.stream().forEach(listener -> listener.onEvent(event));
            callbacks.forEach(c -> c.setEvent(event));
            callbacks.clear();

        });

    }


    /**
     * Stops the MDNS device discovery process.
     */
    public void stop() {
        if (jmdns != null) {
            try {
                jmdns.close();
                jmdns = null;
            } catch (Exception e) {
                LOG.debug("Exception during stop of discovery service:", e);
            }
        }
    }

    @Override
    public Set<BoardModule> getBoardModules() {
        return Collections.unmodifiableSet(boardModule);
    }

    @Override
    public void registerListener(DeviceDiscoveryEventListener listener) {
        LOG.debug("Added event listener ::= [{}] from ::= [{}]", listener, getClass().getSimpleName());
        listenerSet.add(listener);
    }

    @Override
    public void removeListener(DeviceDiscoveryEventListener listener) {
        LOG.debug("Removed event listener ::= [{}] from ::= [{}]", listener, getClass().getSimpleName());
        listenerSet.remove(listener);
    }

    @Override
    public Future<DeviceDiscoveryEvent> waitForEvent() {
        var callback = new EventCallback();
        callbacks.add(callback);
        return executor.submit(callback);
    }
}
