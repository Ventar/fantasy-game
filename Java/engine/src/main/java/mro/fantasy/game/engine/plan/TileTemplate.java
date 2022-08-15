package mro.fantasy.game.engine.plan;


import mro.fantasy.game.Size;
import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.engine.communication.AudioCommunicationService;

import java.util.Collection;

/**
 * A tile that is placed on a {@link Plan} as part of a scenario the players have to play. A tile can cover multiple fields of a plan and has attributes which are used by the game
 * engine. These attributes define if player or AI characters can walk through these fields, stay in them or if they block the line of sight.
 * <p>
 * On the physical game board tiles are represented by 2D and or 3D models to create a rich experience for the players. Every tile has a unique number and an indicator for the
 * {@link TileRotation} (anchor) so that a player can place the tile in the correct way on the game board during the setup. This is necessary because tiles have not to be
 * symmetric. Since every tile can cover multiple fields on a plan it has an internal data structure that holds information about the attributes of the single plan field.
 * <p>
 * The following tile is an example for a flame pillar one. It has an anchor field (upper right), that indicates the direction, the number 002 which is print to it and an attached
 * scenic part, the name giving flame pillar. While three of the fields can be used for movement and the calculation of the line of sight, the flame pillar field blocks both,
 * movement and the line of sight. The tile has a size of 2 columns and 2 rows.
 * <pre>{@code
 *     Flame Pillar
 *     top view               side view
 *     ┌─────────┬─────────┐  ┌───────────────────┐
 *     │         │ ███████ │  │ 002             > │
 *     │         │ SCENIC  │  └───────────────────┘
 *     │         │ ███████ │       │              │
 *     ├─────────┼─────────┤       Tile Number    Anchor
 *     │         │         │
 *     │         │         │
 *     │ 0°      │         │
 *     └─────────┴─────────┘
 *   }</pre>
 * As describe the flame pillar has 4 embedded fields with individual coordinates in the space of the tile itself. The coordinates are
 * <pre>{@code
 *     ┌─────────┬─────────┐
 *     │         │ ███████ │
 *     │   1/0   │   1/1   │
 *     │         │ ███████ │
 *     ├─────────┼─────────┤
 *     │         │         │
 *     │   0/0   │   1/0   │
 *     │ 0°      │         │
 *     └─────────┴─────────┘
 * }</pre>
 * and the attributes of the field are
 * <ul>
 *     <li>(0|0) - anchor</li>
 *     <li>(1|0) - none</li>
 *     <li>(0|1) - none</li>
 *     <li>(1|1) - block movement, blocks line of sight</li>
 * </ul>
 * This template is backed by a physical 2D or 3D model and may exist multiple times on a single plan.
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
 * @since 2022-07-31
 */
public interface TileTemplate extends GameResource, FieldContainer, AudioCommunicationService.AudioVariable {

    /**
     * Returns the type of this template. If a template consists of multiple {@link Field}s, all fields of the tile MUST HAVE the same type, i.e. it is not possible to group
     * multiple fields to a tile template if they do not have the same type. If for example a floor tile has some scenic content, this scenic content has the type {@link
     * FieldType#DUNGEON_FLOOR} although scenic content is part of that tile. See the flame pillar example in the {@link TileTemplate} JavaDoc.
     *
     * @return the type of the tile and all assigned fields.
     */
    FieldType getType();

    /**
     * The width and height of the tile, i.e. how many columns and rows it spans on the field.
     */
    Size getSize();

    /**
     * Returns all fields of this tile in the tile coordinate system, i.e. the tile
     *
     * <pre>{@code
     *   ┌─────────┬─────────┐
     *   │         │ ███████ │
     *   │   1/0   │   1/1   │
     *   │         │ ███████ │
     *   ├─────────┼─────────┤
     *   │         │         │
     *   │   0/0   │   1/0   │
     *   │ 0°      │         │
     *   └─────────┴─────────┘
     * }</pre>
     * would return the fields
     * <ul>
     *   <li>(0|0) - anchor</li>
     *   <li>(1|0) - none</li>
     *   <li>(0|1) - none</li>
     *   <li>(1|1) - block movement, blocks line of sight</li>
     * </ul>
     *
     * @return the fields of this template
     */
    Collection<Field> getFields();

}
