package mro.fantasy.game.engine.events;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.BoardField;

import java.util.List;

/**
 * Listener that is triggered when an event from a physical board module is received. The physical game board has 5 HAL sensors (detect magnetic fields) for every field. In
 * addition, a button is available for every field that can be pressed by a player.
 *
 * <pre>{@code
 *    Field                     Player Scenic Tile
 *
 *    Board
 *      |
 *   ┌─────────────────────┐    ┌─────────────────────┐
 *   │  █     NORTH        │    │ o                 o │
 *   │         ███         │    │                     │
 *   │ W                 E │    │                     │
 *   │ E █    (5|8)    █ A │    │                     │
 *   │ S █             █ S │    │                     │
 *   │ T                 T │    │                     │
 *   │         ███         │    │         ooo         │
 *   │        SOUTH        │    │ o                 o │
 *   └─────────────────────┘    └─────────────────────┘
 * }</pre>
 * If you place the example player tile on top of the field now (as shown above) the southern sensor of the field would be activated and this event listener would be triggered as a
 * result with an {@link FieldUpdate} of column 5, row 8, northern false, eastern false, southern true and western false.
 * <p>
 * Some tiles have more than one magnet. This allows the detection in case they are spawning multiple fields. Examples for such tiles are scenic ones like crates that have a
 * dimension of 2x1 or monsters with a size of 2x2. Based on the magnets on these tiles the engine is able to detect the correct placement of such tiles.
 *
 * <pre>{@code
 * Legend:
 * ─────────────────────────────────────────────
 *   █  - HAL sensor
 *   o  - magnet
 * ─────────────────────────────────────────────
 * }</pre>
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-12
 */
public interface BoardUpdatedEvent extends GameEvent {

    /**
     * Returns a list of fields which were updated for the game board. This is a delta update, i.e. only the changed fields are transferred. However, the internal data model of the
     * server has the complete state of the game board. That means you can always use the {@link mro.fantasy.game.devices.board.GameBoard} to retrieve all needed information.
     *
     * @return the changed fields
     */
    List<BoardField> getFields();

    /**
     * Checks if the sensor at with the given type and position is active. Active mean that a button was pressed ot the HAL sensor has detected a magnetic field.
     *
     * @param type     the sensor type to check at the given position
     * @param position the position to check
     */
    boolean isSensorActive(BoardField.SensorType type, Position position);

}
