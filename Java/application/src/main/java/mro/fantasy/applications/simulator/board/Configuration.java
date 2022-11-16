package mro.fantasy.applications.simulator.board;

import mro.fantasy.game.devices.events.DeviceEventService;
import mro.fantasy.game.devices.events.impl.EventThreadPool;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import org.springframework.context.annotation.Bean;

import java.awt.*;

/**
 * Utility class with colors and sized to use during rendering.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-21
 */
@org.springframework.context.annotation.Configuration
public class Configuration {

    /**
     * Color of the frame background.
     */
    public static final Color FRAME_BACKGROUND = new Color(92, 92, 92);

    /**
     * Color of the field background.
     */
    public static final Color FIELD_BACKGROUND = new Color(64, 64, 64);

    /**
     * Color of the HAL sensor hover.
     */
    public static final Color POLYGON_HOVER = new Color(255, 128, 128);

    /**
     * Color of the selected HAL sensor.
     */
    public static final Color POLYGON_SELECTED = new Color(128, 128, 128);

    /**
     * The height and width a field on the board has
     */
    public static final int FIELD_SIZE_PX = 100;


    /**
     * The number of rows of the game board.
     */
    public static final int ROWS = 8;

    /**
     * The number of columns of the game board.
     */
    public static final int COLUMNS = 8;


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

}
