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

    /**
     * Add an event handler to the service. While Spring services are registered via the @{@link org.springframework.beans.factory.annotation.Autowired} annotation, some handler
     * like the {@link mro.fantasy.game.devices.board.impl.BoardModuleImpl} are created during runtime and must be registered manually.
     *
     * @param eventHandler the event handler to add.
     */
    void addDeviceEventHandler(DeviceEventHandler eventHandler);
}
