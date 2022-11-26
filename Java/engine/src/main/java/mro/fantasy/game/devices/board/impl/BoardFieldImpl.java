package mro.fantasy.game.devices.board.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.BoardField;
import mro.fantasy.game.devices.impl.Color;
import mro.fantasy.game.devices.impl.ColorEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * The position of this field in the coordinate system of the {@link mro.fantasy.game.devices.board.BoardModule}
     */
    private final Position modulePosition;

    /**
     * The current color of the field.
     */
    private Color color = Color.OFF;

    /**
     * The effect of this field.
     */
    private ColorEffect effect = ColorEffect.FIXED_COLOR;

    /**
     * If the northern HAL sensor is enabled
     */
    private boolean northEnabled;

    /**
     * If the eastern HAL sensor is enabled
     */
    private boolean eastEnabled;

    /**
     * If the southern HAL sensor is enabled
     */
    private boolean southEnabled;

    /**
     * If the western HAL sensor is enabled
     */
    private boolean westEnabled;


    /**
     * Creates a new field.
     *
     * @param modulePosition the position of this field in the coordinate system of the {@link mro.fantasy.game.devices.board.BoardModule}
     */
    BoardFieldImpl(Position modulePosition) {
        this.modulePosition = modulePosition;
    }

    @Override
    public void setColor(Color color) {
        if (color == null) {
            this.color = Color.OFF;
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

    @Override
    public void setEffect(ColorEffect effect) {
        if (effect == null) {
            this.effect = ColorEffect.FIXED_COLOR;
        }

        this.effect = effect;
    }

    @Override
    public void setNorthEnabled(boolean northEnabled) {
        this.northEnabled = northEnabled;
    }

    @Override
    public void setEastEnabled(boolean eastEnabled) {
        this.eastEnabled = eastEnabled;
    }

    @Override
    public void setSouthEnabled(boolean southEnabled) {
        this.southEnabled = southEnabled;
    }

    @Override
    public void setWestEnabled(boolean westEnabled) {
        this.westEnabled = westEnabled;
    }

    @Override
    public boolean isNorthEnabled() {
        return northEnabled;
    }

    @Override
    public boolean isEastEnabled() {
        return eastEnabled;
    }

    @Override
    public boolean isSouthEnabled() {
        return southEnabled;
    }

    @Override
    public boolean isWestEnabled() {
        return westEnabled;
    }

    @Override
    public boolean isAnyEnabled() {
        return isNorthEnabled() || isEastEnabled() || isWestEnabled() || isSouthEnabled();
    }

    @Override
    public void setSensorState(byte state) {
        northEnabled = ((state >> 0) & 1) == 1;
        eastEnabled = ((state >> 2) & 1) == 1;
        southEnabled = ((state >> 3) & 1) == 1;
        westEnabled = ((state >> 1) & 1) == 1;
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
                       ", northEnabled=" + northEnabled +
                       ", eastEnabled=" + eastEnabled +
                       ", southEnabled=" + southEnabled +
                       ", westEnabled=" + westEnabled +
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

