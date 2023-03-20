package mro.fantasy.game.devices.board;

import mro.fantasy.game.Position;
import mro.fantasy.game.Size;
import mro.fantasy.game.devices.board.impl.BoardFieldImpl;
import mro.fantasy.game.devices.impl.Color;
import mro.fantasy.game.engine.events.BoardUpdatedEvent;
import mro.fantasy.game.engine.events.GameEventListener;
import mro.fantasy.game.engine.events.GameEventProducer;

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
public interface BoardModule extends GameEventProducer<BoardUpdatedEvent, GameEventListener<BoardUpdatedEvent>> {

    /**
     *
     */
    enum BoardRotation {
        D0,
        D90,
        D180,
        D270
    }

    /**
     * Returns the size, i.e. number {@link BoardFieldImpl}s in columns and rows, of this module
     *
     * @return the size
     */
    Size getSize();

    /**
     * Returns the board field at the given position
     *
     * @param position the position
     *
     * @return the field.
     */
    BoardField getField(Position position);


    /**
     * Returns the unique ID of the board module.
     */
    String getId();

    /**
     * Changes the color of the field of the given position. The transmission of this information to the physical module is not triggered automatically but started when the {@link
     * #sendColorUpdate()} method is called. This will allow the caller to set multiple colors which are sent with one UDP datagram packet.
     *
     * @param position the position to set
     * @param color    the new color. A color of {@link Color#Black} which is essentially BLACK will turn off the LED
     *
     * @throws IllegalArgumentException in case a passed parameter is {@code null} or the passed position does not exist on the module
     * @see #sendColorUpdate()
     */
    void setColor(Position position, Color color);

    /**
     * Sends the update of all colors which were changed with the {@link #setColor(Position, Color)} since the last call to {@link #setColor(Position, Color)} or {@link
     * #sendClearColors()}.
     */
    void sendColorUpdate();

    /**
     * Clears all colors on the physical module. The UDP message is send immediately to the module. If the {@link #setColor(Position, Color)} was used without sending the data with
     * the {@link #sendColorUpdate()} method, the colors will be cleared.
     */
    void sendClearColors();

    /**
     * Sets the brightness of the LED strip for a board module vom 0% (0) to 100% (255).
     *
     * @param brightness the brightness from 0 to 100 in %
     */
    void sendSetBrightness(int brightness);

    /**
     * Enables or disables the sensors of the physical board modules. If a sensor is disabled no events of the corresponding type will be sent by the board anymore. This can be
     * used to avoid error handling for irrelevant cases. If you wait for a button event and the player shouldn't add / remove / move something on the game board, disabling the
     * BOARD and EDGE sensors can avoid false positives during the interaction. In addition, disabled sensors reduce the overall processing time of the physical board module and my
     * increase the responsiveness of the hardware.
     * <p>
     * The method will always set the state of all sensors, i.e. the full expected value set (all three boolean values) have to be provided, there is no delta calculation.
     *
     * @param button if the button sensor type is enabled or not
     * @param board  if the board sensor type is enabled or not
     * @param edge   if the edge sensor type is enabled or not
     */
    void sendEnableSensors(boolean button, boolean board, boolean edge);

}
