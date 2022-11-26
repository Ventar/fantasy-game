package mro.fantasy.game.engine.events;

import mro.fantasy.game.Position;

import java.util.List;

/**
 * Listener that is triggered when an event from a physicl board module is received. The physical game board has 4 HAL sensors (detect magnetic fields) for every field and 4
 * magnets to keep tiles on top of the fields in place. The scenic tiles which are placed on top of a field have the same 4 magnets to keep them in place and one or more additional
 * ones to activate the HAL sensors.
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
public interface BoardUpdatedEvent extends GameEvent {


    /**
     * Information about an updated field.
     *
     * @param position     the position of the field in the coordinate system of the {@link mro.fantasy.game.devices.board.GameBoard}
     * @param northEnabled if the northern HAL sensor is enabled
     * @param eastEnabled  if the eastern HAL sensor is enabled
     * @param southEnabled if the southern HAL sensor is enabled
     * @param westEnabled  if the western HAL sensor is enabled
     */
    record FieldUpdate(Position position, boolean northEnabled, boolean eastEnabled, boolean southEnabled, boolean westEnabled) {

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
     * Returns a list of fields which were updated for the game board. This is usually a delta update, i.e. only the changed fields are transferred. However, the internal data
     * model of the server has the complete state of the game board. That means you can always use the {@link mro.fantasy.game.devices.board.GameBoard} to retrieve all needed
     * information.
     *
     * @return the changed fields
     */
    List<FieldUpdate> getUpdatedFields();

}
