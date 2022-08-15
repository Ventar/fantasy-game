package mro.fantasy.game.resources;

import mro.fantasy.game.engine.GameLibrary;

import java.util.Map;

/**
 * A resource that is used by the game engine.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-04
 */
public interface GameResource {

    /**
     * A human-readable name of this resource for easier identification in the log messages. The name has not to be unique but can be used multiple times.
     *
     * @return the name
     */
    String getName();

    /**
     * Description of the tile resource.
     *
     * @return the description
     */
    String getDescription();

    /**
     * The unique identifier of resource.
     */
    String getGameId();

    /**
     * Loads the game resource from the passed YAML map.
     *
     * @param library the game library to resolve entities which are only referred by their {@link GameResource#getGameId()}.
     * @param data    the YAML data map.
     */
    void loadFromYAML(GameLibrary library, Map<String, Object> data);

}
