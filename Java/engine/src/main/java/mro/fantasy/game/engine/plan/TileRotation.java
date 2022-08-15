package mro.fantasy.game.engine.plan;

/**
 * The orientation of a {@link TileTemplate} on the {@link Plan}. Every physical tile as an anchor field with a small arrow that indicates on which field on a plan the tile has to
 * be positioned. Imagine the following plan and tile
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┬─────────┐   ┌─────────┬─────────┐
 *   │         │         │         │         │   │         │ ███████ │
 *   │   0/4   │   1/4   │   2/4   │   3/4   │   │   1/0   │   1/1   │
 *   │         │         │         │         │   │         │ ███████ │
 *   ├─────────┼─────────┼─────────┼─────────┤   ├─────────┼─────────┤
 *   │         │         │         │         │   │         │         │
 *   │   0/3   │   1/3   │   2/3   │   3/3   │   │   0/0   │   1/0   │
 *   │         │         │         │         │   │ 0°      │         │
 *   ├─────────┼─────────┼─────────┼─────────┤   └─────────┴─────────┘
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
 * The tile has the anchor in the field 0/0 and needs to be placed on the plan. To achieve this the anchor field has to be placed on a field of the plan by telling the engine the
 * coordinates of the plan. We place the tile on coordinates 1/1:
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┬─────────┐
 *   │         │         │         │         │
 *   │         │         │         │         │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │         │         │         │         │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │ ███████ │         │
 *   │         │    T1   │ SCENIC  │         │
 *   │         │         │ ███████ │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │         │    T1   │    T1   │         │
 *   │         │ 0°      │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │         │         │         │         │
 *   │         │         │         │         │
 *   └─────────┴─────────┴─────────┴─────────┘
 * }</pre>
 * As you can see we decided to place the tile without any rotation, but other positions would be possible depending on the rotation of that tile. The Example above has the arrow
 * showing to the {@link #DEGREE_0} of the plan, but other directions would be possible:
 * <pre>{@code
 *      Position on field 1/2 - WEST                  Position on field 2/2 - WEST
 *     ┌─────────┬─────────┬─────────┬─────────┐     ┌─────────┬─────────┬─────────┬─────────┐
 *     │         │         │         │         │     │         │         │         │         │
 *     │         │         │         │         │     │         │         │         │         │
 *     │         │         │         │         │     │         │         │         │         │
 *     ├─────────┼─────────┼─────────┼─────────┤     ├─────────┼─────────┼─────────┼─────────┤
 *     │         │         │         │         │     │         │         │         │         │
 *     │         │         │         │         │     │         │         │         │         │
 *     │         │         │         │         │     │         │         │         │         │
 *     ├─────────┼─────────┼─────────┼─────────┤     ├─────────┼─────────┼─────────┼─────────┤
 *     │         │         │         │         │     │         │         │         │         │
 *     │         │         │         │         │     │         │         │         │         │
 *     │         │         │         │         │     │         │         │         │         │
 *     ├─────────┼─────────┼─────────┼─────────┤     ├─────────┼─────────┼─────────┼─────────┤
 *     │         │ 90°     │ ███████ │         │     │         │ 180°    │         │         │
 *     │         │    T1   │ SCENIC  │         │     │    T1   │    T1   │         │         │
 *     │         │         │ ███████ │         │     │         │         │         │         │
 *     ├─────────┼─────────┼─────────┼─────────┤     ├─────────┼─────────┼─────────┼─────────┤
 *     │         │         │         │         │     │         │ ███████ │         │         │
 *     │         │    T1   │    T1   │         │     │    T1   │ SCENIC  │         │         │
 *     │         │         │         │         │     │         │ ███████ │         │         │
 *     └─────────┴─────────┴─────────┴─────────┘     └─────────┴─────────┴─────────┴─────────┘
 *  }</pre>
 * The two examples above shows possible positions with an orientation of {@link #DEGREE_90} and {@link #DEGREE_180}. Due to the size of the tile of 2x2 the final position always
 * depend on the anchor field position on the board and the rotation of the anchor field, i.e the rotation center is always the achor field (which is ALWAYS the field with the
 * coordinates (0|0).
 * <p>
 * From the perspective of the Tile (in the tile coordinate system) the anchor is always the field with the coordinates (0|0).
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-30
 */
public enum TileRotation {

    DEGREE_0,    //   0° rotation
    DEGREE_90,   //  90° rotation
    DEGREE_180,  // 180° rotation
    DEGREE_270;  // 270° rotation


    public String toDegreeString() {
        return switch (this) {
            case DEGREE_0 -> "0°";
            case DEGREE_90 -> "90°";
            case DEGREE_180 -> "180°";
            case DEGREE_270 -> "270°";
        };
    }
}
