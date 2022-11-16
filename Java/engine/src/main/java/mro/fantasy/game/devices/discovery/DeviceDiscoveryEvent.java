package mro.fantasy.game.devices.discovery;

import mro.fantasy.game.devices.DeviceType;
import mro.fantasy.game.devices.events.GameEvent;

public class DeviceDiscoveryEvent implements GameEvent {

    public enum EventType {
        DEVICE_DISCOVERED,
        DEVICE_CONNECTION_LOST
    }

    /**
     * The type of device that is discovered or updated.
     */
    private DeviceType deviceType;


}
