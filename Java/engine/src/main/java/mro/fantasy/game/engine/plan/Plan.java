package mro.fantasy.game.engine.plan;

import mro.fantasy.game.Position;
import mro.fantasy.game.Size;
import mro.fantasy.game.resources.GameResource;

import java.util.List;

/**
 * A plan is the blueprint for a physical game board that allows the players to get an overview of the current game state. It consists of multiple {@link Field}s which are arranged
 * in columns and rows. Every combination of a column and a row is called a field and can have additional information assigned to it which affect the game.
 * <p>
 * The plan itself is a matrix of empty fields, i.e. it always has a number of columns and rows which can be filled with additional data. This additional data is represented by
 * {@link TileTemplate}s which are the physical game elements that build the scenario in which the players interact. Such tiles could be blank floor tiles, walls, fire places,
 * weapon racks, levers or traps for example. Tiles cover one or more fields of the plan and some of them can be stacked. Let's imagine we have the following 4x5 fields plan:
 *
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┬─────────┐
 *   │         │         │         │         │
 *   │   0/4   │   1/4   │   2/4   │   3/4   │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   0/3   │   1/3   │   2/3   │   3/3   │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   0/2   │   1/2   │   2/2   │   3/2   │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   0/1   │   1/1   │   2/1   │   3/1   │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   0/0   │   1/0   │   2/0   │   3/0   │
 *   │         │         │         │         │
 *   └─────────┴─────────┴─────────┴─────────┘
 * }</pre>
 * <p>
 * This plan is like a spreadsheet that we have to fill with live now. To achieve this we place tiles on it. The first tile we put in place are floor tiles which indicate that this
 * field is part of the scenario. If no tile is placed on a field of the plan it is not part of the scenario and cannot be used later. The regular {@link FieldType#DUNGEON_FLOOR}
 * tiles have a size of 2x2 fields, so that we cannot cover all fields of the plan. But most of them. A plan with only floor tiles could look like:
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┬─────────┐
 *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
 *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
 *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   F3    │   F3    │   F4    │   F4    │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   F3    │   F3    │   F4    │   F4    │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   F1    │   F1    │   F2    │   F2    │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   F1    │   F1    │   F2    │   F2    │
 *   │         │         │         │         │
 *   └─────────┴─────────┴─────────┴─────────┘
 * }</pre>
 * Only 16 out of 20 possible fields are now part of the scenario, the upper row is not covered by floor tiles and will not be used in this scenario. When the floor tiles were
 * places, additional content can be places on the board. An empty board would be kind of boring so that we add some barrels and crates on the field ({@link FieldType#SCENIC},
 * which block the movement of characters. Afterwards the plan looks like:
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┬─────────┐
 *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
 *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
 *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   F3    │   F3    │   F4    │   F4    │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │ ▓▓▓▓▓▓▓ │         │ ▓▓▓▓▓▓▓ │
 *   │   F3    │ BARREL  │   F4    │  CRATE  │
 *   │         │ ▓▓▓▓▓▓▓ │         │ ▓▓▓▓▓▓▓ │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │ ▓▓▓▓▓▓▓ │
 *   │   F1    │   F1    │   F2    │  CRATE  │
 *   │         │         │         │ ▓▓▓▓▓▓▓ │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   F1    │   F1    │   F2    │   F2    │
 *   │         │         │         │         │
 *   └─────────┴─────────┴─────────┴─────────┘
 * }</pre>
 * The scenic tile can cover multiple fields of the plan like the floor tiles. Finally, a {@link FieldType#PLAYER} and an {@link FieldType#ENEMY} are placed on the plan so that we
 * have a complete scenario:
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┬─────────┐
 *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
 *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
 *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │ ------- │         │
 *   │         │         │  ENEMY  │         │
 *   │         │         │ ------- │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │ ▓▓▓▓▓▓▓ │         │ ▓▓▓▓▓▓▓ │
 *   │         │ SCENIC  │         │ SCENIC  │
 *   │         │ ▓▓▓▓▓▓▓ │         │ ▓▓▓▓▓▓▓ │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │ ▓▓▓▓▓▓▓ │
 *   │         │         │         │ SCENIC  │
 *   │         │         │         │ ▓▓▓▓▓▓▓ │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │ ------- │         │         │         │
 *   │ PLAYER  │         │         │         │
 *   │ ------- │         │         │         │
 *   └─────────┴─────────┴─────────┴─────────┘
 * }</pre>
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
 * @author Michael Rodenbuecher
 * @since 2022-07-26
 */
