package mro.fantasy.game.devices.events;

/**
 * Shared message types for all devices. The messages are specified here to have a common place for the documentation regardless of the used device.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-22
 */
public enum DeviceMessageType {

    /**
     * Event send from a board device to the server when a button on the board was pressed. The following overview shows which colum and row is encoded in which byte of the
     * message
     * <p>
     * <pre>{@code
     *  byte - | HEADER   |
     *  data - |  0  - 7  |
     *
     *  byte - | 8                                               |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|1) (2|1) (3|0) (2|0) (1|1) (0|1) (1|0) (0|0) |
     *
     *  byte - | 9                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|3) (2|3) (3|2) (2|2) (1|3) (0|3) (1|2) (0|2) |
     *
     *  byte - | 10                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (7|1) (6|1) (7|0) (6|0) (5|1) (4|1) (5|0) (4|0) |
     *
     *  byte - | 11                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (7|3) (6|3) (7|2) (6|2) (5|3) (4|3) (5|2) (4|2) |
     *
     *  byte - | 12                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|5) (2|5) (3|4) (2|4) (1|5) (0|5) (1|4) (0|4) |
     *
     *  byte - | 13                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|7) (2|7) (3|6) (2|6) (1|7) (0|7) (1|6) (0|6) |
     *
     *  byte - | 14                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (7|5) (6|5) (7|4) (6|4) (5|5) (4|5) (5|4) (4|4) |
     *
     *  byte - | 15                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (7|7) (6|7) (7|6) (6|6) (5|7) (4|7) (5|6) (4|6) |
     * }</pre>
     * <p>
     *
     * @see DeviceMessage
     */
    BOARD_BUTTON_PRESSED(0),

    /**
     * Event send from a board device to the server when a board sensor detects a magnetic field. The following overview shows which colum and row is encoded in which byte of the
     * message
     * <p>
     * <pre>{@code
     *  byte - | HEADER   |
     *  data - |  0  - 7  |
     *
     *  byte - | 8                                               |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|1) (2|1) (3|0) (2|0) (1|1) (0|1) (1|0) (0|0) |
     *
     *  byte - | 9                                               |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (1|3) (0|3) (1|2) (0|2) (3|3) (2|3) (3|2) (2|2) |
     *
     *  byte - | 10                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (7|1) (6|1) (7|0) (6|0) (5|1) (4|1) (5|0) (4|0) |
     *
     *  byte - | 11                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (5|3) (4|3) (5|2) (4|2) (7|3) (6|3) (7|2) (6|2) |
     *
     *  byte - | 12                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|5) (2|5) (3|4) (2|4) (1|5) (0|5) (1|4) (0|4) |
     *
     *  byte - | 13                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (1|7) (0|7) (1|6) (0|6) (3|7) (2|7) (3|6) (2|6) |
     *
     *  byte - | 14                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (7|5) (6|5) (7|4) (6|4) (5|5) (4|5) (5|4) (4|4) |
     *
     *  byte - | 15                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (5|7) (4|7) (5|6) (4|6) (7|7) (6|7) (7|6) (6|6) |
     * }</pre>
     * <p>
     *
     * @see DeviceMessage
     */
    BOARD_BOARD_CHANGED(1),


    /**
     * Event send from a board device to the server when a board sensor detects a magnetic field. The following overview shows which colum and row is encoded in which byte of the
     * message
     * <p>
     * <pre>{@code
     *  byte - | HEADER   |
     *  data - |  0  - 7  |
     *
     *  byte - | 8                                               |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (1|0) (1|0) (1|0) (1|0) (0|0) (0|0) (0|0) (0|0) |
     *  data - | WEST  SOUTH EAST  NORTH WEST  SOUTH EAST  NORTH |
     *
     *  byte - | 9                                               |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (1|1) (1|1) (1|1) (1|1) (0|1) (0|1) (0|1) (0|1) |
     *  data - | WEST  SOUTH EAST  NORTH WEST  SOUTH EAST  NORTH |
     *
     *  byte - | 10                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|0) (3|0) (3|0) (3|0) (2|0) (2|0) (2|0) (2|0) |
     *  data - | WEST  SOUTH EAST  NORTH WEST  SOUTH EAST  NORTH |
     *
     *  byte - | 11                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|1) (3|1) (3|1) (3|1) (2|1) (2|1) (2|1) (2|1) |
     *  data - | WEST  SOUTH EAST  NORTH WEST  SOUTH EAST  NORTH |
     *
     *  byte - | 12                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (1|2) (1|2) (1|2) (1|2) (0|2) (0|2) (0|2) (0|2) |
     *  data - | WEST  SOUTH EAST  NORTH WEST  SOUTH EAST  NORTH |
     *
     *  byte - | 13                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (1|3) (1|3) (1|3) (1|3) (0|3) (0|3) (0|3) (0|3) |
     *  data - | WEST  SOUTH EAST  NORTH WEST  SOUTH EAST  NORTH |
     *
     *  byte - | 14                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|2) (3|2) (3|2) (3|2) (2|2) (2|2) (2|2) (2|2) |
     *  data - | WEST  SOUTH EAST  NORTH WEST  SOUTH EAST  NORTH |
     *
     *  byte - | 15                                              |
     *  bit  - |   7     6     5     4     3     2     1     0   |
     *  data - | (3|3) (3|3) (3|3) (3|3) (2|3) (2|3) (2|3) (2|3) |
     *  data - | WEST  SOUTH EAST  NORTH WEST  SOUTH EAST  NORTH |
     *
     *  ... I am to lazy, really...but it should be clear how t continue :)
     */
    BOARD_EDGE_CHANGED(2);


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
