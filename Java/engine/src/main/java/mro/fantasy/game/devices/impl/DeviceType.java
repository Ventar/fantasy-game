package mro.fantasy.game.devices.impl;

import mro.fantasy.game.devices.events.DeviceMessage;

/**
 * The type of device.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-13
 */
public enum DeviceType {
    BOARD_MODULE(1),
    PLAYER_CONTROLLER(2);

    /**
     * Unique id to identify an device in the {@link DeviceMessage}.
     */
    private int typeId;

    DeviceType(int typeId) {
        this.typeId = typeId;
    }

    /**
     * Returns the unique id to identify an device in the {@link DeviceMessage}.
     *
     * @return the id
     */
    public int getTypeId() {
        return typeId;
    }

    /**
     * Returns the device type that has a {@link #getTypeId()} that is equals to the passed value
     *
     * @param i the type ID to resolve
     *
     * @return the device type
     *
     * @throws IllegalArgumentException if the passed integer cannot be resolved
     */
    public static DeviceType fromInteger(int i) {

        for (DeviceType t : DeviceType.values()) {
            if (t.typeId == i) {
                return t;
            }
        }

        throw new IllegalArgumentException("Cannot resolve ::= [" + i + "] to a type ID");

    }

}
