package mro.fantasy.game.plan;

/**
 * Responsible to manage the interaction with the player to add / remove / move game elements (tiles) from / to the physical modules.
 * <p>
 * Combines the functionality of
 * <ul>
 *     <li>a {@link Plan} that provides all information about {@link Tile}s and there position in the plan coordinate system (column and rows)</li>
 *     <ul>
 *         <li>that is managed by the {@link PlanLibrary} which has an YAML file that references multiple {@link TileTemplate}s </li>
 *         <li>managed by the {@link TileLibrary}</li>
 *     </ul>
 *     <li>the {@link mro.fantasy.game.communication.AudioCommunicationService} to give audio advices to the player</li>
 *     <li>the {@link mro.fantasy.game.devices.board.GameBoard} to put all the tiles from the plan in place</li>
 *     <ul>
 *         <li>which was constructed from {@link mro.fantasy.game.devices.board.BoardModule} that are constructed by the</li>
 *         <li>{@link mro.fantasy.game.devices.discovery.DeviceDiscoveryService}</li>
 *         <li>and uses the {@link mro.fantasy.game.devices.events.DeviceEventService} / {@link mro.fantasy.game.engine.events.GameEventProducer} /
 *         {@link mro.fantasy.game.engine.events.GameEventListener} to detect the physical interaction of the player with the game board</li>
 *     </ul>
 * </ul>
 * <img src="doc-files/PlanSetupService.png"/>
 * <p>
 *
 * @author Michael Rodenbuecher
 * @since 2023-03-14
 */
public interface PlanSetupService {


}
