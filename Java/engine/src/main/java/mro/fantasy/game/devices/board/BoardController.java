package mro.fantasy.game.devices.board;

import mro.fantasy.game.devices.events.DeviceDataPackage;
import mro.fantasy.game.devices.events.DeviceEventHandler;
import mro.fantasy.game.devices.events.GameEventProducer;

/**
 * The physical game board.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-12
 */
public interface BoardController extends DeviceEventHandler, GameEventProducer<BoardControllerEventListener.BoardEvent, BoardControllerEventListener> {

    /**
     * Parses the datagram package for update information on board field HAL sensors.
     * <p>
     * The {@link DeviceDataPackage#getData()} content has to match the following structure:
     * <pre>{@code
     *  byte -  | 9       | 10     | 11             |
     *  bit  -  | 7..0    | 7..0   | 7..0           |
     *  data -  | records | column | row            |
     *          |         | repeated <records> time |
     *
     *  byte -  | 12                                                                     |
     *  bit  -  | 7 6 5 4   3              2               1              0              |
     *  data -  | <empty>   west enabled   south enabled   east enabled   north enabled  |
     *          | repeated <records> time                                                |
     * }</pre>
     * <p>
     * where the records' field indicate how many fields have changed the value and the following structure of column, row and sensor result indicate the current state of a field
     *
     * @param eventData the device event data
     *
     * @return the event
     */
    BoardControllerEventListener.BoardEvent createEvent(DeviceDataPackage eventData);

}
