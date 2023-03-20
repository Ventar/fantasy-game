package mro.fantasy.applications.simulator.player.controller;

import mro.fantasy.game.devices.events.DeviceEventService;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import mro.fantasy.game.engine.events.impl.EventThreadPool;
import mro.fantasy.game.utils.NetworkConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Configuration class for player controller mock.
 *
 * @author Michael Rodenbuecher
 * @since 2023-03-16
 */
@org.springframework.context.annotation.Configuration
public class Configuration {

    /**
     * Threadpool used by the game event service.
     *
     * @return the tread pool
     */
    @Bean
    EventThreadPool createEventThreadPool() {
        return new EventThreadPool();
    }

    /**
     * The event service to handle incoming events from the server.
     *
     * @return
     */
    @Bean
    DeviceEventService createEventService() {
        return new UDPDeviceEventServiceImpl();
    }

    /**
     * The network configuration bean.
     *
     * @return
     */
    @Bean
    NetworkConfiguration createNetworkConfiguration() {
        return new NetworkConfiguration();
    }

}
