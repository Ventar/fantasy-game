package mro.fantasy.game.devices.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.events.DeviceMessage;

/**
 * Shared message types for all devices. The messages are specified here to have a common place for the documentation regardless of the used device.
 *
 * @author Michael Rodenbuecher
 * @since 2023-02-24
 */
public enum ServerMessageType {

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
     * @see DeviceMessage
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
     * @see DeviceMessage
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
     * @see DeviceMessage
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
     * @see DeviceMessage
     */
    BOARD_COLOR_CLEAR_AND_UPDATE(3);

    /**
     * The unique ID of the event
     */
    private int eventId;

    /**
     * Default constructor.
     *
     * @param eventId the event id
     */
    ServerMessageType(int eventId) {
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
    public static ServerMessageType fromID(int id) {
        for (ServerMessageType t : ServerMessageType.values()) {
            if (t.getEventId() == id) return t;
        }

        throw new IllegalArgumentException("An device event type with ID ::= [" + id + "] does not exist.");
    }

}
