package mro.fantasy.game.devices.discovery.impl;

import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.board.impl.BoardModuleImpl;
import mro.fantasy.game.devices.discovery.DeviceDiscoveryService;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import mro.fantasy.game.engine.events.impl.EventThreadPool;
import mro.fantasy.game.utils.NetworkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
     * UDP port that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.Milliseconds the device discovery service will wait to discover physical board
     * modules
     */
    @Value("${game.device.board.scan.time.ms}")
    private int scanTimeout;

    /**
     * Number of boards which is expected by the discovery service. The service will wait for these boards before ending the scan process.
     */
    @Value("${game.device.board.count}")
    private int numberOfBoards;

    /**
     * Threadpool to manage the discovery future.
     */
    @Autowired
    protected EventThreadPool executor;

    /**
     * Network utilities to get IP and MAC address
     */
    @Autowired
    private NetworkConfiguration networkConfiguration;

    /**
     * Socket to send out data to the devices via UDP
     */
    protected DatagramSocket socket;

    /**
     * A set of board modules that was discoverd by the {@link #jmdns} service.
     */
    private Set<BoardModule> boardModules = new HashSet<>();

    /**
     * The implementation utility class for the mDNS service.
     */
    private JmDNS jmdns;

    @Override
    public void scan() throws IOException {

        LOG.info("");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("DEVICE DISCOVERY");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("");

        LOG.info("Start discovery service on network adapter ::= [{}]", networkConfiguration.getAdapterIPAddress());


        // The future must be defined here to ensure that the MDNS discovery will work.
        Future discoveryFuture = executor.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        LOG.warn("Device discovery Future was interrupted: ", e);
                    }
                    if (boardModules.size() == numberOfBoards) {
                        return;
                    }
                }
            }
        });

        this.socket = new DatagramSocket();

        jmdns = JmDNS.create(networkConfiguration.getAdapterINetAddress());

        registerMDNSListener(jmdns, BOARD_MDNS_TYPE, (serviceEvent) -> {

            try {
                BoardModuleImpl boardModule = new BoardModuleImpl(serviceEvent.getName(), serviceEvent.getInfo().getInetAddresses()[0], serviceEvent.getInfo().getPort());

                if (boardModules.contains(boardModule)) {
                    LOG.debug("[{}] - module with the given ID was already registered, skip registration...", boardModule.getId());
                    return;
                }

                boardModule.sendRegister("SERVER", networkConfiguration.getAdapterIPAddress(), networkConfiguration.getEventUDPPort());
                boardModules.add(boardModule);
                LOG.info("[{}] - found board module ::= [{}]", boardModule.getId(), boardModule);
            } catch (IOException e) {
                LOG.warn("Cannot register board module with id ::= [{}]:", serviceEvent.getName(), e);
            }


        });

        registerMDNSListener(jmdns, PLAYER_MDNS_TYPE, (serviceEvent) -> {
            // var ctrl = new PlayerController(serviceEvent.getInfo().getName(), serviceEvent.getInfo().getInet4Addresses()[0], serviceEvent.getInfo().getPort());
            // ctrl.sendRegisterListenerMessage(StaticConfig.LOCAL_IP_ADDRESS, StaticConfig.UDP_PORT_CONTROLLER_EVENT);
            // ctrl.sendLEDState(Color.OFF);
            // playerController.add(ctrl);
        });

        try {
            discoveryFuture.get(scanTimeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOG.warn("\n\n !!! Device Discovery was not successful, found ::= [{} / {}] board modules !!! \n\n", boardModules.size(), numberOfBoards);
        }

        stop();

        LOG.debug("Device discovery initialization successful finished...");

    }

    @Override
    public Optional<BoardModule> getBoardModuleById(String id) {
        if (boardModules.isEmpty()) {
            throw new IllegalStateException("No board modules are known by the service.");
        }

        return boardModules.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public void stop() {
        if (jmdns != null) {
            try {
                jmdns.close();
                jmdns = null;
                LOG.info("Stopped discovery service on network adapter ::= [{}]", networkConfiguration.getAdapterIPAddress());
            } catch (Exception e) {
                LOG.debug("Exception during stop of discovery service:", e);
            }
        }
    }

    @Override
    public List<BoardModule> getBoardModules() {
        return List.copyOf(boardModules);
    }

}
