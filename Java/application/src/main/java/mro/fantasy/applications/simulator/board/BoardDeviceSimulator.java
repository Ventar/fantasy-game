package mro.fantasy.applications.simulator.board;

import mro.fantasy.game.devices.discovery.DeviceDiscoveryService;
import mro.fantasy.game.devices.events.DeviceEventService;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import mro.fantasy.game.utils.NetworkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Utility class to simulate a device.
 *
 * @author Michael Rodenbuecher
 * @see UDPDeviceEventServiceImpl
 * @since 2022-08-13
 */

@Configuration
@ComponentScan
public class BoardDeviceSimulator implements CommandLineRunner {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BoardDeviceSimulator.class);


    /**
     * The device ID. For real devices this is the MAC address.
     */
    @Value("${game.device.id}")
    private String deviceId;

    /**
     * Service to receive UDP messages from the server.
     */
    @Autowired
    private DeviceEventService deviceEventService;

    /**
     * Network utilities to get IP and MAC address
     */
    @Autowired
    private NetworkConfiguration networkConfiguration;

    @Override
    public void run(String... args) throws Exception {
        JmDNS jmdns = JmDNS.create(networkConfiguration.getAdapterINetAddress());
        ServiceInfo serviceInfo = ServiceInfo.create(DeviceDiscoveryService.BOARD_MDNS_TYPE, deviceId, networkConfiguration.getEventUDPPort(), "n/a");
        jmdns.registerService(serviceInfo);
        deviceEventService.start();

        LOG.info("");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("APPLICATION SUCCESSFULLY INITIALIZED");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("");

    }

    /**
     * Runs the applicaton. As part of the argument list the UDP Port and the unique devive ID should be specified.
     * <p>
     * --game.event.udp.port=4001 --game.device.id=b8eb077f9f7a
     *
     * @param args the passed arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BoardDeviceSimulator.class, args);
    }


}