public interface Plan extends FieldContainer, GameResource {

    /**
     * Returns the size of the plan in column and rows. This size is the space that can be used to assign tiles to the plan. Not every field needs to have a tile assigned. Fields
     * without assignments will not be considered by the game engine.
     *
     * @return the size
     */
    Size getSize();

    /**
     * Checks if the given field blocks the line of sight.
     *
     * @param field the field to check
     *
     * @return <code>true</code> if the line is blocked, <code>false</code> otherwise.
     */
    boolean blocksLineOfSight(Position field);

    /**
     * Checks if the given field blocks the movement of a character.
     *
     * @param field the field to check
     *
     * @return <code>true</code> if the field blocks movement,<code>false</code> otherwise.
     */
    boolean blocksMovement(Position field);

    /**
     * Checks if the given field can be entered by a character, i.e. if it can end its movement on the fields of this tile.
     *
     * @param field the field to check
     *
     * @return <code>true</code> if the field can be entered. <code>false</code> otherwise.
     */
    boolean canEnter(Position field);

    /**
     * Returns all fields of this plan in the plan coordinate system. Only fields which have an assigned tile will be returned, i.e. if we have an empty plan like
     * <pre>{@code
     *   ┌─────────┬─────────┬─────────┬─────────┐
     *   │         │         │         │         │
     *   │   0/4   │   1/4   │   2/4   │   3/4   │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │   0/3   │   1/3   │   2/3   │   3/3   │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │   0/2   │   1/2   │   2/2   │   3/2   │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │   0/1   │   1/1   │   2/1   │   3/1   │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │   0/0   │   1/0   │   2/0   │   3/0   │
     *   │         │         │         │         │
     *   └─────────┴─────────┴─────────┴─────────┘
     * }</pre>
     * and assign 4 flame pillar tiles to it:
     * <pre>{@code
     *   ┌─────────┬─────────┬─────────┬─────────┐
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │ ███████ │         │         │ ███████ │
     *   │ SCENIC  │   F3    │   F4    │ SCENIC  │
     *   │ ███████ │         │         │ ███████ │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │         │
     *   │   F3    │   F3    │   F4    │   F4    │
     *   │         │    270° │ 0°      │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │    180° │ 90°     │         │
     *   │   F1    │   F1    │   F2    │   F2    │
     *   │         │         │         │         │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │ ███████ │         │         │ ███████ │
     *   │ SCENIC  │   F1    │   F2    │ SCENIC  │
     *   │ ███████ │         │         │ ███████ │
     *   └─────────┴─────────┴─────────┴─────────┘
     * }</pre>
     * this method would return fields
     * <ul>
     *   <li>(0|0) - block movement, blocks line of sight</li>
     *   <li>(1|0) - none</li>
     *   <li>(2|0) - none</li>
     *   <li>(3|0) - block movement, blocks line of sight</li>
     *   <li>(0|1) - none</li>
     *   <li>(1|1) - anchor</li>
     *   <li>(2|1) - anchor</li>
     *   <li>(3|1) - none</li>
     *   <li>(0|2) - none</li>
     *   <li>(1|2) - anchor</li>
     *   <li>(2|2) - anchor</li>
     *   <li>(3|2) - none</li>
     *   <li>(0|3) - block movement, blocks line of sight</li>
     *   <li>(1|3) - none</li>
     *   <li>(2|3) - none</li>
     *   <li>(3|3) - block movement, blocks line of sight</li>
     *   <li>(0|4) - NOT RETURNED, because it is empty</li>
     *   <li>(1|4) - NOT RETURNED, because it is empty</li>
     *   <li>(2|4) - NOT RETURNED, because it is empty</li>
     *   <li>(3|4) - NOT RETURNED, because it is empty</li>
     * </ul>
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
     * @return the fields of this template
     */
    List<Field> getFields();

