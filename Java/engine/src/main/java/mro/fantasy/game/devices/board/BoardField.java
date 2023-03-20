package mro.fantasy.game.devices.board;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.impl.Color;
import mro.fantasy.game.devices.impl.ColorEffect;

/**
 * Represents a single field on a physical (or logical) board module. In addition, this class maintains the information about the row and the column of the field in context of the
 * complete board which consists of multiple modules.
 *
 * @author Michael Rodenbuecher
 * @see BoardModule
 * @since 2021-12-16
 */
public interface BoardField {

    /**
     * Identifier for a sensor on the board module.
     *
     * @author Michael Rodenbuecher
     * @since 2023-03-10
     */
    enum SensorType {
        Board,
        North,
        East,
        South,
        West,
        Button
    }

    /**
     * Returns the unique ID of the electronic {@link BoardModule} to which the field belongs
     *
     * @return the device ID
     */
    String getDeviceID();

    /**
     * Returns the position of the field on the logical module
     *
     * @return the position
     */
    Position getPosition();

    /**
     * Returns the color of the field.
     *
     * @return the color
     */
    Color getColor();

    /**
     * Returns the color effect of the field
     *
     * @return the effect
     */
    ColorEffect getEffect();


    /**
     * Returns if the selected sensor is currently active or not.
     *
     * @return <code>true</code> if the sensor is active, <code>false</code> if not
     */
    boolean isSensorEnabled(SensorType type);

    /**
     * Returns if any edge sensor is enabled. An edge sensor is of type north, east, sout or west.
     *
     * @return <code>true</code> if any edge sensor is enabled, <code>false</code> otherwise
     */
    boolean isAnyEdgeEnabled();

}

