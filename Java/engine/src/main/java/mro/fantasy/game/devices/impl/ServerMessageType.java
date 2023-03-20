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
     *  part -  | EVENT ID   |  DATA                  |
     *  byte -  |  0         | 1 - 4      | 5 - 6     |
     *  data -  |            | ip address | UDP port  |
     * }</pre>
     * @see DeviceMessage
     */
    REGISTER(0),

    /**
     * Event send from the server to a physical {@link BoardModule} to clear all colors.
     * <p>
     * <pre>{@code
     *  part -  | EVENT ID   |
     *  byte -  |  0         |
     *  data -  |  1         |
     * }</pre>
     *
     * @see DeviceMessage
     */
    BOARD_COLOR_CLEAR(1),

    /**
     * Event send from the server to a physical {@link BoardModule} when the {@link BoardModule#sendColorUpdate(boolean)} method is called.
     * <p>
     * <pre>{@code
     *  part -  | EVENT ID |  DATA                          |
     *  byte -  |  0       | 1      | 2      | 3            |
     *  data -  |  2       | count  | led ID | led color    |
     *                              | repeated count times  |
     * }</pre>
     *
     * @see DeviceMessage
     */
    BOARD_COLOR_UPDATE(2),

    /**
     * Enables or disables the sensors of the board. If the responsible bit is set to 1 the sensors are enabled, otherwise they are disabled.
     * <p>
     * <pre>{@code
     *  part -  | EVENT ID |  DATA                                                                      |
     *  byte -  |  0       | 8 .. 7 .. 6 .. 5 .. 4 .. 3 .. 2 ............ 1 .............. 0 .......... |
     *  data -  |  3       |                               board enable   button enabled   edge enabled |
     *
     * }</pre>
    *
     * @see DeviceMessage
     */
    BOARD_ENABLE_SENSOR(3),

    /**
     * Enables or disables the sensors of the board. If the responsible bit is set to 1 the sensors are enabled, otherwise they are disabled.
     * <p>
     * <pre>{@code
     *  part -  | EVENT ID |  DATA         |
     *  byte -  |  0       | 1             |
     *  data -  |  4        | brightness    |
     *
     * }</pre>
     *
     * @see DeviceMessage
     */
    BOARD_SET_BRIGHTNESS(4);

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
