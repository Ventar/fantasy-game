package mro.fantasy.game.engine.plan;

import mro.fantasy.game.Position;

/**
 * A field that is part of either a {@link Plan} or a {@link TileTemplate}.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-29
 */
public interface Field {

    /**
     * Returns the type of this field.
     *
     * @return the type of the field
     */
    FieldType getType();

    /**
     * Checks if the given field blocks the line of sight.
     *
     * @return <code>true</code> if the line is blocked, <code>false</code> otherwise.
     */
    boolean blocksLineOfSight();

    /**
     * Checks if the field blocks the movement of a character.
     *
     * @return <code>true</code> if the field blocks movement,<code>false</code> otherwise.
     */
    boolean blocksMovement();

    /**
     * Checks if the field can be entered by a character, i.e. if it can end its movement on the fields of this tile.
     *
     * @return <code>true</code> if the field can be entered. <code>false</code> otherwise.
     */
    boolean canEnter();

    /**
     * If this field is an anchor field in the coordinate system of a {@link TileTemplate}.
     *
     * @return {@code  true} if it is an anchor, {@code false} otherwise.
     *
     * @see TileRotation for more information see TileOrientation
     */
    boolean isAnchor();

    /**
     * Creates a deep copy of this field.
     *
     * @return the deep copy.
     */
    Field copy();

    /**
     * Returns the position of this field either in the coordinate system of a {@link Plan} or a {@link TileTemplate}. If the field is returned by an instance of plan it is in the
     * context of the plan coordinate system, otherwise, if it is returned by a tile template it is in the coordinate system of the plan. Assume that we have the following plan
     * with a tile template assigned to it
     * <pre>{@code
     *   Plan                                         Template
     *   ┌─────────┬─────────┬─────────┬─────────┐   ┌─────────┬─────────┐
     *   │         │         │         │         │   │ ███████ │ ███████ │
     *   │   0/4   │   1/4   │   2/4   │   3/4   │   │   1/0   │   1/1   │
     *   │         │         │         │         │   │ ███████ │ ███████ │
     *   ├─────────┼─────────┼─────────┼─────────┤   ├─────────┼─────────┤
     *   │         │         │         │         │   │ ███████ │ ███████ │
     *   │   0/3   │   1/3   │   2/3   │   3/3   │   │   0/0   │   1/0   │
     *   │         │         │         │         │   │ 0° ████ │ ███████ │
     *   ├─────────┼─────────┼─────────┼─────────┤   └─────────┴─────────┘
     *   │         │         │         │         │
     *   │   0/2   │   1/2   │   2/2   │   3/2   │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │   0/1   │   1/1   │   2/1   │   3/3   │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │   0/0   │   1/0   │   2/0   │   3/0   │
     *   │         │         │         │         │
     *   └─────────┴─────────┴─────────┴─────────┘
     *   Assigned plan (x means nothing assigned)
     *   ┌─────────┬─────────┬─────────┬─────────┐
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │ xxxxxxx │ ███████ │ ███████ │ xxxxxxx │
     *   │ xxxxxxx │ SCENIC  │ SCENIC  │ xxxxxxx │
     *   │ xxxxxxx │ ███████ │ ███████ │ xxxxxxx │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │ xxxxxxx │ ███████ │ ███████ │ xxxxxxx │
     *   │ xxxxxxx │ SCENIC  │ SCENIC  │ xxxxxxx │
     *   │ xxxxxxx │ 0° ████ │ ███████ │ xxxxxxx │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   └─────────┴─────────┴─────────┴─────────┘
     * }</pre>
     * and the following pseudo code assigns the tile template to the plan:
     * <pre>{@code
     *   var template = new TileTemplate() {};
     *   var plan = new Plan() {};
     *   plan.assign(template, new Position(1,1), TileRotation.DEGREE_0);
     * }</pre>
     * A call to the {@link TileTemplate#getFields()} method would return
     * <ul>
     *     <li>(0|0) - block movement, blocks line of sight, anchor</li>
     *     <li>(1|0) - block movement, blocks line of sight</li>
     *     <li>(0|1) - block movement, blocks line of sight</li>
     *     <li>(1|1) - block movement, blocks line of sight</li>
     * </ul>
     * and a call to the {@link Plan#getFields()} would return
     * <ul>
     *     <li>(1|1) - block movement, blocks line of sight, anchor</li>
     *     <li>(1|2) - block movement, blocks line of sight</li>
     *     <li>(2|1) - block movement, blocks line of sight</li>
     *     <li>(2|2) - block movement, blocks line of sight</li>
     * </ul>
     * (Fields without assigned tiles are not returned by the method of the plan).
     * <pre>{@code
     * Legend:
     * ─────────────────────────────────────────────
     *   ░  - blocks line of sight
     *   ▓  - block as movement
     *   █  - blocks movement and line of sight
     *   0° - anchor of the tile
     *        (with the rotation 0°, 90° , 180°, 270°
     *   -  - character
     *   x  - plan field without an assigned tile
     *   .  - marker
     * ─────────────────────────────────────────────
     * }</pre>
     *
     * @return the position.
     */
    Position getPosition();

}
