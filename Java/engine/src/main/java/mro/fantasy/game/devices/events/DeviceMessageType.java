package mro.fantasy.game.devices.events;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.impl.Color;

/**
 * Shared message types for all devices. The messages are specified here to have a common place for the documentation regardless of the used device.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-22
 */
public enum DeviceMessageType {

    /**
     * Event send from the server to the client to inform the client about the IP address and the UDP port of the  server.
     * <p>
     * <pre>{@code
     *  part -  | HEADER   |  DATA                  |
     *  byte -  |  0  - 8  | 9 - 12     | 13 - 14   |
     *  data -  |          | ip address | UDP port  |
     * }</pre>
     * <p>
     * Direction: SERVER -> CLIENT
     *
     * @see mro.fantasy.game.devices.events.DeviceDataPackage
     */
    REGISTER(0),

    /**
     * Event send from the server to a physical {@link BoardModule} when the {@link BoardModule#sendColorUpdate(boolean)} method is called.
     * <p>
     * <pre>{@code
     *  part -  | HEADER   |  DATA                                      |
     *  byte -  |  0  - 8  | 9      | 10     | 11  | 12  | 13    | 14   |
     *  data -  |          | count  | column | row | red | green | blue |
     *                              | repeated count times              |
     * }</pre>
     * <p>
     * Direction: SERVER -> CLIENT
     *
     * @see mro.fantasy.game.devices.events.DeviceDataPackage
     */
    BOARD_COLOR_UPDATE(1),

    /**
     * Event send from the server to a physical {@link BoardModule} to clear all colors.
     * <p>
     * <pre>{@code
     *  part -  | HEADER   |
     *  byte -  |  0  - 8  |
     *  data -  |          |
     * }</pre>
     * <p>
     * Direction: SERVER -> CLIENT
     *
     * @see mro.fantasy.game.devices.events.DeviceDataPackage
     */
    BOARD_COLOR_CLEAR(2),

    /**
     * Event send from the server to a physical {@link BoardModule} when the {@link BoardModule#sendColorUpdate(boolean)} method is called, clears all colors on the board and
     * replace them with the ones which were set with the {@link BoardModule#setColor(Position, Color)} method before.
     * <p>
     * <pre>{@code
     *  part -  | HEADER   |  DATA                                      |
     *  byte -  |  0  - 8  | 9      | 10     | 11  | 12  | 13    | 14   |
     *  data -  |          | count  | column | row | red | green | blue |
     *                              | repeated count times              |
     * }</pre>
     * <p>
     * Direction: SERVER -> CLIENT
     *
     * @see mro.fantasy.game.devices.events.DeviceDataPackage
     */
    BOARD_COLOR_CLEAR_AND_UPDATE(3),


    /**
     * Event send from the server to a physical {@link BoardModule} when the {@link BoardModule#sendColorUpdate(boolean)} method is called, clears all colors on the board and
     * replace them with the ones which were set with the {@link BoardModule#setColor(Position, Color)} method before.
     * <p>
     * <pre>{@code
     *  byte - | HEADER   | 9       | 10     | 11     | 12           |
     *  data - |  0  - 8  | records | column | row    | sensor state |
     *                              | repeated <records> time        |
     *  with sensor state
     *  bit  -  | 7 6 5 4   3     2     1     0      |
     *  data -  | <empty>   west  south east  north  |
     *
     * }</pre>
     * <p>
     * Direction: CLIENT -> SERVER
     *
     * @see mro.fantasy.game.devices.events.DeviceDataPackage
     */
    BOARD_SENSOR_UPDATE(4);


    /**
     * The unique ID of the event
     */
    private int eventId;

    /**
     * Default constructor.
     *
     * @param eventId the event id
     */
    DeviceMessageType(int eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns the unique event ID.
     *
     * @return the unique ID.
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Resolves the passed event ID to a device type event.
     *
     * @param id the ID to resolve
     *
     * @return the corresponding type
     *
     * @throws IllegalArgumentException in case the ID cannot be resolved
     */
    public static DeviceMessageType fromID(int id) {
        for (DeviceMessageType t : DeviceMessageType.values()) {
            if (t.getEventId() == id) return t;
        }

        throw new IllegalArgumentException("An device event type with ID ::= [" + id + "] does not exist.");
    }

}
