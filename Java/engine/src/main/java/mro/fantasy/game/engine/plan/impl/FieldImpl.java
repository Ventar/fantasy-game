package mro.fantasy.game.engine.plan.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.utils.ValidationUtils;
import mro.fantasy.game.utils.YAMLUtilities;
import mro.fantasy.game.engine.plan.Field;
import mro.fantasy.game.engine.plan.FieldContainer;
import mro.fantasy.game.engine.plan.FieldType;

import java.util.Map;

/**
 * Implementation of a {@link Field}.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-31
 */
public class FieldImpl implements Field {

    /**
     * The type of the field.
     *
     * @see #getType()
     */
    private FieldType type;

    /**
     * If the line of sight is blocked.
     *
     * @see #blocksLineOfSight()
     */
    private boolean los;

    /**
     * If movement is blocked.
     *
     * @see #blocksMovement()
     */
    private boolean move;

    /**
     * If a character can enter.
     *
     * @see #canEnter()
     */
    private boolean enter;

    /**
     * The position of the field in the context of its parent container.
     *
     * @see #getPosition()
     */
    private Position position;

    /**
     * If this field is an anchor field.
     */
    private boolean anchor;

    @Override
    public FieldType getType() {
        return type;
    }

    @Override
    public boolean blocksLineOfSight() {
        return los;
    }

    @Override
    public boolean blocksMovement() {
        return move;
    }

    @Override
    public boolean canEnter() {
        return enter;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public boolean isAnchor() {
        return anchor;
    }

    @Override
    public Field copy() {
        FieldImpl newField = new FieldImpl();
        newField.type = this.type;
        newField.enter = this.enter;
        newField.position = new Position(this.position.column(), this.position.row());
        newField.move = this.move;
        newField.anchor = this.anchor;
        newField.los = this.los;
        return newField;
    }

    /**
     * Creates a copy of this field but with a different position
     *
     * @param column the new column
     * @param row    the new row
     *
     * @return the new field
     */
    protected FieldImpl shift(int column, int row) {
        FieldImpl newField = (FieldImpl) copy();
        newField.position = new Position(column, row);
        return newField;
    }

    @Override
    public String toString() {
        return  position.toString();
    }

    /**
     * Creates a new instance from the passed YAML map in the format:
     * <pre>{@code
     *  - column: 1
     *    row: 1
     *    anchor: true
     *    los: false
     *    move: false
     *    enter: true
     * }</pre>
     *
     * @param data the YAML map
     * @param type the type of the field, which depends on the {@link FieldContainer} type if it has one.
     *
     * @return the new field
     *
     * @throws IllegalArgumentException if data is missing or invalid
     */
    public static FieldImpl fromYAMLMap(Map<String, Object> data, FieldType type) {

        ValidationUtils.requireNonNull(data, "The data map cannot be null.");
        ValidationUtils.requireNonNull(type, "The field type cannot be null.");

        FieldImpl field = new FieldImpl();

        field.type = type;
        field.los = YAMLUtilities.getMandatory(data, "los");
        field.move = YAMLUtilities.getMandatory(data, "move");
        field.enter = YAMLUtilities.getMandatory(data, "enter");
        field.position = new Position(YAMLUtilities.getMandatory(data, "column"), YAMLUtilities.getMandatory(data, "row"));
        field.anchor = field.position.equals(new Position(0, 0));

        if (field.move && field.enter) {
            throw new IllegalArgumentException("Cannot have attribute blocks movement set to true and attribute canEnter set to true at the same time");
        }

        return field;

    }
}
