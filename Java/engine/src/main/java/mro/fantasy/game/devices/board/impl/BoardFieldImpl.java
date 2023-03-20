package mro.fantasy.game.devices.board.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.BoardField;
import mro.fantasy.game.devices.events.DeviceMessage;
import mro.fantasy.game.devices.impl.Color;
import mro.fantasy.game.devices.impl.ColorEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a single field on a physical (or logical) board module. In addition, this class maintains the information about the row and the column of the field in context of the
 * complete board which consists of multiple modules.
 *
 * @author Michael Rodenbuecher
 * @since 2021-12-16
 */
public class BoardFieldImpl implements BoardField {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(BoardFieldImpl.class);

    /**
     * The unique ID of the device to which the board field belongs.
     */
    private final String deviceId;

    /**
     * The position of this field in the coordinate system of the {@link mro.fantasy.game.devices.board.BoardModule}
     */
    private final Position modulePosition;

    /**
     * The current color of the field.
     */
    private Color color = Color.Black;

    /**
     * The effect of this field.
     */
    private ColorEffect effect = ColorEffect.FIXED_COLOR;

    /**
     * The current state of the sensors.
     */
    private HashMap<SensorType, Boolean> sensorState = new HashMap<>();


    /**
     * Creates a new field.
     *
     * @param modulePosition the position of this field in the coordinate system of the {@link mro.fantasy.game.devices.board.BoardModule}
     */
    BoardFieldImpl(String deviceId, Position modulePosition) {
        this.deviceId = deviceId;
        this.modulePosition = modulePosition;

        Arrays.stream(SensorType.values()).forEach(type -> sensorState.put(type, false)); // initialize the state map
    }

    @Override
    public String getDeviceID() {
        return deviceId;
    }

    /**
     * Sets the color of this field
     *
     * @param color the color
     */
    public void setColor(Color color) {
        if (color == null) {
            this.color = Color.Black;
        }
        this.color = color;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public ColorEffect getEffect() {
        return effect;
    }

    /**
     * Sets the color effect
     *
     * @param effect the effect
     */
    public void setEffect(ColorEffect effect) {
        if (effect == null) {
            this.effect = ColorEffect.FIXED_COLOR;
        }

        this.effect = effect;
    }

    /**
     * Returns the unique ID of the device to which the board field belongs.
     *
     * @return the ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Set the enabled state of the sensor.
     * <p>
     * If the sensor of type button is currently in state pressed it will be handled in a special way. While the other sensors of a field have a more or less permanent or longer
     * running state, the button state is usually only set when a player pressed a button. When that happened an event is triggered in the physical module and the game engine will
     * set the state for that field to pressed within the {@link BoardModuleImpl#handle(DeviceMessage)} method. Afterwards the {@link mro.fantasy.game.devices.board.GameBoard} will
     * perform a translation to a {@link mro.fantasy.game.engine.events.GameEvent} and clears the state of the field, i.e. in contrast to the other sensors the change will not
     * happen upon  a new event from the hardware.
     *
     * @param enabled {@code true} if the sensor is enabled, {@code false} otherwise.
     */
    public void setSensorEnabled(SensorType type, boolean enabled) {

        if (enabled == true && sensorState.get(type) == false) {
            LOG.trace("Mark [{}] sensor of field ::= {} as ENABLED", type, modulePosition);
        } else if (enabled == false && sensorState.get(type) == true) {
            LOG.trace("Mark [{}] sensor of field ::= {} as DISABLED", type, modulePosition);
        }

        this.sensorState.put(type, enabled);
    }

    @Override
    public boolean isSensorEnabled(SensorType type) {
        return sensorState.get(type);
    }

    @Override
    public boolean isAnyEdgeEnabled() {
        return sensorState.get(SensorType.North) || sensorState.get(SensorType.East) || sensorState.get(SensorType.South) || sensorState.get(SensorType.West);
    }

    @Override
    public Position getPosition() {
        return modulePosition;
    }


    @Override
    public String toString() {
        return "BoardFieldImpl{" +
                       "modulePosition=" + modulePosition +
                       ", color=" + color +
                       ", effect=" + effect +
                       ", northEnabled=" + sensorState.get(SensorType.North) +
                       ", eastEnabled=" + sensorState.get(SensorType.East) +
                       ", southEnabled=" + sensorState.get(SensorType.South) +
                       ", westEnabled=" + sensorState.get(SensorType.West) +
                       ", boardEnabled=" + sensorState.get(SensorType.Board) +
                       ", buttonEnabled=" + sensorState.get(SensorType.Button) +
                       '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardFieldImpl that = (BoardFieldImpl) o;
        return Objects.equals(modulePosition, that.modulePosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modulePosition);
    }
}

