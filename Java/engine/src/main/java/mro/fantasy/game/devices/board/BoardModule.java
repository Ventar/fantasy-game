package mro.fantasy.game.devices.board;

/**
 * Represents a physical board module.
 * <p>
 * A complete game board consists of multiple modules which have fields that are used by the players to move their characters. The {@link BoardController} is responsible for the
 * coordination of these board modules so that they form a single game board with a consistent data model. Let's assume that a physical board module has the following form where
 * every cell of the diagramm represents a single field:
 * <pre>{@code
 *   ┌─────────┬─────────┬─────────┐
 *   │         │         │         │
 *   │   0/2   │   1/2   │   2/1   │
 *   │         │         │         │
 *   ├─────────┼─────────┼─────────┤
 *   │         │         │         │
 *   │   0/1   │   1/1   │   2/1   │
 *   │         │         │         │
 *   ├─────────┼─────────┼─────────┤
 *   │         │         │         │
 *   │   0/0   │   1/0   │   2/0   │
 *   │         │         │         │
 *   └─────────┴─────────┴─────────┘
 * }</pre>
 * As you can see every field can be addressed by a combination of column and row. Each of these fields consist of 4  HAL sensors (detect magnetic fields) and 4 magnets  to keep
 * tiles on top of the fields in place. The scenic tiles which are placed on top of a field have the same 4 magnets to keep them in place and one or more additional ones to
 * activate the HAL sensors.
 * <pre>{@code
 *     Field                     Player Scenic Tile
 *    ┌─────────────────────┐    ┌─────────────────────┐
 *    │ o      NORTH      o │    │ o                 o │
 *    │         ███         │    │                     │
 *    │ W                 E │    │                     │
 *    │ E █    (5|8)    █ A │    │                     │
 *    │ S █             █ S │    │                     │
 *    │ T                 T │    │                     │
 *    │         ███         │    │         ooo         │
 *    │ o      SOUTH      o │    │ o                 o │
 *    └─────────────────────┘    └─────────────────────┘
 *  }</pre>
 * If you place the example player tile on top of the field now (as shown above) the southern sensor of the field would be activated
 * <p>  Some tiles have more than one magnet. This allows the detection in case they are spawning multiple fields. Examples for such tiles are scenic ones like crates that have a
 * dimension of 2x1 or monsters with a size of 2x2. Based on the magnets on these tiles the engine is able to detect the correct placement of such tiles.
 * <pre>{@code
 *  Legend:
 *  ─────────────────────────────────────────────
 *    █  - HAL sensor
 *    o  - magnet
 *  ─────────────────────────────────────────────
 *  }</pre>
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-19
 */
public interface BoardModule {

}
