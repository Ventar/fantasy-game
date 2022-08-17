package mro.fantasy.game.plan;

import mro.fantasy.game.Position;

/**
 * A {@link TileTemplate} that was assigned to a {@link Plan}. During the assignment process the anchor field of a tile is placed on the plan together with an {@link TileRotation}
 * so that a new concrete tile is created on that plan. In addition, the new tile will get a unique ID so that it can be identified later.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-30
 */
public interface Tile extends TileTemplate {

    /**
     * Returns the unique ID of the tile.
     * <p>
     * The ID is not the same as the {@link Tile#getGameId()}. While multiple tiles of the same type share the same game id (the regular dungeon floor from the base game has the
     * game id BGT001 for example) the actual id of a tile is generated as {@link java.util.UUID} when the template of the tile is added to the plan with the {@link
     * Plan#assign(TileTemplate, Position, TileRotation)} method.
     *
     * @return the ID.
     */
    String getId();

    /**
     * Returns the position of the tile on the plan.
     * <p>
     * Assume that we have the following plan with a tile template assigned to it
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
     * the position of that template would be (1|1) with {@link TileRotation#DEGREE_0}.
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
     * @return the position
     */
    Position getPosition();

    /**
     * Returns the rotation of the assigned tile.
     *
     * @return the rotation
     *
     * @see #getPosition()
     */
    TileRotation getRotation();

    /**
     * Creates a deep copy of this tile.
     *
     * @return the deep copy.
     */
    Tile copy();

}
