package mro.fantasy.game.devices.board;

/**
 * Service that is responsible to detect physical {@link BoardModule}s, arrange them on the gaming table and ensure that multiple modules are combined to a {@link GameBoard} that
 * can be used by the players. This service rely on the {@link mro.fantasy.game.communication.AudioCommunicationService} to interact with the players during setup of the game
 * board.
 *
 * @author Michael Rodenbuecher
 * @since 2023-03-14
 */
public interface BoardSetupService {
}