    /**
     * Returns all tiles which cover the passed position. Since on every position multiple fields can be present (dungeon floor on which a monster is placed) more than one tile can
     * be returned. If we have the following tiles that are placed on the plan it is possible to place the BG001 floor tile first and the player tile plus the scenic tile on top of
     * it.
     * <pre>{@code
     *   Plan                                         TileTemplate BG001     Player         Scenic
     *   ┌─────────┬─────────┬─────────┬─────────┐   ┌─────────┬─────────┐   ┌─────────┐   ┌─────────┐
     *   │         │         │         │         │   │         │         │   │ ------- │   │ ▓▓▓▓▓▓▓ │
     *   │   0/4   │   1/4   │   2/4   │   3/4   │   │   1/0   │   1/1   │   │ PLAYER  │   │ SCENIC  │
     *   │         │         │         │         │   │         │         │   │ ------- │   │ ▓▓▓▓▓▓▓ │
     *   ├─────────┼─────────┼─────────┼─────────┤   ├─────────┼─────────┤   └─────────┘   └─────────┘
     *   │         │         │         │         │   │         │         │
     *   │   0/3   │   1/3   │   2/3   │   3/3   │   │   0/0   │   1/0   │
     *   │         │         │         │         │   │ 0°      │         │
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
     * }</pre>
     * <pre>{@code
     *   Plan
     *   ┌─────────┬─────────┬─────────┬─────────┐
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   │ xxxxxxx │ xxxxxxx │ xxxxxxx │ xxxxxxx │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │         │         │ ▓▓▓▓▓▓▓ │
     *   │  BG001  │  BG001  │  BG001  │  BG005  │
     *   │         │         │         │ ▓▓▓▓▓▓▓ │
     *   ├─────────┼─────────┼─────────┼─────────┤
     *   │         │ ------- │         │         │
     *   │  BG001  │ PLAYER  │  BG001  │  BG001  │
     *   │ 0°      │ ------- │ 0°      │         │
     *   └─────────┴─────────┴─────────┴─────────┘
     * }</pre>
     *
     * @param position the position to fetch the tiles for
     *
     * @return the tiles if available
     */
    List<Tile> getTiles(Position position);

    /**
     * Returns all tiles on the plan. The returned list is a copy of the internal one, i.e. changes to the list do not modify the plan. use the {@link #assign(TileTemplate,
     * Position, TileRotation)} or {@link #remove(Tile)} methods instead.
     *
     * @return all tiles on the plan
     *
     * @see #getTiles(Position)
     */
    List<Tile> getTiles();

    /**
     * Returns the tile with the highest {@link FieldType#getLayer()} value on the given position
     *
     * @param position the position to check
     *
     * @return the tile
     *
     * @see #getTiles(Position)
     */
    Tile getTopTile(Position position);

    /**
     * Returns the fields which cover the passed position.
     *
     * @param position the position to check
     *
     * @return the fields
     *
     * @see #getTiles(Position)
     */
    List<Field> getFields(Position position);

    /**
     * Returns the fields with the highest {@link FieldType#getLayer()} value on the given position
     *
     * @param position the position to check
     *
     * @return the field
     *
     * @see #getTiles(Position)
     */
    Field getTopField(Position position);

    /**
     * Returns the tile with the given {@link Tile#getId()}.
     * <p>
     * The ID is not the same as the {@link Tile#getGameId()}. While multiple tiles of the same type share the same game id (the regular dungeon floor from the base game has the
     * game id BGT001 for example) the actual id of a tile is generated as {@link java.util.UUID} when the template of the tile is added to the plan with the {@link
     * Plan#assign(TileTemplate, Position, TileRotation)} method.
     *
     * @param id the ID of the tile to get
     *
     * @return the tile or {@code null} if the tile with the given id is not part of the plan
     */
    Tile getTileById(String id);

    /**
     * Assigns a tile template to this plan. Assuming we have the following plan and tile:
     *
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
     * }</pre>
     * assigning this template to the plan at position (1|1) with {@link TileRotation#DEGREE_0}, would result in
     * <pre>{@code
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
     * From the perspective of the {@link TileTemplate} (in the tile coordinate system) the anchor is always the field (0|0). The rotation only becomes important if we need to add
     * the tile to the coordinates of {@code Plan}.
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
     * @param template    the template to assign
     * @param position    the position where the anchor field of the template is positioned
     * @param orientation the orientation of the tile
     *
     * @return the newly created tile on the plan
     */
    Tile assign(TileTemplate template, Position position, TileRotation orientation);

    /**
     * Removes the passed tile from the plan.
     *
     * @return {@code true} if the tile was removed, {@code false otherwise}
     */
    boolean remove(Tile tile);

    /**
     * Creates a deep copy of this plan.
     *
     * @return the deep copy.
     */
    Plan copy();

}
