package mro.fantasy.game.devices.events;

import mro.fantasy.game.devices.board.BoardModule;

/**
 * Shared event types for all devices. The events are specified here to have a common place for the documentation.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-22
 */
public enum DeviceEventType {

    /**
     * Event send from the server to the client to inform the client about the IP address and the UDP port of the {@link mro.fantasy.game.devices.discovery.DeviceDiscoveryEventListener}
     * of the server. This event is send by the {@link mro.fantasy.game.devices.discovery.DeviceDiscoveryService} when a new {@link BoardModule} was discovered via MDNS.
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
    REGISTER(0);

    /**
     * The unique ID of the event
     */
    private int eventId;

    /**
     * Default constructor.
     *
     * @param eventId the event id
     */
    DeviceEventType(int eventId) {
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
