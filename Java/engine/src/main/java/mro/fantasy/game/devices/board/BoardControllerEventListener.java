package mro.fantasy.game.devices.board;

import mro.fantasy.game.devices.events.GameEvent;
import mro.fantasy.game.devices.events.GameEventListener;

import java.util.List;

/**
 * Listener that is triggered when an event from a {@link BoardController} is received. The physical game board has 4 HAL sensors (detect magnetic fields) for every field and 4 magnets
 * to keep tiles on top of the fields in place. The scenic tiles which are placed on top of a field have the same 4 magnets to keep them in place and one or more additional ones to
 * activate the HAL sensors.
 * <pre>{@code
 *    Field                     Player Scenic Tile
 *   ┌─────────────────────┐    ┌─────────────────────┐
 *   │ o      NORTH      o │    │ o                 o │
 *   │         ███         │    │                     │
 *   │ W                 E │    │                     │
 *   │ E █    (5|8)    █ A │    │                     │
 *   │ S █             █ S │    │                     │
 *   │ T                 T │    │                     │
 *   │         ███         │    │         ooo         │
 *   │ o      SOUTH      o │    │ o                 o │
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
public interface BoardControllerEventListener extends GameEventListener<BoardControllerEventListener.BoardEvent> {

    /**
     * Information about an updated field.
     *
     * @param row          the row of this field on the board.
     * @param column       the column of this field on the board.
     * @param northEnabled if the northern HAL sensor is enabled
     * @param eastEnabled  if the eastern HAL sensor is enabled
     * @param southEnabled if the southern HAL sensor is enabled
     * @param westEnabled  if the western HAL sensor is enabled
     */
    record FieldUpdate(int column, int row, boolean northEnabled, boolean eastEnabled, boolean southEnabled, boolean westEnabled) {

        /**
         * Returns if any sensor is enabled. Can be used in case the direction is not important. Used for the placement of scenic tiles that spawn multiple fields for example.
         *
         * @return <code>true</code> if any sensor is enabled, <code>false</code> otherwise
         */
        public boolean anyEnabled() {
            return northEnabled() || eastEnabled() || westEnabled() || southEnabled();
        }

    }

    /**
     * Event triggered when a field on the passed board was updated, i.e. if one of the four HAL sensor changed its state from off (no magnet detected) to on (magnet detected) or
     * vice versa.
     *
     * @param board         the game board
     * @param updatedFields the updated fields
     */
    record BoardEvent(BoardController board, List<FieldUpdate> updatedFields) implements GameEvent {}

    /**
     * Triggerd by a {@link BoardController} when the state of a HAL sensor changes.
     *
     * @param event the triggered event.
     */
    void onGameBoardEvent(BoardEvent event);

}
