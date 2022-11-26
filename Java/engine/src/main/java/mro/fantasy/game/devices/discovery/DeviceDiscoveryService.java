package mro.fantasy.game.devices.discovery;

import mro.fantasy.game.devices.board.BoardModule;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service that uses the MDNS protocol to discover physical devices. The discovery service is used by the device controllers to get access the physical devices. Most of them
 * aggregate multiple and offer a single abstraction to the game engine.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-19
 */
public interface DeviceDiscoveryService {

    /**
     * MDNS type of  {@link BoardModule}.
     */
    String BOARD_MDNS_TYPE = "_sbmodule._udp.local.";

    /**
     * MDNS type of  {@link mro.fantasy.game.devices.player.PlayeModule}.
     */
    String PLAYER_MDNS_TYPE = "_pcontroller._udp.local.";

    /**
     * Returns a list of discovered board modules.
     *
     * @return
     */
    List<BoardModule> getBoardModules();

    /**
     * Returns the board module with the given id.
     *
     * @param id the ID
     *
     * @return the module if it exists
     *
     * @throws IllegalStateException if no board modules were registered
     */
    Optional<BoardModule> getBoardModuleById(String id);

    /**
     * Starts the discovery of physical devices.
     *
     * @throws IOException if the discovery  process cannot be started.
     */
    void scan() throws IOException;


}
