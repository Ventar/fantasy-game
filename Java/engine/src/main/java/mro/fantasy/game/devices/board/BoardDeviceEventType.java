package mro.fantasy.game.devices.board;

/**
 * Event types for {@link BoardModule}. The events are specified here to have a common place for the documentation.
 * <p>
 * Shared events like the register one are defined in the common {@link mro.fantasy.game.devices.events.DeviceEventType} enum.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-21
 */
public enum BoardDeviceEventType {

    ;

    /**
     * The unique ID of the event
     */
    private int eventId;

    /**
     * Default constructor.
     *
     * @param eventId the event id
     */
    BoardDeviceEventType(int eventId) {
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
}
