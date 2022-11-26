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
     * Returns the position of the field on the logical module
     *
     * @return the position
     */
    Position getPosition();

    /**
     * Sets the color of this field
     *
     * @param color the color
     */
    void setColor(Color color);

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
     * Sets the color effect
     *
     * @param effect the effect
     */
    void setEffect(ColorEffect effect);

    /**
     * Set the enabled state of the northern sensor
     *
     * @param northEnabled {@code true} if the sensor is enabled, {@code false} otherwise.
     */
    void setNorthEnabled(boolean northEnabled);

    /**
     * Set the enabled state of the eastern sensor
     *
     * @param eastEnabled {@code true} if the sensor is enabled, {@code false} otherwise.
     */
    void setEastEnabled(boolean eastEnabled);

    /**
     * Set the enabled state of the southern sensor
     *
     * @param southEnabled {@code true} if the sensor is enabled, {@code false} otherwise.
     */
    void setSouthEnabled(boolean southEnabled);

    /**
     * Set the enabled state of the western sensor
     *
     * @param westEnabled {@code true} if the sensor is enabled, {@code false} otherwise.
     */
    void setWestEnabled(boolean westEnabled);

    /**
     * Parses the passed byte and set the sensor state according to the values:
     * <pre>{@code
     *    bit  -  | 7 6 5 4   3     2     1     0      |
     *    data -  | <empty>   west  south east  north  |
     * }</pre>
     *
     * @param state the sensor state
     */
    void setSensorState(byte state);

    /**
     * Returns if the northern sensor is active
     *
     * @return <code>true</code> if the sensor is active, <code>false</code> if not
     */
    boolean isNorthEnabled();

    /**
     * Returns if the eastern sensor is active
     *
     * @return <code>true</code> if the sensor is active, <code>false</code> if not
     */
    boolean isEastEnabled();

    /**
     * Returns if the southern sensor is active
     *
     * @return <code>true</code> if the sensor is active, <code>false</code> if not
     */
    boolean isSouthEnabled();

    /**
     * Returns if the western sensor is active
     *
     * @return <code>true</code> if the sensor is active, <code>false</code> if not
     */
    boolean isWestEnabled();

    /**
     * Returns if any sensor is enabled
     *
     * @return <code>true</code> if any sensor is enabled, <code>false</code> otherwise
     */
    boolean isAnyEnabled();

}

