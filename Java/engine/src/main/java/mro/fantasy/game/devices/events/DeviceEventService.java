package mro.fantasy.game.devices.events;

/**
 * Service responsible for the handling of incoming events from the hardware devices of the game.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-14
 */
public interface DeviceEventService {

    /**
     * Initializes the event service by creating the datagram socket and starting the service as a background thread.
     */
    void start();
}
